package com.totallywaifu.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/** Comportamientos de las waifus: trabajar, seguirte y protegerte. */
public final class WaifuGoals {
    private WaifuGoals() {}

    /** Modo Trabajar: cada una hace su oficio alrededor del sitio donde la dejaste. */
    public static class Work extends Goal {
        private final WaifuEntity w;

        public Work(WaifuEntity w) {
            this.w = w;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return w.getMode() == WaifuEntity.WORK && w.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void stop() {
            w.work.reset();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            w.work.tick();
        }
    }

    /** Modo Seguir: va contigo (y se teletransporta si te alejas mucho). */
    public static class Follow extends Goal {
        private final WaifuEntity w;
        private Player owner;
        private int recalc;

        public Follow(WaifuEntity w) {
            this.w = w;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (w.getMode() != WaifuEntity.FOLLOW) return false;
            Player p = w.ownerPlayer();
            if (p == null || p.isSpectator() || w.distanceToSqr(p) < 4.5 * 4.5) return false;
            owner = p;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return w.getMode() == WaifuEntity.FOLLOW && owner != null && owner.isAlive() && w.distanceToSqr(owner) > 2.5 * 2.5;
        }

        @Override
        public void stop() {
            owner = null;
            w.getNavigation().stop();
        }

        @Override
        public void tick() {
            w.getLookControl().setLookAt(owner, 10.0F, w.getMaxHeadXRot());
            if (--recalc > 0) return;
            recalc = 10;
            if (w.distanceToSqr(owner) > 20 * 20) {
                // demasiado lejos: aparece a tu lado
                w.moveTo(owner.getX() + (w.getRandom().nextDouble() - 0.5) * 2, owner.getY(), owner.getZ() + (w.getRandom().nextDouble() - 0.5) * 2,
                        w.getYRot(), w.getXRot());
                w.getNavigation().stop();
                return;
            }
            w.getNavigation().moveTo(owner, w.distanceToSqr(owner) > 10 * 10 ? 1.3 : 1.05);
        }
    }

    /** Defiende a su dueño: ataca a los monstruos que lo atacan o lo persiguen. */
    public static class Protect extends TargetGoal {
        private final WaifuEntity w;
        private LivingEntity found;

        public Protect(WaifuEntity w) {
            super(w, false);
            this.w = w;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (w.getMode() == WaifuEntity.STAY || w.kind == WaifuKind.SHIORI) return false;
            Player p = w.ownerPlayer();
            if (p == null) return false;
            LivingEntity a = p.getLastHurtByMob();
            if (a instanceof Enemy && a.isAlive() && a.distanceToSqr(w) < 16 * 16) {
                found = a;
                return true;
            }
            for (Mob m : w.level().getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(10), m -> m instanceof Enemy && m.getTarget() == p)) {
                found = m;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            w.setTarget(found);
            super.start();
        }
    }

    static final TargetingConditions ANY = TargetingConditions.forCombat().ignoreLineOfSight();
}
