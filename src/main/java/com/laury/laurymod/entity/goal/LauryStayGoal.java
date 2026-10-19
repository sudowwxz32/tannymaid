package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class LauryStayGoal extends Goal {
    private final LauryEntity laury;
    private BlockPos anchor;
    private int recalc;

    public LauryStayGoal(LauryEntity laury) {
        this.laury = laury;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    private double dist(BlockPos p) { return laury.distanceToSqr(p.getX() + 0.5, p.getY(), p.getZ() + 0.5); }

    @Override
    public boolean canUse() {
        if (laury.getMode() != LauryEntity.MODE_STAY || laury.isWeakened() || laury.isBusy() || laury.getTarget() != null) return false;
        anchor = laury.getAnchor();
        return anchor != null && dist(anchor) > 9;
    }

    @Override public boolean canContinueToUse() { return anchor != null && !laury.isBusy() && laury.getTarget() == null && dist(anchor) > 2; }
    @Override public void start() { recalc = 0; }
    @Override public void stop() { anchor = null; laury.getNavigation().stop(); }

    @Override
    public void tick() {
        if (--recalc <= 0) {
            recalc = adjustedTickDelay(20);
            laury.getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 1.0D);
        }
    }
}
