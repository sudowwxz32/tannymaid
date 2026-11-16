package com.totallywaifu.data;

import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.network.CollectionData;
import com.totallywaifu.network.Net;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * "Mis Waifus": cada chica se consigue UNA vez y queda registrada en el jugador. Desde la pestaña del inventario
 * puedes llamarla (aparece a tu lado) o guardarla (con todas sus cosas). Si la derrotan, vuelve sola a tu colección.
 */
public final class Collection {
    private Collection() {}

    private static CompoundTag root(Player p) {
        CompoundTag pers = p.getPersistentData();
        if (!pers.contains(Player.PERSISTED_NBT_TAG)) pers.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        CompoundTag r = pers.getCompound(Player.PERSISTED_NBT_TAG);
        if (!r.contains("TotallyWaifu")) r.put("TotallyWaifu", new CompoundTag());
        return r.getCompound("TotallyWaifu");
    }

    private static CompoundTag rec(Player p, WaifuKind k) {
        CompoundTag r = root(p);
        if (!r.contains(k.id())) r.put(k.id(), new CompoundTag());
        return r.getCompound(k.id());
    }

    public static boolean has(Player p, WaifuKind k) {
        return rec(p, k).getBoolean("Got");
    }

    /** Regalo de bienvenida (una sola vez): la Vara de Transformación. */
    public static boolean takeGift(Player p) {
        CompoundTag r = root(p);
        if (r.getBoolean("GiftWand")) return false;
        r.putBoolean("GiftWand", true);
        return true;
    }

    /** ¿Ya hay una (en el mundo o guardada)? Las de las mesas solo pueden existir una vez. */
    public static boolean active(Player p, WaifuKind k) {
        CompoundTag t = rec(p, k);
        return t.hasUUID("Uuid") || t.contains("Stored");
    }

    /** La chica volvió a ser su bloque (sigue desbloqueada). */
    public static void markBlock(Player p, WaifuKind k) {
        CompoundTag t = rec(p, k);
        t.putBoolean("Got", true);
        t.putBoolean("Block", true);
        t.remove("Uuid");
        t.remove("Stored");
    }

    /** Registra a la chica como tuya (y dónde está). */
    public static void track(ServerPlayer p, WaifuEntity w) {
        CompoundTag t = rec(p, w.kind);
        t.putBoolean("Got", true);
        t.putUUID("Uuid", w.getUUID());
        t.putString("Dim", w.level().dimension().location().toString());
        t.putLong("Pos", w.blockPosition().asLong());
        t.remove("Stored");
        t.remove("Block");
    }

    /** Actualiza dónde está (solo si es la chica registrada; puede haber varias ovejas, por ejemplo). */
    public static void refresh(ServerPlayer p, WaifuEntity w) {
        CompoundTag t = rec(p, w.kind);
        if (t.hasUUID("Uuid") ? !t.getUUID("Uuid").equals(w.getUUID()) : t.contains("Stored") || t.getBoolean("Block")) return;
        track(p, w);
    }

    /** Si la derrotan y su dueño está conectado, vuelve guardada a la colección. */
    public static boolean onDefeat(WaifuEntity w) {
        if (!(w.ownerPlayer() instanceof ServerPlayer p)) return false;
        CompoundTag cur = rec(p, w.kind);
        if (cur.contains("Stored") || (cur.hasUUID("Uuid") && !cur.getUUID("Uuid").equals(w.getUUID()))) return false;
        w.setHealth(w.getMaxHealth());
        CompoundTag data = new CompoundTag();
        w.saveWithoutId(data);
        CompoundTag t = rec(p, w.kind);
        t.putBoolean("Got", true);
        t.put("Stored", data);
        t.remove("Uuid");
        w.say(p, "¡Ay! Me retiré a tu colección ♥. Llámame desde la pestaña del inventario cuando quieras.");
        return true;
    }

    @Nullable
    private static WaifuEntity find(ServerPlayer p, WaifuKind k) {
        CompoundTag t = rec(p, k);
        if (!t.hasUUID("Uuid")) return null;
        UUID id = t.getUUID("Uuid");
        for (ServerLevel lv : p.server.getAllLevels()) {
            Entity e = lv.getEntity(id);
            if (e instanceof WaifuEntity w && w.isAlive()) return w;
        }
        return null;
    }

