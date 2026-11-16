package com.totallywaifu.client;

import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = TotallyWaifu.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(WaifuLayers.FACE, WaifuModels::face);
        for (WaifuKind k : WaifuKind.values()) e.registerLayerDefinition(WaifuLayers.body(k), WaifuLayers.bodyDef(k));
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        for (WaifuKind k : WaifuKind.values()) e.registerEntityRenderer(ModRegistry.TYPES.get(k).get(), ctx -> new WaifuRenderer(ctx, k));
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> MenuScreens.register(ModRegistry.PANEL.get(), WaifuScreen::new));
    }
}
