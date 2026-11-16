package com.totallywaifu.entity;

import com.totallywaifu.data.Collection;
import com.totallywaifu.network.Fx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/** Yume: duermes con la cabeza en sus piernas. Ella te cuida toda la noche y recuerda dónde dormiste. */
public final class Sleep {
    private Sleep() {}

    public static boolean start(ServerPlayer p, WaifuEntity w) {
        if (w.kind != WaifuKind.CAMA) return false;
        if (!w.isOwner(p)) {
            w.say(p, "Lo siento... solo dejo dormir en mis piernas a mi persona favorita.");
            return false;
        }
        if (p.isSleeping() || p.isPassenger() || w.isSitting()) return false;
        if (!p.level().dimensionType().natural()) {
            w.say(p, "Aquí no se puede dormir... volvamos a casa ♥");
            return false;
        }
        if (p.level().isDay() && !p.level().isThundering()) {
            w.say(p, "Todavía es de día... vuelve de noche y te dejo dormir en mis piernas ♥");
            w.cheer(40);
            return false;
        }
        ServerLevel lv = p.serverLevel();
        w.getNavigation().stop();
        w.modeBeforeSleep = w.getMode();
        w.setMode(WaifuEntity.STAY);
        w.setSitting(true);
        w.sleeper = p.getUUID();
        float yaw = w.getYRot();
        w.setYBodyRot(yaw);
        w.setYHeadRot(yaw);
        double fx = -Mth.sin(yaw * Mth.DEG_TO_RAD), fz = Mth.cos(yaw * Mth.DEG_TO_RAD);
        Vec3 lap = w.position().add(fx * 0.75, 0, fz * 0.75);
        p.startSleeping(BlockPos.containing(lap));
        p.setPos(lap.x, w.getY() + 0.12, lap.z);
        p.setYRot(yaw);
        p.setYBodyRot(yaw);
        p.connection.teleport(lap.x, w.getY() + 0.12, lap.z, yaw, -25);
        lv.updateSleepingPlayerList();
        p.setRespawnPosition(lv.dimension(), w.blockPosition(), yaw, true, false);
        Collection.setHome(p, lv.dimension().location().toString(), w.blockPosition());
        Fx.send(p, Fx.SLEEP_ON, w.getId());
        w.say(p, "Shh... apoya tu cabeza en mis piernas. Yo te cuido toda la noche ♥");
        w.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F, 0.8F);
        p.displayClientMessage(net.minecraft.network.chat.Component.literal("§9Punto de regreso guardado junto a Yume ♥"), true);
        return true;
    }

    static void tick(WaifuEntity w) {
        ServerLevel lv = (ServerLevel) w.level();
        Player p = w.sleeper == null ? null : lv.getPlayerByUUID(w.sleeper);
        if (p == null || !p.isSleeping() || p.distanceToSqr(w) > 3 * 3) {
            wake(w, p);
            return;
        }
        w.getNavigation().stop();
        w.getLookControl().setLookAt(p.getX(), p.getY(), p.getZ());
        if (w.tickCount % 10 != 0) return;
        // guardiana de tus sueños: ningún monstruo se acerca
        for (Mob m : lv.getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(16, 8, 16), m -> m instanceof Enemy && m.isAlive())) {
            Vec3 a = w.position().add(0, 1.0, 0), b = m.position().add(0, m.getBbHeight() * 0.5, 0);
            for (int i = 0; i <= 10; i++) {
                Vec3 q = a.lerp(b, i / 10.0);
                lv.sendParticles(ParticleTypes.END_ROD, q.x, q.y, q.z, 1, 0.02, 0.02, 0.02, 0);
            }
            m.hurt(w.damageSources().indirectMagic(w, w), 10.0F);
            Vec3 d = m.position().subtract(w.position()).normalize().scale(1.2);
            m.setDeltaMovement(d.x, 0.4, d.z);
            m.hurtMarked = true;
            m.setTarget(null);
        }
        if (w.tickCount % 60 == 0) lv.sendParticles(ParticleTypes.NOTE, w.getX(), w.getY() + 1.6, w.getZ(), 1, 0.3, 0.1, 0.3, 1);
    }

    static void wake(WaifuEntity w, @Nullable Player p) {
        w.setSitting(false);
        w.sleeper = null;
        w.setMode(w.modeBeforeSleep);
        if (p instanceof ServerPlayer sp) {
            Fx.send(sp, Fx.SLEEP_OFF, w.getId());
            if (p.isSleeping()) sp.stopSleepInBed(false, true);
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            p.addEffect(new MobEffectInstance(MobEffects.SATURATION, 10, 0));
            w.say(p, p.level().isDay() ? "¡Buenos días, dormilón! ♥ Te cuidé toda la noche." : "¿Ya te levantas? Aquí estaré cuando tengas sueño ♥");
            w.cheer(80);
        }
    }

    /** ¿Este jugador duerme en las piernas de una Yume? (sirve en el cliente y en el servidor) */
    @Nullable
    public static WaifuEntity lapOf(Player p) {
        for (WaifuEntity w : p.level().getEntitiesOfClass(WaifuEntity.class, p.getBoundingBox().inflate(2.0), w -> w.kind == WaifuKind.CAMA && w.isSitting())) return w;
        return null;
    }
}
