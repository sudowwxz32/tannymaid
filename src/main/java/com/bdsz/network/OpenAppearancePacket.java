package com.bdsz.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor → cliente: abre la pantalla para elegir género, peinado y cola. */
public class OpenAppearancePacket {
    public final boolean online;

    public OpenAppearancePacket(boolean online) {
        this.online = online;
    }

    public static void encode(OpenAppearancePacket p, FriendlyByteBuf b) {
        b.writeBoolean(p.online);
    }

    public static OpenAppearancePacket decode(FriendlyByteBuf b) {
        return new OpenAppearancePacket(b.readBoolean());
    }

    public static void handle(OpenAppearancePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.bdsz.client.ClientHooks.openAppearance(p.online)));
        ctx.get().setPacketHandled(true);
    }
}
