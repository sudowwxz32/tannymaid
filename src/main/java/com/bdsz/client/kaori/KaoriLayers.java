package com.bdsz.client.kaori;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.companion.KaoriEntity;
import com.mojang.blaze3d.vertex.PoseStack;
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

/** Capas 3D de Kaori: la ropa y la cabeza (cabello en mechones, gafas, coletero y pestañas). */
public final class KaoriLayers {
    private KaoriLayers() {}

    public static final ModelLayerLocation OUTFIT = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "kaori"), "outfit");
    public static final ModelLayerLocation HEAD = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "kaori"), "head");
    public static final int HAIR_COLOR = 0x802A4A;

    public static class Outfit extends RenderLayer<KaoriEntity, KaoriModel> {
        private static final ResourceLocation TEX = KaoriRenderer.tex("outfit");
        private final ModelPart root, body, rightArm, leftArm, rightLeg, leftLeg;
        private final Map<String, ModelPart> parts;

        public Outfit(RenderLayerParent<KaoriEntity, KaoriModel> parent, EntityModelSet models) {
            super(parent);
            root = models.bakeLayer(OUTFIT);
            parts = KaoriModels.bakeOutfit(root);
            body = parts.get("body");
            rightArm = parts.get("right_arm");
            leftArm = parts.get("left_arm");
            rightLeg = parts.get("right_leg");
            leftLeg = parts.get("left_leg");
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, KaoriEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            KaoriModel m = getParentModel();
            body.copyFrom(m.body);
            rightArm.copyFrom(m.rightArm);
            leftArm.copyFrom(m.leftArm);
            rightLeg.copyFrom(m.rightLeg);
            leftLeg.copyFrom(m.leftLeg);
            // con armadura se esconde la ropa que quedaría por fuera
            boolean chest = !e.getItemBySlot(EquipmentSlot.CHEST).isEmpty(), legs = !e.getItemBySlot(EquipmentSlot.LEGS).isEmpty(),
                    feet = !e.getItemBySlot(EquipmentSlot.FEET).isEmpty();
            for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
                String n = en.getKey();
                if (n.contains("_arm") || n.contains("_leg") || n.equals("body")) continue;
                boolean v = true;
                if (chest && (n.startsWith("jacket") || n.startsWith("collar") || n.startsWith("sleeve") || n.startsWith("cuff") || n.startsWith("capsule_case"))) v = false;
                if (legs && (n.startsWith("shorts") || n.startsWith("short_leg") || n.startsWith("cargo") || n.startsWith("belt") || n.startsWith("pouch") || n.startsWith("capsule_"))) v = false;
                if (feet && (n.startsWith("boot") || n.startsWith("toe"))) v = false;
                en.getValue().visible = v;
            }
            root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(TEX)), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F));
        }
    }

    public static class Head extends RenderLayer<KaoriEntity, KaoriModel> {
        private static final ResourceLocation TEX = KaoriRenderer.tex("head"), STRAND = KaoriRenderer.tex("hair_strand");
        private final ModelPart root, head, lidR, lidL, lidBR, lidBL, lashR, lashL, goggles, scrunchie;
        private final KaoriHairMesh mesh = new KaoriHairMesh(KaoriHairLocks.L, HAIR_COLOR);

        public Head(RenderLayerParent<KaoriEntity, KaoriModel> parent, EntityModelSet models) {
            super(parent);
            root = models.bakeLayer(HEAD);
            Map<String, ModelPart> m = KaoriModels.bakeHead(root);
            head = m.get("head");
            lidR = m.get("lid_r");
            lidL = m.get("lid_l");
            lidBR = m.get("lidb_r");
            lidBL = m.get("lidb_l");
            lashR = m.get("lash_r");
            lashL = m.get("lash_l");
            goggles = m.get("goggles");
            scrunchie = m.get("scrunchie");
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, KaoriEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            ModelPart ph = getParentModel().head;
            int overlay = LivingEntityRenderer.getOverlayCoords(e, 0.0F);
            boolean helmet = !e.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
            float breeze = Mth.sin(age * 0.07F) * 0.04F;
            float walk = Mth.sin(limbSwing * 0.6662F) * limbAmount;
            // la coleta se balancea al caminar y con la brisa
            float swayX = 0.08F + breeze + limbAmount * 0.45F - ph.xRot * 0.35F;
            float swayZ = Mth.sin(age * 0.05F) * 0.04F + walk * 0.12F - ph.zRot * 0.6F;
            pose.pushPose();
            ph.translateAndRotate(pose);
            mesh.render(buf.getBuffer(RenderType.entityCutoutNoCull(STRAND)), pose.last().pose(), pose.last().normal(),
                    light, overlay, swayX, swayZ, breeze * 0.5F + limbAmount * 0.1F, 0.0F, helmet);
            pose.popPose();

            KaoriRenderer.Face f = KaoriRenderer.face(e);
            boolean open = f == KaoriRenderer.Face.NORMAL || f == KaoriRenderer.Face.SURPRISED || f == KaoriRenderer.Face.FOCUSED;
            boolean wings = open || f == KaoriRenderer.Face.BLINK;
            lidR.visible = lidL.visible = lidBR.visible = lidBL.visible = open;
            lashR.visible = lashL.visible = wings;
            goggles.visible = scrunchie.visible = !helmet;
            head.copyFrom(ph);
            root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(TEX)), light, overlay);
            lidR.visible = lidL.visible = lidBR.visible = lidBL.visible = lashR.visible = lashL.visible = true;
            goggles.visible = scrunchie.visible = true;
        }
    }
}
