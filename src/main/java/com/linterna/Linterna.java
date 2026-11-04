package com.linterna;

import com.linterna.network.Net;
import com.linterna.registry.ModRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Linterna.MODID)
public class Linterna {
    public static final String MODID = "linterna";

    public Linterna() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.register(bus);
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(Net::register);
    }
}
