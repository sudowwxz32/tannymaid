package com.bdsz.client.kaori;

import com.bdsz.companion.KaoriEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Modelo de Kaori: respiración, postura altanera (mano en la cadera al estar quieta), gestos al hablar y sentada al descansar. */
public class KaoriModel extends PlayerModel<KaoriEntity> {
    public KaoriModel(ModelPart root) {
        super(root, true);
    }

    @Override
    public void prepareMobModel(KaoriEntity e, float limbSwing, float amount, float partial) {
        super.prepareMobModel(e, limbSwing, amount, partial);
        rightArmPose = e.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        leftArmPose = e.getOffhandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        if (e.isUsingItem()) {
            net.minecraft.world.item.UseAnim a = e.getUseItem().getUseAnimation();
            ArmPose pose = a == net.minecraft.world.item.UseAnim.BLOCK ? ArmPose.BLOCK : a == net.minecraft.world.item.UseAnim.BOW ? ArmPose.BOW_AND_ARROW : ArmPose.ITEM;
            if (e.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND) rightArmPose = pose; else leftArmPose = pose;
        }
    }

    @Override
    public void setupAnim(KaoriEntity e, float limbSwing, float amount, float age, float headYaw, float headPitch) {
        riding = riding || e.isWeakened() || e.isFlying();
        super.setupAnim(e, limbSwing, amount, age, headYaw, headPitch);
        // valores ASIGNADOS (el modelo no reinicia head.zRot / head.y / body.xRot cada fotograma)
        float breath = Mth.sin(age * 0.09F);
        head.y = (crouching ? 4.2F : 0.0F) + breath * 0.2F;
        body.xRot = (crouching ? 0.5F : 0.0F) + breath * 0.012F;
        float tilt = 0.0F;
        if (e.isFlying()) {
            // sentada en su nave, con las manos en los controles
            rightArm.xRot = -0.95F + Mth.sin(age * 0.2F) * 0.03F;
            rightArm.yRot = -0.15F;
            rightArm.zRot = 0.0F;
            leftArm.xRot = -0.95F - Mth.sin(age * 0.2F) * 0.03F;
            leftArm.yRot = 0.15F;
            leftArm.zRot = 0.0F;
        } else if (e.getForm() > 0 && e.sinceTransform() < 30) {
            // grito al transformarse: puños abajo, pecho afuera, cabeza al cielo
            float tr = Mth.sin(age * 2.5F) * 0.04F;
            head.xRot = -0.6F + tr;
            rightArm.xRot = 0.4F + tr; rightArm.zRot = 0.45F;
            leftArm.xRot = 0.4F - tr; leftArm.zRot = -0.45F;
            body.xRot = -0.15F;
        } else if (e.isWeakened()) {
            head.xRot = 0.45F;
            tilt = 0.12F;
        } else if (e.isTalking()) {
            head.xRot += Mth.sin(age * 0.45F) * 0.06F;
            tilt = Mth.sin(age * 0.2F) * 0.06F;
        } else if (!e.isAggressive() && amount < 0.05F && attackTime <= 0 && e.getMainHandItem().isEmpty()) {
            // altanera: mano izquierda en la cadera y la barbilla un poco arriba
            leftArm.xRot = 0.15F;
            leftArm.yRot = 0.35F;
            leftArm.zRot = -0.65F + breath * 0.02F;
            head.xRot -= 0.08F;
            tilt = -0.05F;
        }
        head.zRot = tilt;
        hat.copyFrom(head);
        jacket.copyFrom(body);
        leftSleeve.copyFrom(leftArm);
        rightSleeve.copyFrom(rightArm);
        leftPants.copyFrom(leftLeg);
        rightPants.copyFrom(rightLeg);
    }
}
