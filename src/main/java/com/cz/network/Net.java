package com.cz.network;

import com.cz.ChakraZorro;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class Net {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ChakraZorro.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private Net() {}

    public static void register() {
        CHANNEL.registerMessage(0, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(1, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
