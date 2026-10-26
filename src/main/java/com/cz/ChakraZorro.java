package com.cz;

import com.cz.network.Net;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/** Chakra del Zorro: los mantos de chakra del zorro de nueve colas (de una a cinco colas). */
@Mod(ChakraZorro.MODID)
public class ChakraZorro {
    public static final String MODID = "chakrazorro";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<com.cz.entity.DebrisEntity>> DEBRIS = ENTITIES.register("debris",
            () -> EntityType.Builder.<com.cz.entity.DebrisEntity>of(com.cz.entity.DebrisEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(6).updateInterval(4).fireImmune().noSave().build("debris"));

    public ChakraZorro() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(bus);
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(Net::register);
    }
}
