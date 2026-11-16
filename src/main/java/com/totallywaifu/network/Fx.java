package com.totallywaifu.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** Servidor → cliente: animaciones (intro de la vara, logros, colección completa, dormir en las piernas de Yume). */
public record Fx(int type, int arg) {
    public static final int INTRO = 0, WAND = 1, COLLECTION = 2, SLEEP_ON = 3, SLEEP_OFF = 4, SURPRISE = 5, LANDED = 6;

    public static void send(ServerPlayer p, int type, int arg) {
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new Fx(type, arg));
    }

    public static void write(Fx m, FriendlyByteBuf b) {
        b.writeVarInt(m.type);
        b.writeVarInt(m.arg);
    }

    public static Fx read(FriendlyByteBuf b) {
        return new Fx(b.readVarInt(), b.readVarInt());
    }

    public static void handle(Fx m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.totallywaifu.client.ClientFx.on(m.type, m.arg)));
        ctx.get().setPacketHandled(true);
    }
}
