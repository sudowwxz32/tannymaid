package com.tanny.tannymaid;

import com.mojang.logging.LogUtils;
import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.registry.ModEntities;
import com.tanny.tannymaid.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TannyMaid.MODID)
public class TannyMaid {
    public static final String MODID = "tannymaid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TannyMaid() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        modBus.addListener(this::onAttributes);
        modBus.addListener(this::onCreativeTab);
        LOGGER.info("TannyMaid Adventures cargado. ¡Hola, Master!");
    }

    private void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TANNY.get(), TannyEntity.createAttributes().build());
    }

    private void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.TANNY_CONTRACT.get());
        }
    }
}
