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
        if (s.selected == 0 && s.kaioken == 0) { msg(p, "§eElige una fase o el Kaioken en el menú (tecla K)."); return; }
        if (s.cooldown > 0 && !p.isCreative()) {
            msg(p, "§cEstás agotado. Podrás transformarte en " + (s.cooldown / 20) + " s (una semilla del ermitaño te recupera).");
            return;
        }
        if (s.ki < TRANSFORM_COST && !p.isCreative()) { msg(p, "§cNo tienes suficiente ki. Come algo para recuperarlo."); return; }
        if (!p.isCreative()) s.ki -= TRANSFORM_COST;
        s.transformed = true;
        s.timerMax = SaiyanState.timeLimit(s.selected, s.kaioken);
        s.timer = s.timerMax;
        SaiyanData.set(p, s);
        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + s.extraHealth()));   // los corazones nuevos llegan llenos
        burst(p, s, true);
        announce(p, s);
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
        Vector3f col = f == Form.BASE && kaio ? new Vector3f(1.0F, 0.15F, 0.1F) : new Vector3f(f.r(), f.g(), f.b());
        DustParticleOptions dust = new DustParticleOptions(col, 2.0F);
        double x = p.getX(), y = p.getY(), z = p.getZ();
        int rings = big ? 3 : 1;
        for (int r = 0; r < rings; r++) {
            double rad = 1.2 + r * 1.1;
            for (int i = 0; i < 48; i++) {
                double a = i * Math.PI * 2 / 48;
                lv.sendParticles(dust, x + Math.cos(a) * rad, y + 0.15 + r * 0.05, z + Math.sin(a) * rad, 1, 0.05, 0.1, 0.05, 0.0);
            }
        }
        lv.sendParticles(dust, x, y + 1.5, z, 160, 0.4, 2.2, 0.4, 0.0);           // columna de luz
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
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 0.7F + f.ordinal() * 0.08F);
        if (big) lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6F, 1.3F);
        if (f == Form.ULTRA) lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.5F, 0.5F);
        if (f == Form.GOD || f == Form.BLUE) lv.playSound(null, p.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 0.8F);
        if (kaio) lv.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.6F);
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
