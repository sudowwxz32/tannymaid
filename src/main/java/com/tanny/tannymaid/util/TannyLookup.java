package com.tanny.tannymaid.util;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/** Busca la Tanny de un jugador y guarda datos del vínculo en el jugador (sobreviven a la muerte). */
public final class TannyLookup {
    private static final String KEY_ID = "TannyMaidId";
    private static final String KEY_POS = "TannyMaidPos";
    private static final String KEY_DIM = "TannyMaidDim";

    private TannyLookup() {}

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) {
            root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    @Nullable
    public static UUID getStoredId(Player player) {
        CompoundTag tag = data(player);
        return tag.hasUUID(KEY_ID) ? tag.getUUID(KEY_ID) : null;
    }

    public static void setStoredId(Player player, @Nullable UUID id) {
        CompoundTag tag = data(player);
        if (id == null) {
            tag.remove(KEY_ID);
            tag.remove(KEY_POS);
            tag.remove(KEY_DIM);
        } else {
            tag.putUUID(KEY_ID, id);
        }
    }

    public static void storeLastPos(Player player, TannyEntity tanny) {
        CompoundTag tag = data(player);
        tag.putLong(KEY_POS, tanny.blockPosition().asLong());
        tag.putString(KEY_DIM, tanny.level().dimension().location().toString());
    }

    @Nullable
    public static String describeLastPos(Player player) {
        CompoundTag tag = data(player);
        if (!tag.contains(KEY_POS)) return null;
        BlockPos pos = BlockPos.of(tag.getLong(KEY_POS));
        return pos.getX() + " " + pos.getY() + " " + pos.getZ() + " (" + tag.getString(KEY_DIM) + ")";
    }

    /** Devuelve la Tanny del jugador si está cargada en alguna dimensión. */
    @Nullable
    public static TannyEntity find(ServerPlayer player) {
        UUID stored = getStoredId(player);
        if (stored != null) {
            for (ServerLevel level : player.getServer().getAllLevels()) {
                Entity e = level.getEntity(stored);
                if (e instanceof TannyEntity tanny && tanny.isAlive()) {
                    return tanny;
                }
            }
        }
        UUID owner = player.getUUID();
        for (ServerLevel level : player.getServer().getAllLevels()) {
            for (TannyEntity tanny : level.getEntities(ModEntities.TANNY.get(),
                    t -> t.isAlive() && owner.equals(t.getOwnerUUID()))) {
                setStoredId(player, tanny.getUUID());
                return tanny;
            }
        }
        return null;
    }
}
