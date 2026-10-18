package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Mientras está debilitada, Tanny se queda sentada y quieta. */
public class TannyWeakenedGoal extends Goal {
    private final TannyEntity tanny;

    public TannyWeakenedGoal(TannyEntity tanny) {
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return tanny.isWeakened();
    }

    @Override
    public void start() {
        tanny.getNavigation().stop();
    }

    @Override
    public void tick() {
        tanny.getNavigation().stop();
    }
}
