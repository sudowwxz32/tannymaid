// GENERADO por tools/gen_kaori.py — no editar a mano.
package com.bdsz.client.kaori;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KaoriModels {
    private KaoriModels() {}

    public static LayerDefinition createHead() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_goggles = p_head.addOrReplaceChild("goggles", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -9.15F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_lens_r = p_goggles.addOrReplaceChild("lens_r", CubeListBuilder.create().texOffs(11, 0).addBox(-1.35F, -1.05F, -0.75F, 2.7F, 2.1F, 1.5F), PartPose.offsetAndRotation(-1.75F, 0F, -0.6F, 0F, 0.12F, 0F));
        PartDefinition p_goggle_bridge = p_goggles.addOrReplaceChild("goggle_bridge", CubeListBuilder.create().texOffs(50, 0).addBox(-0.5F, -0.3F, -0.3F, 1F, 0.6F, 0.6F), PartPose.offsetAndRotation(0F, 0F, -0.5F, 0F, 0F, 0F));
        PartDefinition p_goggle_strap = p_goggles.addOrReplaceChild("goggle_strap", CubeListBuilder.create().texOffs(31, 0).addBox(-4.2F, -0.35F, -0.3F, 8.4F, 0.7F, 0.6F), PartPose.offsetAndRotation(0F, 0F, 0.4F, 0F, 0F, 0F));
        PartDefinition p_nut_r = p_head.addOrReplaceChild("nut_r", CubeListBuilder.create().texOffs(55, 0).addBox(-0.35F, 0F, -0.45F, 0.7F, 0.9F, 0.9F), PartPose.offsetAndRotation(-4.3F, -2.4F, -0.4F, 0F, 0F, 0F));
        PartDefinition p_scrunchie = p_head.addOrReplaceChild("scrunchie", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -0.9F, 3F, 3F, 1.8F), PartPose.offsetAndRotation(0F, -7.5F, 4.15F, 0.75F, 0F, 0F));
        PartDefinition p_lens_l = p_goggles.addOrReplaceChild("lens_l", CubeListBuilder.create().texOffs(21, 0).addBox(-1.35F, -1.05F, -0.75F, 2.7F, 2.1F, 1.5F), PartPose.offsetAndRotation(1.75F, 0F, -0.6F, 0F, -0.12F, 0F));
        PartDefinition p_nut_l = p_head.addOrReplaceChild("nut_l", CubeListBuilder.create().texOffs(60, 0).addBox(-0.35F, 0F, -0.45F, 0.7F, 0.9F, 0.9F), PartPose.offsetAndRotation(4.3F, -2.4F, -0.4F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static Map<String, ModelPart> bakeHead(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("goggles", m.get("head").getChild("goggles"));
        m.put("lens_r", m.get("goggles").getChild("lens_r"));
        m.put("goggle_bridge", m.get("goggles").getChild("goggle_bridge"));
        m.put("goggle_strap", m.get("goggles").getChild("goggle_strap"));
        m.put("nut_r", m.get("head").getChild("nut_r"));
        m.put("scrunchie", m.get("head").getChild("scrunchie"));
        m.put("lens_l", m.get("goggles").getChild("lens_l"));
        m.put("nut_l", m.get("head").getChild("nut_l"));
        return m;
    }

    public static LayerDefinition createOutfit() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_top = p_body.addOrReplaceChild("top", CubeListBuilder.create().texOffs(31, 0).addBox(-4.25F, -0.3F, -2.25F, 8.5F, 7.6F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(0, 68).addBox(-3.3F, 0F, -0.85F, 6.6F, 2.9F, 0.95F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.28F, 0F, 0F));
        PartDefinition p_jacket = p_body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(0, 0).addBox(-4.65F, -0.45F, -2.65F, 9.3F, 8.9F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_collar_back = p_body.addOrReplaceChild("collar_back", CubeListBuilder.create().texOffs(6, 73).addBox(-3.4F, -1.6F, -0.35F, 6.8F, 1.6F, 0.7F), PartPose.offsetAndRotation(0F, -0.35F, 2.35F, 0.35F, 0F, 0F));
        PartDefinition p_collar_r = p_body.addOrReplaceChild("collar_r", CubeListBuilder.create().texOffs(43, 68).addBox(-0.9F, 0F, -0.35F, 1.8F, 2.6F, 0.7F), PartPose.offsetAndRotation(-2.3F, -0.3F, -2.55F, -0.25F, 0.3F, -0.25F));
        PartDefinition p_belt = p_body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(17, 51).addBox(-4.55F, 7.15F, -2.6F, 9.1F, 1.25F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_pouch_r = p_body.addOrReplaceChild("pouch_r", CubeListBuilder.create().texOffs(27, 68).addBox(-1F, 0F, -1.1F, 2F, 2.3F, 1.3F), PartPose.offsetAndRotation(-3.55F, 7.7F, -2.55F, 0F, 0.2F, 0F));
        PartDefinition p_capsule_r = p_body.addOrReplaceChild("capsule_r", CubeListBuilder.create().texOffs(49, 68).addBox(-0.45F, -0.1F, -0.6F, 0.9F, 1.9F, 1.2F), PartPose.offsetAndRotation(-4.6F, 7.9F, 0.6F, 0F, 0F, 0.12F));
        PartDefinition p_wrench = p_body.addOrReplaceChild("wrench", CubeListBuilder.create().texOffs(56, 60).addBox(-0.3F, 0F, -0.35F, 0.6F, 3.6F, 0.7F).texOffs(46, 73).addBox(-0.75F, 3.4F, -0.45F, 1.5F, 1.1F, 0.9F), PartPose.offsetAndRotation(4.75F, 7.6F, -0.4F, 0F, 0F, -0.18F));
        PartDefinition p_capsule_case = p_body.addOrReplaceChild("capsule_case", CubeListBuilder.create().texOffs(17, 68).addBox(-1.6F, 0F, 0F, 3.2F, 2.2F, 1.2F), PartPose.offsetAndRotation(0F, 7.6F, 2.55F, 0F, 0F, 0F));
        PartDefinition p_shorts = p_body.addOrReplaceChild("shorts", CubeListBuilder.create().texOffs(20, 29).addBox(-4.35F, 7.7F, -2.35F, 8.7F, 4.6F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_sleeve_r = p_right_arm.addOrReplaceChild("sleeve_r", CubeListBuilder.create().texOffs(0, 16).addBox(-2.4F, -2.45F, -2.4F, 3.8F, 6.6F, 4.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cuff_r = p_right_arm.addOrReplaceChild("cuff_r", CubeListBuilder.create().texOffs(0, 60).addBox(-2.6F, 3.4F, -2.6F, 4.2F, 1.5F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_glove_r = p_right_arm.addOrReplaceChild("glove_r", CubeListBuilder.create().texOffs(40, 41).addBox(-2.22F, 7.3F, -2.22F, 3.44F, 2.9F, 4.44F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_short_leg_r = p_right_leg.addOrReplaceChild("short_leg_r", CubeListBuilder.create().texOffs(0, 41).addBox(-2.3F, -0.2F, -2.3F, 4.6F, 3.8F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cargo_r = p_right_leg.addOrReplaceChild("cargo_r", CubeListBuilder.create().texOffs(40, 60).addBox(-0.8F, 0F, -1.3F, 0.9F, 2.4F, 2.6F), PartPose.offsetAndRotation(-2.25F, 0.9F, 0F, 0F, 0F, 0.05F));
        PartDefinition p_boot_r = p_right_leg.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(38, 16).addBox(-2.3F, 6.2F, -2.3F, 4.6F, 5.95F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_toe_r = p_right_leg.addOrReplaceChild("toe_r", CubeListBuilder.create().texOffs(22, 73).addBox(-2.3F, 10.1F, -3.05F, 4.6F, 2.05F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_collar_l = p_body.addOrReplaceChild("collar_l", CubeListBuilder.create().texOffs(55, 68).addBox(-0.9F, 0F, -0.35F, 1.8F, 2.6F, 0.7F), PartPose.offsetAndRotation(2.3F, -0.3F, -2.55F, -0.25F, -0.3F, 0.25F));
        PartDefinition p_pouch_l = p_body.addOrReplaceChild("pouch_l", CubeListBuilder.create().texOffs(35, 68).addBox(-1F, 0F, -1.1F, 2F, 2.3F, 1.3F), PartPose.offsetAndRotation(3.55F, 7.7F, -2.55F, 0F, -0.2F, 0F));
        PartDefinition p_capsule_l = p_body.addOrReplaceChild("capsule_l", CubeListBuilder.create().texOffs(0, 73).addBox(-0.45F, -0.1F, -0.6F, 0.9F, 1.9F, 1.2F), PartPose.offsetAndRotation(4.6F, 7.9F, 0.6F, 0F, 0F, -0.12F));
        PartDefinition p_sleeve_l = p_left_arm.addOrReplaceChild("sleeve_l", CubeListBuilder.create().texOffs(19, 16).addBox(-1.4F, -2.45F, -2.4F, 3.8F, 6.6F, 4.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cuff_l = p_left_arm.addOrReplaceChild("cuff_l", CubeListBuilder.create().texOffs(20, 60).addBox(-1.6F, 3.4F, -2.6F, 4.2F, 1.5F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_glove_l = p_left_arm.addOrReplaceChild("glove_l", CubeListBuilder.create().texOffs(0, 51).addBox(-1.22F, 7.3F, -2.22F, 3.44F, 2.9F, 4.44F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_short_leg_l = p_left_leg.addOrReplaceChild("short_leg_l", CubeListBuilder.create().texOffs(20, 41).addBox(-2.3F, -0.2F, -2.3F, 4.6F, 3.8F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cargo_l = p_left_leg.addOrReplaceChild("cargo_l", CubeListBuilder.create().texOffs(48, 60).addBox(-0.1F, 0F, -1.3F, 0.9F, 2.4F, 2.6F), PartPose.offsetAndRotation(2.25F, 0.9F, 0F, 0F, 0F, -0.05F));
        PartDefinition p_boot_l = p_left_leg.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(0, 29).addBox(-2.3F, 6.2F, -2.3F, 4.6F, 5.95F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_toe_l = p_left_leg.addOrReplaceChild("toe_l", CubeListBuilder.create().texOffs(34, 73).addBox(-2.3F, 10.1F, -3.05F, 4.6F, 2.05F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeOutfit(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("top", m.get("body").getChild("top"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("jacket", m.get("body").getChild("jacket"));
        m.put("collar_back", m.get("body").getChild("collar_back"));
        m.put("collar_r", m.get("body").getChild("collar_r"));
        m.put("belt", m.get("body").getChild("belt"));
        m.put("pouch_r", m.get("body").getChild("pouch_r"));
        m.put("capsule_r", m.get("body").getChild("capsule_r"));
        m.put("wrench", m.get("body").getChild("wrench"));
        m.put("capsule_case", m.get("body").getChild("capsule_case"));
        m.put("shorts", m.get("body").getChild("shorts"));
        m.put("sleeve_r", m.get("right_arm").getChild("sleeve_r"));
        m.put("cuff_r", m.get("right_arm").getChild("cuff_r"));
        m.put("glove_r", m.get("right_arm").getChild("glove_r"));
        m.put("short_leg_r", m.get("right_leg").getChild("short_leg_r"));
        m.put("cargo_r", m.get("right_leg").getChild("cargo_r"));
        m.put("boot_r", m.get("right_leg").getChild("boot_r"));
        m.put("toe_r", m.get("right_leg").getChild("toe_r"));
        m.put("collar_l", m.get("body").getChild("collar_l"));
        m.put("pouch_l", m.get("body").getChild("pouch_l"));
        m.put("capsule_l", m.get("body").getChild("capsule_l"));
        m.put("sleeve_l", m.get("left_arm").getChild("sleeve_l"));
        m.put("cuff_l", m.get("left_arm").getChild("cuff_l"));
        m.put("glove_l", m.get("left_arm").getChild("glove_l"));
        m.put("short_leg_l", m.get("left_leg").getChild("short_leg_l"));
        m.put("cargo_l", m.get("left_leg").getChild("cargo_l"));
        m.put("boot_l", m.get("left_leg").getChild("boot_l"));
        m.put("toe_l", m.get("left_leg").getChild("toe_l"));
        return m;
    }
}
