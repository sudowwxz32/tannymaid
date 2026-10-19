package com.laury.laurymod.client;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Cuerpo de Laury: respiración, gestos al hablar y pose sentada al debilitarse. */
public class LauryHumanModel extends PlayerModel<LauryEntity> {

    public LauryHumanModel(ModelPart root) {
        super(root, true);
    }

    @Override
    public void setupAnim(LauryEntity e, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
        this.riding = this.riding || e.isWeakened();
        super.setupAnim(e, limbSwing, limbAmount, age, headYaw, headPitch);
        // Se ASIGNAN valores (no se suman) para que la cabeza no gire en círculos.
        float breath = Mth.sin(age * 0.09F);
        this.head.y = (this.crouching ? 4.2F : 0.0F) + breath * 0.2F;
        this.body.xRot = (this.crouching ? 0.5F : 0.0F) + breath * 0.012F;
        float tilt = 0.0F;
        if (e.isWeakened()) {
            this.head.xRot = 0.5F;
            tilt = 0.12F;
        } else if (e.isTalking()) {
            this.head.xRot += Mth.sin(age * 0.5F) * 0.07F;
            tilt = Mth.sin(age * 0.22F) * 0.08F;
        } else if (!e.isAggressive()) {
            tilt = Mth.sin(age * 0.03F) * 0.04F;
        }
        this.head.zRot = tilt;
        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
    }
}
