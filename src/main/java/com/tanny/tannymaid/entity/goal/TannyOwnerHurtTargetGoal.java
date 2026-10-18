package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enemy;

import java.util.EnumSet;

/** Ayuda al Master a pelear contra el mob hostil que él está atacando. */
public class TannyOwnerHurtTargetGoal extends TargetGoal {
    private final TannyEntity tanny;
    private LivingEntity victim;
    private int timestamp;

    public TannyOwnerHurtTargetGoal(TannyEntity tanny) {
        super(tanny, false);
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (tanny.isWeakened()) return false;
        ServerPlayer owner = tanny.getOwnerPlayer();
        if (owner == null || owner.level() != tanny.level()) return false;
        victim = owner.getLastHurtMob();
        int ts = owner.getLastHurtMobTimestamp();
        return ts != timestamp && victim instanceof Enemy && victim.isAlive()
                && tanny.distanceToSqr(victim) < 32 * 32 && tanny.canAttack(victim);
    }

    @Override
    public void start() {
        tanny.setTarget(victim);
        ServerPlayer owner = tanny.getOwnerPlayer();
        if (owner != null) timestamp = owner.getLastHurtMobTimestamp();
        super.start();
    }
}
