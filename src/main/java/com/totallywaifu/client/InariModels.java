// GENERADO por tools/gen_inari.py — no editar a mano.
package com.totallywaifu.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** Modelos de Inari: la chica zorro diosa y el zorro dios gigante. */
public final class InariModels {
    private InariModels() {}

    public static LayerDefinition foxFace() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("face", CubeListBuilder.create().texOffs(0, 0).addBox(-4.2F, -4.0F, -5.02F, 8F, 7F, 0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 16);
    }

    public static LayerDefinition girl() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-3.9F, 1.6F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(3.9F, 1.6F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.75F, 9F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.75F, 9F, 0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(22, 152).addBox(-1.1F, -1.3F, -1F, 2.2F, 1.7F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(44, 68).addBox(-3.135F, 0F, -1.7F, 6.27F, 4.4F, 3.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(22, 146).addBox(-2.755F, 0F, -0.615F, 5.51F, 2.4125F, 0.715F), PartPose.offsetAndRotation(0F, 1.55F, -1.65F, -0.306F, 0F, 0F));
        PartDefinition p_waist = p_body.addOrReplaceChild("waist", CubeListBuilder.create().texOffs(38, 104).addBox(-2.592F, 4.2F, -1.5F, 5.184F, 2.4F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hips = p_body.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(11, 87).addBox(-3.456F, 6.4F, -1.85F, 6.912F, 3.2F, 3.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_shoulder_r = p_right_arm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(14, 126).addBox(-1.5F, -1.6F, -1.4F, 2.8F, 2.4F, 2.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_upper_r = p_right_arm.addOrReplaceChild("upper_r", CubeListBuilder.create().texOffs(0, 104).addBox(-1.2F, 0.6F, -1.1F, 2.2F, 4.6F, 2.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_fore_r = p_right_arm.addOrReplaceChild("fore_r", CubeListBuilder.create().texOffs(20, 104).addBox(-1.1F, 5F, -1F, 2F, 4.4F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hand_r = p_right_arm.addOrReplaceChild("hand_r", CubeListBuilder.create().texOffs(0, 140).addBox(-1.15F, 9.2F, -1.1F, 2.1F, 2F, 2.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_thigh_r = p_right_leg.addOrReplaceChild("thigh_r", CubeListBuilder.create().texOffs(39, 31).addBox(-1.8F, -0.4F, -1.85F, 3.6F, 7.4F, 3.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_calf_r = p_right_leg.addOrReplaceChild("calf_r", CubeListBuilder.create().texOffs(17, 57).addBox(-1.5F, 6.8F, -1.5F, 3F, 6.6F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_r = p_right_leg.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(0, 119).addBox(-1.8F, 11.8F, -2F, 3.6F, 1.4F, 3.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_foot_r = p_right_leg.addOrReplaceChild("foot_r", CubeListBuilder.create().texOffs(0, 112).addBox(-1.8F, 12.6F, -3F, 3.6F, 1F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_sole_r = p_right_leg.addOrReplaceChild("sole_r", CubeListBuilder.create().texOffs(34, 87).addBox(-1.95F, 13.6F, -3.2F, 3.9F, 1.4F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_skirt_front = p_body.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(20, 78).addBox(-3.552F, 0F, -0.4F, 7.104F, 6.8F, 0.8F).texOffs(45, 161).addBox(-3.452F, 6.5F, -0.3F, 6.904F, 0.7F, 0.6F), PartPose.offsetAndRotation(0F, 6.6F, -1.95F, -0.26F, 0F, 0F));
        PartDefinition p_skirt_back = p_body.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(37, 78).addBox(-3.552F, 0F, -0.4F, 7.104F, 6.8F, 0.8F).texOffs(0, 165).addBox(-3.452F, 6.5F, -0.3F, 6.904F, 0.7F, 0.6F), PartPose.offsetAndRotation(0F, 6.6F, 1.95F, -0.26F, 3.1416F, 0F));
        PartDefinition p_skirt_right = p_body.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(54, 78).addBox(-2F, 0F, -0.4F, 4F, 6.8F, 0.8F).texOffs(17, 165).addBox(-1.9F, 6.5F, -0.3F, 3.8F, 0.7F, 0.6F), PartPose.offsetAndRotation(-3.504F, 6.6F, 0F, -0.26F, 1.5708F, 0F));
        PartDefinition p_skirt_left = p_body.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(0, 87).addBox(-2F, 0F, -0.4F, 4F, 6.8F, 0.8F).texOffs(27, 165).addBox(-1.9F, 6.5F, -0.3F, 3.8F, 0.7F, 0.6F), PartPose.offsetAndRotation(3.504F, 6.6F, 0F, -0.26F, -1.5708F, 0F));
        PartDefinition p_hair_top = p_head.addOrReplaceChild("hair_top", CubeListBuilder.create().texOffs(0, 31).addBox(-4.55F, -8.95F, -4.65F, 9.1F, 1.6F, 9.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hair_back = p_head.addOrReplaceChild("hair_back", CubeListBuilder.create().texOffs(23, 0).addBox(-4.65F, -8F, -0.2F, 9.3F, 7.8F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hair_side_r = p_head.addOrReplaceChild("hair_side_r", CubeListBuilder.create().texOffs(0, 17).addBox(-4.9F, -8F, -4F, 1.45F, 4.6F, 7.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hair_lock_r = p_head.addOrReplaceChild("hair_lock_r", CubeListBuilder.create().texOffs(40, 17).addBox(-4.75F, -6.4F, -4.55F, 1.35F, 11F, 1.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bang0 = p_head.addOrReplaceChild("bang0", CubeListBuilder.create().texOffs(20, 140).addBox(-4.45F, -8.6F, -4.75F, 1.12F, 3.4F, 0.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bang1 = p_head.addOrReplaceChild("bang1", CubeListBuilder.create().texOffs(26, 140).addBox(-3.3375F, -8.6F, -4.75F, 1.12F, 3.4F, 0.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bang2 = p_head.addOrReplaceChild("bang2", CubeListBuilder.create().texOffs(32, 140).addBox(-2.225F, -8.6F, -4.75F, 1.12F, 3.4F, 0.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bang3 = p_head.addOrReplaceChild("bang3", CubeListBuilder.create().texOffs(38, 140).addBox(-1.1125F, -8.6F, -4.75F, 1.12F, 3.4F, 0.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bang4 = p_head.addOrReplaceChild("bang4", CubeListBuilder.create().texOffs(44, 140).addBox(0F, -8.6F, -4.75F, 1.12F, 3.4F, 0.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bang5 = p_head.addOrReplaceChild("bang5", CubeListBuilder.create().texOffs(50, 140).addBox(1.1125F, -8.6F, -4.75F, 1.12F, 3.4F, 0.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bang6 = p_head.addOrReplaceChild("bang6", CubeListBuilder.create().texOffs(56, 140).addBox(2.225F, -8.6F, -4.75F, 1.12F, 3.4F, 0.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bang7 = p_head.addOrReplaceChild("bang7", CubeListBuilder.create().texOffs(0, 146).addBox(3.3375F, -8.6F, -4.75F, 1.12F, 3.4F, 0.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_long_back = p_head.addOrReplaceChild("long_back", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, 0F, -1.4F, 9F, 13.5F, 2F), PartPose.offsetAndRotation(0F, -0.6F, 3.2F, 0.04F, 0F, 0F));
        PartDefinition p_lock0 = p_head.addOrReplaceChild("lock0", CubeListBuilder.create().texOffs(36, 112).addBox(-3.4F, 0F, -0.6F, 6.8F, 4F, 1.2F), PartPose.offsetAndRotation(0F, -1F, 3.6F, 0.15F, 0F, 0F));
        PartDefinition p_lock1 = p_lock0.addOrReplaceChild("lock1", CubeListBuilder.create().texOffs(16, 119).addBox(-3.1F, 0F, -0.6F, 6.2F, 4F, 1.2F), PartPose.offsetAndRotation(0F, 3.8F, 0F, 0.05F, 0F, 0F));
        PartDefinition p_lock2 = p_lock1.addOrReplaceChild("lock2", CubeListBuilder.create().texOffs(49, 133).addBox(-2.7F, 0F, -0.5F, 5.4F, 3.4F, 1F), PartPose.offsetAndRotation(0F, 3.8F, 0F, 0.05F, 0F, 0F));
        PartDefinition p_iear_r = p_head.addOrReplaceChild("iear_r", CubeListBuilder.create().texOffs(36, 146).addBox(-2.1F, -2F, -0.8F, 4.2F, 2.2F, 1.6F).texOffs(0, 152).addBox(-1.6F, -3.8F, -0.7F, 3.2F, 1.9F, 1.4F).texOffs(36, 157).addBox(-1F, -5.2F, -0.6F, 2F, 1.5F, 1.2F).texOffs(21, 161).addBox(-0.55F, -6.2F, -0.5F, 1.1F, 1.1F, 1F).texOffs(52, 157).addBox(-1.2F, -2.6F, -1F, 2.4F, 2.4F, 0.4F), PartPose.offsetAndRotation(-2.6F, -8F, -0.2F, -0.1F, 0.25F, -0.48F));
        PartDefinition p_furi_r = p_right_arm.addOrReplaceChild("furi_r", CubeListBuilder.create().texOffs(40, 44).addBox(-2.6F, 2.4F, -2F, 3.8F, 5.6F, 4F).texOffs(48, 119).addBox(-2.4F, 8F, -1.2F, 3.4F, 2.4F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_tail0 = p_body.addOrReplaceChild("tail0", CubeListBuilder.create().texOffs(27, 126).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F), PartPose.offsetAndRotation(0F, 9F, 2F, 0.95F, 0F, 0F));
        PartDefinition p_tail1 = p_tail0.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(24, 68).addBox(-2.3F, 0F, -2.3F, 4.6F, 3.6F, 4.6F).texOffs(13, 133).addBox(-2.8F, 0.9F, -1.7F, 0.6F, 1.98F, 3.4F).texOffs(22, 133).addBox(2.2F, 0.9F, -1.7F, 0.6F, 1.98F, 3.4F).texOffs(0, 157).addBox(-1.7F, 0.72F, 2.2F, 3.4F, 2.16F, 0.6F), PartPose.offsetAndRotation(0F, 2.7F, 0F, -0.25F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(16, 44).addBox(-2.8F, 0F, -2.8F, 5.6F, 3.6F, 5.6F).texOffs(20, 96).addBox(-3.3F, 0.9F, -2.2F, 0.6F, 1.98F, 4.4F).texOffs(31, 96).addBox(2.7F, 0.9F, -2.2F, 0.6F, 1.98F, 4.4F).texOffs(40, 152).addBox(-2.2F, 0.72F, 2.7F, 4.4F, 2.16F, 0.6F), PartPose.offsetAndRotation(0F, 3.3F, 0F, -0.3F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(0, 68).addBox(-2.8F, 0F, -2.8F, 5.6F, 3.4F, 5.6F).texOffs(42, 96).addBox(-3.3F, 0.85F, -2.2F, 0.6F, 1.87F, 4.4F).texOffs(53, 96).addBox(2.7F, 0.85F, -2.2F, 0.6F, 1.87F, 4.4F).texOffs(51, 152).addBox(-2.2F, 0.68F, 2.7F, 4.4F, 2.04F, 0.6F), PartPose.offsetAndRotation(0F, 3.3F, 0F, -0.3F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(0, 78).addBox(-2.3F, 0F, -2.3F, 4.6F, 3F, 4.6F).texOffs(31, 133).addBox(-2.8F, 0.75F, -1.7F, 0.6F, 1.65F, 3.4F).texOffs(40, 133).addBox(2.2F, 0.75F, -1.7F, 0.6F, 1.65F, 3.4F).texOffs(9, 157).addBox(-1.7F, 0.6F, 2.2F, 3.4F, 1.8F, 0.6F), PartPose.offsetAndRotation(0F, 3.1F, 0F, -0.25F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create().texOffs(40, 126).addBox(-1.5F, 0F, -1.5F, 3F, 2.6F, 3F), PartPose.offsetAndRotation(0F, 2.7F, 0F, -0.2F, 0F, 0F));
        PartDefinition p_kanzashi_r = p_head.addOrReplaceChild("kanzashi_r", CubeListBuilder.create().texOffs(32, 152).addBox(-0.3F, -0.3F, -0.3F, 0.6F, 3F, 0.6F).texOffs(18, 157).addBox(-0.9F, -1.2F, -0.9F, 1.8F, 1F, 1.8F).texOffs(39, 161).addBox(-0.25F, 2.6F, -0.25F, 0.5F, 2.2F, 0.5F), PartPose.offsetAndRotation(-3.6F, -7.4F, 1F, 0.2F, 0F, -0.5F));
        PartDefinition p_rope = p_body.addOrReplaceChild("rope", CubeListBuilder.create().texOffs(6, 146).addBox(-2F, -0.6F, -1.6F, 4F, 0.8F, 3.2F), PartPose.offsetAndRotation(0F, -0.4F, 0F, 0F, 0F, 0F));
        PartDefinition p_rope_bell = p_body.addOrReplaceChild("rope_bell", CubeListBuilder.create().texOffs(27, 161).addBox(-0.6F, 0F, -0.5F, 1.2F, 1.2F, 1F), PartPose.offsetAndRotation(0F, 0.4F, -1.9F, 0F, 0F, 0F));
        PartDefinition p_ribbon_knot = p_body.addOrReplaceChild("ribbon_knot", CubeListBuilder.create().texOffs(37, 165).addBox(-0.65F, -0.585F, -0.39F, 1.3F, 1.17F, 0.78F), PartPose.offsetAndRotation(0F, 5.2F, 1.9F, 0F, 3.1416F, 0F));
        PartDefinition p_ribbon_wl = p_ribbon_knot.addOrReplaceChild("ribbon_wl", CubeListBuilder.create().texOffs(0, 161).addBox(-2.21F, -0.78F, -0.26F, 2.21F, 1.56F, 0.52F), PartPose.offsetAndRotation(-0.52F, 0F, 0F, 0F, 0F, 0.25F));
        PartDefinition p_ribbon_wr = p_ribbon_knot.addOrReplaceChild("ribbon_wr", CubeListBuilder.create().texOffs(7, 161).addBox(0F, -0.78F, -0.26F, 2.21F, 1.56F, 0.52F), PartPose.offsetAndRotation(0.52F, 0F, 0F, 0F, 0F, -0.25F));
        PartDefinition p_shoulder_l = p_left_arm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(0, 133).addBox(-1.3F, -1.6F, -1.4F, 2.8F, 2.4F, 2.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_upper_l = p_left_arm.addOrReplaceChild("upper_l", CubeListBuilder.create().texOffs(10, 104).addBox(-1F, 0.6F, -1.1F, 2.2F, 4.6F, 2.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_fore_l = p_left_arm.addOrReplaceChild("fore_l", CubeListBuilder.create().texOffs(29, 104).addBox(-0.9F, 5F, -1F, 2F, 4.4F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hand_l = p_left_arm.addOrReplaceChild("hand_l", CubeListBuilder.create().texOffs(10, 140).addBox(-0.95F, 9.2F, -1.1F, 2.1F, 2F, 2.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_thigh_l = p_left_leg.addOrReplaceChild("thigh_l", CubeListBuilder.create().texOffs(0, 44).addBox(-1.8F, -0.4F, -1.85F, 3.6F, 7.4F, 3.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_calf_l = p_left_leg.addOrReplaceChild("calf_l", CubeListBuilder.create().texOffs(30, 57).addBox(-1.5F, 6.8F, -1.5F, 3F, 6.6F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_l = p_left_leg.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(32, 119).addBox(-1.8F, 11.8F, -2F, 3.6F, 1.4F, 3.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_foot_l = p_left_leg.addOrReplaceChild("foot_l", CubeListBuilder.create().texOffs(18, 112).addBox(-1.8F, 12.6F, -3F, 3.6F, 1F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_sole_l = p_left_leg.addOrReplaceChild("sole_l", CubeListBuilder.create().texOffs(0, 96).addBox(-1.95F, 13.6F, -3.2F, 3.9F, 1.4F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hair_side_l = p_head.addOrReplaceChild("hair_side_l", CubeListBuilder.create().texOffs(20, 17).addBox(3.45F, -8F, -4F, 1.45F, 4.6F, 7.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hair_lock_l = p_head.addOrReplaceChild("hair_lock_l", CubeListBuilder.create().texOffs(47, 17).addBox(3.4F, -6.4F, -4.55F, 1.35F, 11F, 1.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_iear_l = p_head.addOrReplaceChild("iear_l", CubeListBuilder.create().texOffs(49, 146).addBox(-2.1F, -2F, -0.8F, 4.2F, 2.2F, 1.6F).texOffs(11, 152).addBox(-1.6F, -3.8F, -0.7F, 3.2F, 1.9F, 1.4F).texOffs(44, 157).addBox(-1F, -5.2F, -0.6F, 2F, 1.5F, 1.2F).texOffs(33, 161).addBox(-0.55F, -6.2F, -0.5F, 1.1F, 1.1F, 1F).texOffs(14, 161).addBox(-1.2F, -2.6F, -1F, 2.4F, 2.4F, 0.4F), PartPose.offsetAndRotation(2.6F, -8F, -0.2F, -0.1F, -0.25F, 0.48F));
        PartDefinition p_furi_l = p_left_arm.addOrReplaceChild("furi_l", CubeListBuilder.create().texOffs(0, 57).addBox(-1.2F, 2.4F, -2F, 3.8F, 5.6F, 4F).texOffs(0, 126).addBox(-1F, 8F, -1.2F, 3.4F, 2.4F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_kanzashi_l = p_head.addOrReplaceChild("kanzashi_l", CubeListBuilder.create().texOffs(36, 152).addBox(-0.3F, -0.3F, -0.3F, 0.6F, 3F, 0.6F).texOffs(27, 157).addBox(-0.9F, -1.2F, -0.9F, 1.8F, 1F, 1.8F).texOffs(42, 161).addBox(-0.25F, 2.6F, -0.25F, 0.5F, 2.2F, 0.5F), PartPose.offsetAndRotation(3.6F, -7.4F, 1F, 0.2F, 0F, 0.5F));
        return LayerDefinition.create(mesh, 64, 256);
    }

    public static Map<String, ModelPart> bakeGirl(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("hat", root.getChild("hat"));
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("neck", m.get("body").getChild("neck"));
        m.put("chest", m.get("body").getChild("chest"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("waist", m.get("body").getChild("waist"));
        m.put("hips", m.get("body").getChild("hips"));
        m.put("shoulder_r", m.get("right_arm").getChild("shoulder_r"));
        m.put("upper_r", m.get("right_arm").getChild("upper_r"));
        m.put("fore_r", m.get("right_arm").getChild("fore_r"));
        m.put("hand_r", m.get("right_arm").getChild("hand_r"));
        m.put("thigh_r", m.get("right_leg").getChild("thigh_r"));
        m.put("calf_r", m.get("right_leg").getChild("calf_r"));
        m.put("boot_r", m.get("right_leg").getChild("boot_r"));
        m.put("foot_r", m.get("right_leg").getChild("foot_r"));
        m.put("sole_r", m.get("right_leg").getChild("sole_r"));
        m.put("skirt_front", m.get("body").getChild("skirt_front"));
        m.put("skirt_back", m.get("body").getChild("skirt_back"));
        m.put("skirt_right", m.get("body").getChild("skirt_right"));
        m.put("skirt_left", m.get("body").getChild("skirt_left"));
        m.put("hair_top", m.get("head").getChild("hair_top"));
        m.put("hair_back", m.get("head").getChild("hair_back"));
        m.put("hair_side_r", m.get("head").getChild("hair_side_r"));
        m.put("hair_lock_r", m.get("head").getChild("hair_lock_r"));
        m.put("bang0", m.get("head").getChild("bang0"));
        m.put("bang1", m.get("head").getChild("bang1"));
        m.put("bang2", m.get("head").getChild("bang2"));
        m.put("bang3", m.get("head").getChild("bang3"));
        m.put("bang4", m.get("head").getChild("bang4"));
        m.put("bang5", m.get("head").getChild("bang5"));
        m.put("bang6", m.get("head").getChild("bang6"));
        m.put("bang7", m.get("head").getChild("bang7"));
        m.put("long_back", m.get("head").getChild("long_back"));
        m.put("lock0", m.get("head").getChild("lock0"));
        m.put("lock1", m.get("lock0").getChild("lock1"));
        m.put("lock2", m.get("lock1").getChild("lock2"));
        m.put("iear_r", m.get("head").getChild("iear_r"));
        m.put("furi_r", m.get("right_arm").getChild("furi_r"));
        m.put("tail0", m.get("body").getChild("tail0"));
        m.put("tail1", m.get("tail0").getChild("tail1"));
        m.put("tail2", m.get("tail1").getChild("tail2"));
        m.put("tail3", m.get("tail2").getChild("tail3"));
        m.put("tail4", m.get("tail3").getChild("tail4"));
        m.put("tail5", m.get("tail4").getChild("tail5"));
        m.put("kanzashi_r", m.get("head").getChild("kanzashi_r"));
        m.put("rope", m.get("body").getChild("rope"));
        m.put("rope_bell", m.get("body").getChild("rope_bell"));
        m.put("ribbon_knot", m.get("body").getChild("ribbon_knot"));
        m.put("ribbon_wl", m.get("ribbon_knot").getChild("ribbon_wl"));
        m.put("ribbon_wr", m.get("ribbon_knot").getChild("ribbon_wr"));
        m.put("shoulder_l", m.get("left_arm").getChild("shoulder_l"));
        m.put("upper_l", m.get("left_arm").getChild("upper_l"));
        m.put("fore_l", m.get("left_arm").getChild("fore_l"));
        m.put("hand_l", m.get("left_arm").getChild("hand_l"));
        m.put("thigh_l", m.get("left_leg").getChild("thigh_l"));
        m.put("calf_l", m.get("left_leg").getChild("calf_l"));
        m.put("boot_l", m.get("left_leg").getChild("boot_l"));
        m.put("foot_l", m.get("left_leg").getChild("foot_l"));
        m.put("sole_l", m.get("left_leg").getChild("sole_l"));
        m.put("hair_side_l", m.get("head").getChild("hair_side_l"));
        m.put("hair_lock_l", m.get("head").getChild("hair_lock_l"));
        m.put("iear_l", m.get("head").getChild("iear_l"));
        m.put("furi_l", m.get("left_arm").getChild("furi_l"));
        m.put("kanzashi_l", m.get("head").getChild("kanzashi_l"));
        return m;
    }

    public static LayerDefinition fox() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_fox = root.addOrReplaceChild("fox", CubeListBuilder.create(), PartPose.offset(0F, 10F, 0F));
        PartDefinition p_torso = p_fox.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -4F, -9F, 8F, 7.6F, 18F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_ruff = p_torso.addOrReplaceChild("ruff", CubeListBuilder.create().texOffs(32, 27).addBox(-4.8F, -4.4F, -2.6F, 9.6F, 9.4F, 4.4F), PartPose.offsetAndRotation(0F, 0.4F, -8.6F, 0.15F, 0F, 0F));
        PartDefinition p_neck = p_torso.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(28, 70).addBox(-2.8F, -5.6F, -2.8F, 5.6F, 6F, 5.6F), PartPose.offsetAndRotation(0F, -1.6F, -8F, 0.85F, 0F, 0F));
        PartDefinition p_head = p_fox.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 27).addBox(-4.2F, -4F, -5F, 8.4F, 7F, 7F), PartPose.offsetAndRotation(0F, -6.4F, -12.6F, 0F, 0F, 0F));
        PartDefinition p_snout = p_head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(12, 105).addBox(-2F, -1.4F, -4.4F, 4F, 3F, 4.4F).texOffs(18, 130).addBox(-0.8F, -1.6F, -4.8F, 1.6F, 1F, 0.6F), PartPose.offsetAndRotation(0F, 1.2F, -5F, 0F, 0F, 0F));
        PartDefinition p_jaw = p_snout.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(48, 115).addBox(-1.7F, 0F, -3.8F, 3.4F, 1F, 3.8F), PartPose.offsetAndRotation(0F, 1.6F, -0.2F, 0F, 0F, 0F));
        PartDefinition p_cheek_r = p_head.addOrReplaceChild("cheek_r", CubeListBuilder.create().texOffs(24, 115).addBox(-1.6F, -1.6F, -1.6F, 1.8F, 3.4F, 3.4F), PartPose.offsetAndRotation(-4F, 1F, -2F, 0F, 0.3F, 0.2F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(0, 124).addBox(-2.2F, -2.6F, -1F, 4.4F, 2.8F, 2F).texOffs(28, 124).addBox(-1.6F, -4.8F, -0.8F, 3.2F, 2.4F, 1.6F).texOffs(50, 124).addBox(-1F, -6.4F, -0.6F, 2F, 1.8F, 1.2F).texOffs(8, 130).addBox(-0.5F, -7.4F, -0.5F, 1F, 1.2F, 1F), PartPose.offsetAndRotation(-2.6F, -3.8F, -0.6F, -0.05F, 0.2F, -0.3F));
        PartDefinition p_fleg_r = p_fox.addOrReplaceChild("fleg_r", CubeListBuilder.create().texOffs(52, 70).addBox(-1.5F, 0F, -1.5F, 3F, 7.2F, 3F).texOffs(0, 115).addBox(-1.3F, 7F, -1.6F, 2.6F, 5F, 2.8F), PartPose.offsetAndRotation(-2.6F, 2F, -6.4F, 0F, 0F, 0F));
        PartDefinition p_bleg_r = p_fox.addOrReplaceChild("bleg_r", CubeListBuilder.create().texOffs(32, 42).addBox(-2F, -1.4F, -2.6F, 4F, 7.6F, 5F).texOffs(44, 95).addBox(-1.3F, 6F, -0.8F, 2.6F, 6F, 2.6F), PartPose.offsetAndRotation(-2.8F, 2F, 6.4F, 0F, 0F, 0F));
        PartDefinition p_tail0 = p_torso.addOrReplaceChild("tail0", CubeListBuilder.create().texOffs(0, 95).addBox(-2.1F, 0F, -2.1F, 4.2F, 4.4F, 4.2F), PartPose.offsetAndRotation(0F, -2.4F, 8.6F, 1.15F, 0F, 0F));
        PartDefinition p_tail1 = p_tail0.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 70).addBox(-3.3F, 0F, -3.3F, 6.6F, 5F, 6.6F).texOffs(30, 105).addBox(-4F, 1F, -2.5F, 0.8F, 3F, 5F).texOffs(43, 105).addBox(3.2F, 1F, -2.5F, 0.8F, 3F, 5F), PartPose.offsetAndRotation(0F, 3.9F, 0F, -0.28F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 42).addBox(-3.8F, 0F, -3.8F, 7.6F, 5.4F, 7.6F).texOffs(13, 83).addBox(-4.5F, 1.08F, -3F, 0.8F, 3.24F, 6F).texOffs(28, 83).addBox(3.7F, 1.08F, -3F, 0.8F, 3.24F, 6F), PartPose.offsetAndRotation(0F, 4.5F, 0F, -0.28F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(19, 56).addBox(-3.4F, 0F, -3.4F, 6.8F, 5F, 6.8F).texOffs(18, 95).addBox(-4.1F, 1F, -2.6F, 0.8F, 3F, 5.2F).texOffs(31, 95).addBox(3.3F, 1F, -2.6F, 0.8F, 3F, 5.2F), PartPose.offsetAndRotation(0F, 4.9F, 0F, -0.28F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(43, 83).addBox(-2.2F, 0F, -2.2F, 4.4F, 3.8F, 4.4F), PartPose.offsetAndRotation(0F, 4.5F, 0F, -0.28F, 0F, 0F));
        PartDefinition p_cheek_l = p_head.addOrReplaceChild("cheek_l", CubeListBuilder.create().texOffs(36, 115).addBox(-0.2F, -1.6F, -1.6F, 1.8F, 3.4F, 3.4F), PartPose.offsetAndRotation(4F, 1F, -2F, 0F, -0.3F, -0.2F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(14, 124).addBox(-2.2F, -2.6F, -1F, 4.4F, 2.8F, 2F).texOffs(39, 124).addBox(-1.6F, -4.8F, -0.8F, 3.2F, 2.4F, 1.6F).texOffs(0, 130).addBox(-1F, -6.4F, -0.6F, 2F, 1.8F, 1.2F).texOffs(13, 130).addBox(-0.5F, -7.4F, -0.5F, 1F, 1.2F, 1F), PartPose.offsetAndRotation(2.6F, -3.8F, -0.6F, -0.05F, -0.2F, 0.3F));
        PartDefinition p_fleg_l = p_fox.addOrReplaceChild("fleg_l", CubeListBuilder.create().texOffs(0, 83).addBox(-1.5F, 0F, -1.5F, 3F, 7.2F, 3F).texOffs(12, 115).addBox(-1.3F, 7F, -1.6F, 2.6F, 5F, 2.8F), PartPose.offsetAndRotation(2.6F, 2F, -6.4F, 0F, 0F, 0F));
        PartDefinition p_bleg_l = p_fox.addOrReplaceChild("bleg_l", CubeListBuilder.create().texOffs(0, 56).addBox(-2F, -1.4F, -2.6F, 4F, 7.6F, 5F).texOffs(0, 105).addBox(-1.3F, 6F, -0.8F, 2.6F, 6F, 2.6F), PartPose.offsetAndRotation(2.8F, 2F, 6.4F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 256);
    }

    public static Map<String, ModelPart> bakeFox(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("fox", root.getChild("fox"));
        m.put("torso", m.get("fox").getChild("torso"));
        m.put("ruff", m.get("torso").getChild("ruff"));
        m.put("neck", m.get("torso").getChild("neck"));
        m.put("head", m.get("fox").getChild("head"));
        m.put("snout", m.get("head").getChild("snout"));
        m.put("jaw", m.get("snout").getChild("jaw"));
        m.put("cheek_r", m.get("head").getChild("cheek_r"));
        m.put("ear_r", m.get("head").getChild("ear_r"));
        m.put("fleg_r", m.get("fox").getChild("fleg_r"));
        m.put("bleg_r", m.get("fox").getChild("bleg_r"));
        m.put("tail0", m.get("torso").getChild("tail0"));
        m.put("tail1", m.get("tail0").getChild("tail1"));
        m.put("tail2", m.get("tail1").getChild("tail2"));
        m.put("tail3", m.get("tail2").getChild("tail3"));
        m.put("tail4", m.get("tail3").getChild("tail4"));
        m.put("cheek_l", m.get("head").getChild("cheek_l"));
        m.put("ear_l", m.get("head").getChild("ear_l"));
        m.put("fleg_l", m.get("fox").getChild("fleg_l"));
        m.put("bleg_l", m.get("fox").getChild("bleg_l"));
        return m;
    }
}
