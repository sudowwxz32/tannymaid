package com.linterna.network;

import com.linterna.data.LanternState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncPacket {
    public final int id;
    public final LanternState s;

    public SyncPacket(int id, LanternState s) {
        this.id = id;
        this.s = s;
    }

    public static void encode(SyncPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.id);
        p.s.write(b);
    }

    public static SyncPacket decode(FriendlyByteBuf b) {
        return new SyncPacket(b.readVarInt(), LanternState.read(b));
    }

    public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientStates.put(p.id, p.s));
        ctx.get().setPacketHandled(true);
    }
}