    public static void summon(ServerPlayer p, WaifuKind k) {
        CompoundTag t = rec(p, k);
        if (!t.getBoolean("Got")) return;
        if (t.getBoolean("Block") && !t.contains("Stored") && !t.hasUUID("Uuid")) {
            p.sendSystemMessage(Component.literal("§d" + k.display + " ahora es un bloque. Pon un " + k.role.toLowerCase() + " y usa la Vara de Transformación sobre él."));
            return;
        }
        ServerLevel lv = p.serverLevel();
        double x = p.getX() + p.getLookAngle().x * 1.5, z = p.getZ() + p.getLookAngle().z * 1.5;
        if (t.contains("Stored")) {
            WaifuEntity w = ModRegistry.TYPES.get(k).get().create(lv);
            if (w == null) return;
            w.load(t.getCompound("Stored"));
            w.setUUID(UUID.randomUUID());
            w.moveTo(x, p.getY(), z, p.getYRot() + 180, 0);
            w.setOwner(p.getUUID());
            lv.addFreshEntity(w);
            track(p, w);
            arrive(lv, w);
            w.say(p, "¡Aquí estoy! ♥");
            return;
        }
        WaifuEntity w = find(p, k);
        if (w == null) {
            BlockPos at = BlockPos.of(t.getLong("Pos"));
            p.sendSystemMessage(Component.literal("§d" + k.display + " está lejos, en una zona sin cargar (" + at.getX() + ", " + at.getY() + ", "
                    + at.getZ() + " · " + t.getString("Dim") + "). Acércate y vuelve a llamarla."));
            return;
        }
        w.teleportTo(lv, x, p.getY(), z, Set.of(), p.getYRot() + 180, 0);
        WaifuEntity now = find(p, k);
        if (now != null) {
            now.getNavigation().stop();
            arrive(lv, now);
            now.say(p, "¡Voy! ♥");
        }
    }

    public static void store(ServerPlayer p, WaifuKind k) {
        WaifuEntity w = find(p, k);
        if (w == null) {
            p.sendSystemMessage(Component.literal("§d" + k.display + " no está cerca (zona sin cargar) o ya está guardada."));
            return;
        }
        storeEntity(p, w);
    }

    public static void storeEntity(ServerPlayer p, WaifuEntity w) {
        CompoundTag data = new CompoundTag();
        w.saveWithoutId(data);
        CompoundTag t = rec(p, w.kind);
        t.put("Stored", data);
        t.remove("Uuid");
        if (w.level() instanceof ServerLevel lv) {
            lv.sendParticles(ParticleTypes.HEART, w.getX(), w.getY() + 1.4, w.getZ(), 8, 0.4, 0.6, 0.4, 0);
            lv.playSound(null, w.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.6F);
        }
        w.say(p, "Me guardo en tu colección ♥");
        w.discard();
    }

    private static void arrive(ServerLevel lv, WaifuEntity w) {
        lv.sendParticles(ParticleTypes.HEART, w.getX(), w.getY() + 1.4, w.getZ(), 10, 0.4, 0.6, 0.4, 0);
        lv.sendParticles(ParticleTypes.END_ROD, w.getX(), w.getY() + 1, w.getZ(), 20, 0.3, 0.8, 0.3, 0.05);
        lv.playSound(null, w.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.2F, 1.3F);
    }

    /** Envía al jugador el estado de su colección (y abre / refresca la pestaña). */
    public static void send(ServerPlayer p) {
        List<CollectionData.Entry> out = new ArrayList<>();
        for (WaifuKind k : WaifuKind.values()) {
            CompoundTag t = rec(p, k);
            int status = !t.getBoolean("Got") ? 0 : t.contains("Stored") ? 2 : t.hasUUID("Uuid") ? 1 : 3;
            String where = "";
            if (status == 1) {
                WaifuEntity w = find(p, k);
                if (w != null) {
                    t.putLong("Pos", w.blockPosition().asLong());
                    t.putString("Dim", w.level().dimension().location().toString());
                    where = (int) Math.sqrt(w.distanceToSqr(p)) + " bloques" + (w.level() != p.level() ? " (otra dimensión)" : "")
                            + " · " + modeName(w.getMode());
                } else {
                    BlockPos at = BlockPos.of(t.getLong("Pos"));
                    where = "Lejos: " + at.getX() + ", " + at.getY() + ", " + at.getZ();
                }
            }
            out.add(new CollectionData.Entry(k.ordinal(), status, where));
        }
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new CollectionData(out));
    }

    private static String modeName(int m) {
        return m == WaifuEntity.STAY ? "Quieta" : m == WaifuEntity.WORK ? "Trabajando" : "Siguiéndote";
    }

    static ResourceKey<net.minecraft.world.level.Level> dim(String s) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(s));
    }
}
