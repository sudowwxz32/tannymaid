package com.tanny.tannymaid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tanny.tannymaid.TannyMaid;
import com.tanny.tannymaid.yunn.YunnEntity;
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

import java.util.Map;

/** Cabello largo y liso de Yunn (malla de mechones lavanda), su cofia con corazón y las pestañas 3D. */
public class YunnHairLayer extends RenderLayer<YunnEntity, YunnModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(TannyMaid.MODID, "yunn"), "cap");
    private static final ResourceLocation CAP_TEX = new ResourceLocation(TannyMaid.MODID, "textures/entity/yunn/cap.png");
    private static final ResourceLocation STRAND_TEX = new ResourceLocation(TannyMaid.MODID, "textures/entity/tanny/hair_strand.png");
    public static final int HAIR_COLOR = 0xC49EDE;

    private final ModelPart root, head, lidR, lidL, lidBR, lidBL, lashR, lashL;
    private final TannyHairMesh mesh = new TannyHairMesh(YunnHairLocks.L, HAIR_COLOR);

    public YunnHairLayer(RenderLayerParent<YunnEntity, YunnModel> parent, EntityModelSet models) {
        super(parent);
        root = models.bakeLayer(LAYER);
        Map<String, ModelPart> m = TannyGeneratedModels.bakeYunnCap(root);
        head = m.get("head");
        lidR = m.get("lid_r");
        lidL = m.get("lid_l");
        lidBR = m.get("lidb_r");
        lidBL = m.get("lidb_l");
        lashR = m.get("lash_r");
        lashL = m.get("lash_l");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, YunnEntity e, float limbSwing, float limbAmount,
                       float partial, float age, float headYaw, float headPitch) {
        if (e.isInvisible()) return;
        ModelPart ph = getParentModel().head;
        int overlay = LivingEntityRenderer.getOverlayCoords(e, 0.0F);
        float breeze = Mth.sin(age * 0.07F) * 0.04F;
        float walk = Mth.sin(limbSwing * 0.6662F) * limbAmount;
        float swayX = 0.06F + breeze + limbAmount * 0.35F - ph.xRot * 0.3F;
        float swayZ = Mth.sin(age * 0.05F) * 0.03F + walk * 0.06F - ph.zRot * 0.6F;
        pose.pushPose();
        ph.translateAndRotate(pose);
        mesh.render(buf.getBuffer(RenderType.entityCutoutNoCull(STRAND_TEX)), pose.last().pose(), pose.last().normal(),
                light, overlay, swayX, swayZ, breeze * 0.5F + limbAmount * 0.1F, 0.0F, false);
        pose.popPose();

        TannyRenderer.Face f = YunnRenderer.face(e);
        boolean open = f == TannyRenderer.Face.NORMAL || f == TannyRenderer.Face.SURPRISED;
        boolean wings = open || f == TannyRenderer.Face.FOCUSED || f == TannyRenderer.Face.BLINK;
        lidR.visible = lidL.visible = lidBR.visible = lidBL.visible = open;
        lashR.visible = lashL.visible = wings;
        head.copyFrom(ph);
        root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(CAP_TEX)), light, overlay);
        lidR.visible = lidL.visible = lidBR.visible = lidBL.visible = lashR.visible = lashL.visible = true;
    }
}
