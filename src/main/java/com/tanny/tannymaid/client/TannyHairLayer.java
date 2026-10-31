package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.PoseStack;
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

import java.util.Map;

/**
 * Cabello 3D de Tanny: una malla de mechones curvos y suaves (bob de anime con flequillo en punta,
 * mechones largos, ahoge y brillo) más la cofia de maid. Se mueve con la brisa, al caminar y al hablar;
 * con casco solo se ve el pelo que sale por debajo.
 */
public class TannyHairLayer extends RenderLayer<TannyEntity, TannyModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(TannyMaid.MODID, "tanny"), "hair");
    private static final ResourceLocation BAND_TEX = new ResourceLocation(TannyMaid.MODID, "textures/entity/tanny/hair.png");
    private static final ResourceLocation STRAND_TEX = new ResourceLocation(TannyMaid.MODID, "textures/entity/tanny/hair_strand.png");

    private final ModelPart root;
    private final ModelPart head;
    private final TannyHairMesh mesh = new TannyHairMesh();

    public TannyHairLayer(RenderLayerParent<TannyEntity, TannyModel> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(LAYER);
        Map<String, ModelPart> m = TannyGeneratedModels.bakeHair(root);
        head = m.get("head");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TannyEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        ModelPart parentHead = getParentModel().head;
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        boolean helmet = !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();

        // vaivén: la brisa, el paso al caminar y la inclinación de la cabeza
        float breeze = Mth.sin(ageInTicks * 0.08F) * 0.04F;
        float walk = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount;
        float swayX = 0.08F + breeze + limbSwingAmount * 0.45F - parentHead.xRot * 0.35F;
        float swayZ = Mth.sin(ageInTicks * 0.05F + 1.3F) * 0.03F + walk * 0.08F - parentHead.zRot * 0.6F;
        float bangSway = breeze * 0.6F + limbSwingAmount * 0.12F;
        float ahoge = Mth.sin(ageInTicks * 0.15F) * 0.12F + Math.abs(walk) * 0.35F
                + (entity.isTalking() ? Mth.sin(ageInTicks * 0.6F) * 0.2F : 0.0F);

        poseStack.pushPose();
        parentHead.translateAndRotate(poseStack);
        mesh.render(buffer.getBuffer(RenderType.entityCutoutNoCull(STRAND_TEX)), poseStack.last().pose(), poseStack.last().normal(),
                packedLight, overlay, swayX, swayZ, bangSway, ahoge, helmet);
        poseStack.popPose();

        if (!helmet) {
            head.copyFrom(parentHead);
            root.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(BAND_TEX)), packedLight, overlay);
        }
    }
}
