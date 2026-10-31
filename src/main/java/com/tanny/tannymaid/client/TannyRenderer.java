package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renderiza a Tanny (~1.66 m) y cambia su expresión: normal, parpadeo, feliz, sorpresa, concentrada, cansada. */
public class TannyRenderer extends HumanoidMobRenderer<TannyEntity, TannyModel> {

    private static final ResourceLocation NORMAL = tex("tanny");
    private static final ResourceLocation BLINK = tex("tanny_blink");
    private static final ResourceLocation HAPPY = tex("tanny_happy");
    private static final ResourceLocation SURPRISED = tex("tanny_surprised");
    private static final ResourceLocation FOCUSED = tex("tanny_focused");
    private static final ResourceLocation TIRED = tex("tanny_tired");

    public TannyRenderer(EntityRendererProvider.Context context) {
        super(context, new TannyModel(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.45F);
        this.addLayer(new net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer<>(this,
                new net.minecraft.client.model.HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                new net.minecraft.client.model.HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new TannyOutfitLayer(this, context.getModelSet()));
        this.addLayer(new TannyHairLayer(this, context.getModelSet()));
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(TannyMaid.MODID, "textures/entity/tanny/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(TannyEntity entity) {
        if (entity.isWeakened()) return TIRED;
        if (entity.hurtTime > 0) return SURPRISED;
        long t = entity.tickCount + entity.getId() * 31L;
        boolean blink = t % 90 < 3 || (t % 270 >= 6 && t % 270 < 9);
        if (blink) return BLINK;
        if (entity.isTalking()) return HAPPY;
        if (entity.isAggressive()) return FOCUSED;
        return NORMAL;
    }

    @Override
    protected void scale(TannyEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.92F, 0.92F, 0.92F);
        if (entity.isWeakened()) {
            // Sentada en el suelo mientras descansa
            poseStack.translate(0.0F, 0.6F, 0.0F);
        }
    }
}
