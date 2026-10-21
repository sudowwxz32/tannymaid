package com.bdsz;

import com.bdsz.network.Net;
import com.bdsz.registry.ModRegistry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
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
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.register(bus);
        bus.addListener(this::setup);
        bus.addListener(this::tabs);
    }

    private void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS || e.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            e.accept(ModRegistry.SENZU_BEAN.get());
        }
        if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            e.accept(ModRegistry.TRAINING_LIST_1.get());
            e.accept(ModRegistry.TRAINING_LIST_2.get());
            e.accept(ModRegistry.TRAINING_LIST_3.get());
            e.accept(ModRegistry.TRAINING_LIST_4.get());
        }
    }

    private void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(Net::register);
    }
}
