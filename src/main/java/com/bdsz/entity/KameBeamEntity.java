package com.bdsz.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Kamehameha: rayo de energía continuo que sale de las manos y sigue la mirada del lanzador. */
public class KameBeamEntity extends Entity {
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(KameBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(KameBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(KameBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(KameBeamEntity.class, EntityDataSerializers.INT);
    public static final int DURATION = 40;
    public static final float MAX_LENGTH = 48.0F;
    private float damage = 20.0F;

    public KameBeamEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public void setup(Player owner, float damage, float width, int color) {
        entityData.set(OWNER, owner.getId());
        entityData.set(WIDTH, width);
        entityData.set(COLOR, color);
        this.damage = damage;
        follow(owner);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(OWNER, -1);
        entityData.define(LENGTH, 1.0F);
        entityData.define(WIDTH, 1.0F);
        entityData.define(COLOR, 0x5AC8FF);
    }

    public float getLength() { return entityData.get(LENGTH); }
    public float getWidth() { return entityData.get(WIDTH); }
    public int getColor() { return entityData.get(COLOR); }

    public Entity getOwnerEntity() {
        return level().getEntity(entityData.get(OWNER));
    }

    /** El rayo nace frente a las manos y apunta hacia donde mira el lanzador. */
    private void follow(Entity owner) {
        Vec3 look = owner.getLookAngle();
        Vec3 start = owner.getEyePosition().add(0, -0.35, 0).add(look.scale(0.9));
        setPos(start.x, start.y, start.z);
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwnerEntity();
        if (owner == null || !owner.isAlive()) {
            if (!level().isClientSide) discard();
            return;
        }
        follow(owner);
        Vec3 dir = owner.getLookAngle();
        Vec3 start = position();
        if (!level().isClientSide) {
            if (tickCount > DURATION) { discard(); return; }
            Vec3 end = start.add(dir.scale(MAX_LENGTH));
            BlockHitResult hit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            float len = (float) (hit.getType() == HitResult.Type.MISS ? MAX_LENGTH : hit.getLocation().distanceTo(start));
            entityData.set(LENGTH, len);
            ServerLevel sl = (ServerLevel) level();
            Vec3 tip = start.add(dir.scale(len));
            if (tickCount % 4 == 0) {
                float w = getWidth();
                AABB box = new AABB(start, tip).inflate(w);
                for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box, e -> e != owner && e.isAlive())) {
                    Vec3 rel = e.position().add(0, e.getBbHeight() / 2, 0).subtract(start);
                    double along = rel.dot(dir);
                    if (along < 0 || along > len) continue;
                    double dist = rel.subtract(dir.scale(along)).length();
                    if (dist > w + e.getBbWidth() / 2) continue;
                    e.hurt(damageSources().indirectMagic(this, owner), damage);
                    e.setDeltaMovement(e.getDeltaMovement().add(dir.scale(0.6)));
                    e.hurtMarked = true;
                }
                if (hit.getType() != HitResult.Type.MISS) {
                    sl.sendParticles(ParticleTypes.EXPLOSION, tip.x, tip.y, tip.z, 1, 0.3, 0.3, 0.3, 0);
                    sl.playSound(null, tip.x, tip.y, tip.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.4F, 1.6F);
                }
            }
            if (tickCount % 10 == 0) sl.playSound(null, owner.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.5F, 1.8F);
        } else {
            int c = getColor();
            DustParticleOptions dust = new DustParticleOptions(new Vector3f(((c >> 16) & 255) / 255F, ((c >> 8) & 255) / 255F, (c & 255) / 255F), 1.5F);
            float len = getLength();
            for (int i = 0; i < 6; i++) {
                double d = random.nextDouble() * len;
                Vec3 p = start.add(dir.scale(d));
                level().addParticle(dust, p.x + (random.nextDouble() - 0.5) * getWidth(), p.y + (random.nextDouble() - 0.5) * getWidth(),
                        p.z + (random.nextDouble() - 0.5) * getWidth(), 0, 0, 0);
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 128 * 128;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag t) { }

    @Override
    protected void addAdditionalSaveData(CompoundTag t) { }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
