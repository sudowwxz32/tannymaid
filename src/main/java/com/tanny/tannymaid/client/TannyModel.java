package com.tanny.tannymaid.client;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Modelo de Tanny (cuerpo femenino de brazos delgados) con animaciones extra:
 * respiración, gestos de cabeza al hablar, inclinación curiosa y pose sentada al debilitarse.
 */
public class TannyModel extends PlayerModel<TannyEntity> {

    public TannyModel(ModelPart root) {
        super(root, true);
    }

    @Override
    public void setupAnim(TannyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.riding = this.riding || entity.isWeakened();
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Respiración sutil
        float breath = Mth.sin(ageInTicks * 0.09F);
        this.head.y += breath * 0.25F;
        this.body.xRot += breath * 0.015F;
        this.rightArm.zRot += 0.04F + breath * 0.02F;
        this.leftArm.zRot -= 0.04F + breath * 0.02F;

        if (entity.isWeakened()) {
            this.head.xRot = 0.5F;
            this.head.zRot = 0.15F;
        } else if (entity.isTalking()) {
            // Gestos de cabeza al hablar
            this.head.xRot += Mth.sin(ageInTicks * 0.55F) * 0.10F;
            this.head.zRot += Mth.sin(ageInTicks * 0.27F) * 0.08F;
        } else if (!entity.isAggressive()) {
            // Inclinación curiosa y tierna
            this.head.zRot += Mth.sin(ageInTicks * 0.025F) * 0.05F;
        }

        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
    }
}
