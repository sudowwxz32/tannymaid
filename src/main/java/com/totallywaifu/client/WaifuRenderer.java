package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Dibuja a una waifu de bloques: cuerpo alto y esbelto con la ropa de su oficio, cara con expresiones y accesorios. */
public class WaifuRenderer extends HumanoidMobRenderer<WaifuEntity, WaifuModel> {
    private final ResourceLocation body;
    private final WaifuKind kind;

    public WaifuRenderer(EntityRendererProvider.Context ctx, WaifuKind kind) {
        super(ctx, new WaifuModel(ctx.bakeLayer(WaifuLayers.body(kind)), kind), 0.45F);
        body = tex(kind, "body");
        this.kind = kind;
        shadowRadius = 0.45F * kind.scale;
        addLayer(new WaifuLayers.Face(this, ctx.getModelSet(), kind));
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
        float s = 0.98F * kind.scale;
        pose.scale(s, s, s);
        if (e.isSitting()) pose.translate(0, 0.86F, 0);
        if (kind == WaifuKind.BLAZE || kind == WaifuKind.GHAST || kind == WaifuKind.ALLAY) {   // flotan un poquito
            pose.translate(0, -0.12F - net.minecraft.util.Mth.sin((e.tickCount + partial) * 0.08F) * 0.06F, 0);
        }
    }
}
