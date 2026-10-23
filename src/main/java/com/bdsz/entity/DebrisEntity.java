package com.bdsz.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Escombro de la transformación: un trozo del suelo que sale volando, gira, rebota y desaparece.
 * Es solo visual: no rompe ni coloca bloques.
 */
public class DebrisEntity extends Entity {
    private static final EntityDataAccessor<BlockState> BLOCK = SynchedEntityData.defineId(DebrisEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(DebrisEntity.class, EntityDataSerializers.FLOAT);
    /** Ticks que la roca levita (0 = cae normal). */
    private static final EntityDataAccessor<Integer> FLOAT = SynchedEntityData.defineId(DebrisEntity.class, EntityDataSerializers.INT);
    private int life = 70;

    public DebrisEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    /** Lanza un escombro del bloque que hay bajo {@code ground}. */
    public static void launch(ServerLevel lv, Vec3 from, BlockState state, double vx, double vy, double vz, float size) {
        if (state.isAir() || state.getRenderShape() != RenderShape.MODEL) state = Blocks.DIRT.defaultBlockState();
        DebrisEntity d = new DebrisEntity(com.bdsz.registry.ModRegistry.DEBRIS.get(), lv);
        d.entityData.set(BLOCK, state);
        d.entityData.set(SIZE, size);
        d.life = 50 + lv.random.nextInt(40);
        d.moveTo(from.x, from.y, from.z, lv.random.nextFloat() * 360, 0);
        d.setDeltaMovement(vx, vy, vz);
        lv.addFreshEntity(d);
    }

    /** Roca que se despega del suelo y levita girando (mientras el guerrero carga su poder). */
    public static void levitate(ServerLevel lv, Vec3 from, BlockState state, int floatTicks, float size) {
        if (state.isAir() || state.getRenderShape() != RenderShape.MODEL) state = Blocks.DIRT.defaultBlockState();
        DebrisEntity d = new DebrisEntity(com.bdsz.registry.ModRegistry.DEBRIS.get(), lv);
        d.entityData.set(BLOCK, state);
        d.entityData.set(SIZE, size);
        d.entityData.set(FLOAT, floatTicks);
        d.life = floatTicks + 60;
        d.moveTo(from.x, from.y, from.z, lv.random.nextFloat() * 360, 0);
        d.setDeltaMovement(0, 0.12, 0);
        lv.addFreshEntity(d);
    }

    public boolean isFloating() { return tickCount < entityData.get(FLOAT); }

    /** Termina la levitación y sale disparada hacia afuera. */
    public void blast(Vec3 center, double power) {
        entityData.set(FLOAT, 0);
        Vec3 d = position().subtract(center);
        Vec3 h = new Vec3(d.x, 0, d.z);
        h = h.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : h.normalize();
        setDeltaMovement(h.x * power, 0.35 + power * 0.3, h.z * power);
        hurtMarked = true;
    }

    /** Bloque del suelo bajo una posición (busca hacia abajo unos bloques). */
    public static BlockState groundAt(Level lv, BlockPos pos) {
        for (int i = 0; i < 4; i++) {
            BlockState s = lv.getBlockState(pos.below(i + 1));
            if (!s.isAir() && s.getRenderShape() == RenderShape.MODEL) return s;
        }
        return Blocks.DIRT.defaultBlockState();
    }

    public BlockState getBlock() { return entityData.get(BLOCK); }
    public float getSize() { return entityData.get(SIZE); }

    @Override
    protected void defineSynchedData() {
        entityData.define(BLOCK, Blocks.DIRT.defaultBlockState());
        entityData.define(SIZE, 0.4F);
        entityData.define(FLOAT, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (isFloating()) {
            // sube despacio, se frena y se queda flotando con un leve vaivén
            int ft = entityData.get(FLOAT);
            float k = Math.min(1.0F, tickCount / 25.0F);
            double vy = (1.0 - k) * 0.09 + Math.sin((tickCount + getId()) * 0.15) * 0.008;
            setDeltaMovement(getDeltaMovement().x * 0.8, vy, getDeltaMovement().z * 0.8);
            move(MoverType.SELF, getDeltaMovement());
            if (level().isClientSide && random.nextInt(6) == 0) {
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, getBlock()), getX(), getY(), getZ(), 0, -0.05, 0);
            }
            if (!level().isClientSide && tickCount >= ft - 1) entityData.set(FLOAT, 0);
            return;
        }
        Vec3 v = getDeltaMovement().add(0, -0.045, 0);
        setDeltaMovement(v);
        move(MoverType.SELF, v);
        if (onGround()) {
            Vec3 m = getDeltaMovement();
            if (Math.abs(v.y) > 0.18) {
                setDeltaMovement(m.x * 0.55, -v.y * 0.35, m.z * 0.55);   // rebote
                if (level().isClientSide) {
                    level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, getBlock()), getX(), getY() + 0.1, getZ(), 0, 0.1, 0);
                }
            } else {
                setDeltaMovement(m.x * 0.6, m.y, m.z * 0.6);
            }
        }
        if (!level().isClientSide && --life <= 0) discard();
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) { }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) { }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
