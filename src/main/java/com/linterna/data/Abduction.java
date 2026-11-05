package com.linterna.data;

import com.linterna.network.AskPacket;
import com.linterna.network.Net;
import com.linterna.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

/**
 * La primera noche llega una luz verde, te eleva al cielo y te pregunta "¿Tienes miedo?".
 * Sí → vuelves a donde estabas y el mod termina para ti. No → recibes el anillo y caes en otro lugar con una explosión.
 */
public final class Abduction {
    private Abduction() {}

    public static final int LIGHT = 1, RISE = 2, ASK = 3, FALL = 4, LAND = 5;
    private static final DustParticleOptions GREEN = new DustParticleOptions(new Vector3f(0.25F, 1.0F, 0.35F), 2.5F);
    private static final DustParticleOptions GREEN_S = new DustParticleOptions(new Vector3f(0.5F, 1.0F, 0.55F), 1.2F);

    private static CompoundTag mem(ServerPlayer p) {
        CompoundTag r = LanternData.root(p);
        if (!r.contains("LinternaTrip")) r.put("LinternaTrip", new CompoundTag());
        return r.getCompound("LinternaTrip");
    }

    /** ¿Ya es hora? La primera noche en el mundo normal. */
    public static void check(ServerPlayer p, LanternState s) {
        if (s.stage != 0 || s.abduct != 0 || p.isSpectator() || p.level().dimension() != Level.OVERWORLD) return;
        if (p.tickCount < 200 || !p.level().isNight() || p.isSleeping()) return;
        begin(p, s);
    }

