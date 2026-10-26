package com.cz.data;

import com.cz.entity.DebrisEntity;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/** Lógica del servidor: mantos, chakra, atributos y efectos de la transformación. */
public final class ZorroLogic {
    private static final UUID HEALTH = UUID.fromString("5b0c1e7a-2f3d-4c5e-9a1b-7c8d9e0f1a01");
    private static final UUID DAMAGE = UUID.fromString("5b0c1e7a-2f3d-4c5e-9a1b-7c8d9e0f1a02");
    private static final UUID SPEED = UUID.fromString("5b0c1e7a-2f3d-4c5e-9a1b-7c8d9e0f1a03");
    private static final UUID ATK_SPEED = UUID.fromString("5b0c1e7a-2f3d-4c5e-9a1b-7c8d9e0f1a04");
    private static final UUID ARMOR = UUID.fromString("5b0c1e7a-2f3d-4c5e-9a1b-7c8d9e0f1a05");
    private static final UUID KNOCK = UUID.fromString("5b0c1e7a-2f3d-4c5e-9a1b-7c8d9e0f1a06");
    private static final UUID STEP = UUID.fromString("5b0c1e7a-2f3d-4c5e-9a1b-7c8d9e0f1a07");
    public static final int EXHAUST = 30 * 20;

    private ZorroLogic() {}

    public static void msg(ServerPlayer p, String text) {
        p.displayClientMessage(Component.literal(text), true);
    }

    private static boolean ready(ServerPlayer p, ZorroState s) {
        if (!s.awakened) { msg(p, "§cTodavía no despertaste el chakra del zorro. Usa §e/zorro despertar"); return false; }
        return true;
    }

    // ------------------------------------------------------------ menú, transformación, carga

    public static void select(ServerPlayer p, int phase) {
        ZorroState s = ZorroData.get(p);
        if (!ready(p, s)) return;
        s.selected = Math.max(1, Math.min(5, phase));
        ZorroData.set(p, s);
    }

