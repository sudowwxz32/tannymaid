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

    /** Brazos según lo que usa: escudo en alto, arco tensado o arma en la mano. */
    @Override
    public void prepareMobModel(TannyEntity e, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(e, limbSwing, limbSwingAmount, partialTick);
        this.rightArmPose = e.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        this.leftArmPose = e.getOffhandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        if (e.isUsingItem()) {
            net.minecraft.world.item.UseAnim anim = e.getUseItem().getUseAnimation();
            ArmPose pose = anim == net.minecraft.world.item.UseAnim.BLOCK ? ArmPose.BLOCK
                    : anim == net.minecraft.world.item.UseAnim.BOW ? ArmPose.BOW_AND_ARROW : ArmPose.ITEM;
            if (e.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND) this.rightArmPose = pose;
            else this.leftArmPose = pose;
        }
    }

    @Override
    public void setupAnim(TannyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.riding = this.riding || entity.isWeakened();
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // IMPORTANTE: los valores se ASIGNAN (no se suman) porque el modelo no reinicia
        // head.zRot / head.y / body.xRot en cada fotograma; sumarlos hacía girar la cabeza en círculos.

        // Respiración sutil
        float breath = Mth.sin(ageInTicks * 0.09F);
        this.head.y = (this.crouching ? 4.2F : 0.0F) + breath * 0.2F;
        this.body.xRot = (this.crouching ? 0.5F : 0.0F) + breath * 0.012F;

        float tilt = 0.0F;
        if (entity.isWeakened()) {
            this.head.xRot = 0.5F;
            tilt = 0.15F;
        } else if (entity.isTalking()) {
            // Asiente suavemente y ladea la cabeza al hablar
            this.head.xRot += Mth.sin(ageInTicks * 0.45F) * 0.07F;
            tilt = Mth.sin(ageInTicks * 0.2F) * 0.07F;
        } else if (!entity.isAggressive()) {
            // Inclinación curiosa y tierna, muy lenta
            tilt = Mth.sin(ageInTicks * 0.025F) * 0.05F;
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
