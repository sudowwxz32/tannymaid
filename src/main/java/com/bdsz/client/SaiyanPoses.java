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
        // el modelo se comparte entre jugadores: siempre se reinicia la musculatura
        float muscle = 1.0F;
        if (e instanceof Player pl) {
            SaiyanState st = ClientStates.get(pl.getId());
            if (st.saiyan && (st.transformed || st.powering > 0)) {
                float grow = st.powering > 0 ? 1.0F - (float) st.powering / Math.max(1, st.poweringTotal) : 1.0F;
                muscle = 1.0F + (st.visibleForm().muscle() - 1.0F) * grow;
            }
        }
        setMuscle(m, muscle);
        if (e instanceof Player pf && ClientStates.get(pf.getId()).saiyan && ClientStates.get(pf.getId()).gender == 2) feminine(m);
        if (!(e instanceof Player p)) return;
        SaiyanState s = ClientStates.get(p.getId());
        if (!s.saiyan) return;
        long since = ClientStates.ticksSinceTransform(p.getId());
        boolean powering = s.powering > 0 || since < 14;
        float shake = Mth.sin(age * 2.7F) * 0.035F;

        if (s.pose == SaiyanState.POSE_KAME_CHARGE) {
            // manos juntas a un costado, cuerpo girado, cargando energía
            m.body.yRot = -0.45F;
            m.head.yRot = 0.3F;
            m.rightArm.xRot = 0.35F + shake;
            m.rightArm.yRot = 0.6F;
            m.rightArm.zRot = 0.35F;
            m.leftArm.xRot = 0.25F - shake;
            m.leftArm.yRot = 0.9F;
            m.leftArm.zRot = 0.15F;
            m.rightLeg.zRot = 0.2F;
            m.leftLeg.zRot = -0.2F;
            m.leftLeg.xRot = -0.25F;
        } else if (s.pose == SaiyanState.POSE_KAME_FIRE) {
            // ambos brazos al frente, palmas juntas
            m.body.yRot = 0.0F;
            m.rightArm.xRot = -1.55F + m.head.xRot + shake;
            m.rightArm.yRot = -0.12F;
            m.rightArm.zRot = 0.0F;
            m.leftArm.xRot = -1.55F + m.head.xRot - shake;
            m.leftArm.yRot = 0.12F;
            m.leftArm.zRot = 0.0F;
            m.rightLeg.xRot = 0.25F;
            m.leftLeg.xRot = -0.3F;
        } else if (s.pose == SaiyanState.POSE_BIG_BANG) {
            // brazo derecho extendido con la palma abierta
            m.rightArm.xRot = -1.6F + m.head.xRot;
            m.rightArm.yRot = -0.05F;
            m.rightArm.zRot = 0.0F;
            m.leftArm.xRot = 0.2F;
            m.leftArm.zRot = -0.35F;
            m.leftLeg.xRot = -0.25F;
            m.rightLeg.xRot = 0.15F;
        } else if (s.pose == SaiyanState.POSE_DRAGON) {
            // puño atrás, listo para el golpe del dragón
            m.body.yRot = -0.35F;
            m.rightArm.xRot = 0.9F + shake;
            m.rightArm.zRot = 0.25F;
            m.leftArm.xRot = -1.3F + m.head.xRot;
            m.leftArm.zRot = -0.1F;
            m.rightLeg.zRot = 0.25F;
            m.leftLeg.zRot = -0.25F;
            m.head.xRot -= 0.2F;
        } else if (powering) {
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

    /** Músculos: pecho y espalda más anchos, brazos y piernas más gruesos. */
    private static void setMuscle(PlayerModel<?> m, float k) {
        float arm = 1.0F + (k - 1.0F) * 1.6F;
        float leg = 1.0F + (k - 1.0F) * 0.9F;
        if (k < 1.0F) {
            // Dios: modelo de brazos finos, un poco ensanchados para que no se vea tan flaco
            arm = 1.1F;
            leg = 0.98F;
        }
        scale(m.body, k, 1.0F, 1.0F + (k - 1.0F) * 1.3F);
        scale(m.jacket, k, 1.0F, 1.0F + (k - 1.0F) * 1.3F);
        scale(m.rightArm, arm, 1.0F, arm);
        scale(m.leftArm, arm, 1.0F, arm);
        scale(m.rightSleeve, arm, 1.0F, arm);
        scale(m.leftSleeve, arm, 1.0F, arm);
        scale(m.rightLeg, leg, 1.0F, leg);
        scale(m.leftLeg, leg, 1.0F, leg);
        scale(m.rightPants, leg, 1.0F, leg);
        scale(m.leftPants, leg, 1.0F, leg);
        m.rightLeg.x = -1.9F;
        m.leftLeg.x = 1.9F;
        m.rightPants.x = -1.9F;
        m.leftPants.x = 1.9F;
        m.rightArm.x = -5.0F;
        m.leftArm.x = 5.0F;
        m.rightSleeve.x = -5.0F;
        m.leftSleeve.x = 5.0F;
    }

    /**
     * Cuerpo femenino: hombros más estrechos, cintura fina y un poco menos de espalda, caderas algo marcadas,
     * brazos y piernas un poco más finos.
     */
    private static void feminine(PlayerModel<?> m) {
        m.body.xScale *= 0.9F;
        m.body.zScale *= 0.94F;
        m.jacket.xScale *= 0.9F;
        m.jacket.zScale *= 0.94F;
        m.rightArm.xScale *= 0.86F;
        m.leftArm.xScale *= 0.86F;
        m.rightArm.zScale *= 0.92F;
        m.leftArm.zScale *= 0.92F;
        m.rightSleeve.xScale *= 0.86F;
        m.leftSleeve.xScale *= 0.86F;
        m.rightArm.x = -4.65F;           // hombros más estrechos (valor fijo: el modelo se comparte)
        m.leftArm.x = 4.65F;
        m.rightSleeve.x = m.rightArm.x;
        m.leftSleeve.x = m.leftArm.x;
        m.rightLeg.xScale *= 1.04F;
        m.leftLeg.xScale *= 1.04F;
        m.rightLeg.zScale *= 0.96F;
        m.leftLeg.zScale *= 0.96F;
        m.rightPants.xScale *= 1.04F;
        m.leftPants.xScale *= 1.04F;
        m.rightLeg.x = -2.05F;          // valor fijo: el modelo se comparte y no reinicia esta posición
        m.leftLeg.x = 2.05F;
        m.rightPants.x = m.rightLeg.x;
        m.leftPants.x = m.leftLeg.x;
    }

    private static void scale(net.minecraft.client.model.geom.ModelPart p, float x, float y, float z) {
        p.xScale = x;
        p.yScale = y;
        p.zScale = z;
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
