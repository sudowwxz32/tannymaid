package com.tanny.tannymaid.network;

import com.tanny.tannymaid.TannyMaid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class Net {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TannyMaid.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private Net() {}

    public static void register() {
        CHANNEL.registerMessage(0, WatchPacket.class, WatchPacket::encode, WatchPacket::decode, WatchPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void send(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}
