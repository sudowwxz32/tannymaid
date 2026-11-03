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
    public static final ModelLayerLocation ARMOR_INNER = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "kaori"), "armor_inner");
    public static final ModelLayerLocation ARMOR_OUTER = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "kaori"), "armor_outer");

    public static net.minecraft.client.model.geom.builders.LayerDefinition armorInner() {
        return net.minecraft.client.model.geom.builders.LayerDefinition.create(
                net.minecraft.client.model.HumanoidArmorModel.createBodyLayer(new net.minecraft.client.model.geom.builders.CubeDeformation(0.85F)), 64, 32);
    }

    public static net.minecraft.client.model.geom.builders.LayerDefinition armorOuter() {
        return net.minecraft.client.model.geom.builders.LayerDefinition.create(
                net.minecraft.client.model.HumanoidArmorModel.createBodyLayer(new net.minecraft.client.model.geom.builders.CubeDeformation(1.35F)), 64, 32);
    }

    public static final int HAIR_COLOR = 0x26242C, SSJ_COLOR = 0xFFD84A, SSJ2_COLOR = 0xFFE66A;

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
            boolean pv = KaoriRenderer.PREVIEW;
            boolean chest = pv && !e.getItemBySlot(EquipmentSlot.CHEST).isEmpty(), legs = pv && !e.getItemBySlot(EquipmentSlot.LEGS).isEmpty(),
                    feet = pv && !e.getItemBySlot(EquipmentSlot.FEET).isEmpty();
            for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
                String n = en.getKey();
                if (n.contains("_arm") || n.contains("_leg") || n.equals("body")) continue;
                boolean v = true;
                if (chest && (n.startsWith("jacket") || n.startsWith("collar") || n.startsWith("sleeve") || n.startsWith("cuff") || n.startsWith("capsule_case")
                        || n.equals("bust") || n.startsWith("glove"))) v = false;
                if (legs && (n.startsWith("shorts") || n.startsWith("short_leg") || n.startsWith("cargo") || n.startsWith("belt") || n.startsWith("pouch")
                        || n.startsWith("capsule_") || n.startsWith("wrench"))) v = false;
                if (feet && (n.startsWith("boot") || n.startsWith("toe"))) v = false;
                en.getValue().visible = v;
            }
            root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(TEX)), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F));
            // antorcha colgada del cinturón (lado derecho)
            net.minecraft.world.item.ItemStack torch = e.getBeltTorch();
            if (!torch.isEmpty() && !legs) {
                pose.pushPose();
                body.translateAndRotate(pose);
                pose.translate(-4.9F / 16F, 9.2F / 16F, 0.2F / 16F);
                pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180F + 18F));
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90F));
                pose.scale(0.42F, 0.42F, 0.42F);
                net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(torch,
                        net.minecraft.world.item.ItemDisplayContext.FIXED, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                        pose, buf, e.level(), e.getId());
                pose.popPose();
            }
        }
    }

    public static class Head extends RenderLayer<KaoriEntity, KaoriModel> {
        private static final ResourceLocation TEX = KaoriRenderer.tex("head"), STRAND = KaoriRenderer.tex("hair_strand");
        private final ModelPart root, head, goggles, scrunchie;
        private final KaoriHairMesh mesh = new KaoriHairMesh(KaoriHairLocks.L, HAIR_COLOR);
        private final KaoriHairMesh ssj = new KaoriHairMesh(KaoriHairLocks.L, SSJ_COLOR), ssj2 = new KaoriHairMesh(KaoriHairLocks.L, SSJ2_COLOR),
                ssj3 = new KaoriHairMesh(KaoriHairLocks.L, 0xFFCF2E), god = new KaoriHairMesh(KaoriHairLocks.L, 0xE0304C);

        public Head(RenderLayerParent<KaoriEntity, KaoriModel> parent, EntityModelSet models) {
            super(parent);
            root = models.bakeLayer(HEAD);
            Map<String, ModelPart> m = KaoriModels.bakeHead(root);
            head = m.get("head");
            goggles = m.get("goggles");
            scrunchie = m.get("scrunchie");
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, KaoriEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            ModelPart ph = getParentModel().head;
            int overlay = LivingEntityRenderer.getOverlayCoords(e, 0.0F);
            boolean helmet = KaoriRenderer.PREVIEW && !e.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
            float breeze = Mth.sin(age * 0.07F) * 0.04F;
            float walk = Mth.sin(limbSwing * 0.6662F) * limbAmount;
            // la coleta se balancea al caminar y con la brisa
            float swayX = 0.08F + breeze + limbAmount * 0.45F - ph.xRot * 0.35F;
            float swayZ = Mth.sin(age * 0.05F) * 0.04F + walk * 0.12F - ph.zRot * 0.6F;
            pose.pushPose();
            ph.translateAndRotate(pose);
            int form = e.getForm();
            KaoriHairMesh hair = switch (form) { case 1 -> ssj; case 2 -> ssj2; case 3 -> ssj3; case 4 -> god; default -> mesh; };
            if (form == 3) {
                // Fase 3: cabello mucho más largo y voluminoso (la coleta llega hasta la cintura)
                pose.translate(0, -0.5F / 16F, 0);
                pose.scale(1.12F, 1.6F, 1.18F);
            }
            if (form == 4) {
                // Dios: cabello rojo, sereno, apenas se mueve
                swayX = 0.05F + breeze * 0.5F;
                swayZ = Mth.sin(age * 0.05F) * 0.02F;
                light = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
            } else if (form > 0) {
                // cabello dorado que brilla, se eleva un poco y ondea con el aura
                swayX = -0.25F + Mth.sin(age * 0.6F) * 0.06F;
                swayZ = Mth.sin(age * 0.45F) * 0.05F;
                light = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
            }
            hair.render(buf.getBuffer(RenderType.entityCutoutNoCull(STRAND)), pose.last().pose(), pose.last().normal(),
                    light, overlay, swayX, swayZ, breeze * 0.5F + limbAmount * 0.1F, 0.0F, helmet);
            pose.popPose();

            goggles.visible = scrunchie.visible = !helmet;
            head.copyFrom(ph);
            root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(TEX)), light, overlay);
            goggles.visible = scrunchie.visible = true;
        }
    }

    /** Pequeña nave de cápsula en la que Kaori te acompaña cuando vuelas. */
    public static final ModelLayerLocation SHIP = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "kaori"), "ship");

    public static net.minecraft.client.model.geom.builders.LayerDefinition ship() {
        net.minecraft.client.model.geom.builders.MeshDefinition mesh = new net.minecraft.client.model.geom.builders.MeshDefinition();
        net.minecraft.client.model.geom.builders.PartDefinition root = mesh.getRoot();
        net.minecraft.client.model.geom.builders.CubeListBuilder hull = net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.0F, 12.5F, -11.0F, 14.0F, 5.0F, 20.0F)          // casco blanco
                .texOffs(0, 0).addBox(-5.0F, 13.0F, -15.0F, 10.0F, 4.0F, 4.0F)           // proa
                .texOffs(0, 0).addBox(-3.0F, 13.5F, -17.0F, 6.0F, 3.0F, 2.0F)
                .texOffs(0, 32).addBox(-7.3F, 12.2F, -11.3F, 14.6F, 1.0F, 20.6F)         // franja naranja
                .texOffs(0, 32).addBox(-14.0F, 15.0F, -2.0F, 7.0F, 1.2F, 9.0F)           // alas
                .texOffs(0, 32).addBox(7.0F, 15.0F, -2.0F, 7.0F, 1.2F, 9.0F)
                .texOffs(0, 64).addBox(-7.5F, 13.5F, 8.0F, 4.0F, 4.0F, 4.5F)             // motores
                .texOffs(0, 64).addBox(3.5F, 13.5F, 8.0F, 4.0F, 4.0F, 4.5F)
                .texOffs(0, 64).addBox(-6.0F, 17.5F, -8.0F, 12.0F, 1.5F, 14.0F)          // panza
                .texOffs(0, 64).addBox(-14.5F, 14.0F, 4.0F, 1.0F, 3.0F, 3.0F)            // puntas de las alas
                .texOffs(0, 64).addBox(13.5F, 14.0F, 4.0F, 1.0F, 3.0F, 3.0F);
        root.addOrReplaceChild("hull", hull, net.minecraft.client.model.geom.PartPose.ZERO);
        root.addOrReplaceChild("glass", net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                .texOffs(0, 96).addBox(-6.0F, 6.5F, -11.5F, 12.0F, 6.0F, 1.2F)           // parabrisas
                .texOffs(0, 96).addBox(-6.2F, 9.0F, -10.5F, 1.0F, 3.5F, 5.0F)
                .texOffs(0, 96).addBox(5.2F, 9.0F, -10.5F, 1.0F, 3.5F, 5.0F), net.minecraft.client.model.geom.PartPose.ZERO);
        root.addOrReplaceChild("glow", net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                .texOffs(0, 32).addBox(-6.5F, 14.5F, 12.5F, 2.0F, 2.0F, 0.6F)            // fuego de los motores
                .texOffs(0, 32).addBox(4.5F, 14.5F, 12.5F, 2.0F, 2.0F, 0.6F), net.minecraft.client.model.geom.PartPose.ZERO);
        return net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh, 128, 128);
    }

    public static class Ship extends RenderLayer<KaoriEntity, KaoriModel> {
        private static final ResourceLocation TEX = KaoriRenderer.tex("ship");
        private final ModelPart hull, glass, glow;

        public Ship(RenderLayerParent<KaoriEntity, KaoriModel> parent, EntityModelSet models) {
            super(parent);
            ModelPart root = models.bakeLayer(SHIP);
            hull = root.getChild("hull");
            glass = root.getChild("glass");
            glow = root.getChild("glow");
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, KaoriEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (!e.isFlying() || e.isInvisible()) return;
            int overlay = LivingEntityRenderer.getOverlayCoords(e, 0.0F);
            pose.pushPose();
            pose.translate(0, Mth.sin(age * 0.15F) * 0.03F, 0);                       // flota suavemente
            pose.mulPose(com.mojang.math.Axis.ZP.rotation(Mth.sin(age * 0.07F) * 0.04F));
            hull.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(TEX)), light, overlay);
            glass.render(pose, buf.getBuffer(RenderType.entityTranslucent(TEX)), light, overlay);
            float f = 0.75F + 0.25F * Mth.sin(age * 1.3F);
            glow.render(pose, buf.getBuffer(RenderType.eyes(TEX)), net.minecraft.client.renderer.LightTexture.FULL_BRIGHT, overlay, f, f, f, 1.0F);
            pose.popPose();
        }
    }
}
