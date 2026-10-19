package com.laury.laurymod.client;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.entity.LauryEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Laury en forma humana: cuerpo, armadura, objetos en mano y rasgos de dragón 3D. Parpadea. */
public class LauryHumanRenderer extends HumanoidMobRenderer<LauryEntity, LauryHumanModel> {
    private static final ResourceLocation NORMAL = tex("laury");
    private static final ResourceLocation BLINK = tex("laury_blink");
    private static final ResourceLocation HAPPY = tex("laury_happy");
    private static final ResourceLocation SURPRISED = tex("laury_surprised");
    private static final ResourceLocation ANGRY = tex("laury_angry");
    private static final ResourceLocation TIRED = tex("laury_tired");

    public LauryHumanRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new LauryHumanModel(ctx.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.45F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)), ctx.getModelManager()));
        this.addLayer(new LauryFeaturesLayer(this, ctx.getModelSet()));
    }

    private static ResourceLocation tex(String n) {
        return new ResourceLocation(LauryMod.MODID, "textures/entity/" + n + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(LauryEntity e) {
        if (e.isWeakened()) return TIRED;
        if (e.hurtTime > 0) return SURPRISED;
        long t = e.tickCount + e.getId() * 37L;
        if (t % 85 < 3 || (t % 260 >= 7 && t % 260 < 10)) return BLINK;
        if (e.isTalking()) return HAPPY;
        if (e.isAggressive()) return ANGRY;
        return NORMAL;
    }

    @Override
    protected void scale(LauryEntity e, PoseStack pose, float partial) {
        pose.scale(0.9375F, 0.9375F, 0.9375F);
        if (e.isWeakened()) pose.translate(0.0F, 0.6F, 0.0F);
    }

    @Override
    protected boolean shouldShowName(LauryEntity e) {
        return super.shouldShowName(e) || e.hasCustomName();
    }

    @Override
    protected void renderNameTag(LauryEntity e, Component name, PoseStack pose, MultiBufferSource buf, int light) {
        super.renderNameTag(e, LauryRenderer.healthTag(e), pose, buf, light);
    }
}
