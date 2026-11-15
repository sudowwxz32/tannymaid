package com.totallywaifu;

import com.totallywaifu.registry.ModRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TotallyWaifu.MODID)
public class TotallyWaifu {
    public static final String MODID = "totallywaifu";

    public TotallyWaifu() {
        ModRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
