package com.linterna.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor → cliente: muestra la pregunta "¿Tienes miedo?". */
public class AskPacket {
    public static void encode(AskPacket p, FriendlyByteBuf b) {}

    public static AskPacket decode(FriendlyByteBuf b) { return new AskPacket(); }

    public static void handle(AskPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.linterna.client.ClientHooks.ask()));
        ctx.get().setPacketHandled(true);
    }
}
