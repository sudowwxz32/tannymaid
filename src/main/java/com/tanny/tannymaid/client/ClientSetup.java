package com.tanny.tannymaid.client;

import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.registry.ModEntities;
import com.tanny.tannymaid.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TannyMaid.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TANNY.get(), TannyRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.TANNY.get(), TannyScreen::new));
    }

    public static final net.minecraft.client.KeyMapping OPEN_MENU = new net.minecraft.client.KeyMapping("key.tannymaid.menu",
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_G, "key.categories.tannymaid");

    @SubscribeEvent
    public static void registerKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
    }

    @SubscribeEvent
    public static void registerOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("pokemon_watch", new WatchHud());
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TannyHairLayer.LAYER, TannyGeneratedModels::createHair);
        event.registerLayerDefinition(TannyOutfitLayer.LAYER, TannyGeneratedModels::createOutfit);
    }
}
