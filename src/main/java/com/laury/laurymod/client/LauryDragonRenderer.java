package com.laury.laurymod.client;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.entity.LauryEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Laury en forma de gran dragón azul. */
public class LauryDragonRenderer extends MobRenderer<LauryEntity, LauryDragonModel> {
    private static final ResourceLocation TEX = new ResourceLocation(LauryMod.MODID, "textures/entity/laury_dragon.png");
    public static final float SCALE = 2.0F;

    public LauryDragonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new LauryDragonModel(ctx.bakeLayer(ClientSetup.DRAGON)), 1.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(LauryEntity e) {
        return TEX;
    }

    @Override
    protected void scale(LauryEntity e, PoseStack pose, float partial) {
        pose.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected boolean shouldShowName(LauryEntity e) {
        return (super.shouldShowName(e) || e.hasCustomName()) && !e.isVehicle();
    }

    @Override
    protected void renderNameTag(LauryEntity e, Component name, PoseStack pose, MultiBufferSource buf, int light) {
        super.renderNameTag(e, LauryRenderer.healthTag(e), pose, buf, light);
    }
}
