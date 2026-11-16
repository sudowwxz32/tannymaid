package com.totallywaifu.network;

import com.totallywaifu.TotallyWaifu;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    private Net() {}

    private static final String VERSION = "5";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(TotallyWaifu.MODID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register() {
        CHANNEL.registerMessage(0, CollectionAction.class, CollectionAction::write, CollectionAction::read, CollectionAction::handle);
        CHANNEL.registerMessage(1, CollectionData.class, CollectionData::write, CollectionData::read, CollectionData::handle);
        CHANNEL.registerMessage(2, Fx.class, Fx::write, Fx::read, Fx::handle);
    }
}
