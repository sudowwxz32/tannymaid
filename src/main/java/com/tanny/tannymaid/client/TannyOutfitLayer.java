package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vestido 3D de Tanny: falda redonda con enaguas, delantal, lazos, mangas abullonadas,
 * puños, medias con volante y zapatos. La falda se abre siguiendo las piernas y se esconden
 * las piezas que chocarían con la armadura puesta.
 */
public class TannyOutfitLayer extends RenderLayer<TannyEntity, TannyModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(TannyMaid.MODID, "tanny"), "outfit");
    private static final ResourceLocation TEXTURE = new ResourceLocation(TannyMaid.MODID, "textures/entity/tanny/outfit.png");

    private final ModelPart root, body, rightArm, leftArm, rightLeg, leftLeg;
    private final ModelPart front, back, left, right, fr, fl, br, bl, tailR, tailL, ribbonTailR, ribbonTailL;
    private final List<ModelPart> chestHidden = new ArrayList<>();
    private final List<ModelPart> legsHidden = new ArrayList<>();
    private final List<ModelPart> feetHidden = new ArrayList<>();

    public TannyOutfitLayer(RenderLayerParent<TannyEntity, TannyModel> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(LAYER);
        Map<String, ModelPart> m = TannyGeneratedModels.bakeOutfit(root);
        body = m.get("body");
        rightArm = m.get("right_arm");
        leftArm = m.get("left_arm");
        rightLeg = m.get("right_leg");
        leftLeg = m.get("left_leg");
        front = m.get("skirt_front");
        back = m.get("skirt_back");
        left = m.get("skirt_left");
        right = m.get("skirt_right");
        fr = m.get("skirt_fr");
        fl = m.get("skirt_fl");
        br = m.get("skirt_br");
        bl = m.get("skirt_bl");
        tailR = m.get("bow_tail_r");
        tailL = m.get("bow_tail_l");
        ribbonTailR = m.get("ribbon_tail_r");
        ribbonTailL = m.get("ribbon_tail_l");
        for (String k : new String[] {"bust", "collar", "collar_tip_r", "collar_tip_l", "ribbon_knot", "ribbon_r", "ribbon_l",
                "ribbon_tail_r", "ribbon_tail_l", "puff_r", "puff_l"}) chestHidden.add(m.get(k));
        for (String k : new String[] {"frill_r", "frill_l"}) legsHidden.add(m.get(k));
        for (String k : new String[] {"frill_r", "frill_l", "shoe_r", "shoe_l"}) feetHidden.add(m.get(k));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TannyEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        TannyModel model = getParentModel();
        body.copyFrom(model.body);
        rightArm.copyFrom(model.rightArm);
        leftArm.copyFrom(model.leftArm);
        rightLeg.copyFrom(model.rightLeg);
        leftLeg.copyFrom(model.leftLeg);

        float flare = TannyGeneratedModels.SKIRT_FLARE;
        float forward = Math.max(0.0F, -Math.min(model.rightLeg.xRot, model.leftLeg.xRot));
        float backward = Math.max(0.0F, Math.max(model.rightLeg.xRot, model.leftLeg.xRot));
        float sway = Mth.sin(ageInTicks * 0.08F) * 0.02F;
        float side = Math.abs(model.rightLeg.zRot) + Math.abs(model.leftLeg.zRot);
        front.xRot = -flare - forward * 0.75F - sway;
        back.xRot = -flare - backward * 0.6F + sway;
        left.xRot = -flare - side * 0.5F - limbSwingAmount * 0.05F;
        right.xRot = -flare - side * 0.5F - limbSwingAmount * 0.05F;
        fr.xRot = fl.xRot = -flare - forward * 0.5F - limbSwingAmount * 0.04F;
        br.xRot = bl.xRot = -flare - backward * 0.45F - limbSwingAmount * 0.04F;
        tailR.xRot = 0.15F + limbSwingAmount * 0.25F + sway;
        tailL.xRot = 0.15F + limbSwingAmount * 0.25F - sway;
        ribbonTailR.xRot = -0.1F - limbSwingAmount * 0.15F + sway;
        ribbonTailL.xRot = -0.1F - limbSwingAmount * 0.15F - sway;

        boolean chest = !entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        boolean legs = !entity.getItemBySlot(EquipmentSlot.LEGS).isEmpty();
        boolean feet = !entity.getItemBySlot(EquipmentSlot.FEET).isEmpty();
        setVisible(chest, chestHidden, false);
        setVisible(legs, legsHidden, false);
        setVisible(feet, feetHidden, false);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        root.render(poseStack, vc, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));

        setVisible(chest, chestHidden, true);
        setVisible(legs, legsHidden, true);
        setVisible(feet, feetHidden, true);
    }

    private static void setVisible(boolean when, List<ModelPart> parts, boolean value) {
        if (!when) return;
        for (ModelPart p : parts) p.visible = value;
    }
}
