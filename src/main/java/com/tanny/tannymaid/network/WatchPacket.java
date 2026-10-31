package com.tanny.tannymaid.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor → cliente: el Pokémon vigilado que Tanny encontró (o que se perdió de vista). */
public class WatchPacket {
    public final boolean active;
    public final int entityId;
    public final Component name;
    public final double x, y, z;

    public WatchPacket(boolean active, int entityId, Component name, double x, double y, double z) {
        this.active = active;
        this.entityId = entityId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static WatchPacket clear() {
        return new WatchPacket(false, -1, Component.empty(), 0, 0, 0);
    }

    public static void encode(WatchPacket p, FriendlyByteBuf b) {
        b.writeBoolean(p.active);
        if (!p.active) return;
        b.writeVarInt(p.entityId);
        b.writeComponent(p.name);
        b.writeDouble(p.x);
        b.writeDouble(p.y);
        b.writeDouble(p.z);
    }

    public static WatchPacket decode(FriendlyByteBuf b) {
        if (!b.readBoolean()) return clear();
        return new WatchPacket(true, b.readVarInt(), b.readComponent(), b.readDouble(), b.readDouble(), b.readDouble());
    }

    public static void handle(WatchPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.tanny.tannymaid.client.WatchHud.receive(p)));
        ctx.get().setPacketHandled(true);
    }
}
