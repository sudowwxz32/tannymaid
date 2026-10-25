package com.bdsz.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor → cliente: abre la pantalla para elegir género, peinado y cola. */
public class OpenAppearancePacket {
    public static void encode(OpenAppearancePacket p, FriendlyByteBuf b) {}

    public static OpenAppearancePacket decode(FriendlyByteBuf b) {
        return new OpenAppearancePacket();
    }

    public static void handle(OpenAppearancePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.bdsz.client.ClientHooks.openAppearance()));
        ctx.get().setPacketHandled(true);
    }
}
