package com.linterna.client;

import com.linterna.Linterna;
import com.linterna.registry.ModRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Linterna.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final String CAT = "key.categories.linterna";
    public static final KeyMapping FLY = key("fly", GLFW.GLFW_KEY_L, null);
    public static final KeyMapping ARMOR = key("armor", GLFW.GLFW_KEY_Y, null);
    public static final KeyMapping PICK = key("pick", GLFW.GLFW_KEY_P, KeyModifier.ALT);
    public static final KeyMapping GHOST = key("ghost", GLFW.GLFW_KEY_C, null);
    public static final KeyMapping GUNS = key("guns", GLFW.GLFW_KEY_V, null);
    public static final KeyMapping HAMMER = key("hammer", GLFW.GLFW_KEY_M, KeyModifier.ALT);
    public static final KeyMapping SUIT = key("suit", GLFW.GLFW_KEY_T, KeyModifier.ALT);
    public static final KeyMapping CATALOG = key("catalog", GLFW.GLFW_KEY_M, null);

    public static ModelLayerLocation loc(String n) {
        return new ModelLayerLocation(new ResourceLocation(Linterna.MODID, n), "main");
    }

    public static final ModelLayerLocation SHELL_WIDE = loc("shell_wide"), SHELL_SLIM = loc("shell_slim"),
            ARMOR_WIDE = loc("armor_wide"), ARMOR_SLIM = loc("armor_slim"), SUIT_WIDE = loc("suit_wide"), SUIT_SLIM = loc("suit_slim"),
            CONSTRUCTS = loc("constructs");

    private static KeyMapping key(String name, int code, KeyModifier mod) {
        String id = "key.linterna." + name;
        return mod == null ? new KeyMapping(id, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, code, CAT)
                : new KeyMapping(id, KeyConflictContext.IN_GAME, mod, InputConstants.Type.KEYSYM, code, CAT);
    }

    private ClientModEvents() {}

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        for (KeyMapping k : new KeyMapping[]{FLY, ARMOR, PICK, GHOST, GUNS, HAMMER, SUIT, CATALOG}) e.register(k);
    }

    private static LayerDefinition mesh(float grow, boolean slim) {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(grow), slim), 64, 64);
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(SHELL_WIDE, () -> mesh(0.32F, false));
        e.registerLayerDefinition(SHELL_SLIM, () -> mesh(0.32F, true));
        e.registerLayerDefinition(ARMOR_WIDE, () -> mesh(0.8F, false));
        e.registerLayerDefinition(ARMOR_SLIM, () -> mesh(0.8F, true));
        e.registerLayerDefinition(SUIT_WIDE, () -> mesh(0.04F, false));
        e.registerLayerDefinition(SUIT_SLIM, () -> mesh(0.04F, true));
        e.registerLayerDefinition(CONSTRUCTS, LanternLayer::constructs);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModRegistry.RAYO.get(), RayoRenderer::new);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers e) {
        for (String skin : e.getSkins()) {
            if (e.getSkin(skin) instanceof PlayerRenderer r) r.addLayer(new LanternLayer(r, e.getEntityModels()));
        }
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("linterna_hud", new LanternHud());
    }
}
