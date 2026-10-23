package com.bdsz.block;

import com.bdsz.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Energía, dueño y funcionamiento del domo protector. */
public class ShieldGeneratorBlockEntity extends BlockEntity {
    public static final int RADIUS = 12;
    public static final int FUEL_PER_COPPER = 30 * 60 * 20;        // 30 minutos
    public static final int MAX_FUEL = 48 * FUEL_PER_COPPER;       // 24 horas

    /** Domos encendidos de cada dimensión (para bloquear apariciones y explosiones). */
    private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE = new ConcurrentHashMap<>();

    private int fuel;
    private UUID owner;
    private String ownerName = "?";

    public ShieldGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.SHIELD_BE.get(), pos, state);
    }

    public boolean on() {
        return getBlockState().getValue(ShieldGeneratorBlock.ON);
    }

    public void setOwner(UUID id, String name) {
        owner = id;
        ownerName = name;
        setChanged();
    }

    public boolean isOwner(Player p) {
        return owner == null || owner.equals(p.getUUID());
    }

    public String ownerName() {
        return ownerName;
    }

    /** Agrega lingotes de cobre; devuelve cuántos se usaron. */
    public int addFuel(int ingots) {
        int room = (MAX_FUEL - fuel) / FUEL_PER_COPPER;
        int used = Math.max(0, Math.min(room, ingots));
        fuel += used * FUEL_PER_COPPER;
        setChanged();
        return used;
    }

    public String status() {
        int sec = fuel / 20;
        return String.format("%s §7· energía %d:%02d:%02d", on() ? "§aENCENDIDO" : "§cAPAGADO", sec / 3600, (sec / 60) % 60, sec % 60);
    }

    public void toggle(ServerPlayer p) {
        if (!on() && fuel <= 0) {
            p.displayClientMessage(Component.literal("§cSin energía: aliméntalo con un lingote de cobre (30 min cada uno)."), true);
            return;
        }
        setOn(!on());
        p.displayClientMessage(Component.literal("§bGenerador Protector " + status()), true);
    }

    private void setOn(boolean value) {
        if (level == null) return;
        level.setBlock(worldPosition, getBlockState().setValue(ShieldGeneratorBlock.ON, value), 3);
        level.playSound(null, worldPosition, value ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.5F, value ? 1.2F : 0.8F);
        track(value);
        setChanged();
    }

    private void track(boolean value) {
        if (level == null) return;
        Set<BlockPos> set = ACTIVE.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet());
        if (value) set.add(worldPosition.immutable()); else set.remove(worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) track(false);
    }

    /** ¿Esta posición está dentro de algún domo encendido? */
    public static boolean protectedAt(Level level, Vec3 p) {
        Set<BlockPos> set = ACTIVE.get(level.dimension());
        if (set == null) return false;
        for (BlockPos c : set) {
            if (c.getCenter().distanceToSqr(p) < (RADIUS + 0.5) * (RADIUS + 0.5)) return true;
        }
        return false;
    }

    /** No deja pasar a esta entidad (enemigos, sus proyectiles y otros jugadores). */
    private boolean blocked(Entity e) {
        if (e instanceof Enemy) return true;
        if (e instanceof Projectile pr) {
            Entity o = pr.getOwner();
            return o instanceof Enemy || (o instanceof Player op && owner != null && !op.getUUID().equals(owner));
        }
        if (e instanceof Player p) {
            return owner != null && !p.getUUID().equals(owner) && !p.isCreative() && !p.isSpectator();
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ShieldGeneratorBlockEntity be) {
        if (!(level instanceof ServerLevel sl)) return;
        if (!state.getValue(ShieldGeneratorBlock.ON)) return;
        be.track(true);
        if (--be.fuel <= 0) {
            be.fuel = 0;
            be.setOn(false);
            return;
        }
        if (be.fuel % 200 == 0) be.setChanged();
        if (level.getGameTime() % 2 != 0) return;
        Vec3 c = pos.getCenter();
        double r = RADIUS;
        for (Entity e : sl.getEntities((Entity) null, new AABB(pos).inflate(r + 2), be::blocked)) {
            Vec3 d = e.position().add(0, e.getBbHeight() / 2, 0).subtract(c);
            double dist = d.length();
            if (dist > r + 0.5) continue;
            Vec3 out = dist < 0.1 ? new Vec3(1, 0, 0) : d.scale(1.0 / dist);
            if (e instanceof Projectile) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY(), e.getZ(), 10, 0.2, 0.2, 0.2, 0.2);
                e.discard();
                continue;
            }
            // empuja al intruso fuera del domo
            if (dist < r - 1.5) {
                Vec3 edge = c.add(out.x * (r + 1.5), 0, out.z * (r + 1.5));
                e.teleportTo(edge.x, e.getY(), edge.z);
            }
            e.setDeltaMovement(out.x * 0.9, 0.35, out.z * 0.9);
            e.hurtMarked = true;
            if (level.getGameTime() % 10 == 0) {
                sl.sendParticles(new DustParticleOptions(new Vector3f(0.4F, 0.9F, 1.0F), 1.5F), e.getX(), e.getY() + 1, e.getZ(), 15, 0.4, 0.6, 0.4, 0);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + 1, e.getZ(), 8, 0.3, 0.5, 0.3, 0.3);
                sl.playSound(null, e.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6F, 2.0F);
            }
        }
        if (level.getGameTime() % 80 == 0) sl.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.6F, 1.3F);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Fuel", fuel);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putString("OwnerName", ownerName);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuel = tag.getInt("Fuel");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ownerName = tag.getString("OwnerName");
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(RADIUS + 1);
    }
}
