package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.entity.TannyMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;
import java.util.List;

/** Detecta mobs hostiles cerca del Master (o de Tanny, si está en casa) y los ataca. */
public class TannyDefendGoal extends TargetGoal {
    private final TannyEntity tanny;
    private LivingEntity candidate;
    private int cooldown;

    public TannyDefendGoal(TannyEntity tanny) {
        super(tanny, false);
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (tanny.isWeakened()) return false;
        if (--cooldown > 0) return false;
        cooldown = 10;

        ServerPlayer owner = tanny.getOwnerPlayer();
        LivingEntity center = tanny;
        if (tanny.getMode().follows() && owner != null && owner.level() == tanny.level()) {
            center = owner;
        }
        double radius = tanny.getMode() == TannyMode.DEFEND ? 16.0D : 10.0D;
        List<Mob> mobs = tanny.level().getEntitiesOfClass(Mob.class, center.getBoundingBox().inflate(radius, 6.0D, radius),
                m -> tanny.isValidDefendTarget(m, owner));
        if (mobs.isEmpty()) return false;

        Mob best = null;
        double bestScore = Double.MAX_VALUE;
        for (Mob m : mobs) {
            double score = m.distanceToSqr(center);
            if (owner != null && m.getTarget() == owner) score -= 10000; // prioridad: quien ataca al Master
            if (m.getTarget() == tanny) score -= 5000;
            if (score < bestScore) {
                bestScore = score;
                best = m;
            }
        }
        candidate = best;
        return candidate != null;
    }

    @Override
    public void start() {
        tanny.setTarget(candidate);
        this.targetMob = candidate;
        super.start();
    }
}
