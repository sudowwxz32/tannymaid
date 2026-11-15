package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Dibuja a una waifu: cuerpo estilizado con su ropa, cara de anime con expresiones, cabello en mechones y accesorios. */
public class WaifuRenderer extends HumanoidMobRenderer<WaifuEntity, WaifuModel> {
    private final ResourceLocation body;

    public WaifuRenderer(EntityRendererProvider.Context ctx, WaifuKind kind) {
        super(ctx, new WaifuModel(ctx.bakeLayer(WaifuLayers.body(kind)), kind), 0.45F);
        body = tex(kind, "body");
        addLayer(new WaifuLayers.Face(this, ctx.getModelSet(), kind));
        addLayer(new WaifuLayers.Hair(this, kind));
        if (kind == WaifuKind.SHIORI) addLayer(new WaifuLayers.Book(this, ctx.getModelSet()));
    }

    public static ResourceLocation tex(WaifuKind k, String n) {
        return new ResourceLocation(TotallyWaifu.MODID, "textures/entity/" + k.id() + "/" + n + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(WaifuEntity e) {
        return body;
    }

    @Override
    protected void scale(WaifuEntity e, PoseStack pose, float partial) {
        pose.scale(0.96F, 0.98F, 0.96F);
    }
}
