package com.linterna.client;

import com.linterna.Linterna;
import com.linterna.entity.RayoVerde;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Disparos y bombas de energía: una esfera de luz verde que mira siempre a la cámara. */
public class RayoRenderer extends EntityRenderer<RayoVerde> {
    private static final ResourceLocation TEX = new ResourceLocation(Linterna.MODID, "textures/entity/rayo.png");

    public RayoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(RayoVerde e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        pose.pushPose();
        float s = e.isBomb() ? 0.7F : 0.45F;
        pose.translate(0, 0.2, 0);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.scale(s, s, s);
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive(TEX));
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        v(vc, m, n, -0.5F, -0.5F, 0, 1);
        v(vc, m, n, 0.5F, -0.5F, 1, 1);
        v(vc, m, n, 0.5F, 0.5F, 1, 0);
        v(vc, m, n, -0.5F, 0.5F, 0, 0);
        pose.popPose();
        super.render(e, yaw, partial, pose, buf, light);
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float u, float w) {
        vc.vertex(m, x, y, 0).color(255, 255, 255, 230).uv(u, w).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 1, 0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(RayoVerde e) {
        return TEX;
    }
}
