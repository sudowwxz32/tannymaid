package com.laury.laurymod.util;

import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/** Vínculo jugador ↔ Laury, guardado en el jugador (sobrevive a la muerte). */
public final class LauryLookup {
    private static final String KEY_ID = "LauryId";
    private static final String KEY_POS = "LauryPos";
    private static final String KEY_DIM = "LauryDim";

    private LauryLookup() {}

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    @Nullable
    public static UUID getStoredId(Player player) {
        CompoundTag tag = data(player);
        return tag.hasUUID(KEY_ID) ? tag.getUUID(KEY_ID) : null;
    }

    public static boolean isBound(Player player) {
        return getStoredId(player) != null;
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

    public static void storeLastPos(Player player, LauryEntity laury) {
        CompoundTag tag = data(player);
        tag.putLong(KEY_POS, laury.blockPosition().asLong());
        tag.putString(KEY_DIM, laury.level().dimension().location().toString());
    }

    @Nullable
    public static String describeLastPos(Player player) {
        CompoundTag tag = data(player);
        if (!tag.contains(KEY_POS)) return null;
        BlockPos pos = BlockPos.of(tag.getLong(KEY_POS));
        return pos.getX() + " " + pos.getY() + " " + pos.getZ() + " (" + tag.getString(KEY_DIM) + ")";
    }

    @Nullable
    public static LauryEntity find(ServerPlayer player) {
        UUID stored = getStoredId(player);
        if (stored != null) {
            for (ServerLevel level : player.getServer().getAllLevels()) {
                Entity e = level.getEntity(stored);
                if (e instanceof LauryEntity laury && laury.isAlive()) return laury;
            }
        }
        UUID owner = player.getUUID();
        for (ServerLevel level : player.getServer().getAllLevels()) {
            for (LauryEntity laury : level.getEntities(ModEntities.LAURY.get(), l -> l.isAlive() && owner.equals(l.getOwnerUUID()))) {
                setStoredId(player, laury.getUUID());
                return laury;
            }
        }
        return null;
    }
}
