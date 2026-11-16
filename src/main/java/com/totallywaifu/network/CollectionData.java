package com.totallywaifu.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Servidor → cliente: estado de "Mis Waifus" (0 no la tienes, 1 en el mundo, 2 guardada). */
public record CollectionData(List<Entry> entries, boolean surprise, boolean home, boolean extras) {
    public record Entry(int kind, int status, String where) {}

    public static void write(CollectionData m, FriendlyByteBuf b) {
        b.writeVarInt(m.entries.size());
        for (Entry e : m.entries) {
            b.writeVarInt(e.kind);
            b.writeVarInt(e.status + 1);
            b.writeUtf(e.where);
        }
        b.writeBoolean(m.surprise);
        b.writeBoolean(m.home);
        b.writeBoolean(m.extras);
    }

    public static CollectionData read(FriendlyByteBuf b) {
        int n = b.readVarInt();
        List<Entry> l = new ArrayList<>();
        for (int i = 0; i < n; i++) l.add(new Entry(b.readVarInt(), b.readVarInt() - 1, b.readUtf()));
        return new CollectionData(l, b.readBoolean(), b.readBoolean(), b.readBoolean());
    }

    public static void handle(CollectionData m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.totallywaifu.client.CollectionScreen.show(m)));
        ctx.get().setPacketHandled(true);
    }
}
