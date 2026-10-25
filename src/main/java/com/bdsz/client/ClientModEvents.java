package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = BlockDragonSuperZ.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    public static final String CATEGORY = "key.categories.blockdragonsuperz";
    public static final KeyMapping MENU = new KeyMapping("key.blockdragonsuperz.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
    public static final KeyMapping TRANSFORM = new KeyMapping("key.blockdragonsuperz.transform", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, CATEGORY);
    public static final KeyMapping AURA = new KeyMapping("key.blockdragonsuperz.aura", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, CATEGORY);
    public static final KeyMapping FLY = new KeyMapping("key.blockdragonsuperz.fly", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, CATEGORY);
    public static final KeyMapping CHARGE = new KeyMapping("key.blockdragonsuperz.charge", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY);
    public static final KeyMapping BLAST = new KeyMapping("key.blockdragonsuperz.blast", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    public static final ModelLayerLocation AURA_LAYER = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "aura"), "main");
    public static final ModelLayerLocation KI_BLAST = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "ki_blast"), "main");
    public static final KeyMapping KAMEHAMEHA = new KeyMapping("key.blockdragonsuperz.kamehameha", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping BIG_BANG = new KeyMapping("key.blockdragonsuperz.bigbang", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY);
    public static final KeyMapping DRAGON_FIST = new KeyMapping("key.blockdragonsuperz.dragonfist", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);
    public static final ModelLayerLocation SPIRIT_LAYER = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "spirit_dragon"), "main");
    public static final ModelLayerLocation TAIL_LAYER = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "tail"), "main");
    public static final ModelLayerLocation OZARU_LAYER = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "ozaru"), "main");
    public static final KeyMapping OZARU = new KeyMapping("key.blockdragonsuperz.ozaru", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, CATEGORY);
    /** Cambio rápido de transformación: Alt + V. */
    public static final KeyMapping QUICK = new KeyMapping("key.blockdragonsuperz.quick",
            net.minecraftforge.client.settings.KeyConflictContext.IN_GAME, net.minecraftforge.client.settings.KeyModifier.ALT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final ModelLayerLocation SSJ4_FUR = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "ssj4_fur"), "main");
    public static final ModelLayerLocation SSJ4_FUR_SLIM = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "ssj4_fur_slim"), "main");
    public static final ModelLayerLocation BUST_SKIN = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "bust_skin"), "main");
    public static final ModelLayerLocation BUST_ARMOR = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "bust_armor"), "main");
    public static final ModelLayerLocation HAIR = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "saiyan_hair"), "main");

    private ClientModEvents() {}

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(MENU);
        e.register(TRANSFORM);
        e.register(AURA);
        e.register(FLY);
        e.register(BLAST);
        e.register(CHARGE);
        e.register(KAMEHAMEHA);
        e.register(BIG_BANG);
        e.register(DRAGON_FIST);
        e.register(OZARU);
        e.register(QUICK);
    }

    @SubscribeEvent
    public static void layerDefs(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(HAIR, SaiyanHairLayer::createLayer);
        e.registerLayerDefinition(BUST_SKIN, FemaleBodyLayer::createSkin);
        e.registerLayerDefinition(BUST_ARMOR, FemaleBodyLayer::createArmor);
        e.registerLayerDefinition(SSJ4_FUR, Ssj4FurLayer::createWide);
        e.registerLayerDefinition(SSJ4_FUR_SLIM, Ssj4FurLayer::createSlim);
        e.registerLayerDefinition(OZARU_LAYER, OzaruModelData::createLayer);
        e.registerLayerDefinition(AURA_LAYER, AuraLayer::createLayer);
        e.registerLayerDefinition(TAIL_LAYER, TailLayer::createLayer);
        e.registerLayerDefinition(SPIRIT_LAYER, DragonSpiritLayer::createLayer);
        e.registerLayerDefinition(KI_BLAST, KiBlastRenderer::createLayer);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(com.bdsz.registry.ModRegistry.KI_BLAST.get(), KiBlastRenderer::new);
        e.registerEntityRenderer(com.bdsz.registry.ModRegistry.KAME_BEAM.get(), KameBeamRenderer::new);
        e.registerEntityRenderer(com.bdsz.registry.ModRegistry.DEBRIS.get(), DebrisRenderer::new);
        e.registerBlockEntityRenderer(com.bdsz.registry.ModRegistry.SHIELD_BE.get(), ShieldDomeRenderer::new);
    }

    @SubscribeEvent
    public static void overlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent e) {
        e.registerBelowAll("transform_flash", new TransformFlash());
        e.registerAboveAll("saiyan_hud", new SaiyanHud());
    }

    /** Añade el cabello saiyajin a los jugadores (skins normal y delgada). */
    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers e) {
        OzaruRenderer.reset();
        for (String skin : e.getSkins()) {
            if (e.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new FemaleBodyLayer(renderer, e.getEntityModels()));
                renderer.addLayer(new Ssj4FurLayer(renderer, e.getEntityModels()));
                renderer.addLayer(new SaiyanHairLayer(renderer, e.getEntityModels()));
                renderer.addLayer(new TailLayer(renderer, e.getEntityModels()));
                renderer.addLayer(new DragonSpiritLayer(renderer, e.getEntityModels()));
                renderer.addLayer(new AuraLayer(renderer, e.getEntityModels()));
            }
        }
    }
}
