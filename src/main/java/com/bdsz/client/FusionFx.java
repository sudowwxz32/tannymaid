package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.FusionLogic;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** La escena de fusión: esfera de poder amarilla, rayos eléctricos y un ciclón amarillo hasta el cielo. */
public final class FusionFx {
    private static final ResourceLocation FIRE = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura_fire.png");

    private FusionFx() {}

    public static void render(AbstractClientPlayer p, float partial, PoseStack pose, MultiBufferSource buf) {
        SaiyanState s = ClientStates.get(p.getId());
        float strength, prog;
        int afterMax = 60;
        if (s.fusing > 0) {
            prog = 1.0F - (s.fusing - partial) / FusionLogic.FUSING_TICKS;
            strength = 0.5F + prog * 0.5F;
        } else if (s.fusion > 0 && s.fusionMax - s.fusionTicks < afterMax) {
            prog = 1.0F;
            float f = 1.0F - (s.fusionMax - s.fusionTicks + partial) / afterMax;
            strength = f * f;
        } else {
            return;
        }
        float t = p.tickCount + partial;
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();

        // ---- esfera de poder
        if (s.fusing > 0) {
            float r = 0.9F + prog * 3.0F + 0.12F * Mth.sin(t * 0.9F);
            VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(FIRE, (t * 0.02F) % 1.0F, (t * 0.05F) % 1.0F));
            int LAT = 12, LON = 20;
            for (int i = 0; i < LAT; i++) {
                float a0 = Mth.PI * i / LAT, a1 = Mth.PI * (i + 1) / LAT;
                for (int j = 0; j < LON; j++) {
                    float b0 = Mth.TWO_PI * j / LON, b1 = Mth.TWO_PI * (j + 1) / LON;
                    float br = 0.75F * strength;
                    sph(vc, m, n, r, a0, b0, br, j / (float) LON, i / (float) LAT);
                    sph(vc, m, n, r, a0, b1, br, (j + 1) / (float) LON, i / (float) LAT);
                    sph(vc, m, n, r, a1, b1, br, (j + 1) / (float) LON, (i + 1) / (float) LAT);
                    sph(vc, m, n, r, a1, b0, br, j / (float) LON, (i + 1) / (float) LAT);
                }
            }
            // ---- rayos que salen de la esfera
            VertexConsumer lv = buf.getBuffer(RenderType.lightning());
            RandomSource rnd = RandomSource.create(p.getId() * 31L + (long) (t / 2));
            for (int k = 0; k < 7; k++) {
                float ang = rnd.nextFloat() * Mth.TWO_PI, el = (rnd.nextFloat() - 0.3F) * 1.4F;
                float x = 0, y = 1.0F, z = 0;
                float len = r + 2 + rnd.nextFloat() * 5;
                int segs = 8;
                for (int i = 0; i < segs; i++) {
                    float d = len * (i + 1) / segs;
                    float nx = Mth.cos(ang) * Mth.cos(el) * d + (rnd.nextFloat() - 0.5F) * 0.9F;
                    float ny = 1.0F + Mth.sin(el) * d + (rnd.nextFloat() - 0.5F) * 0.9F;
                    float nz = Mth.sin(ang) * Mth.cos(el) * d + (rnd.nextFloat() - 0.5F) * 0.9F;
                    float w = 0.09F * (1.0F - i / (float) segs) + 0.02F;
                    bolt(lv, m, x, y, z, nx, ny, nz, w, strength);
                    x = nx; y = ny; z = nz;
                }
            }
        }

        // ---- ciclón amarillo que sube hasta el cielo
        float H = 8.0F + prog * 56.0F;
        VertexConsumer cv = buf.getBuffer(RenderType.energySwirl(FIRE, (t * 0.01F) % 1.0F, (-t * 0.07F) % 1.0F));
        int RINGS = 30, SEG = 22;
        for (int layer = 0; layer < 2; layer++) {
            float lb = (layer == 0 ? 0.6F : 0.32F) * strength;
            for (int k = 0; k < RINGS; k++) {
                float y0 = H * k / RINGS, y1 = H * (k + 1) / RINGS;
                for (int j = 0; j < SEG; j++) {
                    cyc(cv, m, n, t, H, y0, j, SEG, layer, lb);
                    cyc(cv, m, n, t, H, y0, j + 1, SEG, layer, lb);
                    cyc(cv, m, n, t, H, y1, j + 1, SEG, layer, lb);
                    cyc(cv, m, n, t, H, y1, j, SEG, layer, lb);
                }
            }
        }
    }

    private static void sph(VertexConsumer vc, Matrix4f m, Matrix3f n, float r, float a, float b, float br, float u, float v) {
        float x = Mth.sin(a) * Mth.cos(b), y = Mth.cos(a), z = Mth.sin(a) * Mth.sin(b);
        vc.vertex(m, x * r, 1.0F + y * r, z * r).color(1.0F * br, 0.85F * br, 0.25F * br, 1.0F).uv(u * 2, v * 2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, x, y, z).endVertex();
    }

    private static void cyc(VertexConsumer vc, Matrix4f m, Matrix3f n, float t, float H, float y, int j, int seg, int layer, float b) {
        float k = y / H;
        float rad = (0.9F + (float) Math.pow(k, 1.3) * 6.0F + 0.25F * Mth.sin(y * 0.6F - t * 0.3F)) * (layer == 0 ? 1.0F : 1.25F);
        float twist = t * (layer == 0 ? 0.3F : -0.2F) - y * 0.2F;
        float a = j * Mth.TWO_PI / seg + twist;
        float fade = b * (1.0F - k * 0.75F);
        vc.vertex(m, Mth.cos(a) * rad, y, Mth.sin(a) * rad).color(1.0F * fade, 0.82F * fade, 0.2F * fade, 1.0F)
                .uv(j / (float) seg, k * 4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 1, 0).endVertex();
    }

    private static void bolt(VertexConsumer lv, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float w, float s) {
        float r = 1.0F * s, g = 0.95F * s, b = 0.55F * s;
        lv.vertex(m, x0 - w, y0, z0).color(r, g, b, 1).endVertex();
        lv.vertex(m, x0 + w, y0, z0).color(r, g, b, 1).endVertex();
        lv.vertex(m, x1 + w, y1, z1).color(r, g, b, 1).endVertex();
        lv.vertex(m, x1 - w, y1, z1).color(r, g, b, 1).endVertex();
        lv.vertex(m, x0, y0 - w, z0).color(r, g, b, 1).endVertex();
        lv.vertex(m, x0, y0 + w, z0).color(r, g, b, 1).endVertex();
        lv.vertex(m, x1, y1 + w, z1).color(r, g, b, 1).endVertex();
        lv.vertex(m, x1, y1 - w, z1).color(r, g, b, 1).endVertex();
    }
}
