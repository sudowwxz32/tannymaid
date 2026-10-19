package com.laury.laurymod;

import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.registry.ModEntities;
import com.laury.laurymod.registry.ModItems;
import com.laury.laurymod.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LauryMod.MODID)
public class LauryMod {
    public static final String MODID = "laury";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LauryMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModMenus.MENUS.register(modBus);
        modBus.addListener(this::onAttributes);
        modBus.addListener(this::onCreativeTab);
    }

    private void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LAURY.get(), LauryEntity.createAttributes().build());
    }

    private void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.SUMMON_HORN.get());
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.DRAGON_MACE.get());
            event.accept(ModItems.FIRE_DRAGON_SWORD.get());
            event.accept(ModItems.FORCE_DRAGON_SWORD.get());
            event.accept(ModItems.DRAGON_SCYTHE.get());
        }
    }
}
