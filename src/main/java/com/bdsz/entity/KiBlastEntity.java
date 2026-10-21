package com.bdsz.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Ráfaga de ki: esfera de energía que vuela recta y explota sin romper bloques. */
public class KiBlastEntity extends Projectile {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(KiBlastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(KiBlastEntity.class, EntityDataSerializers.FLOAT);
    private float damage = 8.0F;
    private int life;

    public KiBlastEntity(EntityType<? extends KiBlastEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public void setup(int color, float size, float damage) {
        entityData.set(COLOR, color);
        entityData.set(SIZE, size);
        this.damage = damage;
    }

    public int getColor() { return entityData.get(COLOR); }
    public float getSize() { return entityData.get(SIZE); }

    @Override
    protected void defineSynchedData() {
        entityData.define(COLOR, 0x8FE8FF);
        entityData.define(SIZE, 0.6F);
    }

    @Override
    public void tick() {
        super.tick();
        if (++life > 70) {
            if (!level().isClientSide) explode(position());
            return;
        }
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hit)) {
            onHit(hit);
        }
        Vec3 v = getDeltaMovement();
        setPos(getX() + v.x, getY() + v.y, getZ() + v.z);
        ProjectileUtil.rotateTowardsMovement(this, 1.0F);
        if (level().isClientSide) {
            int c = getColor();
            DustParticleOptions dust = new DustParticleOptions(new Vector3f(((c >> 16) & 255) / 255F, ((c >> 8) & 255) / 255F, (c & 255) / 255F), 1.4F);
            for (int i = 0; i < 3; i++) {
                level().addParticle(dust, getX() + (random.nextDouble() - 0.5) * getSize(), getY() + (random.nextDouble() - 0.5) * getSize(),
                        getZ() + (random.nextDouble() - 0.5) * getSize(), 0, 0, 0);
            }
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), -v.x * 0.05, -v.y * 0.05, -v.z * 0.05);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = getOwner();
        target.hurt(damageSources().thrown(this, owner), damage);
        if (target instanceof LivingEntity l) {
            Vec3 d = getDeltaMovement().normalize();
            l.knockback(1.2D, -d.x, -d.z);
        }
        explode(result.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        explode(result.getLocation());
    }

    private void explode(Vec3 at) {
        if (level() instanceof ServerLevel sl) {
            int c = getColor();
            DustParticleOptions dust = new DustParticleOptions(new Vector3f(((c >> 16) & 255) / 255F, ((c >> 8) & 255) / 255F, (c & 255) / 255F), 2.0F);
            sl.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0, 0, 0, 0);
            sl.sendParticles(dust, at.x, at.y, at.z, 40, 0.6, 0.6, 0.6, 0.0);
            sl.sendParticles(ParticleTypes.FLASH, at.x, at.y, at.z, 1, 0, 0, 0, 0);
            sl.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6F, 1.6F);
            Entity owner = getOwner();
            double r = 1.5 + getSize();
            for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r),
                    e -> e != owner && e.isAlive() && e.distanceToSqr(at) < r * r)) {
                e.hurt(damageSources().thrown(this, owner), damage * 0.4F);
            }
        }
        discard();
    }

    @Override
    protected boolean canHitEntity(Entity e) {
        return super.canHitEntity(e) && !(e instanceof KiBlastEntity);
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) {
        super.addAdditionalSaveData(t);
        t.putInt("Color", getColor());
        t.putFloat("Size", getSize());
        t.putFloat("Damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) {
        super.readAdditionalSaveData(t);
        entityData.set(COLOR, t.getInt("Color"));
        entityData.set(SIZE, t.getFloat("Size"));
        damage = t.getFloat("Damage");
    }
}
