package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Dibuja a una waifu por capas: cuerpo y cara (skin), armadura, ropa 3D, cabello con orejas de elfa y el objeto que sostiene.
 */
public class WaifuRenderer extends HumanoidMobRenderer<WaifuEntity, WaifuModel> {
    private final ResourceLocation normal, blink, happy, surprised, focused, tired;

    public WaifuRenderer(EntityRendererProvider.Context ctx, WaifuKind kind) {
        super(ctx, new WaifuModel(ctx.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.45F);
        normal = tex(kind, "skin");
        blink = tex(kind, "skin_blink");
        happy = tex(kind, "skin_happy");
        surprised = tex(kind, "skin_surprised");
        focused = tex(kind, "skin_focused");
        tired = tex(kind, "skin_tired");
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(ctx.bakeLayer(WaifuLayers.ARMOR_INNER)),
                new HumanoidArmorModel<>(ctx.bakeLayer(WaifuLayers.ARMOR_OUTER)), ctx.getModelManager()));
        addLayer(new WaifuLayers.Clothes(this, ctx.getModelSet(), kind));
        addLayer(new WaifuLayers.Hair(this, ctx.getModelSet(), kind));
        addLayer(new WaifuLayers.Held(this, kind));
        if (kind == WaifuKind.SHIORI) addLayer(new WaifuLayers.Book(this, ctx.getModelSet()));
    }

    public static ResourceLocation tex(WaifuKind k, String n) {
        return new ResourceLocation(TotallyWaifu.MODID, "textures/entity/" + k.id() + "/" + n + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(WaifuEntity e) {
        if (e.hurtTime > 0) return surprised;
        if (e.getHealth() < e.getMaxHealth() * 0.3F) return tired;
        long t = e.tickCount + e.getId() * 53L;
        if (t % 90 < 3 || (t % 270 >= 6 && t % 270 < 9)) return blink;
        if (e.isHappy()) return happy;
        if (e.isBusy() || e.isAggressive()) return focused;
        return normal;
    }

    @Override
    protected void scale(WaifuEntity e, PoseStack pose, float partial) {
        pose.scale(0.94F, 0.94F, 0.94F);
    }
}
