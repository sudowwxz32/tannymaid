package com.bdsz.client;

import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Poses del personaje: recarga de ki (H), grito de transformación y vuelo tipo Superman. */
public final class SaiyanPoses {
    private SaiyanPoses() {}

    public static void apply(PlayerModel<?> m, LivingEntity e, float limbSwing, float limbAmount, float age) {
        if (!(e instanceof Player p)) return;
        SaiyanState s = ClientStates.get(p.getId());
        if (!s.saiyan) return;
        long since = ClientStates.ticksSinceTransform(p.getId());
        boolean powering = s.powering > 0 || since < 14;
        float shake = Mth.sin(age * 2.7F) * 0.035F;

        if (powering) {
            // grito de transformación: puños a la cintura, codos atrás, pecho al frente, cabeza al cielo
            float progress = s.poweringTotal > 0 && s.powering > 0 ? 1.0F - (float) s.powering / s.poweringTotal : 1.0F;
            float tremble = shake * (1.0F + progress * 2.5F);
            m.body.xRot = -0.18F - progress * 0.1F;
            m.head.xRot = -0.45F - progress * 0.35F + tremble;
            m.head.yRot = tremble;
            m.rightArm.xRot = 0.55F + tremble;
            m.rightArm.yRot = 0.0F;
            m.rightArm.zRot = 0.32F + progress * 0.15F;
            m.leftArm.xRot = 0.55F - tremble;
            m.leftArm.yRot = 0.0F;
            m.leftArm.zRot = -0.32F - progress * 0.15F;
            m.rightLeg.xRot = 0.0F;
            m.leftLeg.xRot = 0.0F;
            m.rightLeg.zRot = 0.22F;
            m.leftLeg.zRot = -0.22F;
        } else if (s.charging) {
            // recarga de ki: postura firme, piernas abiertas, brazos tensos hacia abajo y afuera
            m.body.xRot = 0.22F;
            m.head.xRot = -0.25F + shake;
            m.rightArm.xRot = 0.35F + shake;
            m.rightArm.yRot = 0.0F;
            m.rightArm.zRot = 0.55F;
            m.leftArm.xRot = 0.35F - shake;
            m.leftArm.yRot = 0.0F;
            m.leftArm.zRot = -0.55F;
            m.rightLeg.xRot = -0.15F;
            m.leftLeg.xRot = -0.15F;
            m.rightLeg.zRot = 0.3F;
            m.leftLeg.zRot = -0.3F;
        } else if (s.flight && p.getAbilities().flying) {
            float tilt = ClientFlight.tilt(p, 1.0F);
            if (tilt > 0.25F) {
                // vuelo rápido: un puño al frente, el otro brazo pegado al cuerpo, piernas juntas
                float k = Mth.clamp((tilt - 0.25F) / 0.55F, 0, 1);
                m.head.xRot = Mth.lerp(k, m.head.xRot, -1.15F);
                m.rightArm.xRot = Mth.lerp(k, m.rightArm.xRot, -2.95F);
                m.rightArm.zRot = Mth.lerp(k, m.rightArm.zRot, -0.05F);
                m.leftArm.xRot = Mth.lerp(k, m.leftArm.xRot, 0.25F);
                m.leftArm.zRot = Mth.lerp(k, m.leftArm.zRot, -0.12F);
                m.rightLeg.xRot = Mth.lerp(k, m.rightLeg.xRot, 0.08F + Mth.sin(age * 0.35F) * 0.06F);
                m.leftLeg.xRot = Mth.lerp(k, m.leftLeg.xRot, -0.06F + Mth.sin(age * 0.35F + 1.5F) * 0.06F);
                m.rightLeg.zRot = 0.03F;
                m.leftLeg.zRot = -0.03F;
            } else {
                // suspendido en el aire: piernas colgando que se mecen, brazos relajados
                m.rightLeg.xRot = 0.15F + Mth.sin(age * 0.12F) * 0.12F;
                m.leftLeg.xRot = 0.25F + Mth.sin(age * 0.12F + 1.2F) * 0.12F;
                m.rightArm.zRot = 0.18F + Mth.sin(age * 0.1F) * 0.05F;
                m.leftArm.zRot = -0.18F - Mth.sin(age * 0.1F) * 0.05F;
            }
        } else {
            return;
        }
        copy(m);
    }

    private static void copy(PlayerModel<?> m) {
        m.hat.copyFrom(m.head);
        m.jacket.copyFrom(m.body);
        m.rightSleeve.copyFrom(m.rightArm);
        m.leftSleeve.copyFrom(m.leftArm);
        m.rightPants.copyFrom(m.rightLeg);
        m.leftPants.copyFrom(m.leftLeg);
    }
}
