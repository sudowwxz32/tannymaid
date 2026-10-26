package com.cz.client;

import com.cz.ChakraZorro;
import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Efectos alrededor del jugador (siempre miran a la cámara): aura de chakra con forma de llamas, burbujas
 * que revientan sobre el manto hirviente (fase 3+), chispas y un resplandor rojo en el suelo.
 */
public final class ChakraFx {
    private static final ResourceLocation FLAME = new ResourceLocation(ChakraZorro.MODID, "textures/entity/ki_flame.png");
    private static final ResourceLocation EMBER = new ResourceLocation(ChakraZorro.MODID, "textures/entity/ember.png");
    private static final ResourceLocation GLOW = new ResourceLocation(ChakraZorro.MODID, "textures/entity/ground_glow.png");
    private static final ResourceLocation BUBBLE = new ResourceLocation(ChakraZorro.MODID, "textures/entity/bubble.png");

    private ChakraFx() {}

    public static void render(AbstractClientPlayer p, float partial, PoseStack pose, MultiBufferSource buf) {
        ZorroState s = ClientStates.get(p.getId());
        if (!s.awakened || p.isInvisible()) return;
        Phase v = s.visible();
        if (v == Phase.NONE) return;
        boolean powering = s.powering > 0;
        float pr = s.progress();
        float a = (0.45F + v.ordinal() * 0.08F) * (powering ? 0.35F + pr * 0.85F : 1.0F);
        float t = p.tickCount + partial;
        int ac = v.auraColor;
        float r = ((ac >> 16) & 255) / 255F, g = ((ac >> 8) & 255) / 255F, b = (ac & 255) / 255F;
        float h = p.getBbHeight();
        float wide = v.quadruped ? 0.9F : 0.55F;

        Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f left = new Vector3f(cam.getLeftVector());
        left.y = 0;
        if (left.lengthSquared() < 1.0E-4F) left.set(1, 0, 0);
        left.normalize();
        Vector3f up = new Vector3f(cam.getUpVector());
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();

        // ---- resplandor rojo en el suelo
        VertexConsumer gv = buf.getBuffer(RenderType.energySwirl(GLOW, 0, 0));
        float gr = (1.0F + v.ordinal() * 0.25F) * (powering ? 0.6F + pr * 0.8F : 1.0F);
        float gb = a * 0.5F;
        quadFlat(gv, m, n, gr, r * gb, g * gb * 0.6F, b * gb * 0.5F);

        // ---- aura de chakra: lenguas de fuego que rodean el cuerpo y suben
        VertexConsumer fv = buf.getBuffer(RenderType.energySwirl(FLAME, 0, 0));
        int frame = (int) (t * 0.3F);
        int count = 6 + v.ordinal() * 3 + (powering ? 6 : 0);
        for (int i = 0; i < count; i++) {
            float c = (t * (0.03F + v.ordinal() * 0.006F) + i * 0.618F) % 1.0F;
            float life = Mth.sin(c * Mth.PI);
            float ang = i * 2.399F + t * 0.01F;
            float rad = wide * (0.8F + 0.25F * life);
            float x = Mth.cos(ang) * rad, z = Mth.sin(ang) * rad;
            float y = c * h * 1.1F - 0.1F;
            float fh = (0.55F + v.ordinal() * 0.08F) * (1.1F - c * 0.5F);
            float fw = 0.35F * (1.0F - c * 0.4F);
            float br = a * life * 0.6F;
            float mix = c * 0.7F;                                  // sube del naranja al rojo oscuro
            sprite(fv, m, n, x, y, z, left, fw, fh, frame + i, Math.min(1, r * br + 0.15F * br * (1 - mix)), g * br * (1 - mix), b * br * (1 - mix));
        }
        // llama grande y tenue detrás, con la forma del cuerpo
        float bh = h * (1.15F + 0.05F * Mth.sin(t * 0.7F));
        sprite(fv, m, n, 0, -0.1F, 0, left, wide * 2.2F, bh, frame, r * a * 0.35F, g * a * 0.2F, b * a * 0.15F);

        // ---- burbujas que aparecen sobre el manto hirviente y revientan
        if (v.burns && (!powering || pr > 0.3F)) {
            VertexConsumer bv = buf.getBuffer(RenderType.energySwirl(BUBBLE, 0, 0));
            int bubbles = 8 + v.ordinal() * 3;
            for (int i = 0; i < bubbles; i++) {
                float c = (t * 0.06F + i * 0.377F) % 1.0F;                 // nace, crece y revienta
                java.util.Random rnd = new java.util.Random((long) (t * 0.06F + i * 0.377F) * 7919L + i * 31L + p.getId());
                float ang = rnd.nextFloat() * Mth.TWO_PI;
                float rad = wide * (0.55F + rnd.nextFloat() * 0.3F);
                float x = Mth.cos(ang) * rad, z = Mth.sin(ang) * rad;
                float y = 0.2F + rnd.nextFloat() * h * 0.9F + c * 0.25F;
                float size = 0.05F + c * (0.12F + rnd.nextFloat() * 0.1F);
                float br = a * (c < 0.85F ? 1.0F : (1.0F - c) * 6.0F) * 0.9F;
                billboard(bv, m, n, x, y, z, left, up, size, Math.min(1, r * br + 0.2F * br), g * br * 0.8F + 0.1F * br, b * br * 0.5F);
            }
        }

        // ---- chispas que suben
        VertexConsumer ev = buf.getBuffer(RenderType.energySwirl(EMBER, 0, 0));
        int embers = 6 + v.ordinal() * 3;
        for (int i = 0; i < embers; i++) {
            float c = (t * 0.045F * (1.0F + (i % 3) * 0.25F) + i * 0.731F) % 1.0F;
            float ang = i * 1.913F + c * 2.0F;
            float rad = wide * (0.6F + (i % 4) * 0.15F);
            float br = a * Mth.sin(c * Mth.PI) * 1.2F;
            billboard(ev, m, n, Mth.cos(ang) * rad, c * h * 1.5F, Mth.sin(ang) * rad, left, up, 0.06F * (1 - c * 0.5F), br, br * 0.6F, br * 0.3F);
        }
    }

