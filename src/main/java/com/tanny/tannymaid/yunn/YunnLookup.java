package com.tanny.tannymaid.yunn;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/** Busca la Yunn de un jugador (se guarda en el jugador y sobrevive a la muerte). */
public final class YunnLookup {
    private static final String KEY = "YunnNurseId";

    private YunnLookup() {}

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    @Nullable
    public static UUID getStoredId(Player player) {
        CompoundTag t = data(player);
        return t.hasUUID(KEY) ? t.getUUID(KEY) : null;
    }

    public static void setStoredId(Player player, @Nullable UUID id) {
        if (id == null) data(player).remove(KEY);
        else data(player).putUUID(KEY, id);
    }

    @Nullable
    public static YunnEntity find(ServerPlayer player) {
        UUID stored = getStoredId(player);
        if (stored != null) {
            for (ServerLevel level : player.getServer().getAllLevels()) {
                Entity e = level.getEntity(stored);
                if (e instanceof YunnEntity y && y.isAlive()) return y;
            }
        }
        return null;
    }
}
