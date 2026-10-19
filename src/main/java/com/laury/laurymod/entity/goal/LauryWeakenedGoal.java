package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Debilitada: se queda quieta hasta regenerarse. */
public class LauryWeakenedGoal extends Goal {
    private final LauryEntity laury;

    public LauryWeakenedGoal(LauryEntity laury) {
        this.laury = laury;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override public boolean canUse() { return laury.isWeakened(); }
    @Override public void start() { laury.getNavigation().stop(); }
    @Override public void tick() { laury.getNavigation().stop(); }
}
