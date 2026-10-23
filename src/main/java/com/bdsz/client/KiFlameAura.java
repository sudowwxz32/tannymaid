package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
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
 * Aura de ki clásica: lenguas de fuego que siempre miran a la cámara, rodean el cuerpo, suben y titilan,
 * con una llama grande detrás del guerrero.
 */
public final class KiFlameAura {
    private static final ResourceLocation FLAME = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/ki_flame.png");

    private KiFlameAura() {}

    public static void render(AbstractClientPlayer p, float partial, PoseStack pose, MultiBufferSource buf) {
        SaiyanState s = ClientStates.get(p.getId());
        boolean forced = s.charging || s.powering > 0;
        if (!s.saiyan || s.ozaru || p.isInvisible() || !(s.aura || forced)) return;
        Form f = s.visibleForm();
        int k = s.transformed || s.powering > 0 ? s.kaioken : 0;
        if (f.calm() && k == 0) return;                                // Ultra Instinto: sin llamas
        float r, g, b;
        int oc;
        if (f == Form.BASE) {
            if (k > 0) { r = 1.0F; g = 0.22F; b = 0.12F; oc = 0x8A0A06; } else { r = 0.7F; g = 0.88F; b = 1.0F; oc = 0x4A80C0; }
        } else {
            r = f.ar(); g = f.ag(); b = f.ab();
            oc = f.outerColor();
            if (k > 0) { r = Math.min(1, r * 0.5F + 0.6F); g *= 0.45F; b *= 0.45F; oc = 0x8A0A0A; }
        }
        float or = ((oc >> 16) & 255) / 255F, og = ((oc >> 8) & 255) / 255F, ob = (oc & 255) / 255F;
        float progress = s.powering > 0 ? 1.0F - (float) s.powering / Math.max(1, s.poweringTotal) : 1.0F;
        float drama = Math.max(f.drama(), k > 0 ? 0.45F : 0.3F);
        float size = f.auraSize * (s.charging ? 1.15F : 1.0F) * (s.powering > 0 ? 0.6F + progress * 0.5F : 1.0F);
        float a = (0.5F + drama * 0.35F) * (forced ? 1.15F : 1.0F) * (s.powering > 0 ? 0.4F + progress * 0.6F : 1.0F);
        float t = p.tickCount + partial;
        float scaleH = f.height();

        Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f left = new Vector3f(cam.getLeftVector());
        left.y = 0;
        if (left.lengthSquared() < 1.0E-4F) left.set(1, 0, 0);
        left.normalize();
        Vector3f fwd = new Vector3f(left.z, 0, -left.x);               // hacia donde mira la cámara (en el plano)

        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(FLAME, 0.0F, 0.0F));
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();

        // llama grande detrás del cuerpo
        float bigH = (2.4F + drama * 0.7F) * size * scaleH * (0.94F + 0.06F * Mth.sin(t * 0.7F));
        float bigW = (1.25F + drama * 0.3F) * size * (0.95F + 0.05F * Mth.sin(t * 1.1F));
        sprite(vc, m, n, fwd.x * 0.25F, -0.15F, fwd.z * 0.25F, left, bigW, bigH, or * a * 0.7F, og * a * 0.7F, ob * a * 0.7F);
        sprite(vc, m, n, fwd.x * 0.2F, -0.1F, fwd.z * 0.2F, left, bigW * 0.75F, bigH * 0.9F, r * a * 0.6F, g * a * 0.6F, b * a * 0.6F);

        // lenguas de fuego alrededor que suben y titilan
        int count = 8 + (int) (drama * 8);
        for (int i = 0; i < count; i++) {
            float c = (t * (0.035F + drama * 0.02F) + i * 0.618F) % 1.0F;
            float life = Mth.sin(c * Mth.PI);
            float ang = i * Mth.TWO_PI / count + Mth.sin(t * 0.03F + i) * 0.25F;
            float rad = 0.32F * size;
            float x = Mth.cos(ang) * rad, z = Mth.sin(ang) * rad;
            float y = -0.1F + c * 0.9F * size * scaleH;
            float h = (0.9F + drama * 0.6F) * size * scaleH * (0.6F + life * 0.6F) * (0.9F + 0.15F * Mth.sin(t * 1.3F + i * 2.1F));
            float w = 0.42F * size * (0.7F + life * 0.4F);
            boolean outer = (i % 3) == 0;
            float cr = outer ? or : r, cg = outer ? og : g, cb = outer ? ob : b;
            float br = a * life * (outer ? 0.55F : 0.7F);
            sprite(vc, m, n, x, y, z, left, w, h, cr * br, cg * br, cb * br);
        }
        // núcleo brillante pegado al cuerpo
        sprite(vc, m, n, 0, 0.0F, 0, left, 0.9F * size, (1.9F + drama * 0.3F) * size * scaleH, r * a * 0.35F + 0.08F * a, g * a * 0.35F + 0.08F * a, b * a * 0.35F + 0.08F * a);
    }

    /** Un cuadro vertical que mira a la cámara (la base abajo, la punta arriba). */
    private static void sprite(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, Vector3f left,
                               float w, float h, float r, float g, float b) {
        float lx = left.x * w / 2, lz = left.z * w / 2;
        v(vc, m, n, x - lx, y, z - lz, 0, 1, r, g, b);
        v(vc, m, n, x + lx, y, z + lz, 1, 1, r, g, b);
        v(vc, m, n, x + lx, y + h, z + lz, 1, 0, r, g, b);
        v(vc, m, n, x - lx, y + h, z - lz, 0, 0, r, g, b);
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float vv, float r, float g, float b) {
        vc.vertex(m, x, y, z).color(r, g, b, 1.0F).uv(u, vv).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 1, 0).endVertex();
    }
}