    public static void begin(ServerPlayer p, LanternState s) {
        CompoundTag m = mem(p);
        m.putDouble("X", p.getX());
        m.putDouble("Y", p.getY());
        m.putDouble("Z", p.getZ());
        m.putInt("T", 0);
        s.abduct = LIGHT;
        p.setNoGravity(true);
        LanternData.set(p, s);
        p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a§l. . .")));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§aEl cielo se ilumina de verde...")));
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 0.6F);
    }

    /** Cada tick mientras dura el viaje. */
    public static void tick(ServerPlayer p, LanternState s) {
        if (s.abduct == 0) return;
        ServerLevel lv = p.serverLevel();
        CompoundTag m = mem(p);
        int t = m.getInt("T") + 1;
        m.putInt("T", t);
        p.fallDistance = 0;
        switch (s.abduct) {
            case LIGHT -> {
                p.setNoGravity(true);
                if (t < STRIKE) {
                    // el rayo de luz verde baja del cielo (lo dibuja el cliente); chispas cayendo
                    p.setDeltaMovement(Vec3.ZERO);
                    double h = 80 * (1 - t / (double) STRIKE);
                    lv.sendParticles(GREEN, p.getX(), p.getY() + h, p.getZ(), 20, 0.6, 3, 0.6, 0);
                    if (t == 1) lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 3.0F, 0.5F);
                } else if (t == STRIKE) {
                    // ¡IMPACTO! cae directo sobre el jugador
                    for (int i = 0; i < 4; i++) {
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(lv);
                        if (bolt == null) continue;
                        double a = i * Math.PI / 2 + lv.random.nextDouble();
                        bolt.moveTo(p.getX() + Math.cos(a) * 6, p.getY(), p.getZ() + Math.sin(a) * 6);
                        bolt.setVisualOnly(true);
                        lv.addFreshEntity(bolt);
                    }
                    lv.playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 6.0F, 0.6F);
                    lv.playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 4.0F, 0.5F);
                    lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 4.0F, 0.5F);
                    lv.sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 1, p.getZ(), 3, 0, 0, 0, 0);
                    lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.getX(), p.getY(), p.getZ(), 1, 0, 0, 0, 0);
                    lv.sendParticles(GREEN, p.getX(), p.getY() + 0.2, p.getZ(), 400, 5, 0.3, 5, 0);
                    p.connection.send(new ClientboundSetTitlesAnimationPacket(0, 50, 15));
                    p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a§l⚡ ¡¡LA LUZ VERDE!! ⚡")));
                    p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§2Un ciclón de energía te envuelve...")));
                    // los monstruos de alrededor salen volando
                    for (net.minecraft.world.entity.LivingEntity e : lv.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                            p.getBoundingBox().inflate(8), e -> e != p)) {
                        Vec3 d = e.position().subtract(p.position()).normalize().scale(1.8);
                        e.setDeltaMovement(d.x, 0.9, d.z);
                        e.hurtMarked = true;
                    }
                } else {
                    // el ciclón verde se forma y lo empieza a levantar
                    cyclone(lv, p, t, 1.0 + (t - STRIKE) / 30.0);
                    p.setDeltaMovement(0, Math.min(0.25, (t - STRIKE) * 0.006), 0);
                    p.hurtMarked = true;
                    if (t % 10 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.5F, 0.6F + t / 160F);
                    if (t >= STRIKE + 50) {
                        s.abduct = RISE;
                        m.putInt("T", 0);
                        lv.playSound(null, p.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 4.0F, 0.4F);
                        lv.playSound(null, p.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0F, 1.2F);
                        LanternData.set(p, s);
                    }
                }
            }
            case RISE -> {
                // lo lleva al cielo dentro del ciclón, intangible (atraviesa techos y montañas)
                p.setNoGravity(true);
                p.setDeltaMovement(0, Math.min(3.0, 0.5 + t * 0.12), 0);
                p.hurtMarked = true;
                cyclone(lv, p, t, 2.2);
                lv.sendParticles(GREEN, p.getX(), p.getY() - 2, p.getZ(), 25, 0.8, 3.0, 0.8, 0);
                if (t % 6 == 0) lv.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY(), p.getZ(), 10, 1.5, 2, 1.5, 0.05);
                if (t % 15 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 2.0F, 1.2F);
                int top = lv.getMaxBuildHeight() - 6;
                if (p.getY() >= top || t > 240) {
                    s.abduct = ASK;
                    m.putInt("T", 0);
                    p.setDeltaMovement(Vec3.ZERO);
                    LanternData.set(p, s);
                    lv.playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 5.0F, 0.4F);
                    lv.playSound(null, p.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6F, 1.8F);
                    Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new AskPacket());
                }
            }
            case ASK -> {
                // flota en el cielo dentro de un ciclón y una esfera verde gigantes hasta que responda
                p.setNoGravity(true);
                p.setDeltaMovement(Vec3.ZERO);
                p.hurtMarked = true;
                cyclone(lv, p, t, 3.0);
                if (t % 2 == 0) {
                    for (int i = 0; i < 24; i++) {
                        double a = lv.random.nextDouble() * Math.PI * 2, b = Math.acos(2 * lv.random.nextDouble() - 1), r = 3.5 + Math.sin(t * 0.1) * 0.4;
                        lv.sendParticles(GREEN_S, p.getX() + Math.sin(b) * Math.cos(a) * r, p.getY() + 1 + Math.cos(b) * r,
                                p.getZ() + Math.sin(b) * Math.sin(a) * r, 1, 0, 0, 0, 0);
                    }
                }
                if (t % 60 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0F, 0.5F);
                if (t % 100 == 0) Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new AskPacket());   // por si cerró la pantalla
            }
            case FALL -> {
                // cae como un cometa verde; cerca del suelo frena para un aterrizaje de héroe
                p.setNoGravity(false);
                int ground = lv.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getBlockX(), p.getBlockZ());
                double above = p.getY() - ground;
                double vy = above > 40 ? -3.2 : above > 12 ? -2.0 : -1.4;
                p.setDeltaMovement(0, vy, 0);
                p.hurtMarked = true;
                lv.sendParticles(GREEN, p.getX(), p.getY() + 2, p.getZ(), 30, 0.5, 2.5, 0.5, 0);
                lv.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 6, 0.3, 1.5, 0.3, 0.02);
                if (t % 4 == 0) lv.sendParticles(ParticleTypes.FIREWORK, p.getX(), p.getY() + 1, p.getZ(), 8, 0.3, 0.8, 0.3, 0.05);
                if (t % 10 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, SoundSource.PLAYERS, 2.0F, 0.6F);
                if (p.onGround() || p.isInWater() || above < 1.2 || t > 400) land(p, s);
            }
            case LAND -> wave(p, s, lv, m, t);
            default -> { }
        }
        LanternData.quiet(p, s);
    }

    private static final int STRIKE = 20;

    /** Ciclón verde: espirales de luz que giran alrededor del jugador. */
    private static void cyclone(ServerLevel lv, ServerPlayer p, int t, double size) {
        for (int arm = 0; arm < 3; arm++) {
            for (int i = 0; i < 14; i++) {
                double h = i * 0.35 * size - 1.0;
                double r = (0.6 + i * 0.16) * size;
                double a = t * 0.45 + i * 0.5 + arm * Math.PI * 2 / 3;
                lv.sendParticles(i % 3 == 0 ? GREEN : GREEN_S, p.getX() + Math.cos(a) * r, p.getY() + h, p.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
            }
        }
        if (t % 3 == 0) lv.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1, p.getZ(), 6, size, size, size, 0);
    }

    /** La respuesta del jugador. */
    public static void answer(ServerPlayer p, boolean afraid) {
        LanternState s = LanternData.get(p);
        if (s.abduct != ASK) return;
        CompoundTag m = mem(p);
        p.setNoGravity(false);
        if (afraid) {
            // vuelve al sitio de donde lo recogió la luz y el mod termina para él
            s.abduct = 0;
            s.stage = 1;
            p.teleportTo(m.getDouble("X"), m.getDouble("Y"), m.getDouble("Z"));
            p.fallDistance = 0;
            p.serverLevel().sendParticles(GREEN, p.getX(), p.getY() + 1, p.getZ(), 80, 0.6, 1.2, 0.6, 0);
            p.sendSystemMessage(Component.literal("§7La luz verde te devuelve a casa... y se apaga para siempre."));
            LanternData.set(p, s);
            return;
        }
        // ¡no tiene miedo! el anillo va directo a su dedo y cae en otro lugar
        s.stage = 2;
        s.charge = LanternState.MAX_CHARGE;
        s.abduct = FALL;
        m.putInt("T", 0);
        if (!RingSlot.worn(p)) RingSlot.set(p, new ItemStack(ModRegistry.RING.get()));
        else give(p, new ItemStack(ModRegistry.RING.get()));
        s.worn = true;
        give(p, new ItemStack(ModRegistry.CANDELABRA_ITEM.get()));
        ServerLevel lv = p.serverLevel();
        double x = 0, z = 0;
        for (int tries = 0; tries < 12; tries++) {            // busca tierra firme (no océano ni lava)
            double a = lv.random.nextDouble() * Math.PI * 2, d = 600 + lv.random.nextDouble() * 900;
            x = m.getDouble("X") + Math.cos(a) * d;
            z = m.getDouble("Z") + Math.sin(a) * d;
            BlockPos top = lv.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(x, 0, z));
            if (lv.getFluidState(top.below()).isEmpty() && lv.getFluidState(top).isEmpty()) break;
        }
        p.teleportTo(x, p.getY(), z);
        p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 70, 20));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a§lEL ANILLO TE ELIGIÓ")));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§2No tienes miedo. Ahora tienes el poder.")));
        lv.playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 0.7F);
        lv.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 80, 0.6, 1, 0.6, 0.4);
        LanternData.set(p, s);
    }

    private static void land(ServerPlayer p, LanternState s) {
        ServerLevel lv = p.serverLevel();
        s.abduct = LAND;
        mem(p).putInt("T", 0);
        mem(p).putBoolean("Landing", true);
        LanternData.set(p, s);
        // aterrizaje de héroe: un cráter limpio y una onda expansiva verde (sin destrozar todo)
        BlockPos c = p.blockPosition();
        int r = 4;
        for (BlockPos q : BlockPos.betweenClosed(c.offset(-r, -3, -r), c.offset(r, 2, r))) {
            double dh = Math.sqrt((q.getX() - c.getX()) * (q.getX() - c.getX()) + (q.getZ() - c.getZ()) * (q.getZ() - c.getZ()));
            if (dh > r) continue;
            int depth = (int) Math.round(2.5 * (1 - dh / r));
            int dy = q.getY() - c.getY();
            BlockState st = lv.getBlockState(q);
            if (st.isAir() || lv.getBlockEntity(q) != null || st.getDestroySpeed(lv, q) < 0) continue;
            boolean plant = st.canBeReplaced() || st.is(BlockTags.FLOWERS) || st.is(BlockTags.SAPLINGS);
            boolean soft = st.is(BlockTags.DIRT) || st.is(BlockTags.SAND) || st.is(BlockTags.BASE_STONE_OVERWORLD) || st.is(Blocks.GRAVEL)
                    || st.is(Blocks.SNOW_BLOCK) || st.is(Blocks.SNOW) || st.is(BlockTags.TERRACOTTA);
            if ((plant && dy >= -1) || (soft && dy < 0 && dy >= -depth)) {
                lv.setBlock(q, Blocks.AIR.defaultBlockState(), 3);
                if (lv.random.nextInt(3) == 0) lv.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, st),
                        q.getX() + 0.5, q.getY() + 1, q.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.2);
            }
        }
        lv.explode(p, p.getX(), p.getY(), p.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.getX(), p.getY(), p.getZ(), 2, 1, 0.3, 1, 0);
        lv.sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 1, p.getZ(), 1, 0, 0, 0, 0);
        lv.sendParticles(GREEN, p.getX(), p.getY() + 1, p.getZ(), 300, 3, 1.5, 3, 0);
        lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0F, 0.5F);
        lv.playSound(null, p.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0F, 0.4F);
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 3.0F, 0.6F);
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 30, 5, false, false));
        p.fallDistance = 0;
        p.setDeltaMovement(Vec3.ZERO);
        Glow.on(p);
    }

    /** Después del impacto: tres ondas expansivas verdes que empujan a todo lo que hay cerca. */
    private static void wave(ServerPlayer p, LanternState s, ServerLevel lv, CompoundTag m, int t) {
        p.fallDistance = 0;
        for (int w = 0; w < 3; w++) {
            int k = t - w * 8;
            if (k <= 0 || k > 20) continue;
            double r = k * 0.75;
            for (int i = 0; i < 48; i++) {
                double a = i * Math.PI * 2 / 48;
                lv.sendParticles(i % 2 == 0 ? GREEN : GREEN_S, p.getX() + Math.cos(a) * r, p.getY() + 0.2, p.getZ() + Math.sin(a) * r, 1, 0, 0.05, 0, 0);
            }
            if (k % 4 == 0) lv.sendParticles(ParticleTypes.POOF, p.getX(), p.getY() + 0.2, p.getZ(), 20, r * 0.5, 0.1, r * 0.5, 0.05);
            for (net.minecraft.world.entity.LivingEntity e : lv.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                    p.getBoundingBox().inflate(r + 1, 2, r + 1), e -> e != p)) {
                double d = e.distanceTo(p);
                if (Math.abs(d - r) > 1.2) continue;
                Vec3 push = e.position().subtract(p.position()).normalize().scale(1.2);
                e.setDeltaMovement(push.x, 0.5, push.z);
                e.hurtMarked = true;
                if (e instanceof net.minecraft.world.entity.monster.Enemy) e.hurt(lv.damageSources().playerAttack(p), 6.0F);
            }
        }
        // se levanta del cráter brillando
        if (t % 2 == 0) lv.sendParticles(GREEN, p.getX(), p.getY() + 1, p.getZ(), 6, 0.4, 0.9, 0.4, 0);
        if (t == 30) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 60, 20));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a§l✦ PORTADOR DEL ANILLO ✦")));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§2El anillo ya está en tu dedo · M para el catálogo")));
            lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.5F, 0.8F);
        }
        if (t >= 44) {
            s.abduct = 0;
            m.remove("Landing");
            Glow.off(p);
            LanternData.set(p, s);
            p.sendSystemMessage(Component.literal("§a§l✦ Tienes el Anillo Verde §r§a(puesto en su ranura: lo ves en el catálogo §fM§a)."));
            p.sendSystemMessage(Component.literal("§a  Teclas: §fL§a volar · §fY§a armadura · §fAlt+P§a pico · §fC§a intangible · §fV§a armas · "
                    + "§fAlt+M§a martillo · §fAlt+T§a traje · §fM§a catálogo"));
            p.sendSystemMessage(Component.literal("§a  Clic derecho sobre algo: rayo de selección para levantarlo · clic al aire: disparos de energía"));
            p.sendSystemMessage(Component.literal("§a  El anillo siempre te protege y nunca lo pierdes al morir. Recárgalo con la §2Lámpara de Poder§a."));
        }
    }

    /** ¿Debe ignorar este daño? (durante el viaje y la explosión de llegada) */
    public static boolean immune(ServerPlayer p, LanternState s) {
        return s.abduct != 0 || mem(p).getBoolean("Landing");
    }

    /** Si se desconectó a mitad del viaje, se le devuelve a donde estaba y la luz volverá otra noche. */
    public static void onLogin(ServerPlayer p, LanternState s) {
        if (s.abduct == 0) return;
        if (s.abduct == FALL || s.abduct == LAND) {
            s.abduct = 0;
            mem(p).remove("Landing");
        } else {
            CompoundTag m = mem(p);
            p.teleportTo(m.getDouble("X"), m.getDouble("Y"), m.getDouble("Z"));
            s.abduct = 0;
        }
        p.setNoGravity(false);
        LanternData.set(p, s);
    }

    private static void give(ServerPlayer p, ItemStack st) {
        if (!p.getInventory().add(st)) p.drop(st, false);
    }
}
