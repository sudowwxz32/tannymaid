package com.cz.client;

import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ClientStates;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Poses del cuerpo:
 *  - transformación: 1 cola se agarra la cabeza y ruge · 2 colas cae de rodillas · 3 colas gestos de ira
 *  - 4 y 5 colas: en cuatro patas, espalda jorobada, brazos al frente y el cuerpo alargado
 *  - correr con el manto: brazos hacia atrás · cargar chakra: manos juntas
 */
public final class ZorroPoses {
    private ZorroPoses() {}

    /** Datos de la postura en cuatro patas (los usa también el render para bajar el modelo al suelo). */
    public static final class Quad {
        public float pitch, k, hipY, hipZ, armScale, shift;
    }

    /** blend 0 = de pie, 1 = en cuatro patas. */
    public static Quad quad(Phase ph, float blend) {
        Quad q = new Quad();
        q.pitch = 1.15F * blend;                       // > 90° no: la cadera queda más alta que los hombros (jorobado)
        q.k = 1.0F + (ph.elongation - 1.0F) * blend;   // el torso se alarga
        q.hipY = 12.0F * q.k * Mth.cos(q.pitch);
        q.hipZ = 12.0F * q.k * Mth.sin(q.pitch);
        float feetY = q.hipY + 12.0F;
        q.shift = 24.0F - feetY;
        float reach = (feetY - 2.0F) / 12.0F / Mth.cos(0.3F * blend);
        q.armScale = Mth.lerp(blend, 1.0F, reach);
        return q;
    }

    /** Cuántos píxeles hay que bajar el modelo (cuatro patas o de rodillas). */
    public static float shift(Player p) {
        ZorroState s = ClientStates.get(p.getId());
        if (!s.awakened) return 0;
        Phase v = s.visible();
        if (v.quadruped) return quad(v, quadBlend(s)).shift;
        if (s.powering > 0 && v == Phase.TWO) return 6.0F * Math.min(1.0F, s.progress() / 0.2F);   // de rodillas
        return 0;
    }

    public static float quadBlend(ZorroState s) {
        if (s.powering > 0) {
            if (!Phase.byId(s.target).quadruped) return 0;
            float pr = s.progress();
            return s.active().quadruped ? 1.0F : Mth.clamp(pr / 0.55F, 0, 1);       // se desploma en cuatro patas
        }
        return s.active().quadruped ? 1.0F : 0.0F;
    }

    public static void apply(PlayerModel<?> m, LivingEntity e, float swing, float amount, float age) {
        // el modelo se comparte entre jugadores: se reinicia lo que se cambia
        for (ModelPart p : new ModelPart[]{m.body, m.jacket, m.rightArm, m.leftArm, m.rightSleeve, m.leftSleeve}) {
            p.xScale = 1; p.yScale = 1; p.zScale = 1;
        }
        m.rightArm.z = 0;
        m.leftArm.z = 0;
        m.head.z = 0;
        if (!(e instanceof Player p)) return;
        ZorroState s = ClientStates.get(p.getId());
        if (!s.awakened) return;
        Phase v = s.visible();
        float pr = s.progress();
        float shake = Mth.sin(age * 2.9F) * 0.04F;
        boolean powering = s.powering > 0;

        // las capas exteriores de la piel no se ven cuando el cuerpo ya es de bestia
        if (v.beast && (!powering || pr > 0.5F)) {
            m.hat.visible = false;
            m.jacket.visible = false;
            m.leftSleeve.visible = false;
            m.rightSleeve.visible = false;
            m.leftPants.visible = false;
            m.rightPants.visible = false;
        }

        float blend = quadBlend(s);
        if (blend > 0) {
            quadPose(m, v, blend, swing, amount, age, powering ? pr : 1.0F, shake);
        } else if (powering) {
            float tr = shake * (1.0F + pr * 3.0F);
            switch (v) {
                case TWO -> {
                    // de rodillas, una mano en el suelo y la otra apretando el pecho
                    float k = Math.min(1.0F, pr / 0.2F);
                    m.rightLeg.xRot = -1.45F * k;
                    m.leftLeg.xRot = 0.05F;
                    m.body.xRot = 0.45F * k;
                    m.rightArm.xRot = -0.35F + tr;
                    m.rightArm.zRot = 0.15F;
                    m.leftArm.xRot = -1.1F - tr;
                    m.leftArm.yRot = 0.7F;
                    m.head.xRot = pr < 0.75F ? 0.55F + tr : -0.6F + tr;       // al final levanta la cabeza y ruge
                    m.head.yRot = tr;
                }
                case THREE -> {
                    // ira: puños apretados que golpean el aire, cabeza que se sacude, cuerpo encorvado
                    float pound = Mth.sin(age * 1.6F);
                    m.body.xRot = 0.3F + 0.08F * Mth.sin(age * 0.8F);
                    m.rightArm.xRot = 0.15F + pound * 0.7F * pr + tr;
                    m.leftArm.xRot = 0.15F - pound * 0.7F * pr - tr;
                    m.rightArm.zRot = 0.45F;
                    m.leftArm.zRot = -0.45F;
                    m.head.yRot = Mth.sin(age * 0.9F) * 0.55F;
                    m.head.xRot = -0.35F + tr;
                    m.rightLeg.zRot = 0.18F;
                    m.leftLeg.zRot = -0.18F;
                }
                default -> {
                    // sufrimiento: se agarra la cabeza... y al final ruge con los brazos abiertos
                    if (pr < 0.65F) {
                        m.rightArm.xRot = -2.6F + tr;
                        m.rightArm.zRot = -0.45F;
                        m.leftArm.xRot = -2.6F - tr;
                        m.leftArm.zRot = 0.45F;
                        m.body.xRot = 0.35F;
                        m.head.xRot = 0.45F + tr;
                        m.head.yRot = Mth.sin(age * 0.7F) * 0.3F;
                    } else {
                        m.rightArm.xRot = 0.3F + tr;
                        m.rightArm.zRot = 0.7F;
                        m.leftArm.xRot = 0.3F - tr;
                        m.leftArm.zRot = -0.7F;
                        m.body.xRot = -0.2F;
                        m.head.xRot = -0.75F + tr;
                    }
                    m.rightLeg.zRot = 0.2F;
                    m.leftLeg.zRot = -0.2F;
                }
            }
        } else if (s.charging) {
            // concentrando chakra: manos juntas al frente, piernas abiertas
            m.rightArm.xRot = -1.15F + shake * 0.5F;
            m.rightArm.yRot = -0.55F;
            m.leftArm.xRot = -1.15F - shake * 0.5F;
            m.leftArm.yRot = 0.55F;
            m.rightLeg.zRot = 0.14F;
            m.leftLeg.zRot = -0.14F;
        } else if (s.phase != 0 && p.isSprinting() && !p.getAbilities().flying) {
            // corre con el cuerpo inclinado y los brazos hacia atrás
            m.body.xRot = 0.5F;
            m.head.xRot -= 0.35F;
            m.rightArm.xRot = 1.35F;
            m.leftArm.xRot = 1.35F;
            m.rightArm.zRot = 0.12F;
            m.leftArm.zRot = -0.12F;
            m.rightArm.z = 2.0F;
            m.leftArm.z = 2.0F;
            m.rightLeg.z = 3.0F;
            m.leftLeg.z = 3.0F;
            m.rightLeg.y = 11.0F;
            m.leftLeg.y = 11.0F;
        } else {
            return;
        }
        copy(m);
    }

