package com.bdsz;

import com.bdsz.network.Net;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BlockDragonSuperZ.MODID)
public class BlockDragonSuperZ {
    public static final String MODID = "blockdragonsuperz";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BlockDragonSuperZ() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(Net::register);
    }
}
