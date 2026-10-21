package com.bdsz.data;

import com.bdsz.network.Net;
import com.bdsz.network.SyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

/** Lectura/escritura del estado en los datos persistentes del jugador (sobreviven a la muerte). */
public final class SaiyanData {
    private static final String KEY = "BlockDragonSuperZ";

    private SaiyanData() {}

    private static CompoundTag root(Player p) {
        CompoundTag data = p.getPersistentData();
        if (!data.contains(Player.PERSISTED_NBT_TAG)) data.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return data.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static SaiyanState get(Player p) {
        return SaiyanState.load(root(p).getCompound(KEY));
    }

    public static void set(ServerPlayer p, SaiyanState s) {
        root(p).put(KEY, s.save());
        PowerLogic.applyAttributes(p, s);
        sync(p, s);
    }

    public static void sync(ServerPlayer p, SaiyanState s) {
        Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), new SyncPacket(p.getId(), s));
    }

    public static void syncTo(ServerPlayer target, ServerPlayer viewer) {
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), new SyncPacket(target.getId(), get(target)));
    }
}
