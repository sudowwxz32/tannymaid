package com.cz.network;

import com.cz.data.ZorroState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor → cliente: estado del chakra del zorro de un jugador. */
public class SyncPacket {
    private final int id;
    private final ZorroState state;

    public SyncPacket(int id, ZorroState state) {
        this.id = id;
        this.state = state;
    }

    public static void encode(SyncPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.id);
        p.state.write(b);
    }

    public static SyncPacket decode(FriendlyByteBuf b) {
        return new SyncPacket(b.readVarInt(), ZorroState.read(b));
    }

    public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            boolean quadBefore = ClientStates.get(p.id).active().quadruped;
            ClientStates.put(p.id, p.state);
            if (quadBefore != p.state.active().quadruped) {
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
                        () -> () -> com.cz.client.ClientHooks.refreshSize(p.id));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