    private static void sprite(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, Vector3f left,
                               float w, float h, int frame, float r, float g, float b) {
        float lx = left.x * w / 2, lz = left.z * w / 2;
        float u0 = (frame & 3) * 0.25F, u1 = u0 + 0.25F;
        v(vc, m, n, x - lx, y, z - lz, u0, 1, r, g, b);
        v(vc, m, n, x + lx, y, z + lz, u1, 1, r, g, b);
        v(vc, m, n, x + lx, y + h, z + lz, u1, 0, r, g, b);
        v(vc, m, n, x - lx, y + h, z - lz, u0, 0, r, g, b);
    }

    private static void billboard(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, Vector3f left, Vector3f up,
                                  float s, float r, float g, float b) {
        float lx = left.x * s, lz = left.z * s, ux = up.x * s, uy = up.y * s, uz = up.z * s;
        v(vc, m, n, x - lx - ux, y - uy, z - lz - uz, 0, 1, r, g, b);
        v(vc, m, n, x + lx - ux, y - uy, z + lz - uz, 1, 1, r, g, b);
        v(vc, m, n, x + lx + ux, y + uy, z + lz + uz, 1, 0, r, g, b);
        v(vc, m, n, x - lx + ux, y + uy, z - lz + uz, 0, 0, r, g, b);
    }

    private static void quadFlat(VertexConsumer vc, Matrix4f m, Matrix3f n, float rad, float r, float g, float b) {
        v(vc, m, n, -rad, 0.03F, -rad, 0, 0, r, g, b);
        v(vc, m, n, -rad, 0.03F, rad, 0, 1, r, g, b);
        v(vc, m, n, rad, 0.03F, rad, 1, 1, r, g, b);
        v(vc, m, n, rad, 0.03F, -rad, 1, 0, r, g, b);
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float vv, float r, float g, float b) {
        vc.vertex(m, x, y, z).color(r, g, b, 1.0F).uv(u, vv).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 1, 0).endVertex();
    }
}
