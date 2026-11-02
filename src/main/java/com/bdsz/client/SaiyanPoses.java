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
        if (e instanceof Player pd && ClientStates.get(pd.getId()).fusion == com.bdsz.data.Fusion.DRAGON.ordinal()) {
            muscle = Math.max(muscle, 1.12F);                  // Guerrero Dragón: un poco más fornido
        }
        setMuscle(m, muscle);
        if (e instanceof Player pf && ClientStates.get(pf.getId()).saiyan && ClientStates.get(pf.getId()).gender == 2) feminine(m);
        if (!(e instanceof Player p)) return;
        SaiyanState s = ClientStates.get(p.getId());
        if (fusionPoses(m, s, age) || fusedPose(m, p, s, limbAmount, age)) {
            copy(m);
            return;
        }
        if (!s.saiyan) {
            if (s.fusion > 0 && m.attackTime > 0) {
                punch(m, m.attackTime);
                copy(m);
            }
            return;
        }
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
            // ambos brazos al frente, palmas juntas; el cuerpo se echa hacia atrás por el retroceso
            m.body.yRot = 0.0F;
            m.body.xRot = -0.22F + shake * 2;
            m.head.xRot -= 0.1F;
            m.rightArm.xRot = -1.55F + m.head.xRot + shake;
            m.rightArm.yRot = -0.12F;
            m.rightArm.zRot = 0.0F;
            m.leftArm.xRot = -1.55F + m.head.xRot - shake;
            m.leftArm.yRot = 0.12F;
            m.leftArm.zRot = 0.0F;
            m.rightLeg.xRot = 0.25F;
            m.leftLeg.xRot = -0.3F;
        } else if (s.pose == SaiyanState.POSE_BIG_BANG) {
            // brazo derecho extendido con la palma abierta, el cuerpo gira y retrocede
            m.body.yRot = 0.35F;
            m.body.xRot = -0.15F + shake * 2;
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
        } else if (powering && com.bdsz.data.Form.byId(s.selected) == com.bdsz.data.Form.UI_PERFECT) {
            // al transformarse en Ultra Instinto Perfecto: un poco encorvado, como cansado; brazos sueltos que cuelgan hacia adelante
            float br = Mth.sin(age * 0.06F) * 0.025F;
            m.body.xRot = 0.24F + br;
            m.head.xRot = m.head.xRot * 0.6F + 0.18F;
            m.rightArm.xRot = -0.32F + br;
            m.rightArm.zRot = 0.08F;
            m.leftArm.xRot = -0.28F + br;
            m.leftArm.zRot = -0.08F;
            m.rightLeg.zRot = 0.06F;
            m.leftLeg.zRot = -0.06F;
        } else if (powering && com.bdsz.data.Form.byId(s.selected) == com.bdsz.data.Form.UI_SELF) {
            // al transformarse en Ultra Instinto Propio: pose relajada, serena; una mano abierta frente al pecho, la otra suelta
            float br = Mth.sin(age * 0.045F);
            m.body.xRot = -0.02F;
            m.head.xRot = m.head.xRot * 0.5F - 0.05F + br * 0.02F;
            m.rightArm.xRot = -1.05F + br * 0.03F;
            m.rightArm.yRot = -0.45F;
            m.rightArm.zRot = 0.0F;
            m.leftArm.xRot = 0.05F;
            m.leftArm.zRot = -0.12F - br * 0.03F;
            m.rightLeg.zRot = 0.04F;
            m.leftLeg.zRot = -0.04F;
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
            // recarga de ki: guardia baja y firme; cada pulso de energía el cuerpo se tensa, los brazos
            // se abren con fuerza y la cabeza se levanta gritando, luego vuelve a concentrarse
            float cycle = (age % 40.0F) / 40.0F;
            float pulse = cycle < 0.25F ? Mth.sin(cycle / 0.25F * Mth.HALF_PI) : 1.0F - (cycle - 0.25F) / 0.75F;
            pulse = pulse * pulse;
            float tr = shake * (1.5F + pulse * 2.0F);
            m.body.xRot = 0.28F - pulse * 0.32F;
            m.body.yRot = Mth.sin(age * 0.07F) * 0.05F;
            m.head.xRot = 0.15F - pulse * 0.75F + tr;
            m.head.yRot = tr * 0.5F;
            m.rightArm.xRot = 0.55F - pulse * 0.45F + tr;
            m.rightArm.yRot = 0.15F;
            m.rightArm.zRot = 0.35F + pulse * 0.75F;
            m.leftArm.xRot = 0.55F - pulse * 0.45F - tr;
            m.leftArm.yRot = -0.15F;
            m.leftArm.zRot = -0.35F - pulse * 0.75F;
            m.rightLeg.xRot = -0.2F;
            m.leftLeg.xRot = -0.2F;
            m.rightLeg.zRot = 0.36F + pulse * 0.06F;
            m.leftLeg.zRot = -0.36F - pulse * 0.06F;
            m.body.y = 1.0F - pulse * 0.8F;   // se agacha y se impulsa con cada pulso
            m.head.y = m.body.y;
            m.rightArm.y += m.body.y;
            m.leftArm.y += m.body.y;
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
                m.leftArm.zRot += Mth.sin(age * 0.3F) * 0.06F * k;      // el viento mueve el brazo libre
                m.body.yRot = Mth.sin(age * 0.18F) * 0.05F * k;
            } else {
                // suspendido en el aire: piernas colgando que se mecen, brazos relajados
                m.rightLeg.xRot = 0.15F + Mth.sin(age * 0.12F) * 0.12F;
                m.leftLeg.xRot = 0.25F + Mth.sin(age * 0.12F + 1.2F) * 0.12F;
                m.rightArm.zRot = 0.18F + Mth.sin(age * 0.1F) * 0.05F;
                m.leftArm.zRot = -0.18F - Mth.sin(age * 0.1F) * 0.05F;
                m.rightArm.xRot += Mth.sin(age * 0.08F) * 0.08F;
                m.leftArm.xRot += Mth.sin(age * 0.08F + 1.0F) * 0.08F;
                m.body.xRot = 0.05F + Mth.sin(age * 0.1F) * 0.03F;
            }
            if (m.attackTime > 0) punch(m, m.attackTime);
        } else if (m.attackTime > 0) {
            punch(m, m.attackTime);       // golpes con más movimiento
        } else {
            return;
        }
        copy(m);
    }

    /** Golpe con todo el cuerpo: giro de cadera, brazo que sale disparado, el otro atrás y un paso al frente. */
    private static void punch(PlayerModel<?> m, float attack) {
        float a = Mth.sin(attack * Mth.PI);
        float twist = Mth.sin(Mth.sqrt(attack) * Mth.TWO_PI) * 0.55F;
        m.body.yRot = twist;
        m.body.xRot = 0.18F * a;
        m.rightArm.xRot = -1.45F * a + m.head.xRot * a * 0.5F;
        m.rightArm.yRot = twist - 0.25F * a;
        m.leftArm.xRot = 0.65F * a;
        m.leftArm.zRot = -0.25F * a;
        m.rightLeg.xRot = -0.45F * a;
        m.leftLeg.xRot = 0.35F * a;
    }

    /** Ponerse / quitarse el arete y la escena de la fusión. Devuelve true si aplicó una pose. */
    private static boolean fusionPoses(PlayerModel<?> m, SaiyanState s, float age) {
        if (s.fusing > 0) {
            float prog = 1.0F - s.fusing / (float) com.bdsz.data.FusionLogic.FUSING_TICKS;
            float tr = Mth.sin(age * 3.1F) * (0.04F + prog * 0.1F);
            m.body.xRot = -0.25F - prog * 0.15F;
            m.head.xRot = -0.6F - prog * 0.4F + tr;
            m.head.yRot = tr;
            m.rightArm.xRot = -0.2F + tr;
            m.rightArm.yRot = 0;
            m.rightArm.zRot = 1.2F + prog * 0.9F;
            m.leftArm.xRot = -0.2F - tr;
            m.leftArm.yRot = 0;
            m.leftArm.zRot = -1.2F - prog * 0.9F;
            m.rightLeg.zRot = 0.3F;
            m.leftLeg.zRot = -0.3F;
            m.rightLeg.xRot = 0;
            m.leftLeg.xRot = 0;
            return true;
        }
        if (s.earAnim != 0) {
            int total = com.bdsz.data.FusionLogic.EAR_ANIM;
            float t = 1.0F - Math.abs(s.earAnim) / (float) total;          // 0 → 1
            float reach = t < 0.4F ? t / 0.4F : t < 0.7F ? 1.0F : 1.0F - (t - 0.7F) / 0.3F;
            m.rightArm.xRot = Mth.lerp(reach, 0.0F, -2.55F);
            m.rightArm.yRot = Mth.lerp(reach, 0.0F, -0.75F);
            m.rightArm.zRot = Mth.lerp(reach, 0.0F, 0.15F);
            m.head.zRot = -0.28F * reach;
            m.head.yRot = -0.2F * reach;
            if (s.earAnim < 0 && reach > 0.9F) m.rightArm.xRot += Mth.sin(age * 1.5F) * 0.05F;   // forcejea con el arete
            return true;
        }
        return false;
    }

    /**
     * Fusionado: el arco de ki del esqueleto, la mecha del creeper y una pose de base distinta para cada
     * fusión cuando estás quieto (brazos cruzados, brazos atrás, etc.).
     */
    private static boolean fusedPose(PlayerModel<?> m, Player p, SaiyanState s, float limbAmount, float age) {
        if (s.fusion <= 0 || s.fusing > 0) return false;
        com.bdsz.data.Fusion f = com.bdsz.data.Fusion.byId(s.fusion);
        float breath = Mth.sin(age * 0.09F) * 0.03F;
        if (s.fusionAnim > 0 && f == com.bdsz.data.Fusion.SKELETON) {
            // tensa el arco de ki: brazo izquierdo extendido con el arco, el derecho tira de la cuerda
            m.leftArm.xRot = -Mth.HALF_PI + m.head.xRot;
            m.leftArm.yRot = 0.1F + m.head.yRot;
            m.leftArm.zRot = 0.0F;
            m.rightArm.xRot = -Mth.HALF_PI + m.head.xRot;
            m.rightArm.yRot = -0.1F + m.head.yRot - 0.55F;
            m.rightArm.zRot = 0.0F;
            m.body.yRot = m.head.yRot * 0.4F;
            m.leftLeg.xRot = -0.3F;
            m.rightLeg.xRot = 0.25F;
            return true;
        }
        if (s.fusionAnim > 0 && f == com.bdsz.data.Fusion.CREEPER) {
            // a punto de explotar: tiembla cada vez más, se encoge y abre los brazos
            float k = 1.0F - s.fusionAnim / 30.0F;
            float tr = Mth.sin(age * 4.0F) * (0.04F + k * 0.12F);
            m.body.xRot = 0.25F * (1 - k) + tr;
            m.head.xRot = -0.3F * k + tr;
            m.rightArm.xRot = -0.3F + tr;
            m.rightArm.zRot = 0.4F + k * 0.9F;
            m.leftArm.xRot = -0.3F - tr;
            m.leftArm.zRot = -0.4F - k * 0.9F;
            m.rightLeg.zRot = 0.2F + k * 0.15F;
            m.leftLeg.zRot = -0.2F - k * 0.15F;
            return true;
        }
        boolean idle = limbAmount < 0.06F && m.attackTime <= 0 && s.pose == 0 && !s.charging && s.powering <= 0
                && !p.getAbilities().flying && !p.isCrouching() && !p.isUsingItem() && !p.isPassenger() && !p.isSwimming()
                && !p.isFallFlying() && p.onGround() && s.earAnim == 0;
        if (!idle) return false;
        // la pose se va formando poco a poco cuando te quedas quieto
        float k = Mth.clamp((p.tickCount - ClientStates.lastMove(p)) / 12.0F, 0.0F, 1.0F);
        if (k <= 0) return false;
        switch (f) {
            case DRAGON -> {
                // engreído: manos en la cintura, pecho afuera y la barbilla en alto
                set(m.rightArm, k, 0.15F + breath, 0.0F, 0.75F);
                set(m.leftArm, k, 0.15F + breath, 0.0F, -0.75F);
                m.body.xRot = Mth.lerp(k, m.body.xRot, -0.12F);
                m.head.xRot = Mth.lerp(k, m.head.xRot, m.head.xRot - 0.3F);
                m.rightLeg.zRot = Mth.lerp(k, 0, 0.16F);
                m.leftLeg.zRot = Mth.lerp(k, 0, -0.16F);
            }
            case GOLEM -> {
                // brazos cruzados, firme como un gólem
                set(m.rightArm, k, -1.05F + breath, -0.72F, 0.05F);
                set(m.leftArm, k, -0.92F + breath, 0.72F, -0.05F);
                m.body.xRot = Mth.lerp(k, m.body.xRot, -0.04F);
                m.head.xRot = Mth.lerp(k, m.head.xRot, m.head.xRot - 0.12F);
                m.rightLeg.zRot = Mth.lerp(k, 0, 0.12F);
                m.leftLeg.zRot = Mth.lerp(k, 0, -0.12F);
            }
            case WARDEN -> {
                // brazos atrás de la espalda, pecho afuera, mirada desafiante
                set(m.rightArm, k, 0.62F + breath, 0.55F, -0.18F);
                set(m.leftArm, k, 0.62F + breath, -0.55F, 0.18F);
                m.body.xRot = Mth.lerp(k, m.body.xRot, -0.1F);
                m.head.xRot = Mth.lerp(k, m.head.xRot, m.head.xRot - 0.18F);
                m.rightLeg.zRot = Mth.lerp(k, 0, 0.1F);
                m.leftLeg.zRot = Mth.lerp(k, 0, -0.1F);
            }
            case ZOMBIE -> {
                // brazos al frente como un zombi, balanceándose
                set(m.rightArm, k, -1.45F + Mth.sin(age * 0.07F) * 0.06F, -0.08F, 0.05F + breath);
                set(m.leftArm, k, -1.4F + Mth.sin(age * 0.07F + 1.3F) * 0.06F, 0.08F, -0.05F - breath);
                m.head.zRot = Mth.lerp(k, 0, 0.12F);
                m.body.xRot = Mth.lerp(k, m.body.xRot, 0.06F);
            }
            case SKELETON -> {
                // mano en la cadera, el otro brazo relajado, cabeza ladeada
                set(m.rightArm, k, 0.25F, -0.45F, 0.72F);
                set(m.leftArm, k, 0.05F + breath, 0.0F, -0.1F - breath);
                m.head.zRot = Mth.lerp(k, 0, -0.1F);
                m.body.yRot = Mth.lerp(k, 0, 0.12F);
                m.leftLeg.xRot = Mth.lerp(k, 0, -0.12F);
                m.leftLeg.zRot = Mth.lerp(k, 0, -0.1F);
            }
            case ENDERMAN -> {
                // brazos largos colgando, un poco encorvado, mirada fija
                set(m.rightArm, k, 0.08F, 0.0F, 0.04F + breath * 0.5F);
                set(m.leftArm, k, 0.08F, 0.0F, -0.04F - breath * 0.5F);
                m.body.xRot = Mth.lerp(k, m.body.xRot, 0.14F);
                m.head.xRot = Mth.lerp(k, m.head.xRot, m.head.xRot + 0.05F);
                m.head.zRot = Mth.lerp(k, 0, Mth.sin(age * 0.05F) * 0.08F);
            }
            case CREEPER -> {
                // agazapado, brazos pegados, listo para saltar
                set(m.rightArm, k, 0.35F + breath, 0.0F, 0.08F);
                set(m.leftArm, k, 0.35F + breath, 0.0F, -0.08F);
                m.body.xRot = Mth.lerp(k, m.body.xRot, 0.2F);
                m.head.xRot = Mth.lerp(k, m.head.xRot, m.head.xRot - 0.2F);
                m.rightLeg.xRot = Mth.lerp(k, 0, -0.15F);
                m.leftLeg.xRot = Mth.lerp(k, 0, 0.12F);
                m.rightLeg.zRot = Mth.lerp(k, 0, 0.18F);
                m.leftLeg.zRot = Mth.lerp(k, 0, -0.18F);
            }
            default -> { return false; }
        }
        return true;
    }

    private static void set(ModelPart part, float k, float x, float y, float z) {
        part.xRot = Mth.lerp(k, part.xRot, x);
        part.yRot = Mth.lerp(k, part.yRot, y);
        part.zRot = Mth.lerp(k, part.zRot, z);
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
