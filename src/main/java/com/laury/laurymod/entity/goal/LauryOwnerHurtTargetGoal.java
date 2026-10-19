package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enemy;

import java.util.EnumSet;

public class LauryOwnerHurtTargetGoal extends TargetGoal {
    private final LauryEntity laury;
    private LivingEntity victim;
    private int timestamp;

    public LauryOwnerHurtTargetGoal(LauryEntity laury) {
        super(laury, false);
        this.laury = laury;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (laury.isWeakened()) return false;
        ServerPlayer o = laury.getOwnerPlayer();
        if (o == null || o.level() != laury.level()) return false;
        victim = o.getLastHurtMob();
        return o.getLastHurtMobTimestamp() != timestamp && victim instanceof Enemy && victim.isAlive()
                && laury.distanceToSqr(victim) < 32 * 32 && laury.canAttack(victim);
    }

    @Override
    public void start() {
        laury.setTarget(victim);
        ServerPlayer o = laury.getOwnerPlayer();
        if (o != null) timestamp = o.getLastHurtMobTimestamp();
        super.start();
    }
}
