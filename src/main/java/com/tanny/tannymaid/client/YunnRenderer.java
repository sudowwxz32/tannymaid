package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.yunn.YunnEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Yunn: uniforme de enfermera rosa y blanco, cabello largo lavanda, cofia con corazón y expresiones tiernas. */
public class YunnRenderer extends HumanoidMobRenderer<YunnEntity, YunnModel> {
    private static final ResourceLocation NORMAL = tex("yunn"), BLINK = tex("yunn_blink"), HAPPY = tex("yunn_happy"),
            SURPRISED = tex("yunn_surprised"), FOCUSED = tex("yunn_focused"), TIRED = tex("yunn_tired");
    public static final ResourceLocation OUTFIT = tex("outfit");

    public YunnRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new YunnModel(ctx.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.45F);
        addLayer(new TannyOutfitLayer<>(this, ctx.getModelSet(), OUTFIT));
        addLayer(new YunnHairLayer(this, ctx.getModelSet()));
    }

    private static ResourceLocation tex(String n) {
        return new ResourceLocation(TannyMaid.MODID, "textures/entity/yunn/" + n + ".png");
    }

    public static TannyRenderer.Face face(YunnEntity e) {
        if (e.isWeakened()) return TannyRenderer.Face.TIRED;
        if (e.hurtTime > 0) return TannyRenderer.Face.SURPRISED;
        long t = e.tickCount + e.getId() * 37L;
        if (t % 100 < 3 || (t % 300 >= 6 && t % 300 < 9)) return TannyRenderer.Face.BLINK;
        if (e.isTalking()) return TannyRenderer.Face.HAPPY;
        if (e.isAggressive()) return TannyRenderer.Face.FOCUSED;
        return TannyRenderer.Face.NORMAL;
    }

    @Override
    public ResourceLocation getTextureLocation(YunnEntity e) {
        return switch (face(e)) {
            case TIRED -> TIRED;
            case SURPRISED -> SURPRISED;
            case BLINK -> BLINK;
            case HAPPY -> HAPPY;
            case FOCUSED -> FOCUSED;
            default -> NORMAL;
        };
    }

    @Override
    protected void scale(YunnEntity e, PoseStack pose, float partial) {
        pose.scale(0.92F, 0.92F, 0.92F);
        if (e.isWeakened()) pose.translate(0.0F, 0.6F, 0.0F);
    }
}
