package com.totallywaifu.client;

import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Cuerpo de la waifu: el modelo "slim" del jugador (así su skin también sirve como skin de jugador).
 * En reposo: de pie, con los brazos un poco levantados sosteniendo su objeto frente al pecho.
 */
public class WaifuModel extends PlayerModel<WaifuEntity> {
    public WaifuModel(ModelPart root) {
        super(root, true);
    }

    @Override
    public void prepareMobModel(WaifuEntity e, float limbSwing, float amount, float partial) {
        super.prepareMobModel(e, limbSwing, amount, partial);
        rightArmPose = e.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        leftArmPose = ArmPose.EMPTY;
    }

    /** ¿Está en reposo, sosteniendo su objeto? */
    public static boolean holding(WaifuEntity e, float amount) {
        return amount < 0.08F && !e.isBusy() && e.getMainHandItem().isEmpty() && !e.isAggressive();
    }

    @Override
    public void setupAnim(WaifuEntity e, float limbSwing, float amount, float age, float headYaw, float headPitch) {
        super.setupAnim(e, limbSwing, amount, age, headYaw, headPitch);
        float breath = Mth.sin(age * 0.09F);
        head.y = (crouching ? 4.2F : 0.0F) + breath * 0.15F;
        body.xRot = (crouching ? 0.5F : 0.0F) + breath * 0.01F;
        float tilt = 0;
        if (holding(e, amount) && attackTime <= 0) {
            // brazos un poco levantados, las manos juntas frente al pecho
            rightArm.xRot = -1.05F + breath * 0.03F;
            rightArm.yRot = -0.42F;
            rightArm.zRot = 0.0F;
            leftArm.xRot = -1.05F + breath * 0.03F;
            leftArm.yRot = 0.42F;
            leftArm.zRot = 0.0F;
            tilt = Mth.sin(age * 0.04F + e.getId()) * 0.04F;
        }
        if (e.isHappy()) {
            tilt = Mth.sin(age * 0.3F) * 0.12F;
            head.xRot -= 0.1F;
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
