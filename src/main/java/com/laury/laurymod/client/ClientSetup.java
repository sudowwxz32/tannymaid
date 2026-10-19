package com.laury.laurymod.client;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.registry.ModEntities;
import com.laury.laurymod.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = LauryMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    public static final ModelLayerLocation FEATURES = new ModelLayerLocation(new ResourceLocation(LauryMod.MODID, "laury"), "features");
    public static final ModelLayerLocation DRAGON = new ModelLayerLocation(new ResourceLocation(LauryMod.MODID, "laury_dragon"), "main");

    private ClientSetup() {}

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.LAURY.get(), LauryRenderer::new);
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(FEATURES, LauryGeneratedModels::createFeatures);
        e.registerLayerDefinition(DRAGON, LauryGeneratedModels::createDragon);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> MenuScreens.register(ModMenus.LAURY.get(), LauryScreen::new));
    }
}
