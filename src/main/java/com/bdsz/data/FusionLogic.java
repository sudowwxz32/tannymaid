package com.bdsz.data;

import com.bdsz.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fusiones con los Aretes de Fusión:
 * 1) te pones un arete (animación de ponértelo), 2) lanzas el otro arete a un zombi, esqueleto, enderman,
 * creeper, gólem de hierro o warden, 3) cuando lo recoge se produce la fusión: esfera de poder amarilla,
 * rayos y un ciclón amarillo hasta el cielo. Te quedas con su apariencia mezclada, sus habilidades y
 * tus fases y transformaciones, durante un tiempo que depende de con quién te fusionaste.
 */
public final class FusionLogic {
    public static final int EAR_ANIM = 30, FUSING_TICKS = 110;
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.86F, 0.2F), 2.2F);
    private static final DustParticleOptions GOLD_S = new DustParticleOptions(new Vector3f(1.0F, 0.95F, 0.5F), 1.2F);

    /** Aretes lanzados que aún no recoge nadie: entidad → jugador que lo lanzó. */
    private static final Map<ItemEntity, UUID> THROWN = new ConcurrentHashMap<>();
    /** Mob que está siendo absorbido durante la escena de fusión. */
    private static final Map<UUID, Mob> PULLING = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> PENDING = new ConcurrentHashMap<>();

    private FusionLogic() {}

    private static void msg(ServerPlayer p, String t) {
        p.displayClientMessage(Component.literal(t), true);
    }

    // ------------------------------------------------------------------ aretes

    /** Clic derecho con el arete: ponérselo, quitárselo (Shift) o lanzarlo (si ya lleva uno puesto). */
    public static void useEarring(ServerPlayer p, ItemStack stack) {
        SaiyanState s = SaiyanData.get(p);
        if (s.fusing > 0 || s.earAnim != 0) return;
        if (s.fusion > 0) {
            msg(p, "§eYa estás fusionado con " + Fusion.byId(s.fusion).display + " (" + s.fusionTicks / 20 + " s)");
            return;
        }
        if (!s.earring) {
            s.earring = true;
            s.earAnim = EAR_ANIM;
            if (!p.getAbilities().instabuild) stack.shrink(1);
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD, SoundSource.PLAYERS, 1.0F, 1.2F);
            msg(p, "§6Te pones el arete. §7Lánzale el otro arete (clic derecho) al que quieras fusionar.");
            SaiyanData.set(p, s);
            return;
        }
        if (p.isShiftKeyDown()) {
            s.earring = false;
            s.earAnim = -EAR_ANIM;
            ItemStack back = new ItemStack(ModRegistry.FUSION_EARRING.get());
            if (!p.getInventory().add(back)) p.drop(back, false);
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD, SoundSource.PLAYERS, 1.0F, 0.8F);
            msg(p, "§6Te quitas el arete.");
            SaiyanData.set(p, s);
            return;
        }
        // lanzar el otro arete
        ItemStack one = stack.copyWithCount(1);
        if (!p.getAbilities().instabuild) stack.shrink(1);
        Vec3 look = p.getLookAngle();
        ItemEntity it = new ItemEntity(p.level(), p.getX(), p.getEyeY() - 0.2, p.getZ(), one);
        it.setDeltaMovement(look.x * 0.9, look.y * 0.9 + 0.15, look.z * 0.9);
        it.setThrower(p.getUUID());
        it.setPickUpDelay(60);
        p.level().addFreshEntity(it);
        track(it, p);
        p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8F, 1.4F);
    }

    public static void track(ItemEntity it, ServerPlayer p) {
        if (SaiyanData.get(p).earring) THROWN.put(it, p.getUUID());
    }

    /** Cada tick del servidor: ¿algún mob recogió un arete lanzado? */
    public static void serverTick(MinecraftServer server) {
        Iterator<Map.Entry<ItemEntity, UUID>> itr = THROWN.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<ItemEntity, UUID> e = itr.next();
            ItemEntity it = e.getKey();
            if (!it.isAlive() || it.getAge() > 600) {
                itr.remove();
                continue;
            }
            if (it.tickCount % 2 == 0 && it.level() instanceof ServerLevel lv) {
                lv.sendParticles(GOLD_S, it.getX(), it.getY() + 0.2, it.getZ(), 1, 0.05, 0.05, 0.05, 0);
            }
            ServerPlayer p = server.getPlayerList().getPlayer(e.getValue());
            if (p == null || p.level() != it.level()) continue;
            SaiyanState s = SaiyanData.get(p);
            if (!s.earring || s.fusion > 0 || s.fusing > 0) continue;
            for (Mob m : it.level().getEntitiesOfClass(Mob.class, it.getBoundingBox().inflate(1.4), m -> m.isAlive() && Fusion.of(m) != Fusion.NONE)) {
                itr.remove();
                start(p, s, m, it);
                break;
            }
        }
    }

    // ------------------------------------------------------------------ escena de fusión

    private static void start(ServerPlayer p, SaiyanState s, Mob mob, ItemEntity it) {
        Fusion f = Fusion.of(mob);
        it.discard();
        ServerLevel lv = p.serverLevel();
        lv.playSound(null, mob.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 1.0F, 0.8F);
        mob.setNoAi(true);
        mob.setInvulnerable(true);
        mob.setGlowingTag(true);
        PULLING.put(p.getUUID(), mob);
        PENDING.put(p.getUUID(), f.ordinal());
        s.fusing = FUSING_TICKS;
        s.charging = false;
        SaiyanData.set(p, s);
        p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 50, 20));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§6§l¡FUUU... SIÓN!")));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§e" + f.display + " recogió el arete")));
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 0.6F);
    }

    /** Se llama cada tick mientras dura la escena. */
    public static void tickFusing(ServerPlayer p, SaiyanState s) {
        ServerLevel lv = p.serverLevel();
        int elapsed = FUSING_TICKS - s.fusing;
        float prog = elapsed / (float) FUSING_TICKS;
        double x = p.getX(), y = p.getY() + 1.0, z = p.getZ();
        p.setDeltaMovement(0, Math.max(0, p.getDeltaMovement().y) * 0.0 + (prog < 0.8 ? 0.02 : 0), 0);
        p.hurtMarked = true;
        p.fallDistance = 0;
        Mob mob = PULLING.get(p.getUUID());
        if (mob != null && mob.isAlive()) {
            // el mob es atraído hacia el jugador, girando dentro de la energía
            Vec3 to = new Vec3(x, y - 1.0, z).subtract(mob.position());
            double dist = to.length();
            if (dist < 0.8 || elapsed > 60) {
                lv.sendParticles(ParticleTypes.FLASH, mob.getX(), mob.getY() + 1, mob.getZ(), 1, 0, 0, 0, 0);
                lv.sendParticles(GOLD, mob.getX(), mob.getY() + 1, mob.getZ(), 60, 0.6, 0.9, 0.6, 0);
                mob.discard();
                PULLING.remove(p.getUUID());
            } else {
                Vec3 v = to.normalize().scale(Math.min(dist, 0.12 + prog * 0.35));
                mob.setPos(mob.getX() + v.x, mob.getY() + v.y + 0.02, mob.getZ() + v.z);
                mob.setYRot(mob.getYRot() + 25);
                lv.sendParticles(GOLD_S, mob.getX(), mob.getY() + 1, mob.getZ(), 6, 0.4, 0.6, 0.4, 0);
            }
        }
        // esfera de poder amarilla que crece
        double r = 0.8 + prog * 3.6;
        for (int i = 0; i < 26 + (int) (prog * 40); i++) {
            double a = p.getRandom().nextDouble() * Math.PI * 2, b = Math.acos(2 * p.getRandom().nextDouble() - 1);
            lv.sendParticles(GOLD, x + Math.sin(b) * Math.cos(a) * r, y + Math.cos(b) * r, z + Math.sin(b) * Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
        lv.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 12, r * 0.6, r * 0.6, r * 0.6, 0.3);
        // ciclón amarillo que sube
        for (int k = 0; k < 10; k++) {
            double hh = p.getRandom().nextDouble() * (6 + prog * 40);
            double ang = hh * 0.5 + p.tickCount * 0.4 + k;
            double rr = 1.2 + hh * 0.12;
            lv.sendParticles(GOLD, x + Math.cos(ang) * rr, p.getY() + hh, z + Math.sin(ang) * rr, 1, 0, 0, 0, 0);
        }
        // rayos
        if (elapsed % 9 == 0) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
            if (bolt != null) {
                double a = p.getRandom().nextDouble() * Math.PI * 2, d = 3 + p.getRandom().nextDouble() * 6;
                bolt.moveTo(x + Math.cos(a) * d, p.getY(), z + Math.sin(a) * d);
                bolt.setVisualOnly(true);
                lv.addFreshEntity(bolt);
            }
        }
        if (elapsed % 20 == 0) {
            lv.playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.2F, 0.7F + prog * 0.6F);
            PowerLogic.debrisBurst(lv, p, 0.4F + prog * 0.6F);
            BlockPos g = p.blockPosition().below();
            lv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, lv.getBlockState(g)), x, p.getY(), z, 40, 2.5, 0.2, 2.5, 0.3);
        }
        if (elapsed % 5 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 2.0F, 0.5F + prog);
        s.fusing--;
        if (s.fusing <= 0) {
            complete(p, s);
        } else {
            SaiyanData.setQuiet(p, s);
            if (elapsed % 10 == 0) SaiyanData.sync(p, s);
        }
    }

    private static void complete(ServerPlayer p, SaiyanState s) {
        ServerLevel lv = p.serverLevel();
        Fusion f = Fusion.byId(PENDING.getOrDefault(p.getUUID(), Fusion.ZOMBIE.ordinal()));
        PENDING.remove(p.getUUID());
        Mob left = PULLING.remove(p.getUUID());
        if (left != null) left.discard();
        s.fusion = f.ordinal();
        s.fusionMax = s.fusionTicks = f.seconds * 20;
        s.fusing = 0;
        s.earring = true;
        double x = p.getX(), y = p.getY() + 1, z = p.getZ();
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 2, 0.5, 0.5, 0.5, 0);
        lv.sendParticles(ParticleTypes.FLASH, x, y, z, 3, 0, 0, 0, 0);
        lv.sendParticles(GOLD, x, y, z, 300, 3, 3, 3, 0);
        lv.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 120, 1, 1.5, 1, 0.8);
        lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.6F);
        lv.playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 0.8F);
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(8), e -> e != p)) {
            Vec3 push = e.position().subtract(p.position()).normalize().scale(1.6);
            e.setDeltaMovement(push.x, 0.6, push.z);
            e.hurtMarked = true;
        }
        p.connection.send(new ClientboundSetTitlesAnimationPacket(0, 60, 20));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§6§l¡FUSIÓN COMPLETA!")));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§eGuerrero " + f.display + " · " + f.seconds / 60 + " min")));
        p.sendSystemMessage(Component.literal("§6✦ Fusión con " + f.display + ": §7" + f.ability));
        p.setHealth(p.getMaxHealth());
        SaiyanData.set(p, s);
    }

    /** Se separan: se acaba el tiempo o el jugador muere. Los aretes se rompen. */
    public static void defuse(ServerPlayer p, SaiyanState s, boolean quiet) {
        Fusion f = Fusion.byId(s.fusion);
        s.fusion = 0;
        s.fusionTicks = s.fusionMax = 0;
        s.earring = false;
        s.fusing = 0;
        PENDING.remove(p.getUUID());
        Mob m = PULLING.remove(p.getUUID());
        if (m != null) {
            m.setNoAi(false);
            m.setInvulnerable(false);
            m.setGlowingTag(false);
        }
        if (!quiet) {
            ServerLevel lv = p.serverLevel();
            lv.sendParticles(GOLD, p.getX(), p.getY() + 1, p.getZ(), 120, 1.2, 1.2, 1.2, 0);
            lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 0.8F);
            p.sendSystemMessage(Component.literal("§6✦ La fusión con " + f.display + " terminó. Los aretes se rompieron."));
        }
        SaiyanData.set(p, s);
    }

    /** Tecla N: separarse a voluntad (los aretes se rompen). */
    public static void defuseByChoice(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (s.fusion <= 0 && s.fusing <= 0) {
            msg(p, "§7No estás fusionado.");
            return;
        }
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 1, p.getZ(), 1, 0, 0, 0, 0);
        lv.playSound(null, p.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.8F);
        defuse(p, s, false);
    }

    // ------------------------------------------------------------------ cada tick del jugador

    public static void tick(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (s.earAnim != 0) {
            s.earAnim += s.earAnim > 0 ? -1 : 1;
            SaiyanData.setQuiet(p, s);
            if (s.earAnim == 0 || Math.abs(s.earAnim) % 6 == 0) SaiyanData.sync(p, s);
        }
        if (s.fusing > 0) {
            tickFusing(p, s);
            return;
        }
        if (s.fusion <= 0) return;
        if (s.fusionCd > 0) s.fusionCd--;
        s.fusionTicks--;
        if (s.fusionTicks <= 0) {
            defuse(p, s, false);
            return;
        }
        if (p.tickCount % 20 == 0) {
            effects(p, Fusion.byId(s.fusion));
            SaiyanData.setQuiet(p, s);
            if (s.fusionTicks == 600 || s.fusionTicks == 200) msg(p, "§eLa fusión termina en " + s.fusionTicks / 20 + " s");
        } else if (s.fusionCd > 0) {
            SaiyanData.setQuiet(p, s);
        } else {
            SaiyanData.setQuiet(p, s);
        }
    }

    private static void eff(ServerPlayer p, net.minecraft.world.effect.MobEffect e, int amp) {
        p.addEffect(new MobEffectInstance(e, 60, amp, true, false, true));
    }

    private static void effects(ServerPlayer p, Fusion f) {
        switch (f) {
            case ZOMBIE -> { eff(p, MobEffects.DAMAGE_BOOST, 1); eff(p, MobEffects.REGENERATION, 0); p.removeEffect(MobEffects.HUNGER); }
            case SKELETON -> { eff(p, MobEffects.MOVEMENT_SPEED, 1); eff(p, MobEffects.JUMP, 0); eff(p, MobEffects.DAMAGE_BOOST, 0); }
            case ENDERMAN -> { eff(p, MobEffects.NIGHT_VISION, 0); eff(p, MobEffects.MOVEMENT_SPEED, 0); eff(p, MobEffects.DAMAGE_BOOST, 0); }
            case CREEPER -> { eff(p, MobEffects.MOVEMENT_SPEED, 0); eff(p, MobEffects.DAMAGE_BOOST, 0); }
            case GOLEM -> { eff(p, MobEffects.DAMAGE_BOOST, 2); eff(p, MobEffects.DAMAGE_RESISTANCE, 1); }
            case WARDEN -> {
                eff(p, MobEffects.DAMAGE_BOOST, 3);
                eff(p, MobEffects.DAMAGE_RESISTANCE, 2);
                eff(p, MobEffects.HEALTH_BOOST, 4);
                eff(p, MobEffects.NIGHT_VISION, 0);
                for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(10), m -> m instanceof Enemy)) {
                    m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                    m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
                }
            }
            default -> { }
        }
    }

    // ------------------------------------------------------------------ habilidad (tecla X)

    public static void ability(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (s.fusion <= 0) {
            msg(p, "§7Primero fusiónate: ponte un Arete de Fusión y lanza el otro a un mob.");
            return;
        }
        if (s.fusionCd > 0) {
            msg(p, "§7Habilidad lista en " + (s.fusionCd + 19) / 20 + " s");
            return;
        }
        ServerLevel lv = p.serverLevel();
        Fusion f = Fusion.byId(s.fusion);
        Vec3 eye = p.getEyePosition(), look = p.getLookAngle();
        switch (f) {
            case ZOMBIE -> {
                for (int i = 0; i < 3; i++) {
                    Zombie z = EntityType.ZOMBIE.create(lv);
                    if (z == null) continue;
                    double a = i * 2.1;
                    z.moveTo(p.getX() + Math.cos(a) * 2, p.getY(), p.getZ() + Math.sin(a) * 2, p.getYRot(), 0);
                    z.finalizeSpawn(lv, lv.getCurrentDifficultyAt(z.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                    z.addTag("bdsz_ally");
                    z.getPersistentData().putUUID("BdszAllyOf", p.getUUID());
                    lv.addFreshEntity(z);
                    lv.sendParticles(ParticleTypes.SOUL, z.getX(), z.getY() + 0.5, z.getZ(), 15, 0.3, 0.5, 0.3, 0.02);
                }
                lv.playSound(null, p.blockPosition(), SoundEvents.ZOMBIE_AMBIENT, SoundSource.PLAYERS, 1.5F, 0.7F);
                msg(p, "§2¡Horda! 3 zombis luchan a tu lado");
                s.fusionCd = 900;
            }
            case SKELETON -> {
                Vec3 target = rayTarget(p, 40);
                for (int i = 0; i < 16; i++) {
                    Arrow a = new Arrow(lv, p);
                    a.setPos(target.x + p.getRandom().nextGaussian() * 2.5, target.y + 14 + p.getRandom().nextDouble() * 4,
                            target.z + p.getRandom().nextGaussian() * 2.5);
                    a.setDeltaMovement(0, -2.2, 0);
                    a.setBaseDamage(5.0);
                    a.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;
                    lv.addFreshEntity(a);
                }
                lv.playSound(null, p.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.5F, 0.6F);
                msg(p, "§f¡Lluvia de flechas!");
                s.fusionCd = 300;
            }
            case ENDERMAN -> {
                Vec3 t = rayTarget(p, 40);
                lv.sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 60, 0.5, 1, 0.5, 0.4);
                p.teleportTo(t.x, t.y, t.z);
                p.fallDistance = 0;
                lv.sendParticles(ParticleTypes.PORTAL, t.x, t.y + 1, t.z, 60, 0.5, 1, 0.5, 0.4);
                lv.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                s.fusionCd = 80;
            }
            case CREEPER -> {
                lv.playSound(null, p.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 1.5F, 0.8F);
                lv.explode(p, p.getX(), p.getY() + 0.5, p.getZ(), 4.5F, Level.ExplosionInteraction.MOB);
                msg(p, "§a¡Ka-boom!");
                s.fusionCd = 200;
            }
            case GOLEM -> {
                BlockPos g = p.blockPosition().below();
                lv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, lv.getBlockState(g)), p.getX(), p.getY(), p.getZ(), 120, 3, 0.3, 3, 0.5);
                lv.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY(), p.getZ(), 6, 2.5, 0.2, 2.5, 0);
                lv.playSound(null, p.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 2.0F, 0.6F);
                lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.2F);
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(6, 2, 6), e -> e != p)) {
                    e.hurt(lv.damageSources().playerAttack(p), 16.0F);
                    e.setDeltaMovement(e.getDeltaMovement().add(0, 1.1, 0));
                    e.hurtMarked = true;
                }
                s.fusionCd = 160;
            }
            case WARDEN -> {
                lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);
                for (int i = 1; i <= 24; i++) {
                    Vec3 q = eye.add(look.scale(i));
                    lv.sendParticles(ParticleTypes.SONIC_BOOM, q.x, q.y, q.z, 1, 0, 0, 0, 0);
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(q, q).inflate(1.3), e -> e != p)) {
                        if (e.invulnerableTime > 0 && e.getLastHurtByMob() == p) continue;
                        e.hurt(lv.damageSources().sonicBoom(p), 30.0F);
                        e.setDeltaMovement(e.getDeltaMovement().add(look.x * 2.0, 0.4, look.z * 2.0));
                        e.hurtMarked = true;
                    }
                }
                s.fusionCd = 120;
            }
            default -> { }
        }
        SaiyanData.setQuiet(p, s);
    }

    private static Vec3 rayTarget(ServerPlayer p, double dist) {
        Vec3 eye = p.getEyePosition();
        Vec3 end = eye.add(p.getLookAngle().scale(dist));
        BlockHitResult hit = p.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos b = hit.getBlockPos().relative(hit.getDirection());
            return new Vec3(b.getX() + 0.5, b.getY(), b.getZ() + 0.5);
        }
        return end;
    }
}
