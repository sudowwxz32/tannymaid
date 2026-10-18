package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Sigue al Master; si se aleja demasiado, se teletransporta a su lado. */
public class TannyFollowOwnerGoal extends Goal {
    private final TannyEntity tanny;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private ServerPlayer owner;
    private int recalc;

    public TannyFollowOwnerGoal(TannyEntity tanny, double speed, float startDistance, float stopDistance) {
        this.tanny = tanny;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!tanny.getMode().follows() || tanny.isWeakened() || tanny.isGathering()) return false;
        ServerPlayer o = tanny.getOwnerPlayer();
        if (o == null || o.isSpectator() || o.level() != tanny.level()) return false;
        if (tanny.distanceToSqr(o) < startDistance * startDistance) return false;
        this.owner = o;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || !owner.isAlive() || owner.level() != tanny.level()) return false;
        if (!tanny.getMode().follows() || tanny.isWeakened()) return false;
        return tanny.distanceToSqr(owner) > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void stop() {
        owner = null;
        tanny.getNavigation().stop();
    }

    @Override
    public void tick() {
        tanny.getLookControl().setLookAt(owner, 10.0F, tanny.getMaxHeadXRot());
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(10);
            double distSqr = tanny.distanceToSqr(owner);
            if (distSqr >= 24 * 24) {
                tanny.teleportNear(owner);
            } else {
                tanny.getNavigation().moveTo(owner, distSqr > 64 ? speed * 1.3 : speed);
            }
        }
    }
}
