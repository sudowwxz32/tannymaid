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

/** Dibuja el vestido 3D. La falda se abre siguiendo las piernas al caminar o al sentarse. */
public class TannyOutfitLayer extends RenderLayer<TannyEntity, TannyModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TannyMaid.MODID, "textures/entity/tanny/outfit.png");

    private final TannyOutfitModel outfit;

    public TannyOutfitLayer(RenderLayerParent<TannyEntity, TannyModel> parent, EntityModelSet models) {
        super(parent);
        this.outfit = new TannyOutfitModel(models.bakeLayer(TannyOutfitModel.LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TannyEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        TannyModel model = getParentModel();
        outfit.body.copyFrom(model.body);
        outfit.rightArm.copyFrom(model.rightArm);
        outfit.leftArm.copyFrom(model.leftArm);

        float forward = Math.max(0.0F, -Math.min(model.rightLeg.xRot, model.leftLeg.xRot));
        float backward = Math.max(0.0F, Math.max(model.rightLeg.xRot, model.leftLeg.xRot));
        float sway = Mth.sin(ageInTicks * 0.08F) * 0.02F;
        outfit.skirtFront.xRot = -TannyOutfitModel.SKIRT_FLARE - forward * 0.75F - sway;
        outfit.skirtBack.xRot = TannyOutfitModel.SKIRT_FLARE + backward * 0.6F + sway;
        float side = Math.abs(model.rightLeg.zRot) + Math.abs(model.leftLeg.zRot);
        outfit.skirtLeft.zRot = -TannyOutfitModel.SKIRT_FLARE - side * 0.5F - limbSwingAmount * 0.05F;
        outfit.skirtRight.zRot = TannyOutfitModel.SKIRT_FLARE + side * 0.5F + limbSwingAmount * 0.05F;
        outfit.bowTailLeft.xRot = 0.15F + limbSwingAmount * 0.25F + sway;
        outfit.bowTailRight.xRot = 0.15F + limbSwingAmount * 0.25F - sway;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        outfit.body.render(poseStack, vc, packedLight, overlay);
        outfit.rightArm.render(poseStack, vc, packedLight, overlay);
        outfit.leftArm.render(poseStack, vc, packedLight, overlay);
    }
}
