package com.totallywaifu.client;

import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Cuerpo de la waifu: respiración, poses de reposo con personalidad y gestos al trabajar. */
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

    @Override
    public void setupAnim(WaifuEntity e, float limbSwing, float amount, float age, float headYaw, float headPitch) {
        super.setupAnim(e, limbSwing, amount, age, headYaw, headPitch);
        float breath = Mth.sin(age * 0.09F);
        head.y = breath * 0.2F;
        body.xRot = breath * 0.012F;
        float tilt = 0;
        boolean idle = amount < 0.05F && attackTime <= 0 && !e.isBusy();
        if (idle) {
            switch (e.kind) {
                case KOZUE -> {               // manos en la cintura, segura de sí
                    rightArm.xRot = 0.1F; rightArm.zRot = 0.6F; rightArm.yRot = -0.3F;
                    leftArm.xRot = 0.1F; leftArm.zRot = -0.6F; leftArm.yRot = 0.3F;
                }
                case HOMURA -> {              // brazos cruzados, orgullosa
                    rightArm.xRot = -0.95F; rightArm.yRot = -0.55F; rightArm.zRot = 0.1F;
                    leftArm.xRot = -0.85F; leftArm.yRot = 0.55F; leftArm.zRot = -0.1F;
                    head.xRot -= 0.06F;
                    tilt = -0.06F;
                }
                case KURA -> {                // manos juntas al frente, como sirvienta
                    rightArm.xRot = -0.35F; rightArm.yRot = 0.45F; rightArm.zRot = 0.05F;
                    leftArm.xRot = -0.35F; leftArm.yRot = -0.45F; leftArm.zRot = -0.05F;
                    tilt = Mth.sin(age * 0.05F) * 0.05F;
                }
                case SHIORI -> {              // una mano levantada como sosteniendo magia
                    leftArm.xRot = -0.9F + breath * 0.05F; leftArm.yRot = 0.2F; leftArm.zRot = -0.1F;
                    rightArm.zRot = 0.12F;
                }
            }
        }
        if (e.isHappy()) {
            tilt = Mth.sin(age * 0.3F) * 0.12F;     // contenta: mueve la cabeza
            head.xRot -= 0.1F;
        }
        if (e.kind == WaifuKind.SHIORI && !idle && attackTime > 0) {
            leftArm.xRot = -1.5F;                    // lanza el hechizo con la mano izquierda
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
