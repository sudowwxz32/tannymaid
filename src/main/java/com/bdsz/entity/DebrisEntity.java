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
    }

    @Override
    public void tick() {
        super.tick();
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
