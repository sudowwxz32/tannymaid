package com.laury.laurymod.client;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.entity.LauryEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
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

    /** Inclina todo el cuerpo: nariz abajo en picada, arriba al subir, alabeo en las curvas y vaivén de cada aletazo. */
    @Override
    protected void setupRotations(LauryEntity e, PoseStack pose, float age, float yaw, float partial) {
        super.setupRotations(e, pose, age, yaw, partial);
        if (e.isWeakened()) return;
        float pitch = Mth.lerp(partial, e.prevFlightPitch, e.flightPitch);
        float roll = Mth.lerp(partial, e.prevFlightRoll, e.flightRoll);
        float phase = Mth.lerp(partial, e.prevFlapPhase, e.flapPhase);
        float str = e.flapStrength;
        roll += Mth.sin(phase) * 3.0F * str;            // vaivén lateral con cada aletazo
        pitch += Mth.cos(phase) * 2.0F * str;
        float bob = -Mth.cos(phase) * 0.12F * str;      // el cuerpo sube cuando las alas bajan
        float center = 1.4F;
        pose.translate(0.0F, center + bob, 0.0F);
        pose.mulPose(Axis.XP.rotationDegrees(-pitch));
        pose.mulPose(Axis.ZP.rotationDegrees(-roll));
        pose.translate(0.0F, -center, 0.0F);
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
