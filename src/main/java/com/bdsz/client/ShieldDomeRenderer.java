package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.block.ShieldGeneratorBlock;
import com.bdsz.block.ShieldGeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Domo de energía translúcido alrededor del Generador Protector encendido. */
public class ShieldDomeRenderer implements BlockEntityRenderer<ShieldGeneratorBlockEntity> {
    private static final ResourceLocation TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura.png");
    private static final int LON = 40, LAT = 20;

    public ShieldDomeRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ShieldGeneratorBlockEntity be, float partial, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        if (!be.getBlockState().getValue(ShieldGeneratorBlock.ON) || be.getLevel() == null) return;
        float t = be.getLevel().getGameTime() + partial;
        float r = ShieldGeneratorBlockEntity.RADIUS;
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.002F) % 1.0F, (t * 0.004F) % 1.0F));
        float pulse = 0.85F + 0.15F * Mth.sin(t * 0.05F);
        // media esfera (de la base al cénit) un poco hundida en el suelo
        for (int i = 0; i < LAT; i++) {
            float a0 = (float) i / LAT * Mth.HALF_PI * 1.08F - 0.08F, a1 = (float) (i + 1) / LAT * Mth.HALF_PI * 1.08F - 0.08F;
            for (int j = 0; j < LON; j++) {
                float b0 = (float) j / LON * Mth.TWO_PI, b1 = (float) (j + 1) / LON * Mth.TWO_PI;
                float bright = (0.10F + 0.08F * (1.0F - (float) i / LAT)) * pulse;     // más brillante cerca del suelo
                v(vc, m, n, r, a0, b0, j * 0.25F, i * 0.25F, bright);
                v(vc, m, n, r, a0, b1, (j + 1) * 0.25F, i * 0.25F, bright);
                v(vc, m, n, r, a1, b1, (j + 1) * 0.25F, (i + 1) * 0.25F, bright);
                v(vc, m, n, r, a1, b0, j * 0.25F, (i + 1) * 0.25F, bright);
            }
        }
        // anillo brillante en la base
        VertexConsumer ring = buf.getBuffer(RenderType.lightning());
        for (int j = 0; j < LON; j++) {
            float b0 = (float) j / LON * Mth.TWO_PI, b1 = (float) (j + 1) / LON * Mth.TWO_PI;
            float c = 0.35F * pulse;
            ring.vertex(m, Mth.cos(b0) * r, -0.45F, Mth.sin(b0) * r).color(0.3F * c, 0.9F * c, c, 1.0F).endVertex();
            ring.vertex(m, Mth.cos(b1) * r, -0.45F, Mth.sin(b1) * r).color(0.3F * c, 0.9F * c, c, 1.0F).endVertex();
            ring.vertex(m, Mth.cos(b1) * r, 0.6F, Mth.sin(b1) * r).color(0, 0, 0, 1.0F).endVertex();
            ring.vertex(m, Mth.cos(b0) * r, 0.6F, Mth.sin(b0) * r).color(0, 0, 0, 1.0F).endVertex();
        }
        pose.popPose();
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float r, float lat, float lon, float u, float vv, float b) {
        float y = Mth.sin(lat) * r, h = Mth.cos(lat) * r;
        vc.vertex(m, Mth.cos(lon) * h, y - 0.5F, Mth.sin(lon) * h).color(0.35F * b, 0.85F * b, b, 1.0F).uv(u, vv)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 1, 0).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(ShieldGeneratorBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
