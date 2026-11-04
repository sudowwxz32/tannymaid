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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

/**
 * La primera noche llega una luz verde, te eleva al cielo y te pregunta "¿Tienes miedo?".
 * Sí → vuelves a donde estabas y el mod termina para ti. No → recibes el anillo y caes en otro lugar con una explosión.
 */
public final class Abduction {
    private Abduction() {}

    public static final int LIGHT = 1, RISE = 2, ASK = 3, FALL = 4;
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
        LanternData.set(p, s);
        p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a§l...")));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§aUna luz verde baja del cielo")));
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
                // columna de luz verde que baja hasta el jugador
                for (int i = 0; i < 30; i++) {
                    double h = lv.random.nextDouble() * 60;
                    lv.sendParticles(GREEN, p.getX() + (lv.random.nextDouble() - 0.5) * 1.6, p.getY() + h, p.getZ() + (lv.random.nextDouble() - 0.5) * 1.6,
                            1, 0, 0, 0, 0);
                }
                lv.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 4, 0.4, 0.8, 0.4, 0.02);
                p.setDeltaMovement(0, 0.04, 0);
                p.hurtMarked = true;
                if (t % 20 == 0) lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 2.0F, 0.6F + t / 100F);
                if (t >= 70) {
                    s.abduct = RISE;
                    m.putInt("T", 0);
                    p.setNoGravity(true);
                    lv.playSound(null, p.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 3.0F, 0.5F);
                    LanternData.set(p, s);
                }
            }
            case RISE -> {
                // te eleva al cielo súper rápido
                p.setNoGravity(true);
                p.setDeltaMovement(0, 3.2, 0);
                p.hurtMarked = true;
                lv.sendParticles(GREEN, p.getX(), p.getY() - 1, p.getZ(), 12, 0.5, 2.0, 0.5, 0);
                int top = lv.getMaxBuildHeight() - 6;
                if (p.getY() >= top || t > 200) {
                    s.abduct = ASK;
                    m.putInt("T", 0);
                    p.setDeltaMovement(Vec3.ZERO);
                    LanternData.set(p, s);
                    Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new AskPacket());
                }
            }
            case ASK -> {
                // flota en el cielo dentro de una esfera verde hasta que responda
                p.setNoGravity(true);
                p.setDeltaMovement(Vec3.ZERO);
                p.hurtMarked = true;
                if (t % 2 == 0) {
                    for (int i = 0; i < 16; i++) {
                        double a = lv.random.nextDouble() * Math.PI * 2, b = Math.acos(2 * lv.random.nextDouble() - 1);
                        lv.sendParticles(GREEN_S, p.getX() + Math.sin(b) * Math.cos(a) * 2.2, p.getY() + 1 + Math.cos(b) * 2.2,
                                p.getZ() + Math.sin(b) * Math.sin(a) * 2.2, 1, 0, 0, 0, 0);
                    }
                }
                if (t % 100 == 0) Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new AskPacket());   // por si cerró la pantalla
            }
            case FALL -> {
                // cae como un cometa verde
                p.setNoGravity(false);
                p.setDeltaMovement(p.getDeltaMovement().x * 0.5, -3.0, p.getDeltaMovement().z * 0.5);
                p.hurtMarked = true;
                lv.sendParticles(GREEN, p.getX(), p.getY() + 2, p.getZ(), 20, 0.6, 2.0, 0.6, 0);
                lv.sendParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1, p.getZ(), 6, 0.4, 0.6, 0.4, 0.02);
                if (p.onGround() || p.isInWater() || t > 400) land(p, s);
            }
            default -> { }
        }
        LanternData.quiet(p, s);
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
        // ¡no tiene miedo! recibe el anillo y es enviado a otro lugar
        s.stage = 2;
        s.charge = LanternState.MAX_CHARGE;
        s.abduct = FALL;
        m.putInt("T", 0);
        give(p, new ItemStack(ModRegistry.RING.get()));
        give(p, new ItemStack(ModRegistry.CANDELABRA_ITEM.get()));
        ServerLevel lv = p.serverLevel();
        double a = lv.random.nextDouble() * Math.PI * 2, d = 600 + lv.random.nextDouble() * 900;
        double x = m.getDouble("X") + Math.cos(a) * d, z = m.getDouble("Z") + Math.sin(a) * d;
        p.teleportTo(x, p.getY(), z);
        p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 70, 20));
        p.connection.send(new ClientboundSetTitleTextPacket(Component.literal("§a§lEL ANILLO TE ELIGIÓ")));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§2No tienes miedo. Ahora tienes el poder.")));
        lv.playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 0.7F);
        LanternData.set(p, s);
    }

    private static void land(ServerPlayer p, LanternState s) {
        ServerLevel lv = p.serverLevel();
        BlockPos at = p.blockPosition();
        s.abduct = 0;
        LanternData.set(p, s);
        mem(p).putBoolean("Landing", true);
        // ¡BOOM! el suelo se destroza (a ti no te hace daño)
        lv.explode(p, p.getX(), p.getY(), p.getZ(), 6.0F, Level.ExplosionInteraction.TNT);
        mem(p).remove("Landing");
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, p.getX(), p.getY(), p.getZ(), 3, 1, 0.5, 1, 0);
        lv.sendParticles(GREEN, p.getX(), p.getY() + 1, p.getZ(), 300, 4, 1.5, 4, 0);
        p.fallDistance = 0;
        p.sendSystemMessage(Component.literal("§a✦ Tienes el §2Anillo Verde§a. Teclas: §fL§a volar · §fY§a armadura · §fAlt+P§a pico · "
                + "§fC§a intangible · §fV§a armas · §fAlt+M§a martillo · §fAlt+T§a traje · §fM§a catálogo. "
                + "Con el anillo en la mano, clic derecho para levantar cosas. Recárgalo con clic izquierdo usando el §2Candelabro de Poder§a."));
        if (lv.getHeight(Heightmap.Types.MOTION_BLOCKING, at.getX(), at.getZ()) < at.getY() - 3) p.fallDistance = 0;
    }

    /** ¿Debe ignorar este daño? (durante el viaje y la explosión de llegada) */
    public static boolean immune(ServerPlayer p, LanternState s) {
        return s.abduct != 0 || mem(p).getBoolean("Landing");
    }

    /** Si se desconectó a mitad del viaje, se le devuelve a donde estaba y la luz volverá otra noche. */
    public static void onLogin(ServerPlayer p, LanternState s) {
        if (s.abduct == 0) return;
        if (s.abduct == FALL) {
            s.abduct = 0;
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
