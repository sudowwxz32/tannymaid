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
        this.ctx = ctx;
        body = tex(kind, "props");
        addLayer(new WaifuLayers.Body(this, kind));
        addLayer(new WaifuLayers.Face(this, kind));
        addLayer(new WaifuLayers.Hair(this, kind));
        if (kind == WaifuKind.SHIORI) addLayer(new WaifuLayers.Book(this, ctx.getModelSet()));
    }

    private NoaRenderer noa;
    private final EntityRendererProvider.Context ctx;

    @Override
    public void render(WaifuEntity e, float yaw, float partial, PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buf, int light) {
        if (e.getLook() == 1) {
            if (noa == null) noa = new NoaRenderer(ctx);
            noa.render(e, yaw, partial, pose, buf, light);
            return;
        }
        super.render(e, yaw, partial, pose, buf, light);
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
        pose.scale(1.02F, 1.05F, 1.02F);        // más altas y esbeltas
    }
}
