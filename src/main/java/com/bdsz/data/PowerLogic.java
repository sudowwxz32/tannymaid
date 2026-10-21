package com.bdsz.data;

import com.bdsz.entity.KiBlastEntity;
import com.bdsz.registry.ModRegistry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/** Lógica de poder (servidor): transformaciones, atributos, ki, tiempo límite, vuelo y ráfagas de ki. */
public final class PowerLogic {
    private static final UUID SPEED_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c01");
    private static final UUID KB_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c02");
    private static final UUID HEALTH_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c03");
    private static final UUID AGILITY_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c04");

    public static final int BLAST_COST = 12;
    public static final int FLIGHT_COST = 2;      // ki por segundo volando
    public static final int TRANSFORM_COST = 15;
    private static final int EXHAUST_TICKS = 60 * 20;

    private PowerLogic() {}

    public static void msg(ServerPlayer p, String text) {
        p.displayClientMessage(Component.literal(text), true);
    }

    // ------------------------------------------------------------ menú, transformación y aura

    public static void select(ServerPlayer p, int form, int kaioken) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) return;
        if (!s.unlocked(Form.byId(form))) {
            msg(p, "§cEsa fase está bloqueada. Necesitas el Bloque " + Form.byId(form).tier() + " (Lista de Entrenamiento).");
            return;
        }
        Form old = s.activeForm();
        int oldK = s.activeKaioken();
        int oldMax = Math.max(1, s.timerMax);
        s.selected = Form.byId(form).ordinal();
        s.kaioken = Math.max(0, Math.min(100, (kaioken / 10) * 10));
        if (s.transformed && !s.powered()) s.transformed = false;
        if (s.transformed && (old != s.activeForm() || oldK != s.activeKaioken())) {
            // el tiempo que quedaba se conserva en proporción a la nueva fase
            int newMax = SaiyanState.timeLimit(s.selected, s.kaioken);
            s.timer = Math.max(20, (int) ((long) s.timer * newMax / oldMax));
            s.timerMax = newMax;
            SaiyanData.set(p, s);
            burst(p, s, s.activeForm() != old);
            announce(p, s);
        } else {
            SaiyanData.set(p, s);
        }
    }

    public static void toggleTransform(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza sayajin"); return; }
        if (s.transformed) { revert(p, s, "§7Vuelves a tu estado base."); return; }
        if (s.powering > 0) {
            s.powering = 0;
            SaiyanData.set(p, s);
            msg(p, "§7Detienes la transformación.");
            return;
        }
        if (s.selected == 0 && s.kaioken == 0) { msg(p, "§eElige una fase o el Kaioken en el menú (tecla K)."); return; }
        if (!s.unlocked(Form.byId(s.selected))) { msg(p, "§cEsa fase está bloqueada. Desbloquéala con una Lista de Entrenamiento."); return; }
        if (s.cooldown > 0 && !p.isCreative()) {
            msg(p, "§cEstás agotado. Podrás transformarte en " + (s.cooldown / 20) + " s (una semilla del ermitaño te recupera).");
            return;
        }
        if (s.ki < TRANSFORM_COST && !p.isCreative()) { msg(p, "§cNo tienes suficiente ki. Recárgalo con H o comiendo."); return; }
        // empieza la carga dramática; la transformación ocurre al terminarla
        s.charging = false;
        s.poweringTotal = Form.byId(s.selected).powerUpTicks();
        s.powering = s.poweringTotal;
        SaiyanData.set(p, s);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 0.5F);
        msg(p, "§e¡Aaaaah...! §7(cargando poder)");
    }

    /** Cada tick durante la carga: tierra que tiembla, polvo, energía que sube y sonidos cada vez más agudos. */
    public static void tickPowerUp(ServerPlayer p, SaiyanState s) {
        ServerLevel lv = p.serverLevel();
        Form f = Form.byId(s.selected);
        float progress = 1.0F - (float) s.powering / Math.max(1, s.poweringTotal);
        double x = p.getX(), y = p.getY(), z = p.getZ();
        p.setDeltaMovement(0, Math.min(0, p.getDeltaMovement().y), 0);   // queda clavado en el sitio
        p.hurtMarked = true;
        Vector3f col = new Vector3f(f.ar(), f.ag(), f.ab());
        if (f == Form.BASE) col = new Vector3f(1.0F, 0.15F, 0.1F);
        DustParticleOptions dust = new DustParticleOptions(col, 1.5F + progress * 1.5F);
        lv.sendParticles(dust, x, y + 1.0, z, 4 + (int) (progress * 14), 0.35 + progress * 0.6, 0.9 + progress, 0.35 + progress * 0.6, 0.0);
        // el suelo se resquebraja
        net.minecraft.world.level.block.state.BlockState ground = lv.getBlockState(p.blockPosition().below());
        if (!ground.isAir()) {
            double rad = 0.8 + progress * (2.5 + f.ordinal() * 0.4);
            for (int i = 0; i < 3 + (int) (progress * 8); i++) {
                double a = lv.random.nextDouble() * Math.PI * 2;
                lv.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground),
                        x + Math.cos(a) * rad, y + 0.1, z + Math.sin(a) * rad, 1, 0.1, 0.2, 0.1, 0.15 + progress * 0.3);
            }
        }
        if (s.powering % 6 == 0) {
            lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.8F + progress, 0.5F + progress * 1.3F);
            lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.5F + progress * 0.8F, 0.8F + progress * 0.8F);
        }
        if (progress > 0.4F && s.powering % 9 == 0) {
            lv.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 1.2, z, 15 + (int) (progress * 30), 0.6, 1.0, 0.6, 0.5);
            lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.2F + progress * 0.3F, 0.5F);
        }
        if (progress > 0.55F && f.ordinal() >= Form.FALSE_SSJ.ordinal() && f.ordinal() <= Form.SSJ3.ordinal()
                && lv.random.nextInt(Math.max(3, 14 - f.ordinal() * 2)) == 0) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
            if (bolt != null) {
                bolt.setVisualOnly(true);
                bolt.moveTo(x + (lv.random.nextDouble() - 0.5) * 10, y, z + (lv.random.nextDouble() - 0.5) * 10);
                lv.addFreshEntity(bolt);
            }
        }
        if (--s.powering <= 0) {
            completeTransform(p, s);
        } else if (s.powering % 10 == 0) {
            SaiyanData.sync(p, s);
            root(p, s);
        } else {
            root(p, s);
        }
    }

    private static void root(ServerPlayer p, SaiyanState s) {
        p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG)
                .put("BlockDragonSuperZ", s.save());
    }

    private static void completeTransform(ServerPlayer p, SaiyanState s) {
        s.powering = 0;
        if (!p.isCreative()) s.ki = Math.max(0, s.ki - TRANSFORM_COST);
        s.transformed = true;
        s.totemReady = true;
        s.timerMax = SaiyanState.timeLimit(s.selected, s.kaioken);
        s.timer = s.timerMax;
        SaiyanData.set(p, s);
        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + s.extraHealth()));   // los corazones nuevos llegan llenos
        burst(p, s, true);
        announce(p, s);
    }

    // ------------------------------------------------------------ recarga de ki (H)

    /** H: la primera pulsación empieza a recargar, la segunda la detiene (también se detiene sola con el ki lleno). */
    public static void toggleCharging(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza sayajin"); return; }
        if (s.powering > 0) return;
        if (!s.charging && s.ki >= s.maxKi()) { msg(p, "§bTu ki ya está al máximo."); return; }
        setCharging(p, !s.charging);
    }

    public static void setCharging(ServerPlayer p, boolean on) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan || s.powering > 0 || s.charging == on) return;
        s.charging = on;
        SaiyanData.set(p, s);
        if (on) {
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 0.6F);
            msg(p, "§b⚡ Recargando ki... §7(H para detener)");
        } else {
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.7F, 1.0F);
        }
    }

    public static void tickCharging(ServerPlayer p, SaiyanState s) {
        ServerLevel lv = p.serverLevel();
        Form f = s.visibleForm();
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 3, true, false, false));
        int max = s.maxKi();
        if (p.tickCount % 2 == 0 && s.ki < max) s.ki = Math.min(max, s.ki + Math.max(1, max / 160));
        Vector3f col = f == Form.BASE ? new Vector3f(0.75F, 0.9F, 1.0F) : new Vector3f(f.ar(), f.ag(), f.ab());
        lv.sendParticles(new DustParticleOptions(col, 1.6F), p.getX(), p.getY() + 0.9, p.getZ(), 6, 0.6, 0.9, 0.6, 0.0);
        net.minecraft.world.level.block.state.BlockState ground = lv.getBlockState(p.blockPosition().below());
        if (!ground.isAir() && p.tickCount % 2 == 0) {
            double a = lv.random.nextDouble() * Math.PI * 2;
            lv.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground),
                    p.getX() + Math.cos(a) * 1.4, p.getY() + 0.1, p.getZ() + Math.sin(a) * 1.4, 2, 0.1, 0.1, 0.1, 0.2);
        }
        if (p.tickCount % 16 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.0F, 0.7F + (float) s.ki / max * 0.6F);
        if (s.ki >= max) {
            // ki lleno: la recarga termina sola
            s.ki = max;
            s.charging = false;
            SaiyanData.set(p, s);
            lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.4F);
            msg(p, "§b¡Ki al máximo!");
            return;
        }
        if (p.tickCount % 10 == 0) SaiyanData.setQuiet(p, s);
        else root(p, s);
    }

    public static void toggleAura(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza sayajin"); return; }
        s.aura = !s.aura;
        SaiyanData.set(p, s);
        p.serverLevel().playSound(null, p.blockPosition(), s.aura ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.8F, s.aura ? 1.4F : 1.0F);
        msg(p, s.aura ? "§bAura activada" : "§7Aura desactivada");
    }

    public static void toggleFlight(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza sayajin"); return; }
        if (!s.flight && s.ki < FLIGHT_COST * 3 && !p.isCreative()) { msg(p, "§cNo tienes ki para volar. Come algo."); return; }
        s.flight = !s.flight;
        SaiyanData.set(p, s);
        applyFlight(p, s);
        if (s.flight) {
            p.getAbilities().flying = true;
            p.setDeltaMovement(p.getDeltaMovement().add(0, 0.5, 0));
            p.hurtMarked = true;
            p.onUpdateAbilities();
            p.serverLevel().sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY(), p.getZ(), 15, 0.5, 0.1, 0.5, 0.05);
        }
        msg(p, s.flight ? "§bVuelo activado §7(espacio sube, shift baja, I para aterrizar)" : "§7Vuelo desactivado");
    }

    public static void applyFlight(ServerPlayer p, SaiyanState s) {
        boolean creative = p.isCreative() || p.isSpectator();
        if (s.saiyan && s.flight) {
            p.getAbilities().mayfly = true;
            p.getAbilities().setFlyingSpeed(s.flySpeed());
        } else {
            p.getAbilities().setFlyingSpeed(0.05F);
            if (!creative) {
                p.getAbilities().mayfly = false;
                p.getAbilities().flying = false;
            }
        }
        p.onUpdateAbilities();
    }

    public static void revert(ServerPlayer p, SaiyanState s, String reason) {
        s.transformed = false;
        s.powering = 0;
        s.timer = 0;
        s.ki = Math.min(s.ki, s.maxKi());
        SaiyanData.set(p, s);
        applyFlight(p, s);
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 1, p.getZ(), 25, 0.4, 0.6, 0.4, 0.05);
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 0.8F);
        msg(p, reason);
    }

    private static void announce(ServerPlayer p, SaiyanState s) {
        StringBuilder sb = new StringBuilder();
        Form f = s.activeForm();
        if (f != Form.BASE) sb.append("§6§l").append(f.display);
        if (s.activeKaioken() > 0) sb.append(sb.length() > 0 ? " §r§c+ " : "§c§l").append("Kaioken x").append(s.activeKaioken());
        sb.append(String.format(" §r§7(fuerza x%.1f, +%d ❤)", s.damageMultiplier(), s.extraHealth() / 2));
        msg(p, sb.toString());
    }

    // ------------------------------------------------------------ ráfaga de ki

    public static void kiBlast(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza sayajin"); return; }
        long now = p.level().getGameTime();
        if (now - p.getPersistentData().getLong("bdsz_last_blast") < 8) return;
        p.getPersistentData().putLong("bdsz_last_blast", now);
        if (s.ki < BLAST_COST && !p.isCreative()) { msg(p, "§cNo tienes suficiente ki. Come algo para recuperarlo."); return; }
        if (!p.isCreative()) s.ki -= BLAST_COST;
        SaiyanData.setQuiet(p, s);
        Form f = s.activeForm();
        int color = f == Form.BASE ? (s.activeKaioken() > 0 ? 0xFF3020 : 0x8FE8FF) : f.color;
        float size = 0.6F + f.ordinal() * 0.12F + s.activeKaioken() / 250.0F;
        float damage = 8.0F * s.damageMultiplier();
        KiBlastEntity blast = new KiBlastEntity(ModRegistry.KI_BLAST.get(), p.level());
        blast.setOwner(p);
        Vec3 look = p.getLookAngle();
        blast.setPos(p.getX() + look.x * 0.8, p.getEyeY() - 0.25 + look.y * 0.8, p.getZ() + look.z * 0.8);
        blast.setup(color, size, damage);
        blast.shoot(look.x, look.y, look.z, 1.8F, 0.0F);
        p.level().addFreshEntity(blast);
        p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 1.5F);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6F, 1.8F);
    }

    // ------------------------------------------------------------ efectos visuales al transformarse

    public static void burst(ServerPlayer p, SaiyanState s, boolean big) {
        ServerLevel lv = p.serverLevel();
        Form f = s.activeForm();
        boolean kaio = s.activeKaioken() > 0;
        Vector3f col = f == Form.BASE && kaio ? new Vector3f(1.0F, 0.15F, 0.1F) : new Vector3f(f.ar(), f.ag(), f.ab());
        DustParticleOptions dust = new DustParticleOptions(col, 2.0F);
        double x = p.getX(), y = p.getY(), z = p.getZ();
        int rings = big ? 2 + f.ordinal() / 2 : 1;
        for (int r = 0; r < rings; r++) {
            double rad = 1.2 + r * 1.1;
            for (int i = 0; i < 48; i++) {
                double a = i * Math.PI * 2 / 48;
                lv.sendParticles(dust, x + Math.cos(a) * rad, y + 0.15 + r * 0.05, z + Math.sin(a) * rad, 1, 0.05, 0.1, 0.05, 0.0);
            }
        }
        lv.sendParticles(dust, x, y + 1.5, z, 160 + f.ordinal() * 40, 0.4, 2.2 + f.ordinal() * 0.5, 0.4, 0.0);   // columna de luz
        if (big) {
            // pilar de energía que sube al cielo
            for (int i = 0; i < 12 + f.ordinal() * 4; i++) {
                lv.sendParticles(dust, x, y + 2 + i * 0.8, z, 6, 0.25, 0.3, 0.25, 0.0);
            }
            lv.sendParticles(ParticleTypes.END_ROD, x, y + 1, z, 30 + f.ordinal() * 10, 0.2, 0.2, 0.2, 0.35 + f.ordinal() * 0.05);
        }
        lv.sendParticles(ParticleTypes.FLASH, x, y + 1, z, 1, 0, 0, 0, 0);
        if (big) {
            lv.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5, z, 5, 1.2, 0.3, 1.2, 0.0);
            lv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.1, z, 20, 2.0, 0.05, 2.0, 0.02);
        }
        if (kaio) lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 2.4F), x, y + 1.1, z, 90, 0.6, 1.3, 0.6, 0.0);
        switch (f) {
            case SSJ2, SSJ3, BLUE -> lv.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 1.2, z, 80, 0.7, 1.2, 0.7, 0.5);
            case ULTRA -> {
                lv.sendParticles(ParticleTypes.END_ROD, x, y + 1.2, z, 120, 1.0, 1.4, 1.0, 0.08);
                lv.sendParticles(ParticleTypes.SONIC_BOOM, x, y + 1, z, 1, 0, 0, 0, 0);
            }
            case GOD -> lv.sendParticles(ParticleTypes.FLAME, x, y + 0.5, z, 80, 0.5, 1.6, 0.5, 0.04);
            default -> { }
        }
        if (big && f.ordinal() >= Form.FALSE_SSJ.ordinal() && f.ordinal() <= Form.SSJ3.ordinal()) {
            int bolts = f == Form.SSJ3 ? 4 : f == Form.SSJ2 ? 3 : 1;
            for (int i = 0; i < bolts; i++) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
                if (bolt != null) {
                    bolt.setVisualOnly(true);
                    bolt.moveTo(x + (lv.random.nextDouble() - 0.5) * 7, y, z + (lv.random.nextDouble() - 0.5) * 7);
                    lv.addFreshEntity(bolt);
                }
            }
        }
        // sonidos por capas, cada fase con su propio "rugido"
        net.minecraft.core.BlockPos bp = p.blockPosition();
        lv.playSound(null, bp, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2F, 0.7F + f.ordinal() * 0.08F);
        if (big) {
            lv.playSound(null, bp, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8F, 0.9F);
            lv.playSound(null, bp, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
        switch (f) {
            case FALSE_SSJ, SSJ1 -> lv.playSound(null, bp, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6F, 1.4F);
            case SSJ2 -> {
                lv.playSound(null, bp, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.8F, 1.2F);
                lv.playSound(null, bp, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            case SSJ3 -> {
                lv.playSound(null, bp, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 0.8F);
                lv.playSound(null, bp, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 0.7F);
            }
            case GOD -> {
                lv.playSound(null, bp, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 1.2F, 0.8F);
                lv.playSound(null, bp, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.6F);
            }
            case BLUE -> {
                lv.playSound(null, bp, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8F, 1.3F);
                lv.playSound(null, bp, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.2F, 0.8F);
            }
            case ULTRA -> {
                lv.playSound(null, bp, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 2.0F, 0.5F);
                lv.playSound(null, bp, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6F, 1.8F);
                lv.playSound(null, bp, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.5F, 0.6F);
            }
            default -> { }
        }
        if (kaio) {
            lv.playSound(null, bp, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.6F);
            lv.playSound(null, bp, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.5F);
        }
        if (big) {
            double push = 1.0 + f.ordinal() * 0.2;
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(5.0D + f.ordinal()),
                    e -> e != p && e.isAlive() && !(e instanceof Player))) {
                Vec3 d = e.position().subtract(p.position()).normalize();
                e.push(d.x * push, 0.4, d.z * push);
                e.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------ atributos

    public static void applyAttributes(ServerPlayer p, SaiyanState s) {
        boolean on = s.saiyan && s.powered();
        setMod(p.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID, "bdsz_speed", on ? s.speedBonus() : 0, AttributeModifier.Operation.MULTIPLY_BASE);
        setMod(p.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KB_ID, "bdsz_kb", on ? Math.min(1.0, s.resistance()) : 0, AttributeModifier.Operation.ADDITION);
        setMod(p.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID, "bdsz_health", on ? s.extraHealth() : 0, AttributeModifier.Operation.ADDITION);
        setMod(p.getAttribute(Attributes.ATTACK_SPEED), AGILITY_ID, "bdsz_agility", on ? s.agility() : 0, AttributeModifier.Operation.ADDITION);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
        if (s.flight) p.getAbilities().setFlyingSpeed(s.flySpeed());
    }

    private static void setMod(AttributeInstance inst, UUID id, String name, double amount, AttributeModifier.Operation op) {
        if (inst == null) return;
        inst.removeModifier(id);
        if (amount != 0) inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
    }

    // ------------------------------------------------------------ cada segundo

    public static void secondTick(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) return;
        boolean changed = false;
        if (s.cooldown > 0) { s.cooldown = Math.max(0, s.cooldown - 20); changed = true; }

        if (s.powered()) {
            Form f = s.activeForm();
            int jump = Math.min(5, f.jump + (s.activeKaioken() >= 50 ? 1 : 0));
            if (jump > 0) p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, jump - 1, true, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false));          // visión
            p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false, false));      // respiración acuática
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, false));      // resistencia al fuego
            // habilidades de la manzana de Notch, más fuertes en cada fase
            int res = f == Form.ULTRA ? 2 : f.ordinal() >= Form.GOD.ordinal() ? 1 : f.ordinal() >= Form.SSJ2.ordinal() ? 0 : -1;
            if (res >= 0) p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, res, true, false, false));
            int abs = f == Form.ULTRA || f == Form.BLUE ? 3 : f == Form.GOD ? 2 : f == Form.SSJ3 ? 1 : f.ordinal() >= Form.SSJ1.ordinal() ? 0 : -1;
            if (abs >= 0 && p.tickCount % 400 == 0) p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, abs, true, false, true));
            if (f.ordinal() >= Form.SSJ3.ordinal() && f != Form.ULTRA && f != Form.BLUE) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, false));
            if (f.ordinal() >= Form.GOD.ordinal()) p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, f == Form.ULTRA ? 2 : 1, true, false, false));
            if (f == Form.ULTRA || f == Form.BLUE) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, f == Form.ULTRA ? 1 : 0, true, false, false));
            if (!p.isCreative()) {
                s.timer -= 20;
                changed = true;
                if (s.timer <= 0) {
                    s.cooldown = EXHAUST_TICKS;
                    revert(p, s, "§cSe acabó el tiempo de tu transformación. Estás agotado por 60 s.");
                    return;
                }
            }
        }

        // vuelo: gasta ki mientras estás en el aire
        if (s.flight && p.getAbilities().flying && !p.isCreative() && !p.isSpectator()) {
            s.ki -= FLIGHT_COST;
            changed = true;
            if (s.ki <= 0) {
                s.ki = 0;
                s.flight = false;
                SaiyanData.set(p, s);
                applyFlight(p, s);
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, true, false, true));
                msg(p, "§cTe quedaste sin ki y dejas de volar.");
                return;
            }
        } else if (s.flight && !p.getAbilities().mayfly) {
            applyFlight(p, s);
        }
        if (s.ki > s.maxKi()) { s.ki = s.maxKi(); changed = true; }
        if (changed) SaiyanData.setQuiet(p, s);
    }

    /** Usar una Lista de Entrenamiento: desbloquea el siguiente bloque de fases. */
    public static boolean unlockTier(ServerPlayer p, int targetTier) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cPrimero usa /raza sayajin"); return false; }
        if (s.tier >= targetTier) { msg(p, "§7Ya desbloqueaste el Bloque " + targetTier + "."); return false; }
        if (s.tier < targetTier - 1) { msg(p, "§cPrimero debes desbloquear el Bloque " + (targetTier - 1) + "."); return false; }
        s.tier = targetTier;
        SaiyanData.set(p, s);
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 80, 0.6, 1.0, 0.6, 0.4);
        lv.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 40, 0.4, 1.2, 0.4, 0.15);
        lv.playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.2F);
        StringBuilder names = new StringBuilder();
        for (Form f : Form.values()) {
            if (f.tier() == targetTier) names.append(names.length() > 0 ? ", " : "").append(f.display);
        }
        p.sendSystemMessage(Component.literal("§6§l¡Bloque " + targetTier + " desbloqueado! §r§e" + names));
        p.sendSystemMessage(Component.literal("§7Elígelas en el menú (K) y transfórmate con P."));
        return true;
    }

    /** Efecto de tótem: si mueres transformado, sobrevives una vez por transformación. */
    public static boolean totemSave(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan || !s.powered() || !s.totemReady) return false;
        s.totemReady = false;
        SaiyanData.set(p, s);
        p.setHealth(Math.max(4.0F, p.getMaxHealth() * 0.25F));
        p.removeAllEffects();
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        p.level().broadcastEntityEvent(p, (byte) 35);     // animación del tótem
        msg(p, "§6¡Tu poder saiyajin te salvó de la muerte!");
        return true;
    }

    /** Comer recupera ki (más con comida más nutritiva). */
    public static void onAte(ServerPlayer p, int nutrition, float saturation) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) return;
        int gain = (int) (nutrition * 12 + saturation * 4);
        if (gain <= 0 || s.ki >= s.maxKi()) return;
        s.ki = Math.min(s.maxKi(), s.ki + gain);
        SaiyanData.setQuiet(p, s);
        msg(p, "§b+" + gain + " ki");
    }

    /** Semilla del ermitaño: todo al máximo y cura todo. */
    public static void senzu(ServerPlayer p) {
        p.removeAllEffects();
        p.clearFire();
        p.setHealth(p.getMaxHealth());
        p.getFoodData().setFoodLevel(20);
        p.getFoodData().setSaturation(20.0F);
        p.setAirSupply(p.getMaxAirSupply());
        SaiyanState s = SaiyanData.get(p);
        if (s.saiyan) {
            s.ki = s.maxKi();
            s.cooldown = 0;
            if (s.transformed) s.timer = s.timerMax;
            SaiyanData.set(p, s);
        }
        p.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 25, 0.5, 0.8, 0.5, 0.0);
        p.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.3);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.6F);
        msg(p, "§a¡Te sientes completamente renovado!");
    }
}
