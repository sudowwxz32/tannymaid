package com.cz.client;

import com.cz.entity.DebrisEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/** Dibuja el escombro como un bloque pequeño que gira en el aire. */
public class DebrisRenderer extends EntityRenderer<DebrisEntity> {
    public DebrisRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(DebrisEntity e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        float size = e.getSize();
        float t = e.tickCount + partial;
        boolean moving = !e.onGround() || e.getDeltaMovement().horizontalDistanceSqr() > 0.0004;
        float spin = e.isFloating() ? t * 2.0F + e.getId() * 37 : moving ? t * (9.0F + (e.getId() % 7)) : (e.getId() % 4) * 20.0F;
        pose.pushPose();
        pose.translate(0, size / 2, 0);
        pose.mulPose(Axis.YP.rotationDegrees(e.getYRot()));
        pose.mulPose(Axis.XP.rotationDegrees(spin));
        pose.mulPose(Axis.ZP.rotationDegrees(spin * 0.6F));
        pose.scale(size, size, size);
        pose.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(e.getBlock(), pose, buf, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(e, yaw, partial, pose, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(DebrisEntity e) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
