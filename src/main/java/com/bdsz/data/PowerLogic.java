package com.bdsz.data;

import com.bdsz.entity.KiBlastEntity;
import com.bdsz.entity.KameBeamEntity;
import com.bdsz.registry.ModRegistry;
import net.minecraft.core.BlockPos;
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
    private static final UUID STEP_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c05");
    private static final UUID REACH_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c06");
    private static final UUID BREACH_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c07");

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
            msg(p, "§cEsa fase está bloqueada. Necesitas el " + Form.blockName(Form.byId(form).tier()) + " (Lista de Entrenamiento).");
            return;
        }
        Form old = s.activeForm();
        int oldK = s.activeKaioken();
        int oldMax = s.timerMax;
        s.selected = Form.byId(form).ordinal();
        s.kaioken = Math.max(0, Math.min(100, (kaioken / 10) * 10));
        if (s.transformed && !s.powered()) s.transformed = false;
        if (s.transformed && (old != s.activeForm() || oldK != s.activeKaioken())) {
            // el tiempo que quedaba se conserva en proporción a la nueva fase
            int newMax = SaiyanState.timeLimit(s.selected, s.kaioken, s.mastery);
            if (newMax < 0) s.timer = -1;
            else if (oldMax <= 0) s.timer = newMax;
            else s.timer = Math.max(20, (int) ((long) s.timer * newMax / oldMax));
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
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza saiyajin"); return; }
        if (s.ozaru) { msg(p, "§cPrimero sal del Modo Ozaru (tecla O)."); return; }
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

    /** Cada tick durante la carga: tierra que tiembla, polvo, escombros, energía que sube y sonidos cada vez más agudos. */
    public static void tickPowerUp(ServerPlayer p, SaiyanState s) {
        ServerLevel lv = p.serverLevel();
        Form f = Form.byId(s.selected);
        float progress = 1.0F - (float) s.powering / Math.max(1, s.poweringTotal);
        double x = p.getX(), y = p.getY(), z = p.getZ();
        p.setDeltaMovement(0, Math.min(0, p.getDeltaMovement().y), 0);   // queda clavado en el sitio
        p.hurtMarked = true;
        if (f.calm()) {
            calmPowerUp(p, s, lv, progress);
        } else {
            dramaticPowerUp(p, s, lv, f, progress, x, y, z);
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

    /** Ultra Instinto: silencio, calma y un brillo plateado que gira despacio. */
    private static void calmPowerUp(ServerPlayer p, SaiyanState s, ServerLevel lv, float progress) {
        double a = s.powering * 0.35;
        for (int i = 0; i < 3; i++) {
            double ang = a + i * Math.PI * 2 / 3;
            double rad = 1.1 - progress * 0.5;
            lv.sendParticles(ParticleTypes.END_ROD, p.getX() + Math.cos(ang) * rad, p.getY() + 0.2 + progress * 1.6 + i * 0.1,
                    p.getZ() + Math.sin(ang) * rad, 1, 0, 0, 0, 0.0);
        }
        lv.sendParticles(new DustParticleOptions(new Vector3f(0.86F, 0.9F, 1.0F), 0.8F), p.getX(), p.getY() + 1.0, p.getZ(), 2, 0.35, 0.7, 0.35, 0.0);
        if (s.powering % 14 == 0) {
            lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 0.6F + progress * 0.6F);
        }
        if (s.powering % 20 == 0) {
            lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.4F, 1.4F);
        }
    }

    private static void dramaticPowerUp(ServerPlayer p, SaiyanState s, ServerLevel lv, Form f, float progress, double x, double y, double z) {
        float drama = Math.max(0.3F, f.drama());
        Vector3f col = f == Form.BASE ? new Vector3f(1.0F, 0.15F, 0.1F) : new Vector3f(f.ar(), f.ag(), f.ab());
        int oc = f == Form.BASE ? 0x7A0A06 : f.outerColor();
        Vector3f outer = new Vector3f(((oc >> 16) & 255) / 255F, ((oc >> 8) & 255) / 255F, (oc & 255) / 255F);
        DustParticleOptions dust = new DustParticleOptions(col, 1.5F + progress * 1.5F);
        lv.sendParticles(dust, x, y + 1.0, z, 4 + (int) (progress * 14 * drama), 0.35 + progress * 0.6, 0.9 + progress, 0.35 + progress * 0.6, 0.0);
        // lenguas de fuego de ki: del color del aura al color exterior
        net.minecraft.core.particles.DustColorTransitionOptions fire = new net.minecraft.core.particles.DustColorTransitionOptions(col, outer, 1.8F + drama);
        for (int i = 0; i < 2 + (int) (progress * 6 * drama); i++) {
            double a = lv.random.nextDouble() * Math.PI * 2;
            double r = 0.5 + lv.random.nextDouble() * 0.5;
            lv.sendParticles(fire, x + Math.cos(a) * r, y + lv.random.nextDouble() * 0.6, z + Math.sin(a) * r, 0, 0, 1, 0, 0.35 + progress * 0.4);
        }
        // el suelo se resquebraja y levanta polvo
        net.minecraft.world.level.block.state.BlockState ground = com.bdsz.entity.DebrisEntity.groundAt(lv, p.blockPosition());
        double rad = 0.8 + progress * (2.5 + drama * 3.0);
        for (int i = 0; i < 3 + (int) (progress * 10 * drama); i++) {
            double a = lv.random.nextDouble() * Math.PI * 2;
            lv.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground),
                    x + Math.cos(a) * rad, y + 0.1, z + Math.sin(a) * rad, 1, 0.1, 0.2, 0.1, 0.15 + progress * 0.3);
        }
        if (s.powering % 3 == 0) {
            // anillo de polvo que se expande desde los pies
            for (int i = 0; i < 10; i++) {
                double a = i * Math.PI / 5 + s.powering;
                lv.sendParticles(ParticleTypes.POOF, x + Math.cos(a) * rad * 0.8, y + 0.1, z + Math.sin(a) * rad * 0.8, 0,
                        Math.cos(a), 0.05, Math.sin(a), 0.15 + drama * 0.15);
            }
            if (drama > 0.6F) lv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.1, z, 2, rad * 0.5, 0.05, rad * 0.5, 0.01);
        }
        // rocas que se despegan del suelo y levitan alrededor hasta que termina la transformación
        if (drama >= 0.5F && progress > 0.1F && progress < 0.85F && s.powering % Math.max(3, (int) (10 - drama * 6)) == 0) {
            double a = lv.random.nextDouble() * Math.PI * 2;
            double r = 1.8 + lv.random.nextDouble() * (1.5 + drama * 1.5);
            com.bdsz.entity.DebrisEntity.levitate(lv, new Vec3(x + Math.cos(a) * r, y + 0.1, z + Math.sin(a) * r), ground,
                    s.powering + 10, 0.25F + lv.random.nextFloat() * 0.35F * drama);
        }
        // escombros que salen volando del suelo
        int every = Math.max(2, (int) (9 - drama * 6));
        if (progress > 0.15F && s.powering % every == 0) {
            int n = 1 + (int) (drama * 2.5F * progress);
            for (int i = 0; i < n; i++) {
                double a = lv.random.nextDouble() * Math.PI * 2;
                double r = 1.0 + lv.random.nextDouble() * (1.5 + drama * 1.5);
                com.bdsz.entity.DebrisEntity.launch(lv, new Vec3(x + Math.cos(a) * r, y + 0.2, z + Math.sin(a) * r), ground,
                        Math.cos(a) * 0.08, 0.35 + lv.random.nextDouble() * 0.45 * drama + progress * 0.2, Math.sin(a) * 0.08,
                        0.18F + lv.random.nextFloat() * 0.3F * drama);
            }
        }
        if (s.powering % 6 == 0) {
            lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.8F + progress, 0.5F + progress * 1.3F);
            lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.5F + progress * 0.8F, 0.8F + progress * 0.8F);
        }
        if (s.powering % 8 == 0 && drama > 0.5F) {
            lv.playSound(null, p.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.6F + progress, 0.6F);
        }
        if (progress > 0.4F && s.powering % 9 == 0) {
            lv.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 1.2, z, 15 + (int) (progress * 30), 0.6, 1.0, 0.6, 0.5);
            lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.2F + progress * 0.3F, 0.5F);
        }
        if (f.legendary3()) {
            // ciclón: la energía y el polvo giran y suben hacia el cielo
            for (int i = 0; i < 6; i++) {
                double h = lv.random.nextDouble() * (4 + progress * 30);
                double ang = s.powering * 0.4 + h * 0.45 + i * 1.05;
                double r = 0.9 + Math.pow(h / 40.0, 1.3) * 5.5;
                lv.sendParticles(new DustParticleOptions(new Vector3f(0.35F, 1.0F, 0.35F), 2.2F), x + Math.cos(ang) * r, y + h, z + Math.sin(ang) * r, 1, 0, 0, 0, 0);
            }
            if (s.powering % 12 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.5F, 0.5F + progress);
        }
        if (f == Form.BEAST || f == Form.LEGENDARY || f == Form.ROSE) {
            if (s.powering % 15 == 0) {
                lv.playSound(null, p.blockPosition(), f == Form.LEGENDARY ? SoundEvents.RAVAGER_ROAR : SoundEvents.WARDEN_ROAR,
                        SoundSource.PLAYERS, 0.7F + progress, f == Form.ROSE ? 1.2F : 0.7F);
            }
        }
        if (progress > 0.55F && (f.lightningAura() || (f.ordinal() >= Form.FALSE_SSJ.ordinal() && f.ordinal() <= Form.SSJ3.ordinal()) || f == Form.SSJ4)
                && lv.random.nextInt(Math.max(3, 14 - (int) (drama * 10))) == 0) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
            if (bolt != null) {
                bolt.setVisualOnly(true);
                bolt.moveTo(x + (lv.random.nextDouble() - 0.5) * 10, y, z + (lv.random.nextDouble() - 0.5) * 10);
                lv.addFreshEntity(bolt);
            }
        }
    }

    private static void root(ServerPlayer p, SaiyanState s) {
        p.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG)
                .put("BlockDragonSuperZ", s.save());
    }

    private static void completeTransform(ServerPlayer p, SaiyanState s) {
        s.powering = 0;
        if (Form.byId(s.selected).legendary3()) legendaryBlast(p);
        // las rocas que levitaban salen disparadas con la explosión de poder
        for (com.bdsz.entity.DebrisEntity d : p.serverLevel().getEntitiesOfClass(com.bdsz.entity.DebrisEntity.class,
                p.getBoundingBox().inflate(8.0), com.bdsz.entity.DebrisEntity::isFloating)) {
            d.blast(p.position(), 0.6 + Form.byId(s.selected).drama() * 0.6);
        }
        if (!p.isCreative()) s.ki = Math.max(0, s.ki - TRANSFORM_COST);
        s.transformed = true;
        s.totemReady = true;
        s.timerMax = SaiyanState.timeLimit(s.selected, s.kaioken, s.mastery);
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
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza saiyajin"); return; }
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
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza saiyajin"); return; }
        s.aura = !s.aura;
        SaiyanData.set(p, s);
        p.serverLevel().playSound(null, p.blockPosition(), s.aura ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.8F, s.aura ? 1.4F : 1.0F);
        msg(p, s.aura ? "§bAura activada" : "§7Aura desactivada");
    }

    public static void toggleFlight(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza saiyajin"); return; }
        if (s.ozaru && !s.flight) { msg(p, "§cEl Ozaru es demasiado pesado para volar."); return; }
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
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza saiyajin"); return; }
        long now = p.level().getGameTime();
        if (now - p.getPersistentData().getLong("bdsz_last_blast") < 8) return;
        p.getPersistentData().putLong("bdsz_last_blast", now);
        if (s.ki < BLAST_COST && !p.isCreative()) { msg(p, "§cNo tienes suficiente ki. Come algo para recuperarlo."); return; }
        if (!p.isCreative()) s.ki -= BLAST_COST;
        SaiyanData.setQuiet(p, s);
        Form f = s.activeForm();
        int color = s.ozaru ? 0xC070FF : f == Form.BASE ? (s.activeKaioken() > 0 ? 0xFF3020 : 0x8FE8FF) : f.color;
        float size = s.ozaru ? 2.6F : 0.6F + f.ordinal() * 0.12F + s.activeKaioken() / 250.0F;
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
        if (f.calm() && !kaio) {
            // Ultra Instinto: un destello plateado, sereno, sin explosiones
            double x = p.getX(), y = p.getY(), z = p.getZ();
            for (int i = 0; i < 64; i++) {
                double a = i * Math.PI * 2 / 64;
                lv.sendParticles(ParticleTypes.END_ROD, x + Math.cos(a) * 1.6, y + 0.1, z + Math.sin(a) * 1.6, 0, Math.cos(a), 0.02, Math.sin(a), 0.08);
            }
            lv.sendParticles(ParticleTypes.END_ROD, x, y + 1.2, z, 60, 0.6, 1.2, 0.6, 0.02);
            lv.sendParticles(new DustParticleOptions(new Vector3f(0.88F, 0.92F, 1.0F), 1.2F), x, y + 1, z, 60, 0.5, 1.0, 0.5, 0);
            lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 2.0F, 0.5F);
            lv.playSound(null, p.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 0.8F);
            lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0F, 0.7F);
            return;
        }
        if (big) debrisBurst(lv, p, Math.max(0.35F, f.drama()));
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
            case SSJ4 -> {
                lv.sendParticles(ParticleTypes.FLAME, x, y + 0.5, z, 100, 0.8, 1.8, 0.8, 0.05);
                lv.sendParticles(ParticleTypes.LAVA, x, y + 0.5, z, 20, 1.0, 0.5, 1.0, 0.0);
            }
            case ULTRA_EGO -> {
                lv.sendParticles(ParticleTypes.DRAGON_BREATH, x, y + 1.2, z, 150, 1.0, 1.6, 1.0, 0.05);
                lv.sendParticles(ParticleTypes.WITCH, x, y + 1.2, z, 60, 0.8, 1.4, 0.8, 0.1);
            }
            case BEAST -> {
                lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.12F, 0.12F), 2.6F), x, y + 1.2, z, 160, 1.4, 1.8, 1.4, 0);
                lv.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5, z, 60, 1.5, 0.4, 1.5, 0.05);
                lv.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 1.2, z, 120, 1.0, 1.5, 1.0, 0.6);
            }
            case LEGENDARY -> {
                lv.sendParticles(new DustParticleOptions(new Vector3f(0.4F, 1.0F, 0.3F), 3.0F), x, y + 1.2, z, 220, 1.6, 2.2, 1.6, 0);
                lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 0.5, z, 1, 0, 0, 0, 0);
                lv.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y + 1.2, z, 80, 1.5, 1.8, 1.5, 0.1);
            }
            case BLUE_EVO -> {
                lv.sendParticles(ParticleTypes.END_ROD, x, y + 1.2, z, 140, 1.0, 1.6, 1.0, 0.12);
                lv.sendParticles(new DustParticleOptions(new Vector3f(0.25F, 0.5F, 1.0F), 2.6F), x, y + 1.2, z, 160, 1.2, 1.8, 1.2, 0);
                lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 2.0F), x, y + 1.0, z, 100, 1.6, 1.6, 1.6, 0);
            }
            case ROSE -> {
                lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.4F, 0.75F), 2.6F), x, y + 1.2, z, 160, 1.2, 1.8, 1.2, 0);
                lv.sendParticles(new DustParticleOptions(new Vector3f(0.6F, 0.05F, 0.35F), 3.0F), x, y + 1.0, z, 120, 1.8, 1.6, 1.8, 0);
                lv.sendParticles(ParticleTypes.CHERRY_LEAVES, x, y + 1.5, z, 80, 1.5, 1.5, 1.5, 0.05);
            }
            default -> { }
        }
        if (big && ((f.ordinal() >= Form.FALSE_SSJ.ordinal() && f.ordinal() <= Form.SSJ3.ordinal()) || f.tier() == 6)) {
            int bolts = f.tier() == 6 ? 6 : f == Form.SSJ3 ? 4 : f == Form.SSJ2 ? 3 : 1;
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
            case BLUE_EVO -> {
                lv.playSound(null, bp, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 2.0F, 1.2F);
                lv.playSound(null, bp, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.9F, 1.1F);
                lv.playSound(null, bp, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.3F);
            }
            case BLUE -> {
                lv.playSound(null, bp, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8F, 1.3F);
                lv.playSound(null, bp, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.2F, 0.8F);
            }
            case SSJ4 -> {
                lv.playSound(null, bp, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 0.6F);
                lv.playSound(null, bp, SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.2F, 0.7F);
                lv.playSound(null, bp, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 0.6F);
            }
            case ULTRA_EGO -> {
                lv.playSound(null, bp, SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.2F, 0.8F);
                lv.playSound(null, bp, SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 0.5F);
                lv.playSound(null, bp, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
            case ULTRA -> {
                lv.playSound(null, bp, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 2.0F, 0.5F);
                lv.playSound(null, bp, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.5F, 0.6F);
            }
            case BEAST -> {
                lv.playSound(null, bp, SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 1.5F, 0.6F);
                lv.playSound(null, bp, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2F, 0.5F);
                lv.playSound(null, bp, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 0.6F);
            }
            case LEGENDARY -> {
                lv.playSound(null, bp, SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 2.0F, 0.5F);
                lv.playSound(null, bp, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.5F);
                lv.playSound(null, bp, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 0.5F);
                lv.playSound(null, bp, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2F, 0.6F);
            }
            case ROSE -> {
                lv.playSound(null, bp, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.8F, 1.4F);
                lv.playSound(null, bp, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.1F);
                lv.playSound(null, bp, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.PLAYERS, 1.2F, 1.2F);
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

    /**
     * Legendario Fase 3: el suelo se destroza. Abre un cráter en la tierra y la piedra natural
     * (respeta la regla mobGriefing), con rayos, explosiones y una lluvia de escombros.
     */
    private static void legendaryBlast(ServerPlayer p) {
        ServerLevel lv = p.serverLevel();
        BlockPos c = p.blockPosition().below();
        boolean grief = lv.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING);
        int radius = 5;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius || (d < 1.2)) continue;                         // deja en pie el bloque bajo el jugador
                int depth = d < 2.5 ? 2 : 1;
                for (int dy = 0; dy < depth; dy++) {
                    BlockPos bp = c.offset(dx, -dy, dz);
                    net.minecraft.world.level.block.state.BlockState st = lv.getBlockState(bp);
                    if (st.isAir()) continue;
                    if (lv.random.nextFloat() < 0.35F) {
                        com.bdsz.entity.DebrisEntity.launch(lv, bp.getCenter().add(0, 0.6, 0), st,
                                dx / d * 0.35, 0.6 + lv.random.nextDouble() * 0.6, dz / d * 0.35, 0.4F + lv.random.nextFloat() * 0.4F);
                    }
                    boolean natural = st.is(net.minecraft.tags.BlockTags.DIRT) || st.is(net.minecraft.tags.BlockTags.SAND)
                            || st.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || st.is(net.minecraft.world.level.block.Blocks.GRAVEL)
                            || st.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || st.is(net.minecraft.tags.BlockTags.SNOW);
                    if (grief && natural && !st.hasBlockEntity() && st.getDestroySpeed(lv, bp) >= 0 && st.getDestroySpeed(lv, bp) < 3.0F) {
                        lv.destroyBlock(bp, false);
                    }
                }
            }
        }
        debrisBurst(lv, p, 1.0F);
        debrisBurst(lv, p, 1.0F);
        for (int i = 0; i < 10; i++) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
            if (bolt != null) {
                bolt.setVisualOnly(true);
                bolt.moveTo(p.getX() + (lv.random.nextDouble() - 0.5) * 16, p.getY(), p.getZ() + (lv.random.nextDouble() - 0.5) * 16);
                lv.addFreshEntity(bolt);
            }
        }
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.getX(), p.getY() + 0.5, p.getZ(), 4, 3.0, 0.5, 3.0, 0);
        lv.sendParticles(new DustParticleOptions(new Vector3f(0.3F, 1.0F, 0.3F), 3.5F), p.getX(), p.getY() + 2, p.getZ(), 400, 3.0, 6.0, 3.0, 0);
        for (int i = 0; i < 30; i++) {
            lv.sendParticles(new DustParticleOptions(new Vector3f(0.4F, 1.0F, 0.4F), 3.0F), p.getX(), p.getY() + 2 + i * 1.5, p.getZ(), 8, 0.8 + i * 0.1, 0.5, 0.8 + i * 0.1, 0);
        }
        lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0F, 0.4F);
        lv.playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.5F, 0.6F);
        lv.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.5F, 0.45F);
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(12.0), e -> e != p && e.isAlive())) {
            Vec3 d = e.position().subtract(p.position()).normalize();
            e.push(d.x * 3.0, 1.0, d.z * 3.0);
            e.hurtMarked = true;
        }
        for (ServerPlayer o : lv.players()) {
            if (o.distanceTo(p) < 96) o.sendSystemMessage(Component.literal("§a§l⚡ ").append(p.getDisplayName()).append(Component.literal(" §a§lha despertado el poder legendario.")));
        }
    }

    /** Explosión de escombros y polvo alrededor del jugador (al terminar de transformarse). */
    public static void debrisBurst(ServerLevel lv, ServerPlayer p, float drama) {
        net.minecraft.world.level.block.state.BlockState ground = com.bdsz.entity.DebrisEntity.groundAt(lv, p.blockPosition());
        int n = 10 + (int) (drama * 26);
        for (int i = 0; i < n; i++) {
            double a = lv.random.nextDouble() * Math.PI * 2;
            double r = 0.8 + lv.random.nextDouble() * 1.5;
            double out = 0.15 + lv.random.nextDouble() * 0.35 * drama;
            com.bdsz.entity.DebrisEntity.launch(lv, new Vec3(p.getX() + Math.cos(a) * r, p.getY() + 0.2, p.getZ() + Math.sin(a) * r), ground,
                    Math.cos(a) * out, 0.4 + lv.random.nextDouble() * 0.6 * drama, Math.sin(a) * out, 0.2F + lv.random.nextFloat() * 0.4F * drama);
        }
        // nube de polvo que se expande
        for (int i = 0; i < 36; i++) {
            double a = i * Math.PI * 2 / 36;
            lv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX() + Math.cos(a) * 1.2, p.getY() + 0.2, p.getZ() + Math.sin(a) * 1.2, 0,
                    Math.cos(a), 0.02, Math.sin(a), 0.12 + drama * 0.1);
            lv.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, ground),
                    p.getX() + Math.cos(a) * 2.0, p.getY() + 0.15, p.getZ() + Math.sin(a) * 2.0, 3, 0.3, 0.1, 0.3, 0.3);
        }
        lv.playSound(null, p.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.5F, 0.5F);
        lv.playSound(null, p.blockPosition(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 1.5F, 0.6F);
    }


    // ------------------------------------------------------------ Modo Ozaru (tecla O)

    public static final int OZARU_GROW = 50;

    /** O: te conviertes en un Ozaru gigante; otra vez O para volver a tu tamaño. */
    public static void toggleOzaru(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza saiyajin"); return; }
        ServerLevel lv = p.serverLevel();
        if (s.ozaru) {
            s.ozaru = false;
            SaiyanData.set(p, s);
            p.refreshDimensions();
            lv.sendParticles(ParticleTypes.CLOUD, p.getX(), p.getY() + 1, p.getZ(), 60, 1.0, 1.5, 1.0, 0.08);
            lv.sendParticles(ParticleTypes.POOF, p.getX(), p.getY() + 1, p.getZ(), 40, 1.0, 1.5, 1.0, 0.05);
            lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 0.6F);
            msg(p, "§7Vuelves a tu tamaño normal.");
            return;
        }
        if (!s.ozaruUnlocked()) { msg(p, "§cEl Modo Ozaru se desbloquea con el Dominio Total."); return; }
        if (s.powering > 0) return;
        if (s.transformed) {
            s.transformed = false;
            s.timer = 0;
        }
        s.charging = false;
        s.flight = false;
        s.ozaru = true;
        s.ki = Math.max(s.ki, 300);
        SaiyanData.set(p, s);
        applyFlight(p, s);
        p.refreshDimensions();
        p.setHealth(p.getMaxHealth());
        // rugido, luz de luna llena y una explosión de escombros
        double x = p.getX(), y = p.getY(), z = p.getZ();
        lv.sendParticles(ParticleTypes.FLASH, x, y + 2, z, 2, 0, 0, 0, 0);
        lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.95F, 0.6F), 3.0F), x, y + 3, z, 200, 2.0, 3.0, 2.0, 0);
        lv.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5, z, 80, 2.5, 0.5, 2.5, 0.06);
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1, z, 1, 0, 0, 0, 0);
        debrisBurst(lv, p, 1.0F);
        lv.playSound(null, p.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 3.0F, 0.4F);
        lv.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0F, 0.5F);
        lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS, 2.0F, 0.6F);
        lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.5F);
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(8.0D), e -> e != p && e.isAlive() && !(e instanceof Player))) {
            Vec3 d = e.position().subtract(p.position()).normalize();
            e.push(d.x * 2.0, 0.6, d.z * 2.0);
            e.hurtMarked = true;
        }
        msg(p, "§6§l¡GRRROOAAAARRR! §r§eModo Ozaru §7(O para volver)");
    }

    /** Cada segundo como Ozaru: pisadas que hacen temblar la tierra al correr. */
    public static void ozaruTick(ServerPlayer p, SaiyanState s) {
        p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, true, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 2, true, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
        if (p.onGround() && p.getDeltaMovement().horizontalDistanceSqr() > 0.004) {
            ServerLevel lv = p.serverLevel();
            lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_STEP, SoundSource.PLAYERS, 2.0F, 0.5F);
            net.minecraft.world.level.block.state.BlockState g = com.bdsz.entity.DebrisEntity.groundAt(lv, p.blockPosition());
            lv.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, g), p.getX(), p.getY() + 0.1, p.getZ(), 30, 1.2, 0.1, 1.2, 0.2);
        }
    }

    // ------------------------------------------------------------ atributos

    public static void applyAttributes(ServerPlayer p, SaiyanState s) {
        boolean on = s.saiyan && s.powered();
        setMod(p.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID, "bdsz_speed", on ? s.speedBonus() : 0, AttributeModifier.Operation.MULTIPLY_BASE);
        setMod(p.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KB_ID, "bdsz_kb", on ? Math.min(1.0, s.resistance()) : 0, AttributeModifier.Operation.ADDITION);
        setMod(p.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID, "bdsz_health", on ? s.extraHealth() : 0, AttributeModifier.Operation.ADDITION);
        setMod(p.getAttribute(Attributes.ATTACK_SPEED), AGILITY_ID, "bdsz_agility", on ? s.agility() : 0, AttributeModifier.Operation.ADDITION);
        // Ozaru: pasos enormes y brazos largos
        setMod(p.getAttribute(net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get()), STEP_ID, "bdsz_step", s.ozaru ? 2.5 : 0, AttributeModifier.Operation.ADDITION);
        setMod(p.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_REACH.get()), REACH_ID, "bdsz_reach", s.ozaru ? 5.0 : 0, AttributeModifier.Operation.ADDITION);
        setMod(p.getAttribute(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get()), BREACH_ID, "bdsz_breach", s.ozaru ? 4.0 : 0, AttributeModifier.Operation.ADDITION);
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

        if (s.ozaru) ozaruTick(p, s);
        if (s.powered() && !s.ozaru) {
            Form f = s.activeForm();
            int jump = Math.min(5, f.jump + (s.activeKaioken() >= 50 ? 1 : 0));
            if (jump > 0) p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, jump - 1, true, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false));          // visión
            p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false, false));      // respiración acuática
            p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, false));      // resistencia al fuego
            // habilidades de la manzana de Notch, más fuertes en cada fase
            int res = f == Form.ULTRA || f == Form.LEGENDARY ? 2 : f.ordinal() >= Form.GOD.ordinal() ? 1 : f.ordinal() >= Form.SSJ2.ordinal() ? 0 : -1;
            if (res >= 0) p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, res, true, false, false));
            int abs = f == Form.ULTRA || f == Form.BLUE || f.tier() == 6 ? 3 : f == Form.GOD ? 2 : f == Form.SSJ3 ? 1 : f.ordinal() >= Form.SSJ1.ordinal() ? 0 : -1;
            if (abs >= 0 && p.tickCount % 400 == 0) p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, abs, true, false, true));
            if (f.ordinal() >= Form.SSJ3.ordinal() && f != Form.ULTRA && f != Form.BLUE) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, false));
            if (f.ordinal() >= Form.GOD.ordinal()) p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, f == Form.ULTRA ? 2 : 1, true, false, false));
            if (f == Form.ULTRA || f == Form.BLUE) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, f == Form.ULTRA ? 1 : 0, true, false, false));
            if (!p.isCreative()) {
                if (s.timerMax > 0) s.timer -= 20;
                changed = true;
                if (s.timerMax > 0 && s.timer <= 0) {
                    s.cooldown = EXHAUST_TICKS;
                    revert(p, s, "§cSe acabó el tiempo de tu transformación. Estás agotado por 60 s.");
                    return;
                }
            }
        }

        // vuelo: gasta ki mientras estás en el aire
        if (s.flight && p.getAbilities().flying && !p.isCreative() && !p.isSpectator() && !s.infiniteKi()) {
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
        if (!s.saiyan) { msg(p, "§cPrimero usa /raza saiyajin"); return false; }
        if (targetTier == 6) {
            // Dominio Total
            if (s.tier < 5) { msg(p, "§cPrimero debes desbloquear el Bloque Especial."); return false; }
            if (s.mastery) { msg(p, "§7Ya tienes el Dominio Total."); return false; }
            s.mastery = true;
            if (s.transformed) {
                s.timerMax = SaiyanState.timeLimit(s.selected, s.kaioken, true);
                s.timer = s.timerMax;
            }
            SaiyanData.set(p, s);
            ServerLevel lv = p.serverLevel();
            lv.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 120, 0.8, 1.2, 0.8, 0.5);
            lv.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 80, 0.5, 1.5, 0.5, 0.2);
            lv.playSound(null, p.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 0.8F);
            lv.playSound(null, p.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.2F, 0.6F);
            p.sendSystemMessage(Component.literal("§6§l¡DOMINIO TOTAL! §r§eTus transformaciones ya no tienen límite de tiempo."));
            p.sendSystemMessage(Component.literal("§7(Con Kaioken desde la Fase 3 en adelante el límite es de 40 minutos.)"));
            p.sendSystemMessage(Component.literal(s.tier >= 6 ? "§d§l¡Ya puedes craftear la Lista del Bloque Especial 3! §r§7(fases del Legendario)"
                    : "§7Con la Lista del §dBloque Especial 2§7 desbloqueas Gohan Bestia, Rosé y Legendario Z."));
            p.sendSystemMessage(Component.literal("§6§l¡Modo Ozaru desbloqueado! §r§7Pulsa §eO§7 para convertirte en un mono gigante (otra vez §eO§7 para volver)."));
            return true;
        }
        if (targetTier == 7 || targetTier == 8) {
            // 7 = Lista del Bloque Especial 2 · 8 = Lista del Bloque Especial 3
            int newTier = targetTier - 1;
            if (s.tier >= newTier) { msg(p, "§7Ya desbloqueaste el " + Form.blockName(newTier) + "."); return false; }
            if (s.tier < newTier - 1) { msg(p, "§cPrimero debes desbloquear el " + Form.blockName(newTier - 1) + "."); return false; }
            if (newTier == 7 && !s.mastery) { msg(p, "§cPrimero necesitas el Dominio Total."); return false; }
            targetTier = newTier;
        }
        if (s.tier >= targetTier) { msg(p, "§7Ya desbloqueaste el " + Form.blockName(targetTier) + "."); return false; }
        if (s.tier < targetTier - 1) { msg(p, "§cPrimero debes desbloquear el " + Form.blockName(targetTier - 1) + "."); return false; }
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
        p.sendSystemMessage(Component.literal("§6§l¡" + Form.blockName(targetTier) + " desbloqueado! §r§e" + names));
        p.sendSystemMessage(Component.literal("§7Elígelas en el menú (K) y transfórmate con P."));
        return true;
    }

    // ------------------------------------------------------------ ataques especiales

    public static final int KAME_COST = 80, BIG_BANG_COST = 60, DRAGON_COST = 200;
    public static final int DRAGON_WINDOW = 30 * 20, DRAGON_COOLDOWN = 3 * 60 * 20;

    private static boolean canAttack(ServerPlayer p, SaiyanState s, int cost, String name) {
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza saiyajin"); return false; }
        if (s.powering > 0 || s.pose != 0) return false;
        if (s.attackCooldown > 0) return false;
        if (s.ki < cost && !p.isCreative()) { msg(p, "§cNecesitas " + cost + " de ki para " + name + "."); return false; }
        return true;
    }

    /** R: Kamehameha. Carga con las manos a un lado y luego dispara un rayo continuo. */
    public static void kamehameha(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!canAttack(p, s, KAME_COST, "el Kamehameha")) return;
        if (!p.isCreative()) s.ki -= KAME_COST;
        s.charging = false;
        s.pose = SaiyanState.POSE_KAME_CHARGE;
        s.poseTimer = 26;
        SaiyanData.set(p, s);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2F, 1.6F);
        msg(p, "§bKa... me... ha... me...");
    }

    private static void fireKamehameha(ServerPlayer p, SaiyanState s) {
        KameBeamEntity beam = new KameBeamEntity(ModRegistry.KAME_BEAM.get(), p.level());
        Form f = s.activeForm();
        float width = 0.5F + f.ordinal() * 0.06F + s.activeKaioken() / 400.0F;
        int color = f == Form.ULTRA_EGO ? 0xA060FF : (s.activeKaioken() > 0 ? 0xFF5A5A : 0x5AC8FF);
        beam.setup(p, 6.0F * s.damageMultiplier(), width, color);
        p.level().addFreshEntity(beam);
        s.pose = SaiyanState.POSE_KAME_FIRE;
        s.poseTimer = KameBeamEntity.DURATION;
        s.attackCooldown = 40;
        SaiyanData.set(p, s);
        ServerLevel lv = p.serverLevel();
        lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.2F, 1.4F);
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.8F);
        msg(p, "§b§l¡HAAAAAAA!");
    }

    /** G: Big Bang Attack. Una esfera enorme que explota en un área amplia. */
    public static void bigBang(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!canAttack(p, s, BIG_BANG_COST, "el Big Bang Attack")) return;
        if (!p.isCreative()) s.ki -= BIG_BANG_COST;
        s.charging = false;
        s.pose = SaiyanState.POSE_BIG_BANG;
        s.poseTimer = 18;
        s.attackCooldown = 40;
        SaiyanData.set(p, s);
        Form f = s.activeForm();
        KiBlastEntity blast = new KiBlastEntity(ModRegistry.KI_BLAST.get(), p.level());
        blast.setOwner(p);
        Vec3 look = p.getLookAngle();
        blast.setPos(p.getX() + look.x * 1.6, p.getEyeY() - 0.3 + look.y * 1.6, p.getZ() + look.z * 1.6);
        blast.setup(f == Form.ULTRA_EGO ? 0xB070FF : 0xA8E8FF, 2.4F + f.ordinal() * 0.1F, 14.0F * s.damageMultiplier());
        blast.setBigBang(5.5F + f.ordinal() * 0.3F);
        blast.shoot(look.x, look.y, look.z, 1.0F, 0.0F);
        p.level().addFreshEntity(blast);
        ServerLevel lv = p.serverLevel();
        lv.playSound(null, p.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.5F, 0.5F);
        lv.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.5F, 0.6F);
        msg(p, "§b§l¡BIG BANG ATTACK!");
    }

    /** Z: Puño del Dragón. Oscurece todo, caen rayos y un dragón espiritual te envuelve; tu siguiente golpe es devastador. */
    public static void dragonFist(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) { msg(p, "§cNo eres saiyajin. Usa /raza saiyajin"); return; }
        if (s.dragonFist > 0) { msg(p, "§6El dragón ya te envuelve: ¡golpea!"); return; }
        if (s.dragonCooldown > 0 && !p.isCreative()) {
            int sec = s.dragonCooldown / 20;
            msg(p, String.format("§cEl Puño del Dragón estará listo en %d:%02d (una semilla del ermitaño lo recupera).", sec / 60, sec % 60));
            return;
        }
        if (s.ki < DRAGON_COST && !p.isCreative()) { msg(p, "§cNecesitas " + DRAGON_COST + " de ki para el Puño del Dragón."); return; }
        if (!p.isCreative()) s.ki -= DRAGON_COST;
        s.charging = false;
        s.dragonFist = DRAGON_WINDOW;
        s.dragonCooldown = DRAGON_COOLDOWN;
        s.pose = SaiyanState.POSE_DRAGON;
        s.poseTimer = 30;
        SaiyanData.set(p, s);
        ServerLevel lv = p.serverLevel();
        for (ServerPlayer other : lv.players()) {
            if (other.distanceTo(p) < 48) other.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 220, 0, false, false, false));
        }
        for (int i = 0; i < 6; i++) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
            if (bolt != null) {
                bolt.setVisualOnly(true);
                bolt.moveTo(p.getX() + (lv.random.nextDouble() - 0.5) * 14, p.getY(), p.getZ() + (lv.random.nextDouble() - 0.5) * 14);
                lv.addFreshEntity(bolt);
            }
        }
        lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.75F, 0.15F), 2.5F), p.getX(), p.getY() + 1.5, p.getZ(), 200, 1.2, 2.0, 1.2, 0);
        lv.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5F, 0.8F);
        lv.playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.5F, 0.7F);
        lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.PLAYERS, 1.0F, 1.2F);
        msg(p, "§6§l¡PUÑO DEL DRAGÓN! §r§e¡Golpea a tu enemigo! (30 s)");
    }

    /** Golpe del Puño del Dragón: daño devastador (suficiente para el Dragón del End). */
    public static float dragonFistHit(ServerPlayer p, LivingEntity target, float amount) {
        SaiyanState s = SaiyanData.get(p);
        if (s.dragonFist <= 0) return amount;
        s.dragonFist = 0;
        SaiyanData.set(p, s);
        ServerLevel lv = p.serverLevel();
        Vec3 from = p.position().add(0, 1.2, 0), to = target.position().add(0, target.getBbHeight() / 2, 0);
        DustParticleOptions gold = new DustParticleOptions(new Vector3f(1.0F, 0.75F, 0.15F), 2.5F);
        Vec3 d = to.subtract(from);
        for (int i = 0; i <= 40; i++) {
            double t = i / 40.0;
            double spin = t * Math.PI * 6;
            Vec3 pt = from.add(d.scale(t)).add(Math.cos(spin) * 0.8, Math.sin(spin) * 0.8, 0);
            lv.sendParticles(gold, pt.x, pt.y, pt.z, 4, 0.1, 0.1, 0.1, 0);
        }
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, to.x, to.y, to.z, 2, 0.5, 0.5, 0.5, 0);
        lv.sendParticles(ParticleTypes.FLASH, to.x, to.y, to.z, 3, 0.5, 0.5, 0.5, 0);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
        if (bolt != null) {
            bolt.setVisualOnly(true);
            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            lv.addFreshEntity(bolt);
        }
        lv.playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 2.0F, 0.6F);
        lv.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.5F);
        msg(p, "§6§l¡DRAGÓN!");
        return Math.max(amount, 5000.0F);
    }

    /** Cada tick: avanza las poses de ataque y los tiempos de espera. */
    public static void tickAttacks(ServerPlayer p, SaiyanState s) {
        boolean dirty = false;
        if (s.attackCooldown > 0) { s.attackCooldown--; }
        if (s.dragonCooldown > 0) { s.dragonCooldown--; if (s.dragonCooldown % 20 == 0) dirty = true; }
        if (s.dragonFist > 0) {
            s.dragonFist--;
            if (s.dragonFist % 20 == 0) dirty = true;
            if (s.dragonFist == 0) {
                msg(p, "§7El dragón se desvanece...");
                SaiyanData.set(p, s);
                return;
            }
        }
        if (s.pose != 0) {
            if (s.pose == SaiyanState.POSE_KAME_CHARGE) {
                ServerLevel lv = p.serverLevel();
                Vec3 side = p.getLookAngle().cross(new Vec3(0, 1, 0)).normalize();
                Vec3 hands = p.position().add(0, 1.0, 0).add(side.scale(0.45));
                float grow = 1.0F - s.poseTimer / 26.0F;
                lv.sendParticles(new DustParticleOptions(new Vector3f(0.4F, 0.8F, 1.0F), 0.8F + grow * 1.5F), hands.x, hands.y, hands.z, 6, 0.1 + grow * 0.15, 0.1 + grow * 0.15, 0.1 + grow * 0.15, 0);
                if (s.poseTimer % 6 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F + grow);
                p.setDeltaMovement(0, Math.min(0, p.getDeltaMovement().y), 0);
                p.hurtMarked = true;
            }
            if (--s.poseTimer <= 0) {
                if (s.pose == SaiyanState.POSE_KAME_CHARGE) {
                    fireKamehameha(p, s);
                    return;
                }
                s.pose = 0;
                SaiyanData.set(p, s);
                return;
            }
        }
        root(p, s);
        if (dirty) SaiyanData.setQuiet(p, s);
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
            s.dragonCooldown = 0;          // la semilla también recupera el Puño del Dragón
            s.attackCooldown = 0;
            SaiyanData.set(p, s);
        }
        p.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 25, 0.5, 0.8, 0.5, 0.0);
        p.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.3);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.6F);
        msg(p, "§a¡Te sientes completamente renovado!");
    }
}