    /** Y: se transforma en el manto elegido; si ya lo tiene puesto, vuelve a la normalidad. */
    public static void toggleTransform(ServerPlayer p) {
        ZorroState s = ZorroData.get(p);
        if (!ready(p, s)) return;
        if (s.powering > 0) return;
        if (s.phase == s.selected) { revert(p, s, "§7El chakra del zorro se retira."); return; }
        if (s.cooldown > 0 && !p.isCreative()) { msg(p, "§cEstás agotado: podrás volver a usar el chakra en " + s.cooldown / 20 + " s."); return; }
        Phase target = Phase.byId(s.selected);
        if (s.chakra < target.minChakra && !p.isCreative()) {
            msg(p, "§cNecesitas al menos " + target.minChakra + " de chakra para el " + target.display + ". Cárgalo con J.");
            return;
        }
        s.charging = false;
        s.target = target.ordinal();
        s.poweringTotal = target.powerTicks;
        s.powering = s.poweringTotal;
        ZorroData.set(p, s);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.5F, 0.6F);
        msg(p, "§c" + switch (target) {
            case TWO -> "Caes de rodillas... el chakra rojo te envuelve.";
            case THREE -> "¡La ira te consume! El manto hierve.";
            case FOUR, FIVE -> "Te desplomas en cuatro patas... el zorro toma el control.";
            default -> "¡Grrrr...! La ira despierta al zorro.";
        });
    }

    /** J: empieza o detiene la carga de chakra. */
    public static void toggleCharge(ServerPlayer p) {
        ZorroState s = ZorroData.get(p);
        if (!ready(p, s) || s.powering > 0) return;
        if (!s.charging && s.chakra >= ZorroState.MAX_CHAKRA) { msg(p, "§6Tu chakra ya está al máximo."); return; }
        s.charging = !s.charging;
        ZorroData.set(p, s);
        p.serverLevel().playSound(null, p.blockPosition(), s.charging ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.8F, s.charging ? 0.7F : 1.0F);
        if (s.charging) msg(p, "§6Concentrando chakra... §7(J para detener)");
    }

    public static void revert(ServerPlayer p, ZorroState s, String reason) {
        boolean wasQuad = s.active().quadruped;
        s.phase = 0;
        s.powering = 0;
        s.charging = false;
        ZorroData.set(p, s);
        if (wasQuad) p.refreshDimensions();
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(ParticleTypes.LARGE_SMOKE, p.getX(), p.getY() + 1, p.getZ(), 30, 0.5, 0.8, 0.5, 0.03);
        lv.playSound(null, p.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.7F);
        msg(p, reason);
    }

    // ------------------------------------------------------------ transformación (cada tick)

    /** La transformación: sufrimiento e ira. El chakra sale a borbotones, el suelo se agrieta y todo tiembla. */
    public static void tickPowering(ServerPlayer p, ZorroState s) {
        ServerLevel lv = p.serverLevel();
        Phase t = Phase.byId(s.target);
        float pr = s.progress();
        double x = p.getX(), y = p.getY(), z = p.getZ();
        p.setDeltaMovement(0, Math.min(0, p.getDeltaMovement().y), 0);   // queda clavado en el sitio
        p.hurtMarked = true;
        float power = t.ordinal() / 5.0F;
        Vector3f cloak = new Vector3f(t.r(), t.g(), t.b());
        Vector3f dark = new Vector3f(0.35F, 0.02F, 0.02F);

        // chakra rojo que brota del cuerpo y gira alrededor
        DustColorTransitionOptions boil = new DustColorTransitionOptions(cloak, dark, 1.6F + pr * 1.4F);
        for (int i = 0; i < 4 + (int) (pr * 12 * (0.5F + power)); i++) {
            double a = lv.random.nextDouble() * Math.PI * 2, r = 0.3 + lv.random.nextDouble() * 0.5;
            lv.sendParticles(boil, x + Math.cos(a) * r, y + lv.random.nextDouble() * 1.8, z + Math.sin(a) * r, 0,
                    Math.cos(a + 1.5), 0.6, Math.sin(a + 1.5), 0.15 + pr * 0.3);
        }
        // burbujas de chakra que suben y revientan
        if (s.powering % 2 == 0) {
            lv.sendParticles(ParticleTypes.BUBBLE_POP, x, y + 1.0, z, 3 + (int) (pr * 6), 0.4, 0.8, 0.4, 0.05);
            lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.55F, 0.2F), 0.8F), x, y + 1.0, z, 4, 0.5, 0.9, 0.5, 0);
        }
        if (t.ordinal() >= 3) lv.sendParticles(ParticleTypes.FLAME, x, y + 0.6, z, 2 + (int) (pr * 6), 0.4, 0.6, 0.4, 0.03);
        if (t.ordinal() >= 4) lv.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.6, z, 2, 0.5, 0.4, 0.5, 0.02);

        // el suelo se agrieta, se levantan polvo y escombros
        BlockState ground = DebrisEntity.groundAt(lv, p.blockPosition());
        double rad = 0.6 + pr * (2.0 + power * 3.0);
        for (int i = 0; i < 2 + (int) (pr * 8 * (0.4F + power)); i++) {
            double a = lv.random.nextDouble() * Math.PI * 2;
            lv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), x + Math.cos(a) * rad, y + 0.1, z + Math.sin(a) * rad,
                    1, 0.1, 0.2, 0.1, 0.2 + pr * 0.3);
        }
        if (s.powering % 3 == 0) {
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4 + s.powering;
                lv.sendParticles(ParticleTypes.POOF, x + Math.cos(a) * rad * 0.7, y + 0.1, z + Math.sin(a) * rad * 0.7, 0, Math.cos(a), 0.05, Math.sin(a), 0.2);
            }
        }
        if (t.ordinal() >= 2 && pr > 0.2F && s.powering % Math.max(2, 9 - t.ordinal()) == 0) {
            double a = lv.random.nextDouble() * Math.PI * 2, r = 1 + lv.random.nextDouble() * 2.5;
            DebrisEntity.launch(lv, new Vec3(x + Math.cos(a) * r, y + 0.2, z + Math.sin(a) * r), ground,
                    Math.cos(a) * 0.1, 0.35 + lv.random.nextDouble() * 0.4 * power + pr * 0.2, Math.sin(a) * 0.1, 0.2F + lv.random.nextFloat() * 0.3F);
        }
        // sonidos: latidos, gruñidos y rugidos cada vez más fuertes
        if (s.powering % 10 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.0F + pr, 0.6F + pr * 0.6F);
        if (s.powering % 14 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 1.0F + pr, 0.5F + pr * 0.3F);
        if (s.powering % 25 == 0 && pr > 0.3F) {
            lv.playSound(null, p.blockPosition(), t.ordinal() >= 4 ? SoundEvents.RAVAGER_ROAR : SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.2F + pr, t.ordinal() >= 4 ? 0.6F : 0.7F);
        }
        if (s.powering % 8 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 1.0F, 0.6F + pr * 0.4F);
        if (pr > 0.5F && s.powering % 12 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.5F, 0.6F);

        if (--s.powering <= 0) {
            complete(p, s);
        } else {
            if (s.powering % 10 == 0) ZorroData.set(p, s); else ZorroData.setQuiet(p, s);
        }
    }

    private static void complete(ServerPlayer p, ZorroState s) {
        Phase t = Phase.byId(s.target);
        boolean quadBefore = s.active().quadruped;
        s.phase = t.ordinal();
        s.powering = 0;
        if (!p.isCreative()) s.chakra = Math.max(0, s.chakra - t.minChakra / 2);
        ZorroData.set(p, s);
        if (quadBefore != t.quadruped) p.refreshDimensions();
        p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + t.health));
        ServerLevel lv = p.serverLevel();
        double x = p.getX(), y = p.getY(), z = p.getZ();
        float power = t.ordinal() / 5.0F;
        // estallido de chakra
        Vector3f c = new Vector3f(t.r(), t.g(), t.b());
        for (int ring = 0; ring < 1 + t.ordinal() / 2; ring++) {
            double r = 1.4 + ring * 1.2;
            for (int i = 0; i < 48; i++) {
                double a = i * Math.PI * 2 / 48;
                lv.sendParticles(new DustParticleOptions(c, 2.2F), x + Math.cos(a) * r, y + 0.2, z + Math.sin(a) * r, 0, Math.cos(a), 0.05, Math.sin(a), 0.3);
            }
        }
        lv.sendParticles(new DustColorTransitionOptions(c, new Vector3f(0.3F, 0, 0), 2.6F), x, y + 1.0, z, 150 + t.ordinal() * 40, 0.6, 1.2, 0.6, 0);
        lv.sendParticles(ParticleTypes.FLASH, x, y + 1, z, 1, 0, 0, 0, 0);
        lv.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5, z, 3 + t.ordinal(), 1.0, 0.3, 1.0, 0);
        lv.sendParticles(ParticleTypes.LAVA, x, y + 0.5, z, 10 + t.ordinal() * 6, 0.8, 0.3, 0.8, 0);
        BlockState ground = DebrisEntity.groundAt(lv, p.blockPosition());
        for (int i = 0; i < 6 + t.ordinal() * 6; i++) {
            double a = lv.random.nextDouble() * Math.PI * 2, r = 0.8 + lv.random.nextDouble() * 1.5;
            double out = 0.15 + lv.random.nextDouble() * 0.35 * power;
            DebrisEntity.launch(lv, new Vec3(x + Math.cos(a) * r, y + 0.2, z + Math.sin(a) * r), ground, Math.cos(a) * out,
                    0.4 + lv.random.nextDouble() * 0.5 * power, Math.sin(a) * out, 0.2F + lv.random.nextFloat() * 0.4F * power);
        }
        for (int i = 0; i < 36; i++) {
            double a = i * Math.PI * 2 / 36;
            lv.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x + Math.cos(a) * 1.2, y + 0.2, z + Math.sin(a) * 1.2, 0, Math.cos(a), 0.02, Math.sin(a), 0.15 + power * 0.1);
        }
        lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2F + power, 0.7F);
        lv.playSound(null, p.blockPosition(), t.ordinal() >= 4 ? SoundEvents.ENDER_DRAGON_GROWL : SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.5F + power, 0.6F);
        if (t.ordinal() >= 3) lv.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.5F, 0.5F);
        if (t.ordinal() == 5) lv.playSound(null, p.blockPosition(), SoundEvents.SKELETON_HORSE_DEATH, SoundSource.PLAYERS, 1.5F, 0.5F);
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(4.0 + t.ordinal()), e -> e != p && e.isAlive())) {
            Vec3 d = e.position().subtract(p.position()).normalize();
            e.push(d.x * (0.8 + power), 0.4, d.z * (0.8 + power));
            e.hurtMarked = true;
        }
        msg(p, "§c§l" + t.display + " §r§7(" + t.tails + (t.tails == 1 ? " cola" : " colas") + ")");
    }

    // ------------------------------------------------------------ carga de chakra

    public static void tickCharge(ServerPlayer p, ZorroState s) {
        ServerLevel lv = p.serverLevel();
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 3, true, false, false));
        if (p.tickCount % 2 == 0) s.chakra = Math.min(ZorroState.MAX_CHAKRA, s.chakra + 1);
        lv.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.6F, 0.15F), 1.2F), p.getX(), p.getY() + 1, p.getZ(), 3, 0.5, 0.8, 0.5, 0);
        if (p.tickCount % 3 == 0) {
            // el chakra del entorno se concentra en el cuerpo
            double a = lv.random.nextDouble() * Math.PI * 2;
            lv.sendParticles(ParticleTypes.END_ROD, p.getX() + Math.cos(a) * 2.5, p.getY() + 1, p.getZ() + Math.sin(a) * 2.5, 0,
                    -Math.cos(a), 0, -Math.sin(a), 0.15);
        }
        if (p.tickCount % 20 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.8F, 0.8F + (float) s.chakra / ZorroState.MAX_CHAKRA * 0.6F);
        if (s.chakra >= ZorroState.MAX_CHAKRA) {
            s.charging = false;
            ZorroData.set(p, s);
            msg(p, "§6¡Chakra al máximo!");
            return;
        }
        if (p.tickCount % 10 == 0) ZorroData.setQuiet(p, s);
    }

    // ------------------------------------------------------------ cada segundo

    public static void secondTick(ServerPlayer p) {
        ZorroState s = ZorroData.get(p);
        if (!s.awakened) return;
        boolean changed = false;
        if (s.cooldown > 0) { s.cooldown = Math.max(0, s.cooldown - 20); changed = true; }
        Phase ph = s.active();
        if (ph != Phase.NONE) {
            if (ph.jump >= 0) p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, ph.jump, true, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, ph.ordinal() >= 3 ? 1 : 0, true, false, false));
            if (ph.burns) p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
            if (ph.burns) burnNearby(p, ph);
            if (!p.isCreative()) {
                s.chakra -= ph.drain;
                changed = true;
                if (s.chakra <= 0) {
                    s.chakra = 0;
                    s.cooldown = EXHAUST;
                    revert(p, s, "§cTe quedaste sin chakra: el manto desaparece. Estás agotado por 30 s.");
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 1));
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
                    return;
                }
            }
        } else if (!s.charging && s.chakra < ZorroState.MAX_CHAKRA) {
            s.chakra = Math.min(ZorroState.MAX_CHAKRA, s.chakra + 2);      // se recupera solo poco a poco
            changed = true;
        }
        if (changed) ZorroData.setQuiet(p, s);
    }

    /** El manto hirviente quema a todo lo que está pegado al jugador. */
    private static void burnNearby(ServerPlayer p, Phase ph) {
        double r = 1.3 + ph.ordinal() * 0.2;
        for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r), e -> e != p && e.isAlive())) {
            e.setSecondsOnFire(3);
            e.hurt(p.damageSources().onFire(), 1.0F + ph.ordinal() * 0.5F);
        }
    }

    // ------------------------------------------------------------ atributos

    public static void applyAttributes(ServerPlayer p, ZorroState s) {
        Phase ph = s.active();
        boolean on = s.awakened && ph != Phase.NONE;
        mod(p.getAttribute(Attributes.MAX_HEALTH), HEALTH, "cz_health", on ? ph.health : 0, AttributeModifier.Operation.ADDITION);
        mod(p.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE, "cz_damage", on ? ph.damage : 0, AttributeModifier.Operation.ADDITION);
        mod(p.getAttribute(Attributes.MOVEMENT_SPEED), SPEED, "cz_speed", on ? ph.speed : 0, AttributeModifier.Operation.MULTIPLY_BASE);
        mod(p.getAttribute(Attributes.ATTACK_SPEED), ATK_SPEED, "cz_agility", on ? ph.attackSpeed : 0, AttributeModifier.Operation.ADDITION);
        mod(p.getAttribute(Attributes.ARMOR), ARMOR, "cz_armor", on ? ph.armor : 0, AttributeModifier.Operation.ADDITION);
        mod(p.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCK, "cz_knock", on ? ph.knockback : 0, AttributeModifier.Operation.ADDITION);
        mod(p.getAttribute(net.minecraftforge.common.ForgeMod.STEP_HEIGHT_ADDITION.get()), STEP, "cz_step", on && ph.quadruped ? 0.6 : 0, AttributeModifier.Operation.ADDITION);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    private static void mod(AttributeInstance inst, UUID id, String name, double amount, AttributeModifier.Operation op) {
        if (inst == null) return;
        inst.removeModifier(id);
        if (amount != 0) inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
    }
}
