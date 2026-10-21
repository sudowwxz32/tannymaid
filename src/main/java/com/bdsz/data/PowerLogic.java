package com.bdsz.data;

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

/** Lógica de poder en el servidor: transformarse, efectos, atributos y desgaste. */
public final class PowerLogic {
    private static final UUID SPEED_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c01");
    private static final UUID KB_ID = UUID.fromString("b1d5a3c0-1e2f-4a6b-9c7d-0e1f2a3b4c02");

    private PowerLogic() {}

    public static void msg(ServerPlayer p, String text) {
        p.displayClientMessage(Component.literal(text), true);
    }

    // ------------------------------------------------------------ acciones del jugador

    public static void select(ServerPlayer p, int form, int kaioken) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) return;
        Form old = s.activeForm();
        int oldK = s.activeKaioken();
        s.selected = Form.byId(form).ordinal();
        s.kaioken = Math.max(0, Math.min(100, (kaioken / 10) * 10));
        if (s.transformed && !s.powered()) s.transformed = false;
        SaiyanData.set(p, s);
        if (s.transformed && (old != s.activeForm() || oldK != s.activeKaioken())) {
            burst(p, s, s.activeForm() != old);
            announce(p, s);
        }
    }

    public static void toggleTransform(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) {
            msg(p, "§cNo eres saiyajin. Usa /raza sayajin");
            return;
        }
        if (s.transformed) {
            revert(p, s, "§7Vuelves a tu estado base.");
            return;
        }
        if (s.selected == 0 && s.kaioken == 0) {
            msg(p, "§eElige una fase o el Kaioken en el menú (tecla K).");
            return;
        }
        if (!p.isCreative() && p.getFoodData().getFoodLevel() <= 4) {
            msg(p, "§cNo tienes suficiente energía (hambre) para transformarte.");
            return;
        }
        s.transformed = true;
        SaiyanData.set(p, s);
        burst(p, s, true);
        announce(p, s);
    }

    public static void toggleAura(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan) {
            msg(p, "§cNo eres saiyajin. Usa /raza sayajin");
            return;
        }
        s.aura = !s.aura;
        SaiyanData.set(p, s);
        ServerLevel lv = p.serverLevel();
        lv.playSound(null, p.blockPosition(), s.aura ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.8F, s.aura ? 1.4F : 1.0F);
        msg(p, s.aura ? "§bAura activada" : "§7Aura desactivada");
    }

    public static void revert(ServerPlayer p, SaiyanState s, String reason) {
        s.transformed = false;
        SaiyanData.set(p, s);
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
        sb.append(String.format(" §r§7(daño x%.1f, resistencia %d%%)", s.damageMultiplier(), Math.round(s.resistance() * 100)));
        msg(p, sb.toString());
    }

    /** Explosión de energía al transformarse: luz, rayos, onda de choque y sonido. */
    public static void burst(ServerPlayer p, SaiyanState s, boolean big) {
        ServerLevel lv = p.serverLevel();
        Form f = s.activeForm();
        boolean kaio = s.activeKaioken() > 0;
        Vector3f col = f == Form.BASE && kaio ? new Vector3f(1.0F, 0.15F, 0.1F) : new Vector3f(f.r(), f.g(), f.b());
        DustParticleOptions dust = new DustParticleOptions(col, 2.0F);
        double x = p.getX(), y = p.getY(), z = p.getZ();
        // anillo de energía
        for (int i = 0; i < 48; i++) {
            double a = i * Math.PI * 2 / 48;
            lv.sendParticles(dust, x + Math.cos(a) * 1.6, y + 0.2, z + Math.sin(a) * 1.6, 2, 0.05, 0.4, 0.05, 0.0);
        }
        // columna de luz
        lv.sendParticles(dust, x, y + 1.2, z, 120, 0.45, 1.4, 0.45, 0.0);
        lv.sendParticles(ParticleTypes.FLASH, x, y + 1, z, 1, 0, 0, 0, 0);
        if (big) lv.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5, z, 4, 1.0, 0.3, 1.0, 0.0);
        if (kaio) {
            lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 2.2F), x, y + 1.1, z, 80, 0.6, 1.2, 0.6, 0.0);
        }
        switch (f) {
            case SSJ2, BLUE -> lv.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 1.2, z, 60, 0.6, 1.0, 0.6, 0.4);
            case ULTRA -> lv.sendParticles(ParticleTypes.END_ROD, x, y + 1.2, z, 70, 0.8, 1.2, 0.8, 0.05);
            case GOD -> lv.sendParticles(ParticleTypes.FLAME, x, y + 0.5, z, 60, 0.5, 1.4, 0.5, 0.03);
            default -> { }
        }
        // rayos decorativos (no hacen daño ni queman)
        if (big && (f == Form.FALSE_SSJ || f == Form.SSJ1 || f == Form.SSJ2 || f == Form.SSJ3)) {
            int bolts = f == Form.SSJ3 ? 3 : f == Form.SSJ2 ? 2 : 1;
            for (int i = 0; i < bolts; i++) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
                if (bolt != null) {
                    bolt.setVisualOnly(true);
                    bolt.moveTo(x + (lv.random.nextDouble() - 0.5) * 6, y, z + (lv.random.nextDouble() - 0.5) * 6);
                    lv.addFreshEntity(bolt);
                }
            }
        }
        // sonido
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 0.7F + f.ordinal() * 0.08F);
        if (big) lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 1.4F);
        if (f == Form.ULTRA) lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2F, 0.6F);
        if (kaio) lv.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.6F);
        // onda de choque: empuja a las criaturas cercanas
        if (big) {
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(5.0D),
                    e -> e != p && e.isAlive() && !(e instanceof Player))) {
                Vec3 d = e.position().subtract(p.position()).normalize();
                e.push(d.x * 1.2, 0.4, d.z * 1.2);
                e.hurtMarked = true;
            }
        }
    }

    // ------------------------------------------------------------ atributos y desgaste

    public static void applyAttributes(ServerPlayer p, SaiyanState s) {
        setMod(p.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID, "bdsz_speed", s.speedBonus(), AttributeModifier.Operation.MULTIPLY_BASE);
        setMod(p.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KB_ID, "bdsz_kb", s.powered() ? Math.min(1.0, s.resistance()) : 0,
                AttributeModifier.Operation.ADDITION);
    }

    private static void setMod(AttributeInstance inst, UUID id, String name, double amount, AttributeModifier.Operation op) {
        if (inst == null) return;
        inst.removeModifier(id);
        if (amount != 0) inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
    }

    /** Cada segundo: salto mejorado y desgaste de energía (hambre) mientras estás transformado. */
    public static void secondTick(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!s.saiyan || !s.powered()) return;
        Form f = s.activeForm();
        int jump = Math.min(4, f.ordinal() / 2 + (s.activeKaioken() >= 50 ? 1 : 0));
        if (jump > 0) p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, jump - 1, true, false, false));
        if (!p.isCreative() && !p.isSpectator()) {
            p.causeFoodExhaustion(f.drain * 8.0F + s.activeKaioken() / 100.0F * 1.6F);
            if (p.getFoodData().getFoodLevel() <= 2) revert(p, s, "§cTe quedaste sin energía y vuelves a tu estado base.");
        }
    }
}
