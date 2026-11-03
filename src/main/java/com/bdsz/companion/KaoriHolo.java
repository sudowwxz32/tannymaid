package com.bdsz.companion;

import com.bdsz.network.Net;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Holograma de Kaori (tecla J): su inventario y estado desde cualquier lugar, y dos teletransportes:
 * traer a Kaori a tu lado, o ir a la casa-cápsula que te hizo (si dormiste en su cama).
 */
public final class KaoriHolo {
    private KaoriHolo() {}

    public static final int OPEN = 0, TP_KAORI = 1, TP_HOME = 2;

    // ------------------------------------------------------------------ servidor

    private record Hut(BlockPos pos, int type, String dim) {}

    @Nullable
    private static Hut hut(ServerPlayer p, @Nullable KaoriEntity k) {
        if (k != null) return k.hutPos == null ? null : new Hut(k.hutPos, k.hutType, k.hutDim);
        CompoundTag b = KaoriActions.data(p).getCompound("KaoriBackup");
        return b.contains("Hut") ? new Hut(BlockPos.of(b.getLong("Hut")), b.getInt("HutType"), b.getString("HutDim")) : null;
    }

    /** ¿Tu cama (punto de reaparición) está dentro de la casa de Kaori? */
    @Nullable
    private static BlockPos homeBed(ServerPlayer p, @Nullable Hut h) {
        BlockPos bed = p.getRespawnPosition();
        if (h == null || bed == null || !p.getRespawnDimension().location().toString().equals(h.dim())) return null;
        int r = 4 + h.type();
        if (Math.abs(bed.getX() - h.pos().getX()) > r || Math.abs(bed.getZ() - h.pos().getZ()) > r || Math.abs(bed.getY() - h.pos().getY()) > 6) return null;
        return bed;
    }

