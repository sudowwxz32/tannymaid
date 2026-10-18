package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Dibuja el cabello 3D pegado a la cabeza, con un leve vaivén al caminar y respirar. */
public class TannyHairLayer extends RenderLayer<TannyEntity, TannyModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TannyMaid.MODID, "textures/entity/tanny/hair.png");

    private final TannyHairModel hair;

    public TannyHairLayer(RenderLayerParent<TannyEntity, TannyModel> parent, EntityModelSet models) {
        super(parent);
        this.hair = new TannyHairModel(models.bakeLayer(TannyHairModel.LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TannyEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;

        hair.root.copyFrom(getParentModel().head);

        // Vaivén suave del cabello
        float breeze = Mth.sin(ageInTicks * 0.08F) * 0.025F;
        float walk = Math.abs(Mth.cos(limbSwing * 0.6662F)) * limbSwingAmount;
        hair.sideLeft.zRot = -TannyHairModel.SIDE_SPLAY - breeze - walk * 0.08F;
        hair.sideRight.zRot = TannyHairModel.SIDE_SPLAY + breeze + walk * 0.08F;
        hair.back.xRot = TannyHairModel.BACK_TILT + breeze + limbSwingAmount * 0.18F;
        hair.lockLeft.xRot = -0.05F + walk * 0.12F;
        hair.lockRight.xRot = -0.05F + walk * 0.12F;
        for (int i = 0; i < hair.bangs.length; i++) {
            hair.bangs[i].xRot = TannyHairModel.BANG_TILT - limbSwingAmount * 0.12F
                    + Mth.sin(ageInTicks * 0.1F + i) * 0.015F;
        }

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        hair.root.render(poseStack, vc, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
    }
}
