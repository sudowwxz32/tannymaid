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
        tickKiArrows();
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
            for (Mob m : it.level().getEntitiesOfClass(Mob.class, it.getBoundingBox().inflate(10.0),
                    m -> m.isAlive() && Fusion.of(m) != Fusion.NONE && (Fusion.isDragon(m) || m.getBoundingBox().inflate(1.4).intersects(it.getBoundingBox())))) {
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
        // se guarda el mob para que vuelva a aparecer cuando se separen
        net.minecraft.nbt.CompoundTag saved = new net.minecraft.nbt.CompoundTag();
        if (mob.saveAsPassenger(saved)) fusedData(p).put(MOB_KEY, saved);
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
        boolean forever = f == Fusion.DRAGON && p.getRandom().nextFloat() < PERMANENT_CHANCE;
        if (forever) {
            s.fusionMax = -1;                 // ¡para siempre!
            s.fusionTicks = 1;
        }
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
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(forever ? "§5§lGuerrero Dragón · §d¡PARA SIEMPRE!"
                : "§eGuerrero " + f.display + " · " + f.seconds / 60 + " min")));
        if (f == Fusion.DRAGON) {
            lv.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 3.0F, 0.8F);
            lv.sendParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 300, 3, 2, 3, 0.05);
            if (forever) {
                p.sendSystemMessage(Component.literal("§5§l¡La fusión con el dragón es PERMANENTE! §r§7Para separarte usa §e/desfusionar§7 o bebe un §dElixir de Separación§7."));
            }
        }
        p.sendSystemMessage(Component.literal("§6✦ Fusión con " + f.display + ": §7" + f.ability));
        p.sendSystemMessage(Component.literal("§e✦ Afinidad: §7" + f.affinityText() + " §8(consumen menos tiempo y dan más poder)"));
        p.setHealth(p.getMaxHealth());
        SaiyanData.set(p, s);
    }

    /** Se separan: se acaba el tiempo o el jugador muere. Los aretes se rompen. */
    private static final String MOB_KEY = "BdszFusedMob";

    private static net.minecraft.nbt.CompoundTag fusedData(ServerPlayer p) {
        net.minecraft.nbt.CompoundTag root = p.getPersistentData();
        if (!root.contains(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG)) root.put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, new net.minecraft.nbt.CompoundTag());
        return root.getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
    }

    /** El mob con el que te fusionaste vuelve a aparecer a tu lado. */
    private static void releaseMob(ServerPlayer p) {
        net.minecraft.nbt.CompoundTag data = fusedData(p);
        if (!data.contains(MOB_KEY)) return;
        net.minecraft.nbt.CompoundTag tag = data.getCompound(MOB_KEY);
        data.remove(MOB_KEY);
        ServerLevel lv = p.serverLevel();
        Vec3 look = p.getLookAngle();
        double x = p.getX() + look.x * 2.0, z = p.getZ() + look.z * 2.0;
        net.minecraft.world.entity.Entity e = EntityType.loadEntityRecursive(tag, lv, en -> {
            en.moveTo(x, p.getY(), z, en.getYRot(), en.getXRot());
            return en;
        });
        if (e == null) return;
        if (e instanceof Mob m) {
            m.setNoAi(false);
            m.setInvulnerable(false);
            m.setGlowingTag(false);
            m.setHealth(m.getMaxHealth());
        }
        lv.addFreshEntity(e);
        lv.sendParticles(GOLD, e.getX(), e.getY() + 1, e.getZ(), 60, 0.5, 0.8, 0.5, 0);
        lv.sendParticles(ParticleTypes.FLASH, e.getX(), e.getY() + 1, e.getZ(), 1, 0, 0, 0, 0);
    }

    public static void defuse(ServerPlayer p, SaiyanState s, boolean quiet) {
        Fusion f = Fusion.byId(s.fusion);
        boolean wasFused = s.fusion > 0;
        s.fusion = 0;
        s.fusionTicks = s.fusionMax = 0;
        s.earring = false;
        s.fusing = 0;
        s.fusionAnim = 0;
        CREEPER_FUSE.remove(p.getUUID());
        // al separarse: vuelves al estado base, sin energía y cansado por unos segundos
        s.transformed = false;
        s.powering = 0;
        s.charging = false;
        s.ki = 0;
        if (wasFused) {
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1));
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 1));
            p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 240, 1));
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0));
            p.removeEffect(MobEffects.HEALTH_BOOST);
        }
        PENDING.remove(p.getUUID());
        Mob m = PULLING.remove(p.getUUID());
        if (m != null) fusedData(p).remove(MOB_KEY);   // el mob aún existe: no duplicarlo
        releaseMob(p);
        if (m != null) {
            m.setNoAi(false);
            m.setInvulnerable(false);
            m.setGlowingTag(false);
        }
        if (!quiet) {
            ServerLevel lv = p.serverLevel();
            lv.sendParticles(GOLD, p.getX(), p.getY() + 1, p.getZ(), 120, 1.2, 1.2, 1.2, 0);
            lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.5F, 0.8F);
            p.sendSystemMessage(Component.literal("§6✦ La fusión con " + f.display + " terminó. Los aretes se rompieron. §7Estás agotado y sin energía..."));
        }
        SaiyanData.set(p, s);
    }

    /** Tecla N: separarse a voluntad (los aretes se rompen). */
    public static final float PERMANENT_CHANCE = 0.25F;

    public static void defuseByChoice(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (s.fusion <= 0 && s.fusing <= 0) {
            msg(p, "§7No estás fusionado.");
            return;
        }
        if (s.fusionPermanent()) {
            msg(p, "§5La fusión con el dragón es permanente. §7Usa §e/desfusionar§7 o bebe un §dElixir de Separación§7.");
            return;
        }
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 1, p.getZ(), 1, 0, 0, 0, 0);
        lv.playSound(null, p.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.8F);
        defuse(p, s, false);
    }

    /** Separación forzada (comando /desfusionar o Elixir de Separación): funciona incluso con la fusión permanente. */
    public static boolean forceDefuse(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (s.fusion <= 0 && s.fusing <= 0) {
            msg(p, "§7No estás fusionado.");
            return false;
        }
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 1, p.getZ(), 2, 0, 0, 0, 0);
        lv.sendParticles(ParticleTypes.DRAGON_BREATH, p.getX(), p.getY() + 1, p.getZ(), 80, 0.8, 1.0, 0.8, 0.05);
        lv.playSound(null, p.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 0.6F);
        defuse(p, s, false);
        return true;
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
        int animWas = s.fusionAnim;
        if (s.fusionAnim > 0) s.fusionAnim--;
        tickCreeperFuse(p, s);
        if (s.fusion <= 0) return;
        if (s.fusionPermanent()) {
            if (p.tickCount % 20 == 0) effects(p, Fusion.byId(s.fusion));
            SaiyanData.setQuiet(p, s);
            if (s.fusionAnim > 0 && s.fusionAnim % 3 == 0 || s.fusionAnim == 0 && animWas > 0) SaiyanData.sync(p, s);
            return;
        }
        // transformarse gasta más rápido el tiempo de la fusión; el Kaioken aún más
        int drain = 1;
        if (s.transformed && s.activeForm() != Form.BASE && !s.fusionAffinity()) drain += 1 + s.activeForm().ordinal() / 5;
        drain += s.activeKaioken() / 25;
        int before = s.fusionTicks;
        s.fusionTicks -= drain;
        if (s.fusionTicks <= 0) {
            defuse(p, s, false);
            return;
        }
        if ((before > 600 && s.fusionTicks <= 600) || (before > 200 && s.fusionTicks <= 200)) {
            msg(p, "§eLa fusión termina en " + s.fusionTicks / 20 + " s");
        }
        if (p.tickCount % 20 == 0) effects(p, Fusion.byId(s.fusion));
        SaiyanData.setQuiet(p, s);
        // los demás jugadores ven el arco de ki / la mecha del creeper
        if (s.fusionAnim > 0 && s.fusionAnim % 3 == 0 || s.fusionAnim == 0 && animWas > 0) SaiyanData.sync(p, s);
    }

    private static void eff(ServerPlayer p, net.minecraft.world.effect.MobEffect e, int amp) {
        p.addEffect(new MobEffectInstance(e, 60, amp, true, false, true));
    }

    private static void effects(ServerPlayer p, Fusion f) {
        // tu misma especie te reconoce como uno de los suyos: deja de atacarte y se calma
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(32), m -> Fusion.of(m) == f)) {
            if (m.getTarget() == p) {
                m.setTarget(null);
                m.getNavigation().stop();
            }
            m.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET).ifPresent(t -> {
                if (t == p) m.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
            });
            if (m instanceof net.minecraft.world.entity.monster.warden.Warden w) {
                w.clearAnger(p);
                w.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ROAR_TARGET);
            }
            if (m instanceof net.minecraft.world.entity.monster.Creeper c && c.getTarget() == null) c.setSwellDir(-1);
            if (m instanceof net.minecraft.world.entity.animal.IronGolem g) g.stopBeingAngry();
        }
        switch (f) {
            case ZOMBIE -> {
                eff(p, MobEffects.DAMAGE_BOOST, 1);
                eff(p, MobEffects.REGENERATION, 0);
                p.removeEffect(MobEffects.HUNGER);
                p.getFoodData().setFoodLevel(20);            // no necesitas comer
                p.getFoodData().setSaturation(5.0F);
            }
            case SKELETON -> { eff(p, MobEffects.MOVEMENT_SPEED, 1); eff(p, MobEffects.JUMP, 0); eff(p, MobEffects.DAMAGE_BOOST, 0); }
            case ENDERMAN -> { eff(p, MobEffects.NIGHT_VISION, 0); eff(p, MobEffects.MOVEMENT_SPEED, 0); eff(p, MobEffects.DAMAGE_BOOST, 0); }
            case CREEPER -> { eff(p, MobEffects.MOVEMENT_SPEED, 0); eff(p, MobEffects.DAMAGE_BOOST, 0); }
            case GOLEM -> { eff(p, MobEffects.DAMAGE_BOOST, 2); eff(p, MobEffects.DAMAGE_RESISTANCE, 1); }
            case DRAGON -> {
                eff(p, MobEffects.DAMAGE_BOOST, 4);
                eff(p, MobEffects.DAMAGE_RESISTANCE, 3);
                eff(p, MobEffects.HEALTH_BOOST, 9);
                eff(p, MobEffects.REGENERATION, 2);
                eff(p, MobEffects.FIRE_RESISTANCE, 0);
                eff(p, MobEffects.MOVEMENT_SPEED, 1);
                eff(p, MobEffects.NIGHT_VISION, 0);
                if (p.isShiftKeyDown() && !p.onGround()) eff(p, MobEffects.SLOW_FALLING, 0);   // planea con las alas
                p.serverLevel().sendParticles(ParticleTypes.DRAGON_BREATH, p.getX(), p.getY() + 1.2, p.getZ(), 6, 0.5, 0.6, 0.5, 0.01);
                // engreído: se burla de sus enemigos
                if (p.getRandom().nextInt(45) == 0) {
                    String[] t = {"§5[Guerrero Dragón] §f¿Eso es todo? Ni siquiera me despeiné.", "§5[Guerrero Dragón] §fArrodíllense ante el dragón.",
                            "§5[Guerrero Dragón] §fSoy la fusión más poderosa que existe. Admírenme.", "§5[Guerrero Dragón] §fJa. Insectos."};
                    p.sendSystemMessage(Component.literal(t[p.getRandom().nextInt(t.length)]));
                }
            }
            case WARDEN -> {
                eff(p, MobEffects.DAMAGE_BOOST, 3);
                eff(p, MobEffects.DAMAGE_RESISTANCE, 2);
                eff(p, MobEffects.HEALTH_BOOST, 4);
                eff(p, MobEffects.NIGHT_VISION, 0);
                for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(10), m -> m instanceof Enemy)) {
                    m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                    m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
                }
                // siente las vibraciones: todo lo que se mueve cerca brilla
                for (LivingEntity le : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(24),
                        le -> le != p && le.getDeltaMovement().lengthSqr() > 0.0004)) {
                    le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
                }
                // oscuridad a los demás jugadores cercanos (como el warden)
                if (p.tickCount % 200 == 0) {
                    for (ServerPlayer o : p.serverLevel().getEntitiesOfClass(ServerPlayer.class, p.getBoundingBox().inflate(20), o -> o != p)) {
                        o.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 260, 0));
                    }
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
                // crea un arco de ki amarillo (como el Puño del Dragón) y dispara flechas de energía
                s.fusionAnim = 24;
                float dmg = 2.0F + s.damageMultiplier() * 0.25F;
                for (int i = -2; i <= 2; i++) {
                    Arrow a = new Arrow(lv, p);
                    a.setPos(eye.x + look.x * 0.8, eye.y - 0.15, eye.z + look.z * 0.8);
                    float yaw = p.getYRot() + i * 4.0F;
                    a.shootFromRotation(p, p.getXRot(), yaw, 0.0F, 3.6F, 0.2F);
                    a.setNoGravity(true);
                    a.setCritArrow(true);
                    a.setGlowingTag(true);
                    a.setBaseDamage(dmg);
                    a.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED;
                    a.addTag("bdsz_ki_arrow");
                    lv.addFreshEntity(a);
                    KI_ARROWS.add(a);
                }
                lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.2F, 1.8F);
                lv.playSound(null, p.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.5F, 0.6F);
                s.fusionCd = 40;
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
                // como un creeper: sisea, se infla y parpadea... ¡y explota! (a ti no te hace daño)
                lv.playSound(null, p.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 1.5F, 0.8F);
                CREEPER_FUSE.put(p.getUUID(), 30);
                s.fusionAnim = 30;
                msg(p, "§a¡Ssssss...!");
                s.fusionCd = 220;
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
            case DRAGON -> {
                // aliento de dragón: bola de fuego del End que deja una nube dañina
                lv.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 2.0F, 1.0F);
                lv.playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 1.3F);
                for (int i = -1; i <= 1; i++) {
                    net.minecraft.world.entity.projectile.DragonFireball fb = new net.minecraft.world.entity.projectile.DragonFireball(lv, p,
                            look.x + i * 0.12, look.y, look.z + i * 0.12);
                    fb.setPos(eye.x + look.x * 1.5, eye.y - 0.2, eye.z + look.z * 1.5);
                    lv.addFreshEntity(fb);
                }
                for (int i = 1; i <= 16; i++) {
                    Vec3 q = eye.add(look.scale(i));
                    lv.sendParticles(ParticleTypes.DRAGON_BREATH, q.x, q.y, q.z, 8, 0.4, 0.4, 0.4, 0.02);
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(q, q).inflate(1.5), e -> e != p)) {
                        e.hurt(lv.damageSources().indirectMagic(p, p), 40.0F);
                    }
                }
                s.fusionCd = 100;
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

    private static final Map<UUID, Integer> CREEPER_FUSE = new ConcurrentHashMap<>();
    private static final java.util.Set<Arrow> KI_ARROWS = ConcurrentHashMap.newKeySet();
    private static final DustParticleOptions KI_YELLOW = new DustParticleOptions(new Vector3f(1.0F, 0.9F, 0.25F), 1.4F);

    private static void tickCreeperFuse(ServerPlayer p, SaiyanState s) {
        Integer left = CREEPER_FUSE.get(p.getUUID());
        if (left == null) return;
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(ParticleTypes.SMOKE, p.getX(), p.getY() + 1.6, p.getZ(), 3, 0.2, 0.2, 0.2, 0.01);
        if (left % 6 == 0) lv.sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 1, p.getZ(), 1, 0, 0, 0, 0);
        p.setDeltaMovement(p.getDeltaMovement().multiply(0.3, 1, 0.3));
        if (left <= 1) {
            CREEPER_FUSE.remove(p.getUUID());
            float power = 6.0F * (s.fusionAffinity() ? 1.4F : 1.0F);
            lv.explode(p, p.getX(), p.getY() + 0.5, p.getZ(), power, Level.ExplosionInteraction.MOB);
            msg(p, "§a¡BOOOM!");
        } else {
            CREEPER_FUSE.put(p.getUUID(), left - 1);
        }
    }

    /** Estela amarilla de las flechas de ki; desaparecen al chocar. */
    public static void tickKiArrows() {
        for (Iterator<Arrow> it = KI_ARROWS.iterator(); it.hasNext(); ) {
            Arrow a = it.next();
            if (!a.isAlive() || a.tickCount > 80) {
                if (a.isAlive()) a.discard();
                it.remove();
                continue;
            }
            if (a.level() instanceof ServerLevel lv) {
                lv.sendParticles(KI_YELLOW, a.getX(), a.getY(), a.getZ(), 3, 0.05, 0.05, 0.05, 0);
                if (a.tickCount > 2 && a.position().distanceToSqr(a.xo, a.yo, a.zo) < 1.0E-4) {
                    lv.sendParticles(ParticleTypes.FLASH, a.getX(), a.getY(), a.getZ(), 1, 0, 0, 0, 0);
                    lv.sendParticles(KI_YELLOW, a.getX(), a.getY(), a.getZ(), 20, 0.3, 0.3, 0.3, 0);
                    a.discard();
                    it.remove();
                }
            }
        }
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
