package com.bdsz.client;

import com.bdsz.entity.DbzEnemy;
import com.bdsz.entity.EnemyKind;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Modelo humanoide de los enemigos con poses propias: lanzar ki, gran esfera, vuelo y postura encorvada. */
public class EnemyModel extends HumanoidModel<DbzEnemy> {
    public EnemyModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(DbzEnemy e, float swing, float amount, float age, float headYaw, float pitch) {
        super.setupAnim(e, swing, amount, age, headYaw, pitch);
        EnemyKind k = e.kind();
        if (k == EnemyKind.ENGENDRO) {
            // encorvado y con los brazos colgando como garras
            body.xRot += 0.35F;
            head.y += 1.5F;
            head.z -= 1.5F;
            rightArm.xRot += 0.35F;
            leftArm.xRot += 0.35F;
            rightArm.z -= 1.5F;
            leftArm.z -= 1.5F;
        }
        boolean flying = e.isNoGravity() && !e.onGround();
        if (flying && e.getCast() == 0 && attackTime <= 0) {
            // flotando: piernas atrás, brazos abiertos
            rightLeg.xRot = 0.35F + Mth.sin(age * 0.1F) * 0.08F;
            leftLeg.xRot = 0.2F + Mth.sin(age * 0.1F + 1) * 0.08F;
            rightArm.zRot = 0.35F;
            leftArm.zRot = -0.35F;
            rightArm.xRot = 0.1F;
            leftArm.xRot = 0.1F;
        }
        int cast = e.getCast();
        if (cast > 0) {
            boolean big = e.isBigCast();
            if (big) {
                // las dos manos arriba cargando una gran esfera
                rightArm.xRot = -2.8F;
                leftArm.xRot = -2.8F;
                rightArm.zRot = 0.25F;
                leftArm.zRot = -0.25F;
            } else {
                rightArm.xRot = -1.55F + head.xRot;
                rightArm.yRot = head.yRot;
                rightArm.zRot = 0;
            }
        }
        if (e.isEnraged()) {
            float sh = Mth.sin(age * 2.5F) * 0.03F;
            head.zRot += sh;
            body.zRot += sh * 0.5F;
        }
        hat.copyFrom(head);
    }
}
