package com.tanny.tannymaid;

import com.mojang.logging.LogUtils;
import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.registry.ModEntities;
import com.tanny.tannymaid.registry.ModItems;
import com.tanny.tannymaid.registry.ModMenus;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TannyMaid.MODID)
public class TannyMaid {
    public static final String MODID = "tannymaid";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Pestaña propia del mod en el menú creativo. */
    public static final net.minecraftforge.registries.DeferredRegister<net.minecraft.world.item.CreativeModeTab> TABS =
            net.minecraftforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.CreativeModeTab> TAB = TABS.register("tannymaid",
            () -> net.minecraft.world.item.CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.tannymaid"))
                    .icon(() -> new net.minecraft.world.item.ItemStack(ModItems.TANNY_CONTRACT.get()))
                    .withTabsBefore(net.minecraft.world.item.CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((params, out) -> {
                        out.accept(ModItems.TANNY_CONTRACT.get());
                        out.accept(ModItems.TANNY_WHISTLE.get());
                    })
                    .build());

    public TannyMaid() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModMenus.MENUS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(this::onAttributes);
        modBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent e) ->
                e.enqueueWork(com.tanny.tannymaid.network.Net::register));
        LOGGER.info("TannyMaid Adventures cargado. ¡Hola, Master!");
    }

    private void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TANNY.get(), TannyEntity.createAttributes().build());
    }
}
