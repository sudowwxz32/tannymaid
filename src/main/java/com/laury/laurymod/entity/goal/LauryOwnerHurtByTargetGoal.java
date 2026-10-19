package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;

public class LauryOwnerHurtByTargetGoal extends TargetGoal {
    private final LauryEntity laury;
    private LivingEntity attacker;
    private int timestamp;

    public LauryOwnerHurtByTargetGoal(LauryEntity laury) {
        super(laury, false);
        this.laury = laury;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (laury.isWeakened()) return false;
        ServerPlayer o = laury.getOwnerPlayer();
        if (o == null || o.level() != laury.level()) return false;
        attacker = o.getLastHurtByMob();
        return o.getLastHurtByMobTimestamp() != timestamp && attacker != null && attacker.isAlive()
                && laury.distanceToSqr(attacker) < 32 * 32 && laury.canAttack(attacker);
    }

    @Override
    public void start() {
        laury.setTarget(attacker);
        ServerPlayer o = laury.getOwnerPlayer();
        if (o != null) timestamp = o.getLastHurtByMobTimestamp();
        super.start();
    }
}
