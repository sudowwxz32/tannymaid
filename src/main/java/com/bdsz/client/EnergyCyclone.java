package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.Form;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Legendario Fase 3: un ciclón de energía verde que gira alrededor del guerrero y se eleva hasta el cielo
 * mientras se transforma; al terminar se va apagando poco a poco.
 */
public final class EnergyCyclone {
    private static final ResourceLocation FIRE = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura_fire.png");
    private static final int RINGS = 28, SEG = 24;
    private static final float AFTER = 110.0F;

    private EnergyCyclone() {}

    public static void render(AbstractClientPlayer p, float partial, PoseStack pose, MultiBufferSource buf) {
        SaiyanState s = ClientStates.get(p.getId());
        if (!s.saiyan || s.ozaru || Form.byId(s.selected) != Form.LEGEND3) return;
        long since = ClientStates.ticksSinceTransform(p.getId());
        float strength, growth;
        if (s.powering > 0) {
            growth = 1.0F - (float) s.powering / Math.max(1, s.poweringTotal);
            strength = 0.4F + growth * 0.6F;
        } else if (since < AFTER) {
            growth = 1.0F;
            float f = 1.0F - (since + partial) / AFTER;
            strength = f * f;
        } else {
            return;
        }
        float t = p.tickCount + partial;
        float H = 6.0F + growth * 42.0F;                          // se eleva hasta el cielo
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();

        // ---- superficie del ciclón: un embudo que gira y se abre hacia arriba
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(FIRE, (t * 0.01F) % 1.0F, (-t * 0.06F) % 1.0F));
        for (int layer = 0; layer < 2; layer++) {
            float lr = layer == 0 ? 1.0F : 1.25F, lb = layer == 0 ? 0.55F : 0.3F;
            for (int k = 0; k < RINGS; k++) {
                float y0 = H * k / RINGS, y1 = H * (k + 1) / RINGS;
                for (int j = 0; j < SEG; j++) {
                    vtx(vc, m, n, t, H, y0, j, lr, layer, lb * strength);
                    vtx(vc, m, n, t, H, y0, j + 1, lr, layer, lb * strength);
                    vtx(vc, m, n, t, H, y1, j + 1, lr, layer, lb * strength);
                    vtx(vc, m, n, t, H, y1, j, lr, layer, lb * strength);
                }
            }
        }
        // ---- tres franjas brillantes en espiral que suben girando
        VertexConsumer lv = buf.getBuffer(RenderType.lightning());
        for (int band = 0; band < 3; band++) {
            float px = 0, py = 0, pz = 0;
            int steps = 48;
            for (int i = 0; i <= steps; i++) {
                float y = H * i / steps;
                float ang = band * Mth.TWO_PI / 3 + t * 0.35F + y * 0.45F;
                float r = radius(y, H, t) * 1.03F;
                float x = Mth.cos(ang) * r, z = Mth.sin(ang) * r;
                if (i > 0) {
                    float fade = strength * (1.0F - (float) i / steps * 0.7F);
                    float w = 0.18F + y * 0.02F;
                    float cr = 0.55F * fade, cg = 1.0F * fade, cb = 0.5F * fade;
                    lv.vertex(m, px, py - w, pz).color(cr, cg, cb, 1.0F).endVertex();
                    lv.vertex(m, px, py + w, pz).color(cr, cg, cb, 1.0F).endVertex();
                    lv.vertex(m, x, y + w, z).color(cr, cg, cb, 1.0F).endVertex();
                    lv.vertex(m, x, y - w, z).color(cr, cg, cb, 1.0F).endVertex();
                }
                px = x; py = y; pz = z;
            }
        }
    }

    private static float radius(float y, float H, float t) {
        float k = y / H;
        return 0.9F + (float) Math.pow(k, 1.3) * 5.5F + 0.25F * Mth.sin(y * 0.6F - t * 0.3F);
    }

    private static void vtx(VertexConsumer vc, Matrix4f m, Matrix3f n, float t, float H, float y, int j, float lr, int layer, float b) {
        float twist = t * (layer == 0 ? 0.25F : -0.18F) - y * 0.18F;
        float a = j * Mth.TWO_PI / SEG + twist;
        float r = radius(y, H, t) * lr;
        float k = y / H;
        // más brillante abajo; se desvanece al subir y en la base
        float fade = b * Math.min(1.0F, k * 8.0F + 0.2F) * (1.0F - k * 0.75F);
        vc.vertex(m, Mth.cos(a) * r, y, Mth.sin(a) * r).color(0.3F * fade, 1.0F * fade, 0.35F * fade, 1.0F)
                .uv(j * 2.0F / SEG, k * 6.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(n, 0, 1, 0).endVertex();
    }
}
