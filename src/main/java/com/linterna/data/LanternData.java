package com.linterna.data;

import com.linterna.network.Net;
import com.linterna.network.SyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Guarda el estado en el jugador (persistente) y lo sincroniza con los clientes. */
public final class LanternData {
    private static final String KEY = "Linterna";
    private static final Map<UUID, int[]> LIVE = new ConcurrentHashMap<>();   // martillo, recarga, sostenido

    private LanternData() {}

    public static CompoundTag root(Player p) {
        CompoundTag d = p.getPersistentData();
        if (!d.contains(Player.PERSISTED_NBT_TAG)) d.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return d.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static LanternState get(Player p) {
        LanternState s = LanternState.load(root(p).getCompound(KEY));
        int[] l = LIVE.get(p.getUUID());
        if (l != null) {
            s.hammer = l[0];
            s.recharge = l[1];
            s.held = l[2];
        }
        return s;
    }

    private static void store(Player p, LanternState s) {
        root(p).put(KEY, s.save());
        LIVE.put(p.getUUID(), new int[]{s.hammer, s.recharge, s.held});
    }

    /** Guarda y avisa a todos los que lo ven. */
    public static void set(ServerPlayer p, LanternState s) {
        store(p, s);
        sync(p, s);
    }

    /** Solo guarda (sin enviar). */
    public static void quiet(ServerPlayer p, LanternState s) {
        store(p, s);
    }

    public static void sync(ServerPlayer p, LanternState s) {
        Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), new SyncPacket(p.getId(), s));
    }

    public static void forget(Player p) {
        LIVE.remove(p.getUUID());
    }
}
