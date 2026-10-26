package com.cz.data;

import com.cz.network.Net;
import com.cz.network.SyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Lectura y escritura del estado (lo que dura una transformación vive en memoria del servidor). */
public final class ZorroData {
    private static final String KEY = "ChakraZorro";
    private static final Map<UUID, int[]> LIVE = new ConcurrentHashMap<>();   // powering, total, target, charging

    private ZorroData() {}

    private static CompoundTag root(Player p) {
        CompoundTag d = p.getPersistentData();
        if (!d.contains(Player.PERSISTED_NBT_TAG)) d.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return d.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static ZorroState get(Player p) {
        ZorroState s = ZorroState.load(root(p).getCompound(KEY));
        int[] l = LIVE.get(p.getUUID());
        if (l != null) {
            s.powering = l[0];
            s.poweringTotal = l[1];
            s.target = l[2];
            s.charging = l[3] != 0;
        }
        return s;
    }

    private static void store(Player p, ZorroState s) {
        root(p).put(KEY, s.save());
        LIVE.put(p.getUUID(), new int[]{s.powering, s.poweringTotal, s.target, s.charging ? 1 : 0});
    }

    /** Guarda, aplica atributos y avisa a todos los que lo ven. */
    public static void set(ServerPlayer p, ZorroState s) {
        store(p, s);
        ZorroLogic.applyAttributes(p, s);
        Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), new SyncPacket(p.getId(), s));
    }

    /** Guarda y avisa solo al propio jugador (cambios de chakra). */
    public static void setQuiet(ServerPlayer p, ZorroState s) {
        store(p, s);
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new SyncPacket(p.getId(), s));
    }

    public static void syncTo(ServerPlayer target, ServerPlayer viewer) {
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), new SyncPacket(target.getId(), get(target)));
    }

    public static void forget(Player p) {
        LIVE.remove(p.getUUID());
    }
}
