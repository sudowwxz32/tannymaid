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
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(RayoVerde.class, EntityDataSerializers.FLOAT);
    private float damage = 9.0F;
    private boolean pierce;
    private final java.util.Set<Integer> struck = new java.util.HashSet<>();

    public void setDamage(float d) { damage = d; }
    public void setPierce(boolean p) { pierce = p; if (p) entityData.set(SIZE, 0.75F); }
    public void setSize(float s) { entityData.set(SIZE, s); }
    public float size() { return entityData.get(SIZE); }

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
        entityData.define(SIZE, 0.45F);
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
        if (e == getOwner() || !struck.add(e.getId())) return;
        if (!isBomb()) e.hurt(damageSources().indirectMagic(this, getOwner()), damage);
    }

    @Override
    protected void onHit(HitResult hit) {
        if (hit instanceof EntityHitResult eh && eh.getEntity() == getOwner()) return;
        if (pierce && hit instanceof EntityHitResult eh2) {          // la lanza atraviesa a los enemigos
            onHitEntity(eh2);
            return;
        }
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
