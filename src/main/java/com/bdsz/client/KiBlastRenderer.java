package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.entity.KiBlastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Esfera de ki: núcleo brillante + envoltura de energía que gira. */
public class KiBlastRenderer extends EntityRenderer<KiBlastEntity> {
    private static final ResourceLocation AURA = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura.png");
    private static final ResourceLocation CORE = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/ki_core.png");
    private final ModelPart sphere;

    public KiBlastRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.sphere = ctx.bakeLayer(ClientModEvents.KI_BLAST);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("ball", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -4, -4, 8, 8, 8)
                .addBox(-5, -3, -3, 10, 6, 6)
                .addBox(-3, -5, -3, 6, 10, 6)
                .addBox(-3, -3, -5, 6, 6, 10), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(KiBlastEntity e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        int c = e.getColor();
        float r = ((c >> 16) & 255) / 255F, g = ((c >> 8) & 255) / 255F, b = (c & 255) / 255F;
        float t = e.tickCount + partial;
        float s = e.getSize();
        pose.pushPose();
        pose.translate(0, e.getBbHeight() / 2, 0);
        pose.mulPose(Axis.YP.rotationDegrees(t * 20));
        pose.mulPose(Axis.XP.rotationDegrees(t * 13));
        pose.scale(s, s, s);
        sphere.render(pose, buf.getBuffer(RenderType.entityTranslucentEmissive(CORE)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                Math.min(1, r + 0.5F), Math.min(1, g + 0.5F), Math.min(1, b + 0.5F), 1.0F);
        float pulse = 1.35F + 0.1F * (float) Math.sin(t * 0.8F);
        pose.scale(pulse, pulse, pulse);
        sphere.render(pose, buf.getBuffer(RenderType.energySwirl(AURA, (t * 0.02F) % 1, (t * 0.05F) % 1)), LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
        pose.popPose();
        super.render(e, yaw, partial, pose, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(KiBlastEntity e) {
        return CORE;
    }
}
