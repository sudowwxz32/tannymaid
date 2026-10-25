package com.bdsz.network;

import com.bdsz.BlockDragonSuperZ;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class Net {
    private static final String VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BlockDragonSuperZ.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private Net() {}

    public static void register() {
        CHANNEL.registerMessage(0, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(1, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(2, OpenAppearancePacket.class, OpenAppearancePacket::encode, OpenAppearancePacket::decode, OpenAppearancePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(3, AppearancePacket.class, AppearancePacket::encode, AppearancePacket::decode, AppearancePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
