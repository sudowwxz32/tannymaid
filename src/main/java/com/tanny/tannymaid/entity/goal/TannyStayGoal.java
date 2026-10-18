package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.entity.TannyMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Mantiene a Tanny cerca de su punto (quedarse, casa o granja). */
public class TannyStayGoal extends Goal {
    private final TannyEntity tanny;
    private BlockPos anchor;
    private int recalc;

    public TannyStayGoal(TannyEntity tanny) {
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private double radius() {
        return tanny.getMode() == TannyMode.FARM ? 10.0D : 3.0D;
    }

    private double distSqr(BlockPos pos) {
        return tanny.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    @Override
    public boolean canUse() {
        if (tanny.getMode().follows() || tanny.isWeakened() || tanny.getTarget() != null) return false;
        BlockPos a = tanny.getAnchor();
        if (a == null) return false;
        double r = radius();
        if (distSqr(a) <= r * r) return false;
        anchor = a;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return anchor != null && !tanny.isWeakened() && tanny.getTarget() == null
                && anchor.equals(tanny.getAnchor()) && distSqr(anchor) > 2.0D;
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void stop() {
        anchor = null;
        tanny.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(20);
            if (distSqr(anchor) > 48 * 48) {
                tanny.moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, tanny.getYRot(), tanny.getXRot());
                tanny.getNavigation().stop();
            } else {
                tanny.getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 1.0D);
            }
        }
    }
}
