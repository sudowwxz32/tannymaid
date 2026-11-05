package com.linterna.data;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Con el anillo en la mano, clic derecho: levantas al animal, mob, jugador, objeto o bloque que miras con una mano
 * de energía verde; otro clic derecho lo lanza hacia donde miras.
 */
public final class Holding {
    private Holding() {}

    private static final Map<UUID, Integer> HELD = new ConcurrentHashMap<>();

    public static boolean isHolding(ServerPlayer p) {
        return HELD.containsKey(p.getUUID());
    }

    public static void use(ServerPlayer p) {
        LanternState s = LanternData.get(p);
        if (!s.bearer()) return;
        if (isHolding(p)) {
            release(p, true);
            return;
        }
        if (s.charge <= 0) {
            LanternLogic.msg(p, "§cEl anillo no tiene energía.");
            return;
        }
        ServerLevel lv = p.serverLevel();
        Vec3 eye = p.getEyePosition(), look = p.getLookAngle(), end = eye.add(look.scale(24));
        BlockHitResult bh = lv.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
        Vec3 limit = bh.getType() == HitResult.Type.MISS ? end : bh.getLocation();
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(lv, p, eye, limit, new AABB(eye, limit).inflate(1.5),
                e -> !e.isSpectator() && e.isAlive() && e != p && !(e instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon));
        // el rayo de selección sale de la mano hasta lo que miras
        Vec3 hand = eye.subtract(0, 0.35, 0).add(new Vec3(-look.z, 0, look.x).normalize().scale(0.35));
        Vec3 hitAt = eh != null ? eh.getEntity().position().add(0, eh.getEntity().getBbHeight() / 2, 0) : limit;
        for (int i = 0; i <= 24; i++) {
            Vec3 q = hand.lerp(hitAt, i / 24.0);
            lv.sendParticles(LanternLogic.GREEN, q.x, q.y, q.z, 1, 0.02, 0.02, 0.02, 0);
        }
        Entity grab = null;
        if (eh != null) {
            grab = eh.getEntity();
        } else if (bh.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = bh.getBlockPos();
            BlockState st = lv.getBlockState(pos);
            if (st.getDestroySpeed(lv, pos) < 0 || lv.getBlockEntity(pos) != null || st.isAir()) {
                LanternLogic.msg(p, "§7Ese bloque es demasiado firme para el anillo.");
                return;
            }
            FallingBlockEntity fb = FallingBlockEntity.fall(lv, pos, st);
            fb.setNoGravity(true);
            fb.dropItem = true;
            grab = fb;
        }
        if (grab == null) {
            LanternLogic.ringShot(p);              // nada que levantar: el anillo dispara
            return;
        }
        HELD.put(p.getUUID(), grab.getId());
        grab.setNoGravity(true);
        Glow.on(grab);                              // silueta verde en lo que sostienes
        s.held = grab.getId();
        LanternData.set(p, s);
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.6F);
    }

    public static void tick(ServerPlayer p) {
        Integer id = HELD.get(p.getUUID());
        if (id == null) return;
        ServerLevel lv = p.serverLevel();
        Entity e = lv.getEntity(id);
        if (e == null || !e.isAlive() || e.distanceToSqr(p) > 40 * 40) {
            HELD.remove(p.getUUID());
            if (e != null) Glow.off(e);
            clearHeld(p);
            return;
        }
        double dist = 3.5 + e.getBbWidth();
        Vec3 to = p.getEyePosition().add(p.getLookAngle().scale(dist)).subtract(0, e.getBbHeight() / 2, 0);
        if (e instanceof ServerPlayer sp) {
            sp.teleportTo(to.x, to.y, to.z);
        } else {
            e.setPos(to.x, to.y, to.z);
        }
        e.setDeltaMovement(Vec3.ZERO);
        e.fallDistance = 0;
        e.hurtMarked = true;
        if (e instanceof FallingBlockEntity fb) fb.time = 1;      // no se rompe ni desaparece mientras flota
        // brillo verde alrededor de lo que sostiene (el rayo lo dibuja el cliente)
        if (p.tickCount % 4 == 0) {
            Vec3 b = e.position().add(0, e.getBbHeight() / 2, 0);
            lv.sendParticles(LanternLogic.GREEN, b.x, b.y, b.z, 3, e.getBbWidth() * 0.5, e.getBbHeight() * 0.5, e.getBbWidth() * 0.5, 0);
        }
    }

    private static void clearHeld(ServerPlayer p) {
        LanternState s = LanternData.get(p);
        s.held = -1;
        LanternData.set(p, s);
    }

    /** Suelta lo que tiene; si throwIt, lo lanza hacia donde mira. */
    public static void release(ServerPlayer p, boolean throwIt) {
        Integer id = HELD.remove(p.getUUID());
        if (id == null) return;
        clearHeld(p);
        Entity e = p.serverLevel().getEntity(id);
        if (e == null) return;
        e.setNoGravity(false);
        Glow.off(e);
        if (throwIt) {
            Vec3 v = p.getLookAngle().scale(1.8);
            e.setDeltaMovement(v.x, v.y + 0.2, v.z);
            e.hurtMarked = true;
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
    }
}
