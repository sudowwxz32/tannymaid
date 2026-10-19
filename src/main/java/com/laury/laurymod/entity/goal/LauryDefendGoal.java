package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;
import java.util.List;

/** Busca monstruos cerca del jugador y los ataca (humana o dragón). */
public class LauryDefendGoal extends TargetGoal {
    private final LauryEntity laury;
    private LivingEntity candidate;
    private int cooldown;

    public LauryDefendGoal(LauryEntity laury) {
        super(laury, false);
        this.laury = laury;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (laury.isWeakened() || laury.isGathering()) return false;
        if (--cooldown > 0) return false;
        cooldown = 10;
        ServerPlayer owner = laury.getOwnerPlayer();
        LivingEntity center = laury;
        if (laury.getMode() == LauryEntity.MODE_FOLLOW && owner != null && owner.level() == laury.level()) center = owner;
        double r = laury.isDragon() ? 18.0D : 14.0D;
        List<Mob> mobs = laury.level().getEntitiesOfClass(Mob.class, center.getBoundingBox().inflate(r, 8.0D, r),
                m -> laury.isValidDefendTarget(m, owner));
        Mob best = null;
        double bestScore = Double.MAX_VALUE;
        for (Mob m : mobs) {
            double s = m.distanceToSqr(center);
            if (owner != null && m.getTarget() == owner) s -= 10000;
            if (m.getTarget() == laury) s -= 5000;
            if (s < bestScore) { bestScore = s; best = m; }
        }
        candidate = best;
        return best != null;
    }

    @Override
    public void start() {
        laury.setTarget(candidate);
        targetMob = candidate;
        super.start();
    }
}