    /** Cuatro patas: torso alargado e inclinado, piernas que salen de la cadera y brazos largos al frente. */
    private static void quadPose(PlayerModel<?> m, Phase v, float blend, float swing, float amount, float age, float done, float shake) {
        Quad q = quad(v, blend);
        float gait = swing * 0.6662F;
        m.body.xRot = q.pitch;
        m.body.yScale = q.k;
        m.jacket.yScale = q.k;
        m.rightLeg.y = q.hipY;
        m.leftLeg.y = q.hipY;
        m.rightLeg.z = q.hipZ;
        m.leftLeg.z = q.hipZ;
        m.rightLeg.xRot = Mth.cos(gait) * 1.0F * amount;
        m.leftLeg.xRot = Mth.cos(gait + Mth.PI) * 1.0F * amount;
        m.rightLeg.zRot = 0.06F;
        m.leftLeg.zRot = -0.06F;
        float armFwd = -0.3F * blend;
        m.rightArm.xRot = armFwd + Mth.cos(gait + Mth.PI) * 0.9F * amount + shake;
        m.leftArm.xRot = armFwd + Mth.cos(gait) * 0.9F * amount - shake;
        m.rightArm.yRot = 0;
        m.leftArm.yRot = 0;
        m.rightArm.zRot = 0.08F;
        m.leftArm.zRot = -0.08F;
        m.rightArm.yScale = q.armScale;
        m.leftArm.yScale = q.armScale;
        m.rightArm.z = -1.0F * blend;
        m.leftArm.z = -1.0F * blend;
        // la cabeza mira al frente; respira jadeando; al terminar la transformación ruge al cielo
        m.head.xRot = Mth.clamp(m.head.xRot - 0.2F, -0.9F, 0.5F) + Mth.sin(age * 0.35F) * 0.04F;
        if (done < 1.0F && done > 0.8F) m.head.xRot = -0.8F + shake;
    }

    private static void copy(PlayerModel<?> m) {
        m.hat.copyFrom(m.head);
        m.jacket.copyFrom(m.body);
        m.rightSleeve.copyFrom(m.rightArm);
        m.leftSleeve.copyFrom(m.leftArm);
        m.rightPants.copyFrom(m.rightLeg);
        m.leftPants.copyFrom(m.leftLeg);
        m.jacket.yScale = m.body.yScale;
        m.rightSleeve.yScale = m.rightArm.yScale;
        m.leftSleeve.yScale = m.leftArm.yScale;
    }
}
