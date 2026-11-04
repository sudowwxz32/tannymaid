package com.linterna.entity;

import com.linterna.data.LanternLogic;
import com.linterna.registry.ModRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Disparo de energía verde (armas del anillo) o bomba de energía (explota al chocar). */
public class RayoVerde extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> BOMB = SynchedEntityData.defineId(RayoVerde.class, EntityDataSerializers.BOOLEAN);

    public RayoVerde(EntityType<? extends RayoVerde> type, Level level) {
        super(type, level);
    }

    public RayoVerde(Level level, LivingEntity owner, boolean bomb) {
        super(ModRegistry.RAYO.get(), owner, level);
        entityData.set(BOMB, bomb);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(BOMB, false);
    }

    public boolean isBomb() {
        return entityData.get(BOMB);
    }

    @Override
    protected float getGravity() {
        return isBomb() ? 0.04F : 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel lv) {
            lv.sendParticles(LanternLogic.GREEN, getX(), getY(), getZ(), 2, 0.05, 0.05, 0.05, 0);
            if (tickCount > 100) discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity e = hit.getEntity();
        if (e == getOwner()) return;
        if (!isBomb()) e.hurt(damageSources().indirectMagic(this, getOwner()), 9.0F);
    }

    @Override
    protected void onHit(HitResult hit) {
        if (hit instanceof EntityHitResult eh && eh.getEntity() == getOwner()) return;
        super.onHit(hit);
        if (level() instanceof ServerLevel lv) {
            if (isBomb()) {
                lv.explode(getOwner(), getX(), getY(), getZ(), 3.5F, Level.ExplosionInteraction.TNT);
            }
            lv.sendParticles(LanternLogic.GREEN, getX(), getY(), getZ(), isBomb() ? 120 : 15, 0.4, 0.4, 0.4, 0);
            discard();
        }
    }
}
