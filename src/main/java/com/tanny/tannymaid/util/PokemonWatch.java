package com.tanny.tannymaid.util;

import com.tanny.tannymaid.cobblemon.CobblemonBridge;
import com.tanny.tannymaid.cobblemon.LucarioCompanion;
import com.tanny.tannymaid.network.Net;
import com.tanny.tannymaid.network.WatchPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Avisos de Pokémon: el Master le dice a Tanny qué Pokémon vigilar y ella avisa cuando aparece
 * uno cerca, marcando los metros hasta él en la pantalla.
 */
public final class PokemonWatch {
    private static final String KEY = "TannyWatch";
    public static final double RANGE = 64.0;
    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private static final class State {
        final Set<UUID> alerted = new LinkedHashSet<>();
        int tracked = -1;
    }

    private PokemonWatch() {}

    private static CompoundTag data(Player player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static List<String> list(Player player) {
        ListTag tag = data(player).getList(KEY, Tag.TAG_STRING);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++) out.add(tag.getString(i));
        return out;
    }

    private static void save(Player player, List<String> names) {
        ListTag tag = new ListTag();
        for (String n : names) tag.add(StringTag.valueOf(n));
        data(player).put(KEY, tag);
    }

    public static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "").replace("-", "").replace(".", "").replace("'", "");
    }

    public static boolean add(ServerPlayer player, String name) {
        List<String> names = list(player);
        String n = normalize(name);
        if (names.contains(n)) return false;
        names.add(n);
        save(player, names);
        return true;
    }

    public static boolean remove(ServerPlayer player, String name) {
        List<String> names = list(player);
        boolean ok = names.remove(normalize(name));
        save(player, names);
        if (names.isEmpty()) stopTracking(player);
        return ok;
    }

    public static void clear(ServerPlayer player) {
        save(player, new ArrayList<>());
        stopTracking(player);
    }

    public static String describe(ServerPlayer player) {
        List<String> names = list(player);
        return names.isEmpty() ? "ninguno" : String.join(", ", names);
    }

    private static void stopTracking(ServerPlayer player) {
        State st = STATES.remove(player.getUUID());
        if (st != null && st.tracked != -1) Net.send(player, WatchPacket.clear());
    }

    public static void onLogout(ServerPlayer player) {
        STATES.remove(player.getUUID());
    }

    /** Se llama cada 10 ticks por jugador. Busca el Pokémon vigilado más cercano. */
    public static void tick(ServerPlayer player) {
        if (!CobblemonBridge.isLoaded()) return;
        List<String> names = list(player);
        if (names.isEmpty() || TannyLookup.getStoredId(player) == null) return;
        Set<String> wanted = new HashSet<>(names);
        State st = STATES.computeIfAbsent(player.getUUID(), k -> new State());

        List<Mob> found = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RANGE, 40.0, RANGE),
                m -> m.isAlive() && CobblemonBridge.isPokemonEntity(m) && !LucarioCompanion.isTannyPokemon(m));
        Mob best = null;
        double bestD = Double.MAX_VALUE;
        for (Mob m : found) {
            double d = m.distanceToSqr(player);
            if (d > RANGE * RANGE || d >= bestD) continue;
            String id = CobblemonBridge.speciesId(m);
            if (id == null || !wanted.contains(normalize(id)) || !CobblemonBridge.isWild(m)) continue;
            best = m;
            bestD = d;
        }
        if (best == null) {
            if (st.tracked != -1) {
                st.tracked = -1;
                Net.send(player, WatchPacket.clear());
            }
            return;
        }
        Component name = best.getName();
        if (!st.alerted.contains(best.getUUID())) {
            st.alerted.add(best.getUUID());
            if (st.alerted.size() > 200) st.alerted.remove(st.alerted.iterator().next());
            int meters = (int) Math.round(Math.sqrt(bestD));
            player.sendSystemMessage(Component.literal("§d[Tanny] §f¡Amo, Amo! Vi un ").append(name.copy().withStyle(s -> s.withColor(0xFFD966).withBold(true)))
                    .append(Component.literal("§f a §e" + meters + " m§f, hacia el §e" + direction(player, best) + "§f. ¡Te marco el camino! ♡")));
            player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.6F);
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        st.tracked = best.getId();
        Net.send(player, new WatchPacket(true, best.getId(), name, best.getX(), best.getY() + best.getBbHeight() * 0.5, best.getZ()));
    }

    private static String direction(Player p, Mob m) {
        double ang = Math.toDegrees(Math.atan2(-(m.getX() - p.getX()), m.getZ() - p.getZ()));
        String[] dirs = {"sur", "suroeste", "oeste", "noroeste", "norte", "noreste", "este", "sureste"};
        int i = (int) Math.floorMod(Math.round(ang / 45.0), 8L);
        return dirs[i];
    }
}
