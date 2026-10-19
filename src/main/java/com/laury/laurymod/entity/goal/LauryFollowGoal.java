package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class LauryFollowGoal extends Goal {
    private final LauryEntity laury;
    private ServerPlayer owner;
    private int recalc;

    public LauryFollowGoal(LauryEntity laury) {
        this.laury = laury;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private float startDist() { return laury.isDragon() ? 7.0F : 4.5F; }
    private float stopDist() { return laury.isDragon() ? 4.5F : 2.3F; }

    @Override
    public boolean canUse() {
        if (laury.getMode() != LauryEntity.MODE_FOLLOW || laury.isWeakened() || laury.isBusy()) return false;
        ServerPlayer o = laury.getOwnerPlayer();
        if (o == null || o.isSpectator() || o.level() != laury.level() || o.getVehicle() == laury) return false;
        if (laury.distanceToSqr(o) < startDist() * startDist()) return false;
        owner = o;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && owner.isAlive() && owner.level() == laury.level() && !laury.isBusy() && !laury.isWeakened()
                && laury.getMode() == LauryEntity.MODE_FOLLOW && laury.distanceToSqr(owner) > stopDist() * stopDist();
    }

    @Override public void start() { recalc = 0; }
    @Override public void stop() { owner = null; laury.getNavigation().stop(); }

    @Override
    public void tick() {
        laury.getLookControl().setLookAt(owner, 10.0F, laury.getMaxHeadXRot());
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            double d = laury.distanceToSqr(owner);
            if (d > 22 * 22 || (laury.isDragon() && d > 14 * 14 && laury.getNavigation().isDone())) {
                laury.teleportToOwner(owner);
            } else {
                laury.getNavigation().moveTo(owner, d > 81 ? 1.35D : 1.1D);
            }
        }
    }
}
