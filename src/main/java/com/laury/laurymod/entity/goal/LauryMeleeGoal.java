package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/** Ataque cuerpo a cuerpo (humana o dragón), salvo cuando la montan o está débil. */
public class LauryMeleeGoal extends MeleeAttackGoal {
    private final LauryEntity laury;

    public LauryMeleeGoal(LauryEntity laury) {
        super(laury, 1.3D, true);
        this.laury = laury;
    }

    @Override public boolean canUse() { return !laury.isVehicle() && !laury.isWeakened() && super.canUse(); }
    @Override public boolean canContinueToUse() { return !laury.isVehicle() && !laury.isWeakened() && super.canContinueToUse(); }
}
