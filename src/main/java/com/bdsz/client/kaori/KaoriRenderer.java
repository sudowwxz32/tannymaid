package com.bdsz.client.kaori;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.companion.KaoriEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/** Kaori: chaqueta oliva, top blanco, cinturón de cápsulas, shorts cargo, botas, gafas de mecánica y coleta alta. */
public class KaoriRenderer extends HumanoidMobRenderer<KaoriEntity, KaoriModel> {
    public enum Face { NORMAL, BLINK, HAPPY, SURPRISED, FOCUSED, TIRED }

    private static final ResourceLocation NORMAL = tex("kaori"), BLINK = tex("kaori_blink"), HAPPY = tex("kaori_happy"),
            SURPRISED = tex("kaori_surprised"), FOCUSED = tex("kaori_focused"), TIRED = tex("kaori_tired");

    public KaoriRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new KaoriModel(ctx.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.45F);
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                new HumanoidArmorModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)), ctx.getModelManager()));
        addLayer(new KaoriLayers.Outfit(this, ctx.getModelSet()));
        addLayer(new KaoriLayers.Head(this, ctx.getModelSet()));
    }

    static ResourceLocation tex(String n) {
        return new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/kaori/" + n + ".png");
    }

    /** Expresión: pestañea cada pocos segundos (a veces dos veces seguidas). */
    public static Face face(KaoriEntity e) {
        if (e.isWeakened()) return Face.TIRED;
        if (e.hurtTime > 0) return Face.SURPRISED;
        long t = e.tickCount + e.getId() * 53L;
        if (t % 90 < 3 || (t % 270 >= 6 && t % 270 < 9)) return Face.BLINK;
        if (e.isTalking()) return Face.HAPPY;
        if (e.isAggressive()) return Face.FOCUSED;
        return Face.NORMAL;
    }

    @Override
    public ResourceLocation getTextureLocation(KaoriEntity e) {
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
    protected void scale(KaoriEntity e, PoseStack pose, float partial) {
        pose.scale(0.93F, 0.93F, 0.93F);
        if (e.isWeakened()) pose.translate(0.0F, 0.6F, 0.0F);
    }
}
