package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.yunn.YunnEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** Ropa 3D de Yunn: camiseta con corazón, shorts, guantes y zapatillas. */
public class YunnOutfitLayer extends RenderLayer<YunnEntity, YunnModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(TannyMaid.MODID, "yunn"), "outfit");
    private static final ResourceLocation TEXTURE = new ResourceLocation(TannyMaid.MODID, "textures/entity/yunn/outfit.png");
    private final ModelPart root, body, rightArm, leftArm, rightLeg, leftLeg;

    public YunnOutfitLayer(RenderLayerParent<YunnEntity, YunnModel> parent, EntityModelSet models) {
        super(parent);
        root = models.bakeLayer(LAYER);
        Map<String, ModelPart> m = TannyGeneratedModels.bakeYunnOutfit(root);
        body = m.get("body");
        rightArm = m.get("right_arm");
        leftArm = m.get("left_arm");
        rightLeg = m.get("right_leg");
        leftLeg = m.get("left_leg");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, YunnEntity e, float limbSwing, float limbAmount,
                       float partial, float age, float headYaw, float headPitch) {
        if (e.isInvisible()) return;
        YunnModel m = getParentModel();
        body.copyFrom(m.body);
        rightArm.copyFrom(m.rightArm);
        leftArm.copyFrom(m.leftArm);
        rightLeg.copyFrom(m.rightLeg);
        leftLeg.copyFrom(m.leftLeg);
        root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F));
    }
}
