package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;

/** Ataca a quien lastime al Master. */
public class TannyOwnerHurtByTargetGoal extends TargetGoal {
    private final TannyEntity tanny;
    private LivingEntity attacker;
    private int timestamp;

    public TannyOwnerHurtByTargetGoal(TannyEntity tanny) {
        super(tanny, false);
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (tanny.isWeakened()) return false;
        ServerPlayer owner = tanny.getOwnerPlayer();
        if (owner == null || owner.level() != tanny.level()) return false;
        attacker = owner.getLastHurtByMob();
        int ts = owner.getLastHurtByMobTimestamp();
        return ts != timestamp && attacker != null && attacker.isAlive()
                && tanny.distanceToSqr(attacker) < 32 * 32 && tanny.canAttack(attacker);
    }

    @Override
    public void start() {
        tanny.setTarget(attacker);
        ServerPlayer owner = tanny.getOwnerPlayer();
        if (owner != null) timestamp = owner.getLastHurtByMobTimestamp();
        super.start();
    }
}