    public static void handle(ServerPlayer p, int action) {
        KaoriEntity k = KaoriActions.findKaori(p);
        switch (action) {
            case TP_KAORI -> {
                if (k == null && !KaoriActions.data(p).contains("KaoriBackup")) {
                    p.displayClientMessage(Component.literal("§bTodavía no tienes a Kaori. Craftea su cápsula."), true);
                    return;
                }
                KaoriActions.summon(p);
            }
            case TP_HOME -> {
                BlockPos bed = homeBed(p, hut(p, k));
                if (bed == null) {
                    p.displayClientMessage(Component.literal("§bPrimero duerme en la cama de la casa que te hizo Kaori."), true);
                    return;
                }
                ServerLevel lv = p.server.getLevel(p.getRespawnDimension());
                if (lv == null) return;
                BlockState st = lv.getBlockState(bed);
                Vec3 to = Vec3.atBottomCenterOf(bed.above());
                if (st.getBlock() instanceof BedBlock) {
                    Direction dir = st.getValue(BedBlock.FACING);
                    Optional<Vec3> stand = BedBlock.findStandUpPosition(EntityType.PLAYER, lv, bed, dir, p.getYRot());
                    if (stand.isPresent()) to = stand.get();
                } else {
                    p.displayClientMessage(Component.literal("§bLa cama ya no está ahí."), true);
                    return;
                }
                p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
                p.teleportTo(lv, to.x, to.y, to.z, p.getYRot(), p.getXRot());
                lv.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, to.x, to.y + 1, to.z, 30, 0.4, 0.8, 0.4, 0.05);
                lv.playSound(null, BlockPos.containing(to), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.3F);
                p.displayClientMessage(Component.literal("§b✦ Teletransportado a tu casa-cápsula"), true);
            }
            default -> { }
        }
        send(p, KaoriActions.findKaori(p));
    }

    private static void send(ServerPlayer p, @Nullable KaoriEntity k) {
        Data d = new Data();
        CompoundTag backup = KaoriActions.data(p).getCompound("KaoriBackup");
        d.has = k != null || !backup.isEmpty();
        d.loaded = k != null;
        if (k != null) {
            for (int i = 0; i < 27; i++) d.bag.add(k.getBag().getItem(i).copy());
            for (int i = 0; i < 6; i++) d.equip.add(k.getEquip().getItem(i).copy());
            d.health = k.getHealth();
            d.max = k.getMaxHealth();
            d.form = k.getForm();
            d.mode = k.getMode().ordinal();
            d.weak = k.isWeakened();
            d.where = k.level() == p.level() ? (int) Math.round(Math.sqrt(k.distanceToSqr(p))) + " m" : k.level().dimension().location().getPath();
        } else {
            fill(d.bag, backup.getList("Bag", 10), 27);
            fill(d.equip, backup.getList("Equip", 10), 6);
            d.health = backup.getFloat("Health");
            d.max = 30;
            d.form = backup.getInt("Form");
            d.mode = backup.getInt("Mode");
            d.where = "lejos (zona sin cargar)";
        }
        d.home = homeBed(p, hut(p, k)) != null;
        Hut h = hut(p, k);
        d.hut = h == null ? "" : HutBuilder.NAMES[Math.max(0, Math.min(HutBuilder.NAMES.length - 1, h.type()))];
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new Packet(d));
    }

    private static void fill(List<ItemStack> out, ListTag l, int size) {
        for (int i = 0; i < size; i++) out.add(ItemStack.EMPTY);
        for (int i = 0; i < l.size(); i++) {
            CompoundTag t = l.getCompound(i);
            int s = t.getByte("Slot") & 255;
            if (s < size) out.set(s, ItemStack.of(t));
        }
    }

    // ------------------------------------------------------------------ datos y paquetes

    public static class Data {
        public boolean has, loaded, weak, home;
        public float health, max;
        public int form, mode;
        public String where = "", hut = "";
        public final List<ItemStack> bag = new ArrayList<>(), equip = new ArrayList<>();
    }

    /** Servidor → cliente: abre/actualiza el holograma. */
    public static class Packet {
        public final Data d;

        public Packet(Data d) { this.d = d; }

        public static void encode(Packet p, FriendlyByteBuf b) {
            Data d = p.d;
            b.writeBoolean(d.has); b.writeBoolean(d.loaded); b.writeBoolean(d.weak); b.writeBoolean(d.home);
            b.writeFloat(d.health); b.writeFloat(d.max); b.writeVarInt(d.form); b.writeVarInt(d.mode);
            b.writeUtf(d.where); b.writeUtf(d.hut);
            b.writeVarInt(d.bag.size()); for (ItemStack s : d.bag) b.writeItem(s);
            b.writeVarInt(d.equip.size()); for (ItemStack s : d.equip) b.writeItem(s);
        }

        public static Packet decode(FriendlyByteBuf b) {
            Data d = new Data();
            d.has = b.readBoolean(); d.loaded = b.readBoolean(); d.weak = b.readBoolean(); d.home = b.readBoolean();
            d.health = b.readFloat(); d.max = b.readFloat(); d.form = b.readVarInt(); d.mode = b.readVarInt();
            d.where = b.readUtf(); d.hut = b.readUtf();
            int n = b.readVarInt(); for (int i = 0; i < n; i++) d.bag.add(b.readItem());
            n = b.readVarInt(); for (int i = 0; i < n; i++) d.equip.add(b.readItem());
            return new Packet(d);
        }

        public static void handle(Packet p, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.bdsz.client.kaori.KaoriHoloScreen.show(p.d)));
            ctx.get().setPacketHandled(true);
        }
    }

    /** Cliente → servidor: abrir el holograma o usar uno de sus teletransportes. */
    public static class Request {
        public final int action;

        public Request(int action) { this.action = action; }

        public static void encode(Request r, FriendlyByteBuf b) { b.writeVarInt(r.action); }

        public static Request decode(FriendlyByteBuf b) { return new Request(b.readVarInt()); }

        public static void handle(Request r, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer sp = ctx.get().getSender();
                if (sp != null) KaoriHolo.handle(sp, r.action);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
