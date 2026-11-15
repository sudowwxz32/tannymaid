package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Dibuja a una waifu: skin con expresiones, ropa 3D, cabello en mechones y sus accesorios. */
public class WaifuRenderer extends HumanoidMobRenderer<WaifuEntity, WaifuModel> {
    private final WaifuKind kind;
    private final ResourceLocation normal, blink, happy, surprised, focused, tired;

    public WaifuRenderer(EntityRendererProvider.Context ctx, WaifuKind kind) {
        super(ctx, new WaifuModel(ctx.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.45F);
        this.kind = kind;
        String n = kind.id();
        normal = tex(kind, n);
        blink = tex(kind, n + "_blink");
        happy = tex(kind, n + "_happy");
        surprised = tex(kind, n + "_surprised");
        focused = tex(kind, n + "_focused");
        tired = tex(kind, n + "_tired");
        addLayer(new WaifuLayers.Outfit(this, ctx.getModelSet(), kind));
        addLayer(new WaifuLayers.Head(this, ctx.getModelSet(), kind));
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
        pose.scale(0.95F, 0.97F, 0.95F);        // esbelta y alta
    }
}
