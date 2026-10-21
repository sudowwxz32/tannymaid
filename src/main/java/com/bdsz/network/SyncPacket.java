package com.bdsz.network;

import com.bdsz.data.SaiyanState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor → cliente: estado saiyajin de un jugador (para el cabello y el aura). */
public class SyncPacket {
    private final int entityId;
    private final SaiyanState state;

    public SyncPacket(int entityId, SaiyanState state) {
        this.entityId = entityId;
        this.state = state;
    }

    public static void encode(SyncPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.entityId);
        p.state.write(b);
    }

    public static SyncPacket decode(FriendlyByteBuf b) {
        return new SyncPacket(b.readVarInt(), SaiyanState.read(b));
    }

    public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientStates.put(p.entityId, p.state));
        ctx.get().setPacketHandled(true);
    }
}
