package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cabello 3D de Tanny (bob con capas, flequillo puntiagudo, mechones largos, ahoge y cofia de maid).
 * Se mueve con la brisa y al caminar; con casco solo se ve el pelo que sale por debajo.
 */
public class TannyHairLayer extends RenderLayer<TannyEntity, TannyModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(TannyMaid.MODID, "tanny"), "hair");
    private static final ResourceLocation TEXTURE = new ResourceLocation(TannyMaid.MODID, "textures/entity/tanny/hair.png");

    private final ModelPart root;
    private final ModelPart head, back, sideR, sideL, lockR, lockL, ahoge;
    private final List<ModelPart> bangs = new ArrayList<>();
    private final List<ModelPart> underHelmet = new ArrayList<>();

    public TannyHairLayer(RenderLayerParent<TannyEntity, TannyModel> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(LAYER);
        Map<String, ModelPart> m = TannyGeneratedModels.bakeHair(root);
        head = m.get("head");
        back = m.get("back");
        sideR = m.get("side_r");
        sideL = m.get("side_l");
        lockR = m.get("lock_r");
        lockL = m.get("lock_l");
        ahoge = m.get("ahoge");
        for (int i = 0; m.containsKey("bang_" + i); i++) bangs.add(m.get("bang_" + i));
        for (String k : new String[] {"crown", "crown_back", "tuft_0", "tuft_1", "ahoge", "headband"}) underHelmet.add(m.get(k));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TannyEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        head.copyFrom(getParentModel().head);

        float breeze = Mth.sin(ageInTicks * 0.08F) * 0.025F;
        float walk = Math.abs(Mth.cos(limbSwing * 0.6662F)) * limbSwingAmount;
        sideR.zRot = 0.07F + breeze + walk * 0.08F;
        sideL.zRot = -0.07F - breeze - walk * 0.08F;
        back.xRot = 0.07F + breeze + limbSwingAmount * 0.18F;
        lockR.xRot = -0.05F + walk * 0.12F;
        lockL.xRot = -0.05F + walk * 0.12F;
        for (int i = 0; i < bangs.size(); i++) {
            bangs.get(i).xRot = TannyGeneratedModels.BANG_TILT - limbSwingAmount * 0.12F + Mth.sin(ageInTicks * 0.1F + i) * 0.015F;
        }
        // el ahoge rebota al caminar y al hablar
        ahoge.zRot = 0.25F + Mth.sin(ageInTicks * 0.15F) * 0.08F + walk * 0.25F;
        ahoge.xRot = -0.35F + (entity.isTalking() ? Mth.sin(ageInTicks * 0.6F) * 0.15F : 0.0F);

        boolean helmet = !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        if (helmet) for (ModelPart p : underHelmet) p.visible = false;
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        root.render(poseStack, vc, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
        if (helmet) for (ModelPart p : underHelmet) p.visible = true;
    }
}
