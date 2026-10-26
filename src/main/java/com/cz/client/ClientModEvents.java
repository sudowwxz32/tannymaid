package com.cz.client;

import com.cz.ChakraZorro;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ChakraZorro.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final String CATEGORY = "key.categories.chakrazorro";
    public static final KeyMapping MENU = new KeyMapping("key.chakrazorro.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);
    public static final KeyMapping TRANSFORM = new KeyMapping("key.chakrazorro.transform", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, CATEGORY);
    public static final KeyMapping CHARGE = new KeyMapping("key.chakrazorro.charge", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
    public static final ModelLayerLocation CLOAK = layer("cloak");
    public static final ModelLayerLocation BEAST = layer("beast");
    public static final ModelLayerLocation BEAST_SLIM = layer("beast_slim");
    public static final ModelLayerLocation SKELETON = layer("skeleton");

    private ClientModEvents() {}

    private static ModelLayerLocation layer(String n) {
        return new ModelLayerLocation(new ResourceLocation(ChakraZorro.MODID, n), "main");
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(MENU);
        e.register(TRANSFORM);
        e.register(CHARGE);
    }

    @SubscribeEvent
    public static void layerDefs(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(CLOAK, CloakLayer::createLayer);
        e.registerLayerDefinition(BEAST, BeastLayer::createWide);
        e.registerLayerDefinition(BEAST_SLIM, BeastLayer::createSlim);
        e.registerLayerDefinition(SKELETON, SkeletonModelData::createLayer);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ChakraZorro.DEBRIS.get(), DebrisRenderer::new);
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerBelowAll("rage", new RageOverlay());
        e.registerAboveAll("chakra_hud", new ZorroHud());
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers e) {
        for (String skin : e.getSkins()) {
            if (e.getSkin(skin) instanceof PlayerRenderer r) {
                r.addLayer(new BeastLayer(r, e.getEntityModels()));
                r.addLayer(new SkeletonLayer(r, e.getEntityModels()));
                r.addLayer(new CloakLayer(r, e.getEntityModels()));
            }
        }
    }
}
