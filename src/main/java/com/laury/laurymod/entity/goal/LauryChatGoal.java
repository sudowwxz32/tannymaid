package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Mientras charlan, Laury se detiene y te mira. */
public class LauryChatGoal extends Goal {
    private final LauryEntity laury;
    private ServerPlayer owner;

    public LauryChatGoal(LauryEntity laury) {
        this.laury = laury;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean valid(ServerPlayer o) {
        return o != null && laury.isChatting() && !laury.isWeakened() && !laury.isBusy() && laury.getTarget() == null
                && o.level() == laury.level() && laury.distanceToSqr(o) < 8 * 8;
    }

    @Override
    public boolean canUse() {
        ServerPlayer o = laury.getOwnerPlayer();
        if (!valid(o)) return false;
        owner = o;
        return true;
    }

    @Override public boolean canContinueToUse() { return valid(owner); }
    @Override public void start() { laury.getNavigation().stop(); }
    @Override public void stop() { owner = null; }

    @Override
    public void tick() {
        laury.getNavigation().stop();
        laury.getLookControl().setLookAt(owner, 30.0F, 30.0F);
    }
}
