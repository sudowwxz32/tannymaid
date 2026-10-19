// GENERADO por tools/gen_models.py — no editar a mano.
package com.laury.laurymod.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.HashMap;
import java.util.Map;

public final class LauryGeneratedModels {
    private LauryGeneratedModels() {}

    public static LayerDefinition createFeatures() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_hair_top = p_head.addOrReplaceChild("hair_top", CubeListBuilder.create().texOffs(0, 15).addBox(-4.6F, -8.7F, -4.6F, 9.2F, 2.5F, 9.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hair_back = p_head.addOrReplaceChild("hair_back", CubeListBuilder.create().texOffs(38, 15).addBox(-4.7F, 0F, -1.1F, 9.4F, 6.4F, 1.7F), PartPose.offsetAndRotation(0F, -6.6F, 4F, 0.12F, 0F, 0F));
        PartDefinition p_hair_side_r = p_head.addOrReplaceChild("hair_side_r", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8F, 0F, -4.2F, 1.35F, 5.4F, 8.3F), PartPose.offsetAndRotation(-4.15F, -7F, 0F, 0F, 0F, 0.05F));
        PartDefinition p_bang_0 = p_head.addOrReplaceChild("bang_0", CubeListBuilder.create().texOffs(8, 78).addBox(-0.95F, 0F, -0.5F, 1.9F, 3.4F, 0.8F), PartPose.offsetAndRotation(-3.3F, -8F, -4.35F, -0.14F, 0F, 0.1F));
        PartDefinition p_bang_1 = p_head.addOrReplaceChild("bang_1", CubeListBuilder.create().texOffs(35, 78).addBox(-0.95F, 0F, -0.5F, 1.9F, 2.5F, 0.8F), PartPose.offsetAndRotation(-1.7F, -8F, -4.35F, -0.14F, 0F, 0.04F));
        PartDefinition p_bang_2 = p_head.addOrReplaceChild("bang_2", CubeListBuilder.create().texOffs(42, 78).addBox(-0.95F, 0F, -0.5F, 1.9F, 3F, 0.8F), PartPose.offsetAndRotation(0F, -8F, -4.35F, -0.14F, 0F, -0.02F));
        PartDefinition p_bang_3 = p_head.addOrReplaceChild("bang_3", CubeListBuilder.create().texOffs(49, 78).addBox(-0.95F, 0F, -0.5F, 1.9F, 2.3F, 0.8F), PartPose.offsetAndRotation(1.7F, -8F, -4.35F, -0.14F, 0F, -0.05F));
        PartDefinition p_bang_4 = p_head.addOrReplaceChild("bang_4", CubeListBuilder.create().texOffs(15, 78).addBox(-0.95F, 0F, -0.5F, 1.9F, 3.3F, 0.8F), PartPose.offsetAndRotation(3.3F, -8F, -4.35F, -0.14F, 0F, -0.1F));
        PartDefinition p_spike_0 = p_head.addOrReplaceChild("spike_0", CubeListBuilder.create().texOffs(56, 78).addBox(-0.6F, -1.8F, -0.6F, 1.2F, 1.8F, 1.6F), PartPose.offsetAndRotation(-1.5F, -8.5F, -1.5F, -0.5F, 0.3F, -0.5F));
        PartDefinition p_spike_1 = p_head.addOrReplaceChild("spike_1", CubeListBuilder.create().texOffs(16, 93).addBox(-0.6F, -1.5F, -0.6F, 1.2F, 1.5F, 1.4F), PartPose.offsetAndRotation(1.8F, -8.5F, 1F, 0.4F, -0.2F, 0.55F));
        PartDefinition p_spike_2 = p_head.addOrReplaceChild("spike_2", CubeListBuilder.create().texOffs(23, 93).addBox(-0.7F, -1.4F, -0.6F, 1.4F, 1.4F, 1.2F), PartPose.offsetAndRotation(0.2F, -8.4F, 3.4F, 0.7F, 0F, 0.1F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(0, 93).addBox(-2.3F, -1F, -0.5F, 2.5F, 2F, 1F), PartPose.offsetAndRotation(-4.1F, -3.9F, 0.6F, 0F, 0.35F, 0.38F));
        PartDefinition p_ear_r_tip = p_ear_r.addOrReplaceChild("ear_r_tip", CubeListBuilder.create().texOffs(42, 93).addBox(-1.5F, -0.55F, -0.4F, 1.7F, 1.1F, 0.8F), PartPose.offsetAndRotation(-2.2F, -0.1F, 0F, 0F, 0F, 0.4F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(12, 71).addBox(-1.05F, -3F, -1.05F, 2.1F, 3.6F, 2.1F), PartPose.offsetAndRotation(-2.5F, -7.9F, -0.4F, -0.25F, 0F, -0.42F));
        PartDefinition p_horn_r2 = p_horn_r.addOrReplaceChild("horn_r2", CubeListBuilder.create().texOffs(50, 71).addBox(-0.8F, -2.8F, -0.8F, 1.6F, 3.2F, 1.6F), PartPose.offsetAndRotation(0F, -2.7F, 0F, -0.3F, 0F, 0.18F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create().texOffs(7, 84).addBox(-0.5F, -2.3F, -0.5F, 1F, 2.6F, 1F), PartPose.offsetAndRotation(0F, -2.6F, 0F, -0.4F, 0F, 0.3F));
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(32, 71).addBox(-3.6F, 0F, -1.1F, 7.2F, 3.2F, 1.2F), PartPose.offsetAndRotation(0F, 3F, -2F, -0.32F, 0F, 0F));
        PartDefinition p_hood = p_body.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(0, 64).addBox(-3.8F, -0.8F, 0F, 7.6F, 3.4F, 1.8F), PartPose.offsetAndRotation(0F, 0.3F, 2F, 0.28F, 0F, 0F));
        PartDefinition p_string_r = p_body.addOrReplaceChild("string_r", CubeListBuilder.create().texOffs(17, 84).addBox(-0.25F, 0F, -0.25F, 0.5F, 3.4F, 0.5F), PartPose.offsetAndRotation(-1.2F, 1.2F, -2.2F, 0F, 0F, 0F));
        PartDefinition p_hem = p_body.addOrReplaceChild("hem", CubeListBuilder.create().texOffs(16, 47).addBox(-4.45F, 0F, -2.4F, 8.9F, 1.8F, 4.8F), PartPose.offsetAndRotation(0F, 8.4F, 0F, 0F, 0F, 0F));
        PartDefinition p_hips = p_body.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(18, 28).addBox(-4.7F, 0F, -2.5F, 9.4F, 2.7F, 5F), PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(22, 89).addBox(-7F, -0.7F, -0.7F, 7F, 1.4F, 1.4F).texOffs(19, 56).addBox(-6.6F, 0.6F, -0.15F, 6.6F, 6F, 0.3F), PartPose.offsetAndRotation(-1.6F, 2.4F, 2.1F, 0F, 0.5F, 0.55F));
        PartDefinition p_wing_r2 = p_wing_r.addOrReplaceChild("wing_r2", CubeListBuilder.create().texOffs(23, 84).addBox(-9F, -0.6F, -0.6F, 9F, 1.2F, 1.2F).texOffs(45, 47).addBox(-8.6F, 0.5F, -0.15F, 8.6F, 6.5F, 0.3F), PartPose.offsetAndRotation(-7F, 0F, 0F, 0F, 0F, -1.55F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 28).addBox(-1.6F, -1.6F, 0F, 3.2F, 3.2F, 5F).texOffs(0, 84).addBox(-0.3F, -2.6F, 1.2F, 0.6F, 1F, 2.2F), PartPose.offsetAndRotation(0F, 10.6F, 1.6F, -0.95F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(42, 38).addBox(-1.35F, -1.35F, 0F, 2.7F, 2.7F, 5F).texOffs(30, 93).addBox(-0.25F, -2.25F, 1F, 0.5F, 0.9F, 2F), PartPose.offsetAndRotation(0F, 0F, 4.7F, 0.28F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(0, 47).addBox(-1.1F, -1.1F, 0F, 2.2F, 2.2F, 5F).texOffs(36, 93).addBox(-0.25F, -2F, 1F, 0.5F, 0.9F, 2F), PartPose.offsetAndRotation(0F, 0F, 4.7F, 0.26F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(49, 56).addBox(-0.85F, -0.85F, 0F, 1.7F, 1.7F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 4.7F, 0.22F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create().texOffs(0, 71).addBox(-0.6F, -0.6F, 0F, 1.2F, 1.2F, 4F), PartPose.offsetAndRotation(0F, 0F, 4.3F, 0.18F, 0F, 0F));
        PartDefinition p_tail_tip = p_tail5.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(22, 78).addBox(-1.5F, -0.3F, 0F, 3F, 0.6F, 2.6F), PartPose.offsetAndRotation(0F, 0F, 3.8F, 0F, 0F, 0F));
        PartDefinition p_cuff_r = p_right_arm.addOrReplaceChild("cuff_r", CubeListBuilder.create().texOffs(20, 64).addBox(-2.3F, 7.4F, -2.3F, 3.6F, 1.3F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cuff_l = p_left_arm.addOrReplaceChild("cuff_l", CubeListBuilder.create().texOffs(38, 64).addBox(-1.3F, 7.4F, -2.3F, 3.6F, 1.3F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_r = p_right_leg.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(0, 38).addBox(-2.3F, 9.5F, -2.7F, 4.6F, 2.7F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_l = p_left_leg.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(21, 38).addBox(-2.3F, 9.5F, -2.7F, 4.6F, 2.7F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hair_side_l = p_head.addOrReplaceChild("hair_side_l", CubeListBuilder.create().texOffs(21, 0).addBox(-0.55F, 0F, -4.2F, 1.35F, 5.4F, 8.3F), PartPose.offsetAndRotation(4.15F, -7F, 0F, 0F, 0F, -0.05F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(8, 93).addBox(-0.2F, -1F, -0.5F, 2.5F, 2F, 1F), PartPose.offsetAndRotation(4.1F, -3.9F, 0.6F, 0F, -0.35F, -0.38F));
        PartDefinition p_ear_l_tip = p_ear_l.addOrReplaceChild("ear_l_tip", CubeListBuilder.create().texOffs(48, 93).addBox(-0.2F, -0.55F, -0.4F, 1.7F, 1.1F, 0.8F), PartPose.offsetAndRotation(2.2F, -0.1F, 0F, 0F, 0F, -0.4F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(22, 71).addBox(-1.05F, -3F, -1.05F, 2.1F, 3.6F, 2.1F), PartPose.offsetAndRotation(2.5F, -7.9F, -0.4F, -0.25F, 0F, 0.42F));
        PartDefinition p_horn_l2 = p_horn_l.addOrReplaceChild("horn_l2", CubeListBuilder.create().texOffs(0, 78).addBox(-0.8F, -2.8F, -0.8F, 1.6F, 3.2F, 1.6F), PartPose.offsetAndRotation(0F, -2.7F, 0F, -0.3F, 0F, -0.18F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create().texOffs(12, 84).addBox(-0.5F, -2.3F, -0.5F, 1F, 2.6F, 1F), PartPose.offsetAndRotation(0F, -2.6F, 0F, -0.4F, 0F, -0.3F));
        PartDefinition p_string_l = p_body.addOrReplaceChild("string_l", CubeListBuilder.create().texOffs(20, 84).addBox(-0.25F, 0F, -0.25F, 0.5F, 3.4F, 0.5F), PartPose.offsetAndRotation(1.2F, 1.2F, -2.2F, 0F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(40, 89).addBox(0F, -0.7F, -0.7F, 7F, 1.4F, 1.4F).texOffs(34, 56).addBox(0F, 0.6F, -0.15F, 6.6F, 6F, 0.3F), PartPose.offsetAndRotation(1.6F, 2.4F, 2.1F, 0F, -0.5F, -0.55F));
        PartDefinition p_wing_l2 = p_wing_l.addOrReplaceChild("wing_l2", CubeListBuilder.create().texOffs(0, 89).addBox(0F, -0.6F, -0.6F, 9F, 1.2F, 1.2F).texOffs(0, 56).addBox(0F, 0.5F, -0.15F, 8.6F, 6.5F, 0.3F), PartPose.offsetAndRotation(7F, 0F, 0F, 0F, 0F, 1.55F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeFeatures(ModelPart root) {
        Map<String, ModelPart> m = new HashMap<>();
        m.put("root", root);
        m.put("head", root.getChild("head"));
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("hair_top", m.get("head").getChild("hair_top"));
        m.put("hair_back", m.get("head").getChild("hair_back"));
        m.put("hair_side_r", m.get("head").getChild("hair_side_r"));
        m.put("bang_0", m.get("head").getChild("bang_0"));
        m.put("bang_1", m.get("head").getChild("bang_1"));
        m.put("bang_2", m.get("head").getChild("bang_2"));
        m.put("bang_3", m.get("head").getChild("bang_3"));
        m.put("bang_4", m.get("head").getChild("bang_4"));
        m.put("spike_0", m.get("head").getChild("spike_0"));
        m.put("spike_1", m.get("head").getChild("spike_1"));
        m.put("spike_2", m.get("head").getChild("spike_2"));
        m.put("ear_r", m.get("head").getChild("ear_r"));
        m.put("ear_r_tip", m.get("ear_r").getChild("ear_r_tip"));
        m.put("horn_r", m.get("head").getChild("horn_r"));
        m.put("horn_r2", m.get("horn_r").getChild("horn_r2"));
        m.put("horn_r3", m.get("horn_r2").getChild("horn_r3"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("hood", m.get("body").getChild("hood"));
        m.put("string_r", m.get("body").getChild("string_r"));
        m.put("hem", m.get("body").getChild("hem"));
        m.put("hips", m.get("body").getChild("hips"));
        m.put("wing_r", m.get("body").getChild("wing_r"));
        m.put("wing_r2", m.get("wing_r").getChild("wing_r2"));
        m.put("tail1", m.get("body").getChild("tail1"));
        m.put("tail2", m.get("tail1").getChild("tail2"));
        m.put("tail3", m.get("tail2").getChild("tail3"));
        m.put("tail4", m.get("tail3").getChild("tail4"));
        m.put("tail5", m.get("tail4").getChild("tail5"));
        m.put("tail_tip", m.get("tail5").getChild("tail_tip"));
        m.put("cuff_r", m.get("right_arm").getChild("cuff_r"));
        m.put("cuff_l", m.get("left_arm").getChild("cuff_l"));
        m.put("boot_r", m.get("right_leg").getChild("boot_r"));
        m.put("boot_l", m.get("left_leg").getChild("boot_l"));
        m.put("hair_side_l", m.get("head").getChild("hair_side_l"));
        m.put("ear_l", m.get("head").getChild("ear_l"));
        m.put("ear_l_tip", m.get("ear_l").getChild("ear_l_tip"));
        m.put("horn_l", m.get("head").getChild("horn_l"));
        m.put("horn_l2", m.get("horn_l").getChild("horn_l2"));
        m.put("horn_l3", m.get("horn_l2").getChild("horn_l3"));
        m.put("string_l", m.get("body").getChild("string_l"));
        m.put("wing_l", m.get("body").getChild("wing_l"));
        m.put("wing_l2", m.get("wing_l").getChild("wing_l2"));
        return m;
    }

    public static LayerDefinition createDragon() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_dbody = root.addOrReplaceChild("dbody", CubeListBuilder.create().texOffs(0, 0).addBox(-6F, -5F, -9F, 12F, 10F, 18F), PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0F, 0F));
        PartDefinition p_dspike_0 = p_dbody.addOrReplaceChild("dspike_0", CubeListBuilder.create().texOffs(58, 98).addBox(-0.6F, -2.4F, -1F, 1.2F, 2.4F, 2.2F), PartPose.offsetAndRotation(0F, -5F, -6F, 0.3F, 0F, 0F));
        PartDefinition p_dspike_1 = p_dbody.addOrReplaceChild("dspike_1", CubeListBuilder.create().texOffs(66, 98).addBox(-0.6F, -2.4F, -1F, 1.2F, 2.4F, 2.2F), PartPose.offsetAndRotation(0F, -5F, -1.8F, 0.3F, 0F, 0F));
        PartDefinition p_dspike_2 = p_dbody.addOrReplaceChild("dspike_2", CubeListBuilder.create().texOffs(74, 98).addBox(-0.6F, -2.4F, -1F, 1.2F, 2.4F, 2.2F), PartPose.offsetAndRotation(0F, -5F, 2.4F, 0.3F, 0F, 0F));
        PartDefinition p_dspike_3 = p_dbody.addOrReplaceChild("dspike_3", CubeListBuilder.create().texOffs(82, 98).addBox(-0.6F, -2.4F, -1F, 1.2F, 2.4F, 2.2F), PartPose.offsetAndRotation(0F, -5F, 6.6F, 0.3F, 0F, 0F));
        PartDefinition p_neck1 = p_dbody.addOrReplaceChild("neck1", CubeListBuilder.create().texOffs(0, 46).addBox(-3F, -3F, -7F, 6F, 6F, 8F).texOffs(90, 98).addBox(-0.5F, -4.6F, -5F, 1F, 1.8F, 2.5F), PartPose.offsetAndRotation(0F, -1.5F, -8F, -0.75F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create().texOffs(58, 46).addBox(-2.6F, -2.6F, -7F, 5.2F, 5.2F, 8F).texOffs(98, 98).addBox(-0.5F, -4F, -5F, 1F, 1.6F, 2.5F), PartPose.offsetAndRotation(0F, 0F, -6.5F, -0.25F, 0F, 0F));
        PartDefinition p_dhead = p_neck2.addOrReplaceChild("dhead", CubeListBuilder.create().texOffs(92, 29).addBox(-4F, -4F, -6F, 8F, 7F, 7F).texOffs(28, 75).addBox(-2.6F, -1.5F, -12F, 5.2F, 4F, 6.5F), PartPose.offsetAndRotation(0F, 0F, -6.5F, 1F, 0F, 0F));
        PartDefinition p_djaw = p_dhead.addOrReplaceChild("djaw", CubeListBuilder.create().texOffs(0, 88).addBox(-2.4F, 0F, -6.8F, 4.8F, 1.6F, 7F), PartPose.offsetAndRotation(0F, 2.6F, -5.5F, 0.05F, 0F, 0F));
        PartDefinition p_dhorn_r = p_dhead.addOrReplaceChild("dhorn_r", CubeListBuilder.create().texOffs(82, 88).addBox(-0.9F, -0.9F, 0F, 1.8F, 1.8F, 6F), PartPose.offsetAndRotation(-2.6F, -3.4F, -1F, 0.55F, -0.3F, 0F));
        PartDefinition p_dhorn_r2 = p_dhorn_r.addOrReplaceChild("dhorn_r2", CubeListBuilder.create().texOffs(23, 98).addBox(-0.6F, -0.6F, 0F, 1.2F, 1.2F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 5.6F, 0.5F, 0F, 0F));
        PartDefinition p_dleg_fr = p_dbody.addOrReplaceChild("dleg_fr", CubeListBuilder.create().texOffs(0, 75).addBox(-1.6F, 0F, -1.6F, 3.2F, 8F, 3.2F), PartPose.offsetAndRotation(-4.6F, 3F, -6F, 0F, 0F, 0F));
        PartDefinition p_dfoot_fr = p_dleg_fr.addOrReplaceChild("dfoot_fr", CubeListBuilder.create().texOffs(44, 88).addBox(-2F, 0F, -3.6F, 4F, 3F, 5F), PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0F, 0F));
        PartDefinition p_dleg_br = p_dbody.addOrReplaceChild("dleg_br", CubeListBuilder.create().texOffs(86, 46).addBox(-2F, 0F, -2.6F, 4F, 8F, 5.2F), PartPose.offsetAndRotation(-5.2F, 2F, 6F, 0F, 0F, 0F));
        PartDefinition p_dfoot_br = p_dleg_br.addOrReplaceChild("dfoot_br", CubeListBuilder.create().texOffs(76, 75).addBox(-2.1F, 0F, -3.8F, 4.2F, 4F, 5.2F), PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0F, 0F));
        PartDefinition p_dwing_r = p_dbody.addOrReplaceChild("dwing_r", CubeListBuilder.create().texOffs(70, 105).addBox(-13F, -1F, -1F, 13F, 2F, 2F).texOffs(0, 61).addBox(-13F, -0.25F, 1F, 13F, 0.5F, 12F), PartPose.offsetAndRotation(-5.5F, -4.5F, -3F, 0F, 0F, 0.1F));
        PartDefinition p_dwing_r2 = p_dwing_r.addOrReplaceChild("dwing_r2", CubeListBuilder.create().texOffs(0, 105).addBox(-15F, -0.8F, -0.8F, 15F, 1.6F, 1.6F).texOffs(61, 0).addBox(-15F, -0.25F, 0.8F, 15F, 0.5F, 15F).texOffs(47, 110).addBox(-16.5F, -0.6F, -0.6F, 1.5F, 1.2F, 1.2F), PartPose.offsetAndRotation(-13F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_dtail1 = p_dbody.addOrReplaceChild("dtail1", CubeListBuilder.create().texOffs(61, 29).addBox(-3F, -3F, 0F, 6F, 6F, 9F).texOffs(49, 98).addBox(-0.5F, -4.6F, 3F, 1F, 1.8F, 3F), PartPose.offsetAndRotation(0F, -1.5F, 8.5F, -0.25F, 0F, 0F));
        PartDefinition p_dtail2 = p_dtail1.addOrReplaceChild("dtail2", CubeListBuilder.create().texOffs(29, 46).addBox(-2.4F, -2.4F, 0F, 4.8F, 4.8F, 9F).texOffs(31, 110).addBox(-0.5F, -3.9F, 3F, 1F, 1.5F, 2.5F), PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.05F, 0F, 0F));
        PartDefinition p_dtail3 = p_dtail2.addOrReplaceChild("dtail3", CubeListBuilder.create().texOffs(102, 61).addBox(-1.9F, -1.9F, 0F, 3.8F, 3.8F, 9F).texOffs(39, 110).addBox(-0.5F, -3.4F, 3F, 1F, 1.5F, 2.5F), PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.06F, 0F, 0F));
        PartDefinition p_dtail4 = p_dtail3.addOrReplaceChild("dtail4", CubeListBuilder.create().texOffs(53, 75).addBox(-1.4F, -1.4F, 0F, 2.8F, 2.8F, 8F), PartPose.offsetAndRotation(0F, 0F, 7.6F, 0.08F, 0F, 0F));
        PartDefinition p_dtail5 = p_dtail4.addOrReplaceChild("dtail5", CubeListBuilder.create().texOffs(25, 88).addBox(-0.9F, -0.9F, 0F, 1.8F, 1.8F, 7F), PartPose.offsetAndRotation(0F, 0F, 7.2F, 0.1F, 0F, 0F));
        PartDefinition p_dtail_tip = p_dtail5.addOrReplaceChild("dtail_tip", CubeListBuilder.create().texOffs(0, 98).addBox(-3F, -0.5F, 0F, 6F, 1F, 5F), PartPose.offsetAndRotation(0F, 0F, 6.6F, 0F, 0F, 0F));
        PartDefinition p_dhorn_l = p_dhead.addOrReplaceChild("dhorn_l", CubeListBuilder.create().texOffs(99, 88).addBox(-0.9F, -0.9F, 0F, 1.8F, 1.8F, 6F), PartPose.offsetAndRotation(2.6F, -3.4F, -1F, 0.55F, 0.3F, 0F));
        PartDefinition p_dhorn_l2 = p_dhorn_l.addOrReplaceChild("dhorn_l2", CubeListBuilder.create().texOffs(36, 98).addBox(-0.6F, -0.6F, 0F, 1.2F, 1.2F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 5.6F, 0.5F, 0F, 0F));
        PartDefinition p_dleg_fr = p_dbody.addOrReplaceChild("dleg_fr", CubeListBuilder.create().texOffs(14, 75).addBox(-1.6F, 0F, -1.6F, 3.2F, 8F, 3.2F), PartPose.offsetAndRotation(4.6F, 3F, -6F, 0F, 0F, 0F));
        PartDefinition p_dfoot_fr = p_dleg_fr.addOrReplaceChild("dfoot_fr", CubeListBuilder.create().texOffs(63, 88).addBox(-2F, 0F, -3.6F, 4F, 3F, 5F), PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0F, 0F));
        PartDefinition p_dleg_br = p_dbody.addOrReplaceChild("dleg_br", CubeListBuilder.create().texOffs(106, 46).addBox(-2F, 0F, -2.6F, 4F, 8F, 5.2F), PartPose.offsetAndRotation(5.2F, 2F, 6F, 0F, 0F, 0F));
        PartDefinition p_dfoot_br = p_dleg_br.addOrReplaceChild("dfoot_br", CubeListBuilder.create().texOffs(96, 75).addBox(-2.1F, 0F, -3.8F, 4.2F, 4F, 5.2F), PartPose.offsetAndRotation(0F, 8F, 0F, 0F, 0F, 0F));
        PartDefinition p_dwing_l = p_dbody.addOrReplaceChild("dwing_l", CubeListBuilder.create().texOffs(0, 110).addBox(0F, -1F, -1F, 13F, 2F, 2F).texOffs(51, 61).addBox(0F, -0.25F, 1F, 13F, 0.5F, 12F), PartPose.offsetAndRotation(5.5F, -4.5F, -3F, 0F, 0F, -0.1F));
        PartDefinition p_dwing_l2 = p_dwing_l.addOrReplaceChild("dwing_l2", CubeListBuilder.create().texOffs(35, 105).addBox(0F, -0.8F, -0.8F, 15F, 1.6F, 1.6F).texOffs(0, 29).addBox(0F, -0.25F, 0.8F, 15F, 0.5F, 15F).texOffs(54, 110).addBox(15F, -0.6F, -0.6F, 1.5F, 1.2F, 1.2F), PartPose.offsetAndRotation(13F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static Map<String, ModelPart> bakeDragon(ModelPart root) {
        Map<String, ModelPart> m = new HashMap<>();
        m.put("root", root);
        m.put("dbody", m.get("root").getChild("dbody"));
        m.put("dspike_0", m.get("dbody").getChild("dspike_0"));
        m.put("dspike_1", m.get("dbody").getChild("dspike_1"));
        m.put("dspike_2", m.get("dbody").getChild("dspike_2"));
        m.put("dspike_3", m.get("dbody").getChild("dspike_3"));
        m.put("neck1", m.get("dbody").getChild("neck1"));
        m.put("neck2", m.get("neck1").getChild("neck2"));
        m.put("dhead", m.get("neck2").getChild("dhead"));
        m.put("djaw", m.get("dhead").getChild("djaw"));
        m.put("dhorn_r", m.get("dhead").getChild("dhorn_r"));
        m.put("dhorn_r2", m.get("dhorn_r").getChild("dhorn_r2"));
        m.put("dleg_fr", m.get("dbody").getChild("dleg_fr"));
        m.put("dfoot_fr", m.get("dleg_fr").getChild("dfoot_fr"));
        m.put("dleg_br", m.get("dbody").getChild("dleg_br"));
        m.put("dfoot_br", m.get("dleg_br").getChild("dfoot_br"));
        m.put("dwing_r", m.get("dbody").getChild("dwing_r"));
        m.put("dwing_r2", m.get("dwing_r").getChild("dwing_r2"));
        m.put("dtail1", m.get("dbody").getChild("dtail1"));
        m.put("dtail2", m.get("dtail1").getChild("dtail2"));
        m.put("dtail3", m.get("dtail2").getChild("dtail3"));
        m.put("dtail4", m.get("dtail3").getChild("dtail4"));
        m.put("dtail5", m.get("dtail4").getChild("dtail5"));
        m.put("dtail_tip", m.get("dtail5").getChild("dtail_tip"));
        m.put("dhorn_l", m.get("dhead").getChild("dhorn_l"));
        m.put("dhorn_l2", m.get("dhorn_l").getChild("dhorn_l2"));
        m.put("dleg_fr", m.get("dbody").getChild("dleg_fr"));
        m.put("dfoot_fr", m.get("dleg_fr").getChild("dfoot_fr"));
        m.put("dleg_br", m.get("dbody").getChild("dleg_br"));
        m.put("dfoot_br", m.get("dleg_br").getChild("dfoot_br"));
        m.put("dwing_l", m.get("dbody").getChild("dwing_l"));
        m.put("dwing_l2", m.get("dwing_l").getChild("dwing_l2"));
        return m;
    }
}
