package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.entity.KameBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Rayo del Kamehameha: núcleo blanco brillante + envoltura de energía, con una esfera en el origen y en la punta. */
public class KameBeamRenderer extends EntityRenderer<KameBeamEntity> {
    private static final ResourceLocation AURA = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura.png");
    private static final ResourceLocation CORE = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/ki_core.png");

    public KameBeamRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(KameBeamEntity e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        float len = e.getLength();
        float t = e.tickCount + partial;
        float grow = Math.min(1.0F, t / 5.0F);
        float fade = e.tickCount > KameBeamEntity.DURATION - 6 ? Math.max(0, (KameBeamEntity.DURATION - t) / 6.0F) : 1.0F;
        float w = e.getWidth() * grow * fade * (1.0F + 0.08F * Mth.sin(t * 1.3F));
        int c = e.getColor();
        float r = ((c >> 16) & 255) / 255F, g = ((c >> 8) & 255) / 255F, b = (c & 255) / 255F;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(partial, e.yRotO, e.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partial, e.xRotO, e.getXRot())));
        pose.mulPose(Axis.ZP.rotationDegrees(t * 12));
        float l = len * Math.min(1.0F, t / 3.0F);
        tube(pose, buf.getBuffer(RenderType.entityTranslucentEmissive(CORE)), w * 0.45F, l, 1, 1, 1, 0.95F);
        tube(pose, buf.getBuffer(RenderType.energySwirl(AURA, (t * 0.02F) % 1, (-t * 0.15F) % 1)), w, l, r, g, b, 1);
        tube(pose, buf.getBuffer(RenderType.energySwirl(AURA, (-t * 0.015F) % 1, (-t * 0.1F) % 1)), w * 1.6F, l, r * 0.5F, g * 0.5F, b * 0.5F, 1);
        // esfera del origen y de la punta
        ball(pose, buf, 0, w * 1.9F, r, g, b, t);
        ball(pose, buf, l, w * 2.3F, r, g, b, t);
        pose.popPose();
        super.render(e, yaw, partial, pose, buf, light);
    }

    private static void ball(PoseStack pose, MultiBufferSource buf, float z, float size, float r, float g, float b, float t) {
        pose.pushPose();
        pose.translate(0, 0, z);
        pose.mulPose(Axis.YP.rotationDegrees(t * 25));
        tube(pose, buf.getBuffer(RenderType.energySwirl(AURA, (t * 0.03F) % 1, (t * 0.05F) % 1)), size, size, r, g, b, 1, -size / 2);
        pose.popPose();
    }

    private static void tube(PoseStack pose, VertexConsumer vc, float w, float len, float r, float g, float b, float a) {
        tube(pose, vc, w, len, r, g, b, a, 0);
    }

    /** Prisma octogonal a lo largo del eje Z. */
    private static void tube(PoseStack pose, VertexConsumer vc, float w, float len, float r, float g, float b, float a, float z0) {
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        int sides = 8;
        for (int i = 0; i < sides; i++) {
            double a0 = i * Math.PI * 2 / sides, a1 = (i + 1) * Math.PI * 2 / sides;
            float x0 = (float) Math.cos(a0) * w, y0 = (float) Math.sin(a0) * w;
            float x1 = (float) Math.cos(a1) * w, y1 = (float) Math.sin(a1) * w;
            float u0 = (float) i / sides, u1 = (float) (i + 1) / sides;
            float nx = (float) Math.cos((a0 + a1) / 2), ny = (float) Math.sin((a0 + a1) / 2);
            v(vc, m, n, x0, y0, z0, r, g, b, a, u0, 0, nx, ny);
            v(vc, m, n, x1, y1, z0, r, g, b, a, u1, 0, nx, ny);
            v(vc, m, n, x1, y1, z0 + len, r, g, b, a, u1, len, nx, ny);
            v(vc, m, n, x0, y0, z0 + len, r, g, b, a, u0, len, nx, ny);
        }
        // tapas
        for (float zz : new float[] {z0, z0 + len}) {
            for (int i = 0; i < sides; i++) {
                double a0 = i * Math.PI * 2 / sides, a1 = (i + 1) * Math.PI * 2 / sides;
                v(vc, m, n, 0, 0, zz, r, g, b, a, 0.5F, 0.5F, 0, 0);
                v(vc, m, n, (float) Math.cos(a0) * w, (float) Math.sin(a0) * w, zz, r, g, b, a, 0, 0, 0, 0);
                v(vc, m, n, (float) Math.cos(a1) * w, (float) Math.sin(a1) * w, zz, r, g, b, a, 1, 0, 0, 0);
                v(vc, m, n, 0, 0, zz, r, g, b, a, 0.5F, 0.5F, 0, 0);
            }
        }
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float r, float g, float b, float a,
                          float u, float vv, float nx, float ny) {
        vc.vertex(m, x, y, z).color(r, g, b, a).uv(u, vv).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, nx, ny, 0).endVertex();
    }

    @Override
    public boolean shouldRender(KameBeamEntity e, net.minecraft.client.renderer.culling.Frustum f, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(KameBeamEntity e) {
        return CORE;
    }
}
