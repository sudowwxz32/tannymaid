// GENERADO por tools/gen_blocky.py — no editar a mano.
package com.totallywaifu.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** Capas de las waifus: ropa (c_ pecho, l_ piernas, f_ pies) y cabello/orejas/accesorios de la cabeza. */
public final class WaifuModels {
    private WaifuModels() {}

    public static LayerDefinition clothesKozue() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_c_shirt = p_body.addOrReplaceChild("c_shirt", CubeListBuilder.create().texOffs(0, 0).addBox(-4.25F, -0.25F, -2.25F, 8.5F, 7.2F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_bust = p_body.addOrReplaceChild("c_bust", CubeListBuilder.create().texOffs(37, 60).addBox(-3.3F, 0F, -0.9F, 6.6F, 2.9F, 1F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_c_bib = p_body.addOrReplaceChild("c_bib", CubeListBuilder.create().texOffs(18, 60).addBox(-3.1F, 3.3F, -2.55F, 6.2F, 4.6F, 0.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_strap_r = p_body.addOrReplaceChild("c_strap_r", CubeListBuilder.create().texOffs(20, 13).addBox(-0.6F, -0.35F, -2.55F, 1.2F, 4F, 5.1F), PartPose.offsetAndRotation(-2.4F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_button_r = p_body.addOrReplaceChild("c_button_r", CubeListBuilder.create().texOffs(18, 75).addBox(-0.45F, -0.45F, -0.2F, 0.9F, 0.9F, 0.35F), PartPose.offsetAndRotation(-2.4F, 3.6F, -2.85F, 0F, 0F, 0F));
        PartDefinition p_c_puff_r = p_right_arm.addOrReplaceChild("c_puff_r", CubeListBuilder.create().texOffs(29, 25).addBox(-2.5F, -2.55F, -2.5F, 4F, 3.8F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_glove_r = p_right_arm.addOrReplaceChild("c_glove_r", CubeListBuilder.create().texOffs(0, 45).addBox(-2.2F, 7.8F, -2.2F, 3.4F, 2.4F, 4.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_hips = p_body.addOrReplaceChild("l_hips", CubeListBuilder.create().texOffs(0, 25).addBox(-4.5F, 7.4F, -2.45F, 9F, 3.4F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_toolbelt = p_body.addOrReplaceChild("l_toolbelt", CubeListBuilder.create().texOffs(19, 35).addBox(-4.65F, 7.5F, -2.6F, 9.3F, 1.3F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_hammer = p_body.addOrReplaceChild("l_hammer", CubeListBuilder.create().texOffs(33, 60).addBox(-0.3F, 0F, -0.3F, 0.6F, 3.8F, 0.6F).texOffs(54, 60).addBox(-0.7F, 3.6F, -1.2F, 1.4F, 1.1F, 2.4F), PartPose.offsetAndRotation(4.85F, 8.1F, -0.5F, 0F, 0F, -0.16F));
        PartDefinition p_l_pouch_r = p_body.addOrReplaceChild("l_pouch_r", CubeListBuilder.create().texOffs(0, 67).addBox(-1.1F, 0F, -1F, 2.2F, 2.4F, 1.2F), PartPose.offsetAndRotation(-3.4F, 8.2F, -2.65F, 0F, 0.22F, 0F));
        PartDefinition p_f_boot_r = p_right_leg.addOrReplaceChild("f_boot_r", CubeListBuilder.create().texOffs(27, 0).addBox(-2.3F, 6.6F, -2.3F, 4.6F, 5.65F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_toe_r = p_right_leg.addOrReplaceChild("f_toe_r", CubeListBuilder.create().texOffs(16, 67).addBox(-2.3F, 10.25F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_skirt_front = p_body.addOrReplaceChild("l_skirt_front", CubeListBuilder.create().texOffs(34, 45).addBox(-4.35F, 0F, -0.45F, 8.7F, 4.6F, 0.9F).texOffs(40, 67).addBox(-4.25F, 4.25F, -0.3F, 8.5F, 0.85F, 0.7F), PartPose.offsetAndRotation(0F, 8.2F, -2.25F, -0.34F, 0F, 0F));
        PartDefinition p_l_skirt_back = p_body.addOrReplaceChild("l_skirt_back", CubeListBuilder.create().texOffs(0, 53).addBox(-4.35F, 0F, -0.45F, 8.7F, 4.6F, 0.9F).texOffs(0, 72).addBox(-4.25F, 4.25F, -0.3F, 8.5F, 0.85F, 0.7F), PartPose.offsetAndRotation(0F, 8.2F, 2.25F, -0.34F, 3.1416F, 0F));
        PartDefinition p_l_skirt_right = p_body.addOrReplaceChild("l_skirt_right", CubeListBuilder.create().texOffs(21, 53).addBox(-2.45F, 0F, -0.45F, 4.9F, 4.6F, 0.9F).texOffs(20, 72).addBox(-2.35F, 4.25F, -0.3F, 4.7F, 0.85F, 0.7F), PartPose.offsetAndRotation(-4.35F, 8.2F, 0F, -0.34F, 1.5708F, 0F));
        PartDefinition p_l_skirt_left = p_body.addOrReplaceChild("l_skirt_left", CubeListBuilder.create().texOffs(34, 53).addBox(-2.45F, 0F, -0.45F, 4.9F, 4.6F, 0.9F).texOffs(32, 72).addBox(-2.35F, 4.25F, -0.3F, 4.7F, 0.85F, 0.7F), PartPose.offsetAndRotation(4.35F, 8.2F, 0F, -0.34F, -1.5708F, 0F));
        PartDefinition p_l_skirt_fr = p_body.addOrReplaceChild("l_skirt_fr", CubeListBuilder.create().texOffs(47, 53).addBox(-1.55F, 0F, -0.45F, 3.1F, 4.6F, 0.9F).texOffs(44, 72).addBox(-1.45F, 4.25F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.2F, -1.85F, -0.34F, 0.7854F, 0F));
        PartDefinition p_l_skirt_fl = p_body.addOrReplaceChild("l_skirt_fl", CubeListBuilder.create().texOffs(56, 53).addBox(-1.55F, 0F, -0.45F, 3.1F, 4.6F, 0.9F).texOffs(53, 72).addBox(-1.45F, 4.25F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.2F, -1.85F, -0.34F, -0.7854F, 0F));
        PartDefinition p_l_skirt_br = p_body.addOrReplaceChild("l_skirt_br", CubeListBuilder.create().texOffs(0, 60).addBox(-1.55F, 0F, -0.45F, 3.1F, 4.6F, 0.9F).texOffs(0, 75).addBox(-1.45F, 4.25F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.2F, 1.85F, -0.34F, 2.3562F, 0F));
        PartDefinition p_l_skirt_bl = p_body.addOrReplaceChild("l_skirt_bl", CubeListBuilder.create().texOffs(9, 60).addBox(-1.55F, 0F, -0.45F, 3.1F, 4.6F, 0.9F).texOffs(9, 75).addBox(-1.45F, 4.25F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.2F, 1.85F, -0.34F, -2.3562F, 0F));
        PartDefinition p_c_strap_l = p_body.addOrReplaceChild("c_strap_l", CubeListBuilder.create().texOffs(34, 13).addBox(-0.6F, -0.35F, -2.55F, 1.2F, 4F, 5.1F), PartPose.offsetAndRotation(2.4F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_button_l = p_body.addOrReplaceChild("c_button_l", CubeListBuilder.create().texOffs(22, 75).addBox(-0.45F, -0.45F, -0.2F, 0.9F, 0.9F, 0.35F), PartPose.offsetAndRotation(2.4F, 3.6F, -2.85F, 0F, 0F, 0F));
        PartDefinition p_c_puff_l = p_left_arm.addOrReplaceChild("c_puff_l", CubeListBuilder.create().texOffs(0, 35).addBox(-1.5F, -2.55F, -2.5F, 4F, 3.8F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_glove_l = p_left_arm.addOrReplaceChild("c_glove_l", CubeListBuilder.create().texOffs(17, 45).addBox(-1.2F, 7.8F, -2.2F, 3.4F, 2.4F, 4.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_pouch_l = p_body.addOrReplaceChild("l_pouch_l", CubeListBuilder.create().texOffs(8, 67).addBox(-1.1F, 0F, -1F, 2.2F, 2.4F, 1.2F), PartPose.offsetAndRotation(3.4F, 8.2F, -2.65F, 0F, -0.22F, 0F));
        PartDefinition p_f_boot_l = p_left_leg.addOrReplaceChild("f_boot_l", CubeListBuilder.create().texOffs(0, 13).addBox(-2.3F, 6.6F, -2.3F, 4.6F, 5.65F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_toe_l = p_left_leg.addOrReplaceChild("f_toe_l", CubeListBuilder.create().texOffs(28, 67).addBox(-2.3F, 10.25F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeClothesKozue(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("c_shirt", m.get("body").getChild("c_shirt"));
        m.put("c_bust", m.get("body").getChild("c_bust"));
        m.put("c_bib", m.get("body").getChild("c_bib"));
        m.put("c_strap_r", m.get("body").getChild("c_strap_r"));
        m.put("c_button_r", m.get("body").getChild("c_button_r"));
        m.put("c_puff_r", m.get("right_arm").getChild("c_puff_r"));
        m.put("c_glove_r", m.get("right_arm").getChild("c_glove_r"));
        m.put("l_hips", m.get("body").getChild("l_hips"));
        m.put("l_toolbelt", m.get("body").getChild("l_toolbelt"));
        m.put("l_hammer", m.get("body").getChild("l_hammer"));
        m.put("l_pouch_r", m.get("body").getChild("l_pouch_r"));
        m.put("f_boot_r", m.get("right_leg").getChild("f_boot_r"));
        m.put("f_toe_r", m.get("right_leg").getChild("f_toe_r"));
        m.put("l_skirt_front", m.get("body").getChild("l_skirt_front"));
        m.put("l_skirt_back", m.get("body").getChild("l_skirt_back"));
        m.put("l_skirt_right", m.get("body").getChild("l_skirt_right"));
        m.put("l_skirt_left", m.get("body").getChild("l_skirt_left"));
        m.put("l_skirt_fr", m.get("body").getChild("l_skirt_fr"));
        m.put("l_skirt_fl", m.get("body").getChild("l_skirt_fl"));
        m.put("l_skirt_br", m.get("body").getChild("l_skirt_br"));
        m.put("l_skirt_bl", m.get("body").getChild("l_skirt_bl"));
        m.put("c_strap_l", m.get("body").getChild("c_strap_l"));
        m.put("c_button_l", m.get("body").getChild("c_button_l"));
        m.put("c_puff_l", m.get("left_arm").getChild("c_puff_l"));
        m.put("c_glove_l", m.get("left_arm").getChild("c_glove_l"));
        m.put("l_pouch_l", m.get("body").getChild("l_pouch_l"));
        m.put("f_boot_l", m.get("left_leg").getChild("f_boot_l"));
        m.put("f_toe_l", m.get("left_leg").getChild("f_toe_l"));
        return m;
    }

    public static LayerDefinition hairKozue() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_h_top = p_head.addOrReplaceChild("h_top", CubeListBuilder.create().texOffs(0, 14).addBox(-4.55F, -8.6F, -4.55F, 9.1F, 2.7F, 9.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_h_back = p_head.addOrReplaceChild("h_back", CubeListBuilder.create().texOffs(0, 39).addBox(-4.75F, 0F, 0F, 9.5F, 6.8F, 3.3F), PartPose.offsetAndRotation(0F, -7.2F, 1.5F, 0.06F, 0F, 0F));
        PartDefinition p_h_side_r = p_head.addOrReplaceChild("h_side_r", CubeListBuilder.create().texOffs(0, 0).addBox(-1F, 0F, -3.2F, 1.5F, 5.9F, 6.6F), PartPose.offsetAndRotation(-4.35F, -6.8F, 0F, 0F, 0F, 0.07F));
        PartDefinition p_h_frame_r = p_head.addOrReplaceChild("h_frame_r", CubeListBuilder.create().texOffs(0, 51).addBox(-1.05F, 0F, -0.65F, 1.7F, 6.9F, 1.2F), PartPose.offsetAndRotation(-3.7F, -7.6F, -3.9F, -0.05F, 0F, 0.06F));
        PartDefinition p_h_flare_r = p_head.addOrReplaceChild("h_flare_r", CubeListBuilder.create().texOffs(27, 39).addBox(-1.1F, 0F, -3.3F, 1.3F, 1.5F, 6.6F), PartPose.offsetAndRotation(-4.75F, -1.6F, 0.2F, 0F, 0F, 0.4F));
        PartDefinition p_h_flare_back = p_head.addOrReplaceChild("h_flare_back", CubeListBuilder.create().texOffs(0, 67).addBox(-4.6F, 0F, -1.2F, 9.2F, 1.4F, 1.3F), PartPose.offsetAndRotation(0F, -1F, 4.5F, -0.4F, 0F, 0F));
        PartDefinition p_h_tuft_a = p_head.addOrReplaceChild("h_tuft_a", CubeListBuilder.create().texOffs(28, 61).addBox(-0.6F, -1.7F, -0.6F, 1.2F, 1.9F, 1.2F), PartPose.offsetAndRotation(1F, -8.5F, -1.2F, 0.35F, 0.3F, 0.5F));
        PartDefinition p_h_tuft_b = p_head.addOrReplaceChild("h_tuft_b", CubeListBuilder.create().texOffs(29, 67).addBox(-0.6F, -1.5F, -0.6F, 1.2F, 1.7F, 1.2F), PartPose.offsetAndRotation(-1.6F, -8.4F, 2F, -0.4F, -0.4F, -0.45F));
        PartDefinition p_h_crown = p_head.addOrReplaceChild("h_crown", CubeListBuilder.create().texOffs(14, 51).addBox(-3.6F, -0.5F, -3.6F, 7.2F, 0.6F, 7.2F), PartPose.offsetAndRotation(0F, -8.6F, 0F, 0F, 0F, 0F));
        PartDefinition p_h_bang_0 = p_head.addOrReplaceChild("h_bang_0", CubeListBuilder.create().texOffs(34, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.7F, 0.76F), PartPose.offsetAndRotation(-3.55F, -8.3F, -4.45F, -0.14F, 0F, 0.08F));
        PartDefinition p_h_bang_1 = p_head.addOrReplaceChild("h_bang_1", CubeListBuilder.create().texOffs(55, 51).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.3F, 0.76F), PartPose.offsetAndRotation(-2.4F, -8.3F, -4.45F, -0.14F, 0F, 0.05F));
        PartDefinition p_h_bang_2 = p_head.addOrReplaceChild("h_bang_2", CubeListBuilder.create().texOffs(40, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.9F, 0.76F), PartPose.offsetAndRotation(-1.2F, -8.3F, -4.45F, -0.14F, 0F, 0.02F));
        PartDefinition p_h_bang_3 = p_head.addOrReplaceChild("h_bang_3", CubeListBuilder.create().texOffs(0, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.6F, 0.76F), PartPose.offsetAndRotation(0F, -8.3F, -4.45F, -0.14F, 0F, 0F));
        PartDefinition p_h_bang_4 = p_head.addOrReplaceChild("h_bang_4", CubeListBuilder.create().texOffs(46, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 3F, 0.76F), PartPose.offsetAndRotation(1.2F, -8.3F, -4.45F, -0.14F, 0F, -0.03F));
        PartDefinition p_h_bang_5 = p_head.addOrReplaceChild("h_bang_5", CubeListBuilder.create().texOffs(6, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.3F, 0.76F), PartPose.offsetAndRotation(2.4F, -8.3F, -4.45F, -0.14F, 0F, -0.05F));
        PartDefinition p_h_bang_6 = p_head.addOrReplaceChild("h_bang_6", CubeListBuilder.create().texOffs(52, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.6F, 0.76F), PartPose.offsetAndRotation(3.55F, -8.3F, -4.45F, -0.14F, 0F, -0.08F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(12, 61).addBox(-1.9F, -1.2F, -0.5F, 2.3F, 2.6F, 1F).texOffs(35, 67).addBox(-3.3F, -1.8F, -0.42F, 1.5F, 2F, 0.84F).texOffs(52, 67).addBox(-4.6F, -2.5F, -0.32F, 1.4F, 1.3F, 0.64F), PartPose.offsetAndRotation(-3.6F, -3.6F, 0.6F, 0.05F, 0.38F, -0.36F));
        PartDefinition p_bandana = p_head.addOrReplaceChild("bandana", CubeListBuilder.create().texOffs(0, 27).addBox(-4.7F, -1.2F, -4.7F, 9.4F, 1.4F, 9.4F), PartPose.offsetAndRotation(0F, -7F, 0F, 0F, 0F, 0F));
        PartDefinition p_knot = p_head.addOrReplaceChild("knot", CubeListBuilder.create().texOffs(22, 67).addBox(-0.9F, -0.9F, -0.5F, 1.8F, 1.8F, 1F).texOffs(58, 61).addBox(-1.6F, 0.4F, 0F, 1.1F, 2.6F, 0.45F).texOffs(47, 67).addBox(0.4F, 0.4F, 0F, 1.1F, 2.3F, 0.45F), PartPose.offsetAndRotation(2F, -6.8F, 4.8F, 0.3F, 0F, 0.3F));
        PartDefinition p_pencil = p_head.addOrReplaceChild("pencil", CubeListBuilder.create().texOffs(44, 51).addBox(-0.28F, -0.28F, -2.1F, 0.56F, 0.56F, 4.2F), PartPose.offsetAndRotation(-4.9F, -4.6F, -0.6F, 0.25F, 0F, 0.2F));
        PartDefinition p_h_side_l = p_head.addOrReplaceChild("h_side_l", CubeListBuilder.create().texOffs(18, 0).addBox(-0.5F, 0F, -3.2F, 1.5F, 5.9F, 6.6F), PartPose.offsetAndRotation(4.35F, -6.8F, 0F, 0F, 0F, -0.07F));
        PartDefinition p_h_frame_l = p_head.addOrReplaceChild("h_frame_l", CubeListBuilder.create().texOffs(7, 51).addBox(-0.65F, 0F, -0.65F, 1.7F, 6.9F, 1.2F), PartPose.offsetAndRotation(3.7F, -7.6F, -3.9F, -0.05F, 0F, -0.06F));
        PartDefinition p_h_flare_l = p_head.addOrReplaceChild("h_flare_l", CubeListBuilder.create().texOffs(44, 39).addBox(-0.2F, 0F, -3.3F, 1.3F, 1.5F, 6.6F), PartPose.offsetAndRotation(4.75F, -1.6F, 0.2F, 0F, 0F, -0.4F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(20, 61).addBox(-0.4F, -1.2F, -0.5F, 2.3F, 2.6F, 1F).texOffs(41, 67).addBox(1.8F, -1.8F, -0.42F, 1.5F, 2F, 0.84F).texOffs(58, 67).addBox(3.2F, -2.5F, -0.32F, 1.4F, 1.3F, 0.64F), PartPose.offsetAndRotation(3.6F, -3.6F, 0.6F, 0.05F, -0.38F, 0.36F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeHairKozue(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("h_top", m.get("head").getChild("h_top"));
        m.put("h_back", m.get("head").getChild("h_back"));
        m.put("h_side_r", m.get("head").getChild("h_side_r"));
        m.put("h_frame_r", m.get("head").getChild("h_frame_r"));
        m.put("h_flare_r", m.get("head").getChild("h_flare_r"));
        m.put("h_flare_back", m.get("head").getChild("h_flare_back"));
        m.put("h_tuft_a", m.get("head").getChild("h_tuft_a"));
        m.put("h_tuft_b", m.get("head").getChild("h_tuft_b"));
        m.put("h_crown", m.get("head").getChild("h_crown"));
        m.put("h_bang_0", m.get("head").getChild("h_bang_0"));
        m.put("h_bang_1", m.get("head").getChild("h_bang_1"));
        m.put("h_bang_2", m.get("head").getChild("h_bang_2"));
        m.put("h_bang_3", m.get("head").getChild("h_bang_3"));
        m.put("h_bang_4", m.get("head").getChild("h_bang_4"));
        m.put("h_bang_5", m.get("head").getChild("h_bang_5"));
        m.put("h_bang_6", m.get("head").getChild("h_bang_6"));
        m.put("ear_r", m.get("head").getChild("ear_r"));
        m.put("bandana", m.get("head").getChild("bandana"));
        m.put("knot", m.get("head").getChild("knot"));
        m.put("pencil", m.get("head").getChild("pencil"));
        m.put("h_side_l", m.get("head").getChild("h_side_l"));
        m.put("h_frame_l", m.get("head").getChild("h_frame_l"));
        m.put("h_flare_l", m.get("head").getChild("h_flare_l"));
        m.put("ear_l", m.get("head").getChild("ear_l"));
        return m;
    }

    public static LayerDefinition clothesHomura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_c_top = p_body.addOrReplaceChild("c_top", CubeListBuilder.create().texOffs(0, 24).addBox(-4.25F, 0.6F, -2.25F, 8.5F, 4.4F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_bust = p_body.addOrReplaceChild("c_bust", CubeListBuilder.create().texOffs(38, 50).addBox(-3.3F, 0F, -0.9F, 6.6F, 2.9F, 1F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_c_corset = p_body.addOrReplaceChild("c_corset", CubeListBuilder.create().texOffs(27, 24).addBox(-4.35F, 4.8F, -2.35F, 8.7F, 2.8F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_choker = p_body.addOrReplaceChild("c_choker", CubeListBuilder.create().texOffs(0, 58).addBox(-1.6F, -0.9F, -1.5F, 3.2F, 0.7F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_ember = p_body.addOrReplaceChild("c_ember", CubeListBuilder.create().texOffs(18, 66).addBox(-0.42F, -0.42F, -0.3F, 0.84F, 0.84F, 0.45F), PartPose.offsetAndRotation(0F, -0.5F, -1.65F, 0F, 0F, 0.78F));
        PartDefinition p_c_gauntlet_r = p_right_arm.addOrReplaceChild("c_gauntlet_r", CubeListBuilder.create().texOffs(40, 0).addBox(-2.3F, 5.4F, -2.3F, 3.6F, 4.8F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_hips = p_body.addOrReplaceChild("l_hips", CubeListBuilder.create().texOffs(18, 13).addBox(-4.5F, 7.4F, -2.45F, 9F, 3.4F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_belt = p_body.addOrReplaceChild("l_belt", CubeListBuilder.create().texOffs(0, 34).addBox(-4.6F, 7.4F, -2.55F, 9.2F, 1F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_tongs = p_body.addOrReplaceChild("l_tongs", CubeListBuilder.create().texOffs(32, 50).addBox(-0.24F, 0F, -0.5F, 0.48F, 4.2F, 0.38F).texOffs(35, 50).addBox(-0.24F, 0F, 0.15F, 0.48F, 4.2F, 0.38F), PartPose.offsetAndRotation(4.9F, 8F, 0.3F, 0F, 0F, -0.14F));
        PartDefinition p_f_boot_r = p_right_leg.addOrReplaceChild("f_boot_r", CubeListBuilder.create().texOffs(0, 0).addBox(-2.3F, 5.8F, -2.3F, 4.6F, 6.45F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_toe_r = p_right_leg.addOrReplaceChild("f_toe_r", CubeListBuilder.create().texOffs(14, 58).addBox(-2.3F, 10.25F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_skirt_front = p_body.addOrReplaceChild("l_skirt_front", CubeListBuilder.create().texOffs(30, 34).addBox(-4.35F, 0F, -0.45F, 8.7F, 5.6F, 0.9F).texOffs(38, 58).addBox(-4.25F, 5.25F, -0.3F, 8.5F, 0.85F, 0.7F), PartPose.offsetAndRotation(0F, 8.2F, -2.25F, -0.3F, 0F, 0F));
        PartDefinition p_l_skirt_back = p_body.addOrReplaceChild("l_skirt_back", CubeListBuilder.create().texOffs(0, 42).addBox(-4.35F, 0F, -0.45F, 8.7F, 5.6F, 0.9F).texOffs(0, 63).addBox(-4.25F, 5.25F, -0.3F, 8.5F, 0.85F, 0.7F), PartPose.offsetAndRotation(0F, 8.2F, 2.25F, -0.3F, 3.1416F, 0F));
        PartDefinition p_l_skirt_right = p_body.addOrReplaceChild("l_skirt_right", CubeListBuilder.create().texOffs(21, 42).addBox(-2.45F, 0F, -0.45F, 4.9F, 5.6F, 0.9F).texOffs(20, 63).addBox(-2.35F, 5.25F, -0.3F, 4.7F, 0.85F, 0.7F), PartPose.offsetAndRotation(-4.35F, 8.2F, 0F, -0.3F, 1.5708F, 0F));
        PartDefinition p_l_skirt_left = p_body.addOrReplaceChild("l_skirt_left", CubeListBuilder.create().texOffs(34, 42).addBox(-2.45F, 0F, -0.45F, 4.9F, 5.6F, 0.9F).texOffs(32, 63).addBox(-2.35F, 5.25F, -0.3F, 4.7F, 0.85F, 0.7F), PartPose.offsetAndRotation(4.35F, 8.2F, 0F, -0.3F, -1.5708F, 0F));
        PartDefinition p_l_skirt_fr = p_body.addOrReplaceChild("l_skirt_fr", CubeListBuilder.create().texOffs(47, 42).addBox(-1.55F, 0F, -0.45F, 3.1F, 5.6F, 0.9F).texOffs(44, 63).addBox(-1.45F, 5.25F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.2F, -1.85F, -0.3F, 0.7854F, 0F));
        PartDefinition p_l_skirt_fl = p_body.addOrReplaceChild("l_skirt_fl", CubeListBuilder.create().texOffs(56, 42).addBox(-1.55F, 0F, -0.45F, 3.1F, 5.6F, 0.9F).texOffs(53, 63).addBox(-1.45F, 5.25F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.2F, -1.85F, -0.3F, -0.7854F, 0F));
        PartDefinition p_l_skirt_br = p_body.addOrReplaceChild("l_skirt_br", CubeListBuilder.create().texOffs(0, 50).addBox(-1.55F, 0F, -0.45F, 3.1F, 5.6F, 0.9F).texOffs(0, 66).addBox(-1.45F, 5.25F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.2F, 1.85F, -0.3F, 2.3562F, 0F));
        PartDefinition p_l_skirt_bl = p_body.addOrReplaceChild("l_skirt_bl", CubeListBuilder.create().texOffs(9, 50).addBox(-1.55F, 0F, -0.45F, 3.1F, 5.6F, 0.9F).texOffs(9, 66).addBox(-1.45F, 5.25F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.2F, 1.85F, -0.3F, -2.3562F, 0F));
        PartDefinition p_l_apron = p_l_skirt_front.addOrReplaceChild("l_apron", CubeListBuilder.create().texOffs(18, 50).addBox(-3F, 0.1F, -0.35F, 6F, 5.4F, 0.35F), PartPose.offsetAndRotation(0F, 0F, -0.5F, 0F, 0F, 0F));
        PartDefinition p_c_gauntlet_l = p_left_arm.addOrReplaceChild("c_gauntlet_l", CubeListBuilder.create().texOffs(0, 13).addBox(-1.3F, 5.4F, -2.3F, 3.6F, 4.8F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_boot_l = p_left_leg.addOrReplaceChild("f_boot_l", CubeListBuilder.create().texOffs(20, 0).addBox(-2.3F, 5.8F, -2.3F, 4.6F, 6.45F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_toe_l = p_left_leg.addOrReplaceChild("f_toe_l", CubeListBuilder.create().texOffs(26, 58).addBox(-2.3F, 10.25F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeClothesHomura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("c_top", m.get("body").getChild("c_top"));
        m.put("c_bust", m.get("body").getChild("c_bust"));
        m.put("c_corset", m.get("body").getChild("c_corset"));
        m.put("c_choker", m.get("body").getChild("c_choker"));
        m.put("c_ember", m.get("body").getChild("c_ember"));
        m.put("c_gauntlet_r", m.get("right_arm").getChild("c_gauntlet_r"));
        m.put("l_hips", m.get("body").getChild("l_hips"));
        m.put("l_belt", m.get("body").getChild("l_belt"));
        m.put("l_tongs", m.get("body").getChild("l_tongs"));
        m.put("f_boot_r", m.get("right_leg").getChild("f_boot_r"));
        m.put("f_toe_r", m.get("right_leg").getChild("f_toe_r"));
        m.put("l_skirt_front", m.get("body").getChild("l_skirt_front"));
        m.put("l_skirt_back", m.get("body").getChild("l_skirt_back"));
        m.put("l_skirt_right", m.get("body").getChild("l_skirt_right"));
        m.put("l_skirt_left", m.get("body").getChild("l_skirt_left"));
        m.put("l_skirt_fr", m.get("body").getChild("l_skirt_fr"));
        m.put("l_skirt_fl", m.get("body").getChild("l_skirt_fl"));
        m.put("l_skirt_br", m.get("body").getChild("l_skirt_br"));
        m.put("l_skirt_bl", m.get("body").getChild("l_skirt_bl"));
        m.put("l_apron", m.get("l_skirt_front").getChild("l_apron"));
        m.put("c_gauntlet_l", m.get("left_arm").getChild("c_gauntlet_l"));
        m.put("f_boot_l", m.get("left_leg").getChild("f_boot_l"));
        m.put("f_toe_l", m.get("left_leg").getChild("f_toe_l"));
        return m;
    }

    public static LayerDefinition hairHomura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_h_top = p_head.addOrReplaceChild("h_top", CubeListBuilder.create().texOffs(0, 14).addBox(-4.55F, -8.6F, -4.55F, 9.1F, 2.7F, 9.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_h_back = p_head.addOrReplaceChild("h_back", CubeListBuilder.create().texOffs(38, 14).addBox(-4.75F, 0F, 0F, 9.5F, 6.8F, 3.3F), PartPose.offsetAndRotation(0F, -7.2F, 1.5F, 0.06F, 0F, 0F));
        PartDefinition p_h_side_r = p_head.addOrReplaceChild("h_side_r", CubeListBuilder.create().texOffs(0, 0).addBox(-1F, 0F, -3.2F, 1.5F, 5.9F, 6.6F), PartPose.offsetAndRotation(-4.35F, -6.8F, 0F, 0F, 0F, 0.07F));
        PartDefinition p_h_frame_r = p_head.addOrReplaceChild("h_frame_r", CubeListBuilder.create().texOffs(34, 27).addBox(-1.05F, 0F, -0.65F, 1.7F, 6.9F, 1.2F), PartPose.offsetAndRotation(-3.7F, -7.6F, -3.9F, -0.05F, 0F, 0.06F));
        PartDefinition p_h_flare_r = p_head.addOrReplaceChild("h_flare_r", CubeListBuilder.create().texOffs(0, 27).addBox(-1.1F, 0F, -3.3F, 1.3F, 1.5F, 6.6F), PartPose.offsetAndRotation(-4.75F, -1.6F, 0.2F, 0F, 0F, 0.4F));
        PartDefinition p_h_flare_back = p_head.addOrReplaceChild("h_flare_back", CubeListBuilder.create().texOffs(24, 52).addBox(-4.6F, 0F, -1.2F, 9.2F, 1.4F, 1.3F), PartPose.offsetAndRotation(0F, -1F, 4.5F, -0.4F, 0F, 0F));
        PartDefinition p_h_tuft_a = p_head.addOrReplaceChild("h_tuft_a", CubeListBuilder.create().texOffs(58, 46).addBox(-0.6F, -1.7F, -0.6F, 1.2F, 1.9F, 1.2F), PartPose.offsetAndRotation(1F, -8.5F, -1.2F, 0.35F, 0.3F, 0.5F));
        PartDefinition p_h_tuft_b = p_head.addOrReplaceChild("h_tuft_b", CubeListBuilder.create().texOffs(0, 57).addBox(-0.6F, -1.5F, -0.6F, 1.2F, 1.7F, 1.2F), PartPose.offsetAndRotation(-1.6F, -8.4F, 2F, -0.4F, -0.4F, -0.45F));
        PartDefinition p_h_crown = p_head.addOrReplaceChild("h_crown", CubeListBuilder.create().texOffs(0, 37).addBox(-3.6F, -0.5F, -3.6F, 7.2F, 0.6F, 7.2F), PartPose.offsetAndRotation(0F, -8.6F, 0F, 0F, 0F, 0F));
        PartDefinition p_h_bang_0 = p_head.addOrReplaceChild("h_bang_0", CubeListBuilder.create().texOffs(0, 52).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.7F, 0.76F), PartPose.offsetAndRotation(-3.55F, -8.3F, -4.45F, -0.14F, 0F, 0.08F));
        PartDefinition p_h_bang_1 = p_head.addOrReplaceChild("h_bang_1", CubeListBuilder.create().texOffs(48, 37).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.3F, 0.76F), PartPose.offsetAndRotation(-2.4F, -8.3F, -4.45F, -0.14F, 0F, 0.05F));
        PartDefinition p_h_bang_2 = p_head.addOrReplaceChild("h_bang_2", CubeListBuilder.create().texOffs(6, 52).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.9F, 0.76F), PartPose.offsetAndRotation(-1.2F, -8.3F, -4.45F, -0.14F, 0F, 0.02F));
        PartDefinition p_h_bang_3 = p_head.addOrReplaceChild("h_bang_3", CubeListBuilder.create().texOffs(54, 37).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.6F, 0.76F), PartPose.offsetAndRotation(0F, -8.3F, -4.45F, -0.14F, 0F, 0F));
        PartDefinition p_h_bang_4 = p_head.addOrReplaceChild("h_bang_4", CubeListBuilder.create().texOffs(12, 52).addBox(-0.68F, 0F, -0.38F, 1.36F, 3F, 0.76F), PartPose.offsetAndRotation(1.2F, -8.3F, -4.45F, -0.14F, 0F, -0.03F));
        PartDefinition p_h_bang_5 = p_head.addOrReplaceChild("h_bang_5", CubeListBuilder.create().texOffs(0, 46).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.3F, 0.76F), PartPose.offsetAndRotation(2.4F, -8.3F, -4.45F, -0.14F, 0F, -0.05F));
        PartDefinition p_h_bang_6 = p_head.addOrReplaceChild("h_bang_6", CubeListBuilder.create().texOffs(18, 52).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.6F, 0.76F), PartPose.offsetAndRotation(3.55F, -8.3F, -4.45F, -0.14F, 0F, -0.08F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(42, 46).addBox(-1.9F, -1.2F, -0.5F, 2.3F, 2.6F, 1F).texOffs(6, 57).addBox(-3.3F, -1.8F, -0.42F, 1.5F, 2F, 0.84F).texOffs(51, 57).addBox(-4.6F, -2.5F, -0.32F, 1.4F, 1.3F, 0.64F), PartPose.offsetAndRotation(-3.6F, -3.6F, 0.6F, 0.05F, 0.38F, -0.36F));
        PartDefinition p_goggles = p_head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(30, 57).addBox(-4.7F, -0.4F, -0.3F, 9.4F, 0.8F, 0.6F), PartPose.offsetAndRotation(0F, -8.4F, -2.8F, -0.4F, 0F, 0F));
        PartDefinition p_lens_r = p_goggles.addOrReplaceChild("lens_r", CubeListBuilder.create().texOffs(6, 46).addBox(-1.25F, -1F, -0.6F, 2.5F, 2F, 1.2F), PartPose.offsetAndRotation(-1.8F, 0F, -0.5F, 0F, 0.1F, 0F));
        PartDefinition p_clip_r = p_head.addOrReplaceChild("clip_r", CubeListBuilder.create().texOffs(12, 57).addBox(-0.7F, -1.8F, -0.35F, 1.4F, 2.3F, 0.7F), PartPose.offsetAndRotation(-3.6F, -6.8F, -4.6F, 0F, 0F, -0.35F));
        PartDefinition p_tail_r = p_head.addOrReplaceChild("tail_r", CubeListBuilder.create().texOffs(30, 37).addBox(-0.9F, 0F, -0.9F, 1.8F, 4.4F, 1.8F).texOffs(46, 52).addBox(-0.7F, 4.2F, -0.7F, 1.4F, 1.6F, 1.4F), PartPose.offsetAndRotation(-4F, -6.2F, 3.6F, 0.5F, 0F, 0.55F));
        PartDefinition p_tie_r = p_head.addOrReplaceChild("tie_r", CubeListBuilder.create().texOffs(15, 46).addBox(-1F, -1F, -1F, 2F, 1.4F, 2F), PartPose.offsetAndRotation(-4F, -6.2F, 3.6F, 0F, 0F, 0F));
        PartDefinition p_h_side_l = p_head.addOrReplaceChild("h_side_l", CubeListBuilder.create().texOffs(18, 0).addBox(-0.5F, 0F, -3.2F, 1.5F, 5.9F, 6.6F), PartPose.offsetAndRotation(4.35F, -6.8F, 0F, 0F, 0F, -0.07F));
        PartDefinition p_h_frame_l = p_head.addOrReplaceChild("h_frame_l", CubeListBuilder.create().texOffs(41, 27).addBox(-0.65F, 0F, -0.65F, 1.7F, 6.9F, 1.2F), PartPose.offsetAndRotation(3.7F, -7.6F, -3.9F, -0.05F, 0F, -0.06F));
        PartDefinition p_h_flare_l = p_head.addOrReplaceChild("h_flare_l", CubeListBuilder.create().texOffs(17, 27).addBox(-0.2F, 0F, -3.3F, 1.3F, 1.5F, 6.6F), PartPose.offsetAndRotation(4.75F, -1.6F, 0.2F, 0F, 0F, -0.4F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(50, 46).addBox(-0.4F, -1.2F, -0.5F, 2.3F, 2.6F, 1F).texOffs(18, 57).addBox(1.8F, -1.8F, -0.42F, 1.5F, 2F, 0.84F).texOffs(57, 57).addBox(3.2F, -2.5F, -0.32F, 1.4F, 1.3F, 0.64F), PartPose.offsetAndRotation(3.6F, -3.6F, 0.6F, 0.05F, -0.38F, 0.36F));
        PartDefinition p_lens_l = p_goggles.addOrReplaceChild("lens_l", CubeListBuilder.create().texOffs(24, 46).addBox(-1.25F, -1F, -0.6F, 2.5F, 2F, 1.2F), PartPose.offsetAndRotation(1.8F, 0F, -0.5F, 0F, -0.1F, 0F));
        PartDefinition p_clip_l = p_head.addOrReplaceChild("clip_l", CubeListBuilder.create().texOffs(24, 57).addBox(-0.7F, -1.8F, -0.35F, 1.4F, 2.3F, 0.7F), PartPose.offsetAndRotation(3.6F, -6.8F, -4.6F, 0F, 0F, 0.35F));
        PartDefinition p_tail_l = p_head.addOrReplaceChild("tail_l", CubeListBuilder.create().texOffs(39, 37).addBox(-0.9F, 0F, -0.9F, 1.8F, 4.4F, 1.8F).texOffs(53, 52).addBox(-0.7F, 4.2F, -0.7F, 1.4F, 1.6F, 1.4F), PartPose.offsetAndRotation(4F, -6.2F, 3.6F, 0.5F, 0F, -0.55F));
        PartDefinition p_tie_l = p_head.addOrReplaceChild("tie_l", CubeListBuilder.create().texOffs(33, 46).addBox(-1F, -1F, -1F, 2F, 1.4F, 2F), PartPose.offsetAndRotation(4F, -6.2F, 3.6F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static Map<String, ModelPart> bakeHairHomura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("h_top", m.get("head").getChild("h_top"));
        m.put("h_back", m.get("head").getChild("h_back"));
        m.put("h_side_r", m.get("head").getChild("h_side_r"));
        m.put("h_frame_r", m.get("head").getChild("h_frame_r"));
        m.put("h_flare_r", m.get("head").getChild("h_flare_r"));
        m.put("h_flare_back", m.get("head").getChild("h_flare_back"));
        m.put("h_tuft_a", m.get("head").getChild("h_tuft_a"));
        m.put("h_tuft_b", m.get("head").getChild("h_tuft_b"));
        m.put("h_crown", m.get("head").getChild("h_crown"));
        m.put("h_bang_0", m.get("head").getChild("h_bang_0"));
        m.put("h_bang_1", m.get("head").getChild("h_bang_1"));
        m.put("h_bang_2", m.get("head").getChild("h_bang_2"));
        m.put("h_bang_3", m.get("head").getChild("h_bang_3"));
        m.put("h_bang_4", m.get("head").getChild("h_bang_4"));
        m.put("h_bang_5", m.get("head").getChild("h_bang_5"));
        m.put("h_bang_6", m.get("head").getChild("h_bang_6"));
        m.put("ear_r", m.get("head").getChild("ear_r"));
        m.put("goggles", m.get("head").getChild("goggles"));
        m.put("lens_r", m.get("goggles").getChild("lens_r"));
        m.put("clip_r", m.get("head").getChild("clip_r"));
        m.put("tail_r", m.get("head").getChild("tail_r"));
        m.put("tie_r", m.get("head").getChild("tie_r"));
        m.put("h_side_l", m.get("head").getChild("h_side_l"));
        m.put("h_frame_l", m.get("head").getChild("h_frame_l"));
        m.put("h_flare_l", m.get("head").getChild("h_flare_l"));
        m.put("ear_l", m.get("head").getChild("ear_l"));
        m.put("lens_l", m.get("goggles").getChild("lens_l"));
        m.put("clip_l", m.get("head").getChild("clip_l"));
        m.put("tail_l", m.get("head").getChild("tail_l"));
        m.put("tie_l", m.get("head").getChild("tie_l"));
        return m;
    }

    public static LayerDefinition clothesKura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_c_dress = p_body.addOrReplaceChild("c_dress", CubeListBuilder.create().texOffs(0, 0).addBox(-4.25F, -0.25F, -2.25F, 8.5F, 7.5F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_bust = p_body.addOrReplaceChild("c_bust", CubeListBuilder.create().texOffs(48, 66).addBox(-3.3F, 0F, -0.9F, 6.6F, 2.9F, 1F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_c_collar = p_body.addOrReplaceChild("c_collar", CubeListBuilder.create().texOffs(29, 59).addBox(-4.35F, -0.45F, -2.35F, 8.7F, 1.1F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_latch = p_body.addOrReplaceChild("c_latch", CubeListBuilder.create().texOffs(28, 73).addBox(-0.85F, -0.4F, -0.4F, 1.7F, 2.1F, 0.55F).texOffs(36, 81).addBox(-0.32F, 0.35F, -0.55F, 0.64F, 0.64F, 0.2F), PartPose.offsetAndRotation(0F, 1.5F, -2.7F, 0F, 0F, 0F));
        PartDefinition p_c_sash = p_body.addOrReplaceChild("c_sash", CubeListBuilder.create().texOffs(0, 59).addBox(-4.4F, 6.8F, -2.4F, 8.8F, 1F, 4.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_puff_r = p_right_arm.addOrReplaceChild("c_puff_r", CubeListBuilder.create().texOffs(21, 13).addBox(-2.55F, -2.55F, -2.55F, 4.1F, 4F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_cuff_r = p_right_arm.addOrReplaceChild("c_cuff_r", CubeListBuilder.create().texOffs(0, 66).addBox(-2.25F, 7.6F, -2.25F, 3.5F, 1.2F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_hips = p_body.addOrReplaceChild("l_hips", CubeListBuilder.create().texOffs(0, 24).addBox(-4.5F, 7.4F, -2.45F, 9F, 3.4F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_petticoat = p_body.addOrReplaceChild("l_petticoat", CubeListBuilder.create().texOffs(20, 34).addBox(-4.9F, 11.6F, -2.8F, 9.8F, 1.2F, 5.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_shoe_r = p_right_leg.addOrReplaceChild("f_shoe_r", CubeListBuilder.create().texOffs(29, 24).addBox(-2.2F, 9.9F, -2.75F, 4.4F, 2.35F, 4.95F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_backpack = p_body.addOrReplaceChild("c_backpack", CubeListBuilder.create().texOffs(0, 13).addBox(-3.4F, 0F, 0F, 6.8F, 6F, 3.1F), PartPose.offsetAndRotation(0F, 1.2F, 2.3F, 0.05F, 0F, 0F));
        PartDefinition p_c_pack_strap_r = p_body.addOrReplaceChild("c_pack_strap_r", CubeListBuilder.create().texOffs(27, 0).addBox(-0.5F, -0.4F, -2.45F, 1F, 5.2F, 4.9F), PartPose.offsetAndRotation(-2.3F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_back_bow_knot = p_body.addOrReplaceChild("c_back_bow_knot", CubeListBuilder.create().texOffs(34, 73).addBox(-0.715F, -0.715F, -0.52F, 1.43F, 1.43F, 1.04F), PartPose.offsetAndRotation(0F, 7.3F, 2.45F, 0F, 0F, 0F));
        PartDefinition p_c_back_bow_wing_a = p_c_back_bow_knot.addOrReplaceChild("c_back_bow_wing_a", CubeListBuilder.create().texOffs(0, 73).addBox(-2.6F, -1.17F, -0.455F, 2.6F, 2.34F, 0.91F), PartPose.offsetAndRotation(-0.52F, 0F, 0F, 0F, 0.2F, 0.3F));
        PartDefinition p_c_back_bow_wing_b = p_c_back_bow_knot.addOrReplaceChild("c_back_bow_wing_b", CubeListBuilder.create().texOffs(9, 73).addBox(0F, -1.17F, -0.455F, 2.6F, 2.34F, 0.91F), PartPose.offsetAndRotation(0.52F, 0F, 0F, 0F, -0.2F, -0.3F));
        PartDefinition p_c_back_bow_tail_a = p_c_back_bow_knot.addOrReplaceChild("c_back_bow_tail_a", CubeListBuilder.create().texOffs(18, 73).addBox(-0.585F, 0F, -0.325F, 1.17F, 3.12F, 0.65F), PartPose.offsetAndRotation(-0.26F, 0.52F, 0F, 0F, 0F, 0.25F));
        PartDefinition p_c_back_bow_tail_b = p_c_back_bow_knot.addOrReplaceChild("c_back_bow_tail_b", CubeListBuilder.create().texOffs(23, 73).addBox(-0.585F, 0F, -0.325F, 1.17F, 3.12F, 0.65F), PartPose.offsetAndRotation(0.26F, 0.52F, 0F, 0F, 0F, -0.25F));
        PartDefinition p_l_skirt_front = p_body.addOrReplaceChild("l_skirt_front", CubeListBuilder.create().texOffs(0, 43).addBox(-4.35F, 0F, -0.45F, 8.7F, 5.8F, 0.9F).texOffs(40, 73).addBox(-4.25F, 5.45F, -0.3F, 8.5F, 0.85F, 0.7F), PartPose.offsetAndRotation(0F, 8.2F, -2.25F, -0.32F, 0F, 0F));
        PartDefinition p_l_skirt_back = p_body.addOrReplaceChild("l_skirt_back", CubeListBuilder.create().texOffs(21, 43).addBox(-4.35F, 0F, -0.45F, 8.7F, 5.8F, 0.9F).texOffs(0, 78).addBox(-4.25F, 5.45F, -0.3F, 8.5F, 0.85F, 0.7F), PartPose.offsetAndRotation(0F, 8.2F, 2.25F, -0.32F, 3.1416F, 0F));
        PartDefinition p_l_skirt_right = p_body.addOrReplaceChild("l_skirt_right", CubeListBuilder.create().texOffs(42, 43).addBox(-2.45F, 0F, -0.45F, 4.9F, 5.8F, 0.9F).texOffs(35, 78).addBox(-2.35F, 5.45F, -0.3F, 4.7F, 0.85F, 0.7F), PartPose.offsetAndRotation(-4.35F, 8.2F, 0F, -0.32F, 1.5708F, 0F));
        PartDefinition p_l_skirt_left = p_body.addOrReplaceChild("l_skirt_left", CubeListBuilder.create().texOffs(0, 51).addBox(-2.45F, 0F, -0.45F, 4.9F, 5.8F, 0.9F).texOffs(47, 78).addBox(-2.35F, 5.45F, -0.3F, 4.7F, 0.85F, 0.7F), PartPose.offsetAndRotation(4.35F, 8.2F, 0F, -0.32F, -1.5708F, 0F));
        PartDefinition p_l_skirt_fr = p_body.addOrReplaceChild("l_skirt_fr", CubeListBuilder.create().texOffs(13, 51).addBox(-1.55F, 0F, -0.45F, 3.1F, 5.8F, 0.9F).texOffs(0, 81).addBox(-1.45F, 5.45F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.2F, -1.85F, -0.32F, 0.7854F, 0F));
        PartDefinition p_l_skirt_fl = p_body.addOrReplaceChild("l_skirt_fl", CubeListBuilder.create().texOffs(22, 51).addBox(-1.55F, 0F, -0.45F, 3.1F, 5.8F, 0.9F).texOffs(9, 81).addBox(-1.45F, 5.45F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.2F, -1.85F, -0.32F, -0.7854F, 0F));
        PartDefinition p_l_skirt_br = p_body.addOrReplaceChild("l_skirt_br", CubeListBuilder.create().texOffs(31, 51).addBox(-1.55F, 0F, -0.45F, 3.1F, 5.8F, 0.9F).texOffs(18, 81).addBox(-1.45F, 5.45F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.2F, 1.85F, -0.32F, 2.3562F, 0F));
        PartDefinition p_l_skirt_bl = p_body.addOrReplaceChild("l_skirt_bl", CubeListBuilder.create().texOffs(40, 51).addBox(-1.55F, 0F, -0.45F, 3.1F, 5.8F, 0.9F).texOffs(27, 81).addBox(-1.45F, 5.45F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.2F, 1.85F, -0.32F, -2.3562F, 0F));
        PartDefinition p_l_apron = p_l_skirt_front.addOrReplaceChild("l_apron", CubeListBuilder.create().texOffs(34, 66).addBox(-3F, 0.2F, -0.35F, 6F, 4.8F, 0.35F).texOffs(20, 78).addBox(-3.25F, 4.9F, -0.4F, 6.5F, 0.9F, 0.45F), PartPose.offsetAndRotation(0F, 0F, -0.5F, 0F, 0F, 0F));
        PartDefinition p_c_puff_l = p_left_arm.addOrReplaceChild("c_puff_l", CubeListBuilder.create().texOffs(41, 13).addBox(-1.55F, -2.55F, -2.55F, 4.1F, 4F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_cuff_l = p_left_arm.addOrReplaceChild("c_cuff_l", CubeListBuilder.create().texOffs(17, 66).addBox(-1.25F, 7.6F, -2.25F, 3.5F, 1.2F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_shoe_l = p_left_leg.addOrReplaceChild("f_shoe_l", CubeListBuilder.create().texOffs(0, 34).addBox(-2.2F, 9.9F, -2.75F, 4.4F, 2.35F, 4.95F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_pack_strap_l = p_body.addOrReplaceChild("c_pack_strap_l", CubeListBuilder.create().texOffs(40, 0).addBox(-0.5F, -0.4F, -2.45F, 1F, 5.2F, 4.9F), PartPose.offsetAndRotation(2.3F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeClothesKura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("c_dress", m.get("body").getChild("c_dress"));
        m.put("c_bust", m.get("body").getChild("c_bust"));
        m.put("c_collar", m.get("body").getChild("c_collar"));
        m.put("c_latch", m.get("body").getChild("c_latch"));
        m.put("c_sash", m.get("body").getChild("c_sash"));
        m.put("c_puff_r", m.get("right_arm").getChild("c_puff_r"));
        m.put("c_cuff_r", m.get("right_arm").getChild("c_cuff_r"));
        m.put("l_hips", m.get("body").getChild("l_hips"));
        m.put("l_petticoat", m.get("body").getChild("l_petticoat"));
        m.put("f_shoe_r", m.get("right_leg").getChild("f_shoe_r"));
        m.put("c_backpack", m.get("body").getChild("c_backpack"));
        m.put("c_pack_strap_r", m.get("body").getChild("c_pack_strap_r"));
        m.put("c_back_bow_knot", m.get("body").getChild("c_back_bow_knot"));
        m.put("c_back_bow_wing_a", m.get("c_back_bow_knot").getChild("c_back_bow_wing_a"));
        m.put("c_back_bow_wing_b", m.get("c_back_bow_knot").getChild("c_back_bow_wing_b"));
        m.put("c_back_bow_tail_a", m.get("c_back_bow_knot").getChild("c_back_bow_tail_a"));
        m.put("c_back_bow_tail_b", m.get("c_back_bow_knot").getChild("c_back_bow_tail_b"));
        m.put("l_skirt_front", m.get("body").getChild("l_skirt_front"));
        m.put("l_skirt_back", m.get("body").getChild("l_skirt_back"));
        m.put("l_skirt_right", m.get("body").getChild("l_skirt_right"));
        m.put("l_skirt_left", m.get("body").getChild("l_skirt_left"));
        m.put("l_skirt_fr", m.get("body").getChild("l_skirt_fr"));
        m.put("l_skirt_fl", m.get("body").getChild("l_skirt_fl"));
        m.put("l_skirt_br", m.get("body").getChild("l_skirt_br"));
        m.put("l_skirt_bl", m.get("body").getChild("l_skirt_bl"));
        m.put("l_apron", m.get("l_skirt_front").getChild("l_apron"));
        m.put("c_puff_l", m.get("left_arm").getChild("c_puff_l"));
        m.put("c_cuff_l", m.get("left_arm").getChild("c_cuff_l"));
        m.put("f_shoe_l", m.get("left_leg").getChild("f_shoe_l"));
        m.put("c_pack_strap_l", m.get("body").getChild("c_pack_strap_l"));
        return m;
    }

    public static LayerDefinition hairKura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_h_top = p_head.addOrReplaceChild("h_top", CubeListBuilder.create().texOffs(0, 15).addBox(-4.55F, -8.6F, -4.55F, 9.1F, 2.7F, 9.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_h_back = p_head.addOrReplaceChild("h_back", CubeListBuilder.create().texOffs(38, 15).addBox(-4.75F, 0F, 0F, 9.5F, 7.4F, 3.3F), PartPose.offsetAndRotation(0F, -7.2F, 1.5F, 0.06F, 0F, 0F));
        PartDefinition p_h_side_r = p_head.addOrReplaceChild("h_side_r", CubeListBuilder.create().texOffs(0, 0).addBox(-1F, 0F, -3.2F, 1.5F, 6.5F, 6.6F), PartPose.offsetAndRotation(-4.35F, -6.8F, 0F, 0F, 0F, 0.07F));
        PartDefinition p_h_frame_r = p_head.addOrReplaceChild("h_frame_r", CubeListBuilder.create().texOffs(34, 28).addBox(-1.05F, 0F, -0.65F, 1.7F, 7.5F, 1.2F), PartPose.offsetAndRotation(-3.7F, -7.6F, -3.9F, -0.05F, 0F, 0.06F));
        PartDefinition p_h_flare_r = p_head.addOrReplaceChild("h_flare_r", CubeListBuilder.create().texOffs(0, 28).addBox(-1.1F, 0F, -3.3F, 1.3F, 1.5F, 6.6F), PartPose.offsetAndRotation(-4.75F, -1F, 0.2F, 0F, 0F, 0.4F));
        PartDefinition p_h_flare_back = p_head.addOrReplaceChild("h_flare_back", CubeListBuilder.create().texOffs(12, 52).addBox(-4.6F, 0F, -1.2F, 9.2F, 1.4F, 1.3F), PartPose.offsetAndRotation(0F, -0.4F, 4.5F, -0.4F, 0F, 0F));
        PartDefinition p_h_tuft_a = p_head.addOrReplaceChild("h_tuft_a", CubeListBuilder.create().texOffs(42, 47).addBox(-0.6F, -1.7F, -0.6F, 1.2F, 1.9F, 1.2F), PartPose.offsetAndRotation(1F, -8.5F, -1.2F, 0.35F, 0.3F, 0.5F));
        PartDefinition p_h_tuft_b = p_head.addOrReplaceChild("h_tuft_b", CubeListBuilder.create().texOffs(52, 52).addBox(-0.6F, -1.5F, -0.6F, 1.2F, 1.7F, 1.2F), PartPose.offsetAndRotation(-1.6F, -8.4F, 2F, -0.4F, -0.4F, -0.45F));
        PartDefinition p_h_crown = p_head.addOrReplaceChild("h_crown", CubeListBuilder.create().texOffs(0, 38).addBox(-3.6F, -0.5F, -3.6F, 7.2F, 0.6F, 7.2F), PartPose.offsetAndRotation(0F, -8.6F, 0F, 0F, 0F, 0F));
        PartDefinition p_h_bang_0 = p_head.addOrReplaceChild("h_bang_0", CubeListBuilder.create().texOffs(48, 47).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.7F, 0.76F), PartPose.offsetAndRotation(-3.55F, -8.3F, -4.45F, -0.14F, 0F, 0.08F));
        PartDefinition p_h_bang_1 = p_head.addOrReplaceChild("h_bang_1", CubeListBuilder.create().texOffs(30, 38).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.3F, 0.76F), PartPose.offsetAndRotation(-2.4F, -8.3F, -4.45F, -0.14F, 0F, 0.05F));
        PartDefinition p_h_bang_2 = p_head.addOrReplaceChild("h_bang_2", CubeListBuilder.create().texOffs(54, 47).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.9F, 0.76F), PartPose.offsetAndRotation(-1.2F, -8.3F, -4.45F, -0.14F, 0F, 0.02F));
        PartDefinition p_h_bang_3 = p_head.addOrReplaceChild("h_bang_3", CubeListBuilder.create().texOffs(36, 38).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.6F, 0.76F), PartPose.offsetAndRotation(0F, -8.3F, -4.45F, -0.14F, 0F, 0F));
        PartDefinition p_h_bang_4 = p_head.addOrReplaceChild("h_bang_4", CubeListBuilder.create().texOffs(0, 52).addBox(-0.68F, 0F, -0.38F, 1.36F, 3F, 0.76F), PartPose.offsetAndRotation(1.2F, -8.3F, -4.45F, -0.14F, 0F, -0.03F));
        PartDefinition p_h_bang_5 = p_head.addOrReplaceChild("h_bang_5", CubeListBuilder.create().texOffs(42, 38).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.3F, 0.76F), PartPose.offsetAndRotation(2.4F, -8.3F, -4.45F, -0.14F, 0F, -0.05F));
        PartDefinition p_h_bang_6 = p_head.addOrReplaceChild("h_bang_6", CubeListBuilder.create().texOffs(6, 52).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.6F, 0.76F), PartPose.offsetAndRotation(3.55F, -8.3F, -4.45F, -0.14F, 0F, -0.08F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(26, 47).addBox(-1.9F, -1.2F, -0.5F, 2.3F, 2.6F, 1F).texOffs(58, 52).addBox(-3.3F, -1.8F, -0.42F, 1.5F, 2F, 0.84F).texOffs(14, 57).addBox(-4.6F, -2.5F, -0.32F, 1.4F, 1.3F, 0.64F), PartPose.offsetAndRotation(-3.6F, -3.6F, 0.6F, 0.05F, 0.38F, -0.36F));
        PartDefinition p_headdress = p_head.addOrReplaceChild("headdress", CubeListBuilder.create().texOffs(34, 52).addBox(-3.8F, -1.6F, -0.4F, 7.6F, 1.8F, 0.8F), PartPose.offsetAndRotation(0F, -8.4F, -1.6F, -0.45F, 0F, 0F));
        PartDefinition p_headband = p_head.addOrReplaceChild("headband", CubeListBuilder.create().texOffs(0, 47).addBox(-4.75F, -0.45F, -1.4F, 9.5F, 0.9F, 2.8F), PartPose.offsetAndRotation(0F, -7.9F, 0F, 0F, 0F, 0F));
        PartDefinition p_hair_bow_knot = p_head.addOrReplaceChild("hair_bow_knot", CubeListBuilder.create().texOffs(38, 57).addBox(-0.44F, -0.44F, -0.32F, 0.88F, 0.88F, 0.64F), PartPose.offsetAndRotation(3.6F, -7.6F, 1.8F, 0F, -0.6F, 0F));
        PartDefinition p_hair_bow_wing_a = p_hair_bow_knot.addOrReplaceChild("hair_bow_wing_a", CubeListBuilder.create().texOffs(20, 57).addBox(-1.6F, -0.72F, -0.28F, 1.6F, 1.44F, 0.56F), PartPose.offsetAndRotation(-0.32F, 0F, 0F, 0F, 0.2F, 0.3F));
        PartDefinition p_hair_bow_wing_b = p_hair_bow_knot.addOrReplaceChild("hair_bow_wing_b", CubeListBuilder.create().texOffs(26, 57).addBox(0F, -0.72F, -0.28F, 1.6F, 1.44F, 0.56F), PartPose.offsetAndRotation(0.32F, 0F, 0F, 0F, -0.2F, -0.3F));
        PartDefinition p_hair_bow_tail_a = p_hair_bow_knot.addOrReplaceChild("hair_bow_tail_a", CubeListBuilder.create().texOffs(6, 57).addBox(-0.36F, 0F, -0.2F, 0.72F, 1.92F, 0.4F), PartPose.offsetAndRotation(-0.16F, 0.32F, 0F, 0F, 0F, 0.25F));
        PartDefinition p_hair_bow_tail_b = p_hair_bow_knot.addOrReplaceChild("hair_bow_tail_b", CubeListBuilder.create().texOffs(10, 57).addBox(-0.36F, 0F, -0.2F, 0.72F, 1.92F, 0.4F), PartPose.offsetAndRotation(0.16F, 0.32F, 0F, 0F, 0F, -0.25F));
        PartDefinition p_h_side_l = p_head.addOrReplaceChild("h_side_l", CubeListBuilder.create().texOffs(18, 0).addBox(-0.5F, 0F, -3.2F, 1.5F, 6.5F, 6.6F), PartPose.offsetAndRotation(4.35F, -6.8F, 0F, 0F, 0F, -0.07F));
        PartDefinition p_h_frame_l = p_head.addOrReplaceChild("h_frame_l", CubeListBuilder.create().texOffs(41, 28).addBox(-0.65F, 0F, -0.65F, 1.7F, 7.5F, 1.2F), PartPose.offsetAndRotation(3.7F, -7.6F, -3.9F, -0.05F, 0F, -0.06F));
        PartDefinition p_h_flare_l = p_head.addOrReplaceChild("h_flare_l", CubeListBuilder.create().texOffs(17, 28).addBox(-0.2F, 0F, -3.3F, 1.3F, 1.5F, 6.6F), PartPose.offsetAndRotation(4.75F, -1F, 0.2F, 0F, 0F, -0.4F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(34, 47).addBox(-0.4F, -1.2F, -0.5F, 2.3F, 2.6F, 1F).texOffs(0, 57).addBox(1.8F, -1.8F, -0.42F, 1.5F, 2F, 0.84F).texOffs(32, 57).addBox(3.2F, -2.5F, -0.32F, 1.4F, 1.3F, 0.64F), PartPose.offsetAndRotation(3.6F, -3.6F, 0.6F, 0.05F, -0.38F, 0.36F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static Map<String, ModelPart> bakeHairKura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("h_top", m.get("head").getChild("h_top"));
        m.put("h_back", m.get("head").getChild("h_back"));
        m.put("h_side_r", m.get("head").getChild("h_side_r"));
        m.put("h_frame_r", m.get("head").getChild("h_frame_r"));
        m.put("h_flare_r", m.get("head").getChild("h_flare_r"));
        m.put("h_flare_back", m.get("head").getChild("h_flare_back"));
        m.put("h_tuft_a", m.get("head").getChild("h_tuft_a"));
        m.put("h_tuft_b", m.get("head").getChild("h_tuft_b"));
        m.put("h_crown", m.get("head").getChild("h_crown"));
        m.put("h_bang_0", m.get("head").getChild("h_bang_0"));
        m.put("h_bang_1", m.get("head").getChild("h_bang_1"));
        m.put("h_bang_2", m.get("head").getChild("h_bang_2"));
        m.put("h_bang_3", m.get("head").getChild("h_bang_3"));
        m.put("h_bang_4", m.get("head").getChild("h_bang_4"));
        m.put("h_bang_5", m.get("head").getChild("h_bang_5"));
        m.put("h_bang_6", m.get("head").getChild("h_bang_6"));
        m.put("ear_r", m.get("head").getChild("ear_r"));
        m.put("headdress", m.get("head").getChild("headdress"));
        m.put("headband", m.get("head").getChild("headband"));
        m.put("hair_bow_knot", m.get("head").getChild("hair_bow_knot"));
        m.put("hair_bow_wing_a", m.get("hair_bow_knot").getChild("hair_bow_wing_a"));
        m.put("hair_bow_wing_b", m.get("hair_bow_knot").getChild("hair_bow_wing_b"));
        m.put("hair_bow_tail_a", m.get("hair_bow_knot").getChild("hair_bow_tail_a"));
        m.put("hair_bow_tail_b", m.get("hair_bow_knot").getChild("hair_bow_tail_b"));
        m.put("h_side_l", m.get("head").getChild("h_side_l"));
        m.put("h_frame_l", m.get("head").getChild("h_frame_l"));
        m.put("h_flare_l", m.get("head").getChild("h_flare_l"));
        m.put("ear_l", m.get("head").getChild("ear_l"));
        return m;
    }

    public static LayerDefinition clothesShiori() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_c_robe = p_body.addOrReplaceChild("c_robe", CubeListBuilder.create().texOffs(21, 0).addBox(-4.25F, -0.25F, -2.25F, 8.5F, 8F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_bust = p_body.addOrReplaceChild("c_bust", CubeListBuilder.create().texOffs(29, 61).addBox(-3.3F, 0F, -0.9F, 6.6F, 2.9F, 1F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_c_collar = p_body.addOrReplaceChild("c_collar", CubeListBuilder.create().texOffs(42, 51).addBox(-2.6F, -1.5F, -2.45F, 5.2F, 1.8F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_brooch = p_body.addOrReplaceChild("c_brooch", CubeListBuilder.create().texOffs(48, 72).addBox(-0.68F, -0.68F, -0.35F, 1.36F, 1.36F, 0.55F), PartPose.offsetAndRotation(0F, 1F, -2.75F, 0F, 0F, 0.78F));
        PartDefinition p_c_sash = p_body.addOrReplaceChild("c_sash", CubeListBuilder.create().texOffs(0, 61).addBox(-4.4F, 6.9F, -2.4F, 8.8F, 1.1F, 4.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_cape = p_body.addOrReplaceChild("c_cape", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6F, 0F, 0F, 9.2F, 15.2F, 0.45F), PartPose.offsetAndRotation(0F, -0.2F, 2.45F, 0.12F, 0F, 0F));
        PartDefinition p_c_bell_r = p_right_arm.addOrReplaceChild("c_bell_r", CubeListBuilder.create().texOffs(0, 51).addBox(-2.65F, 5.4F, -2.65F, 4.3F, 3.4F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_hips = p_body.addOrReplaceChild("l_hips", CubeListBuilder.create().texOffs(18, 40).addBox(-4.5F, 7.4F, -2.45F, 9F, 3.4F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_boot_r = p_right_leg.addOrReplaceChild("f_boot_r", CubeListBuilder.create().texOffs(0, 17).addBox(-2.3F, 6.2F, -2.3F, 4.6F, 6.05F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_toe_r = p_right_leg.addOrReplaceChild("f_toe_r", CubeListBuilder.create().texOffs(46, 61).addBox(-2.3F, 10.25F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_l_skirt_front = p_body.addOrReplaceChild("l_skirt_front", CubeListBuilder.create().texOffs(40, 17).addBox(-4.35F, 0F, -0.45F, 8.7F, 8.2F, 0.9F).texOffs(12, 68).addBox(-4.25F, 7.85F, -0.3F, 8.5F, 0.85F, 0.7F), PartPose.offsetAndRotation(0F, 8.2F, -2.25F, -0.22F, 0F, 0F));
        PartDefinition p_l_skirt_back = p_body.addOrReplaceChild("l_skirt_back", CubeListBuilder.create().texOffs(0, 29).addBox(-4.35F, 0F, -0.45F, 8.7F, 8.2F, 0.9F).texOffs(32, 68).addBox(-4.25F, 7.85F, -0.3F, 8.5F, 0.85F, 0.7F), PartPose.offsetAndRotation(0F, 8.2F, 2.25F, -0.22F, 3.1416F, 0F));
        PartDefinition p_l_skirt_right = p_body.addOrReplaceChild("l_skirt_right", CubeListBuilder.create().texOffs(21, 29).addBox(-2.45F, 0F, -0.45F, 4.9F, 8.2F, 0.9F).texOffs(52, 68).addBox(-2.35F, 7.85F, -0.3F, 4.7F, 0.85F, 0.7F), PartPose.offsetAndRotation(-4.35F, 8.2F, 0F, -0.22F, 1.5708F, 0F));
        PartDefinition p_l_skirt_left = p_body.addOrReplaceChild("l_skirt_left", CubeListBuilder.create().texOffs(34, 29).addBox(-2.45F, 0F, -0.45F, 4.9F, 8.2F, 0.9F).texOffs(0, 72).addBox(-2.35F, 7.85F, -0.3F, 4.7F, 0.85F, 0.7F), PartPose.offsetAndRotation(4.35F, 8.2F, 0F, -0.22F, -1.5708F, 0F));
        PartDefinition p_l_skirt_fr = p_body.addOrReplaceChild("l_skirt_fr", CubeListBuilder.create().texOffs(47, 29).addBox(-1.55F, 0F, -0.45F, 3.1F, 8.2F, 0.9F).texOffs(12, 72).addBox(-1.45F, 7.85F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.2F, -1.85F, -0.22F, 0.7854F, 0F));
        PartDefinition p_l_skirt_fl = p_body.addOrReplaceChild("l_skirt_fl", CubeListBuilder.create().texOffs(56, 29).addBox(-1.55F, 0F, -0.45F, 3.1F, 8.2F, 0.9F).texOffs(21, 72).addBox(-1.45F, 7.85F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.2F, -1.85F, -0.22F, -0.7854F, 0F));
        PartDefinition p_l_skirt_br = p_body.addOrReplaceChild("l_skirt_br", CubeListBuilder.create().texOffs(0, 40).addBox(-1.55F, 0F, -0.45F, 3.1F, 8.2F, 0.9F).texOffs(30, 72).addBox(-1.45F, 7.85F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.2F, 1.85F, -0.22F, 2.3562F, 0F));
        PartDefinition p_l_skirt_bl = p_body.addOrReplaceChild("l_skirt_bl", CubeListBuilder.create().texOffs(9, 40).addBox(-1.55F, 0F, -0.45F, 3.1F, 8.2F, 0.9F).texOffs(39, 72).addBox(-1.45F, 7.85F, -0.3F, 2.9F, 0.85F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.2F, 1.85F, -0.22F, -2.3562F, 0F));
        PartDefinition p_c_bell_l = p_left_arm.addOrReplaceChild("c_bell_l", CubeListBuilder.create().texOffs(21, 51).addBox(-1.65F, 5.4F, -2.65F, 4.3F, 3.4F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_boot_l = p_left_leg.addOrReplaceChild("f_boot_l", CubeListBuilder.create().texOffs(20, 17).addBox(-2.3F, 6.2F, -2.3F, 4.6F, 6.05F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_f_toe_l = p_left_leg.addOrReplaceChild("f_toe_l", CubeListBuilder.create().texOffs(0, 68).addBox(-2.3F, 10.25F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeClothesShiori(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("c_robe", m.get("body").getChild("c_robe"));
        m.put("c_bust", m.get("body").getChild("c_bust"));
        m.put("c_collar", m.get("body").getChild("c_collar"));
        m.put("c_brooch", m.get("body").getChild("c_brooch"));
        m.put("c_sash", m.get("body").getChild("c_sash"));
        m.put("c_cape", m.get("body").getChild("c_cape"));
        m.put("c_bell_r", m.get("right_arm").getChild("c_bell_r"));
        m.put("l_hips", m.get("body").getChild("l_hips"));
        m.put("f_boot_r", m.get("right_leg").getChild("f_boot_r"));
        m.put("f_toe_r", m.get("right_leg").getChild("f_toe_r"));
        m.put("l_skirt_front", m.get("body").getChild("l_skirt_front"));
        m.put("l_skirt_back", m.get("body").getChild("l_skirt_back"));
        m.put("l_skirt_right", m.get("body").getChild("l_skirt_right"));
        m.put("l_skirt_left", m.get("body").getChild("l_skirt_left"));
        m.put("l_skirt_fr", m.get("body").getChild("l_skirt_fr"));
        m.put("l_skirt_fl", m.get("body").getChild("l_skirt_fl"));
        m.put("l_skirt_br", m.get("body").getChild("l_skirt_br"));
        m.put("l_skirt_bl", m.get("body").getChild("l_skirt_bl"));
        m.put("c_bell_l", m.get("left_arm").getChild("c_bell_l"));
        m.put("f_boot_l", m.get("left_leg").getChild("f_boot_l"));
        m.put("f_toe_l", m.get("left_leg").getChild("f_toe_l"));
        return m;
    }

    public static LayerDefinition hairShiori() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_h_top = p_head.addOrReplaceChild("h_top", CubeListBuilder.create().texOffs(0, 14).addBox(-4.55F, -8.6F, -4.55F, 9.1F, 2.7F, 9.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_h_back = p_head.addOrReplaceChild("h_back", CubeListBuilder.create().texOffs(0, 39).addBox(-4.75F, 0F, 0F, 9.5F, 6.8F, 3.3F), PartPose.offsetAndRotation(0F, -7.2F, 1.5F, 0.06F, 0F, 0F));
        PartDefinition p_h_side_r = p_head.addOrReplaceChild("h_side_r", CubeListBuilder.create().texOffs(0, 0).addBox(-1F, 0F, -3.2F, 1.5F, 5.9F, 6.6F), PartPose.offsetAndRotation(-4.35F, -6.8F, 0F, 0F, 0F, 0.07F));
        PartDefinition p_h_frame_r = p_head.addOrReplaceChild("h_frame_r", CubeListBuilder.create().texOffs(0, 51).addBox(-1.05F, 0F, -0.65F, 1.7F, 6.9F, 1.2F), PartPose.offsetAndRotation(-3.7F, -7.6F, -3.9F, -0.05F, 0F, 0.06F));
        PartDefinition p_h_flare_r = p_head.addOrReplaceChild("h_flare_r", CubeListBuilder.create().texOffs(27, 39).addBox(-1.1F, 0F, -3.3F, 1.3F, 1.5F, 6.6F), PartPose.offsetAndRotation(-4.75F, -1.6F, 0.2F, 0F, 0F, 0.4F));
        PartDefinition p_h_flare_back = p_head.addOrReplaceChild("h_flare_back", CubeListBuilder.create().texOffs(0, 66).addBox(-4.6F, 0F, -1.2F, 9.2F, 1.4F, 1.3F), PartPose.offsetAndRotation(0F, -1F, 4.5F, -0.4F, 0F, 0F));
        PartDefinition p_h_tuft_a = p_head.addOrReplaceChild("h_tuft_a", CubeListBuilder.create().texOffs(16, 61).addBox(-0.6F, -1.7F, -0.6F, 1.2F, 1.9F, 1.2F), PartPose.offsetAndRotation(1F, -8.5F, -1.2F, 0.35F, 0.3F, 0.5F));
        PartDefinition p_h_tuft_b = p_head.addOrReplaceChild("h_tuft_b", CubeListBuilder.create().texOffs(22, 66).addBox(-0.6F, -1.5F, -0.6F, 1.2F, 1.7F, 1.2F), PartPose.offsetAndRotation(-1.6F, -8.4F, 2F, -0.4F, -0.4F, -0.45F));
        PartDefinition p_h_crown = p_head.addOrReplaceChild("h_crown", CubeListBuilder.create().texOffs(14, 51).addBox(-3.6F, -0.5F, -3.6F, 7.2F, 0.6F, 7.2F), PartPose.offsetAndRotation(0F, -8.6F, 0F, 0F, 0F, 0F));
        PartDefinition p_h_bang_0 = p_head.addOrReplaceChild("h_bang_0", CubeListBuilder.create().texOffs(22, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.7F, 0.76F), PartPose.offsetAndRotation(-3.55F, -8.3F, -4.45F, -0.14F, 0F, 0.08F));
        PartDefinition p_h_bang_1 = p_head.addOrReplaceChild("h_bang_1", CubeListBuilder.create().texOffs(44, 51).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.3F, 0.76F), PartPose.offsetAndRotation(-2.4F, -8.3F, -4.45F, -0.14F, 0F, 0.05F));
        PartDefinition p_h_bang_2 = p_head.addOrReplaceChild("h_bang_2", CubeListBuilder.create().texOffs(28, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.9F, 0.76F), PartPose.offsetAndRotation(-1.2F, -8.3F, -4.45F, -0.14F, 0F, 0.02F));
        PartDefinition p_h_bang_3 = p_head.addOrReplaceChild("h_bang_3", CubeListBuilder.create().texOffs(50, 51).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.6F, 0.76F), PartPose.offsetAndRotation(0F, -8.3F, -4.45F, -0.14F, 0F, 0F));
        PartDefinition p_h_bang_4 = p_head.addOrReplaceChild("h_bang_4", CubeListBuilder.create().texOffs(34, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 3F, 0.76F), PartPose.offsetAndRotation(1.2F, -8.3F, -4.45F, -0.14F, 0F, -0.03F));
        PartDefinition p_h_bang_5 = p_head.addOrReplaceChild("h_bang_5", CubeListBuilder.create().texOffs(56, 51).addBox(-0.68F, 0F, -0.38F, 1.36F, 3.3F, 0.76F), PartPose.offsetAndRotation(2.4F, -8.3F, -4.45F, -0.14F, 0F, -0.05F));
        PartDefinition p_h_bang_6 = p_head.addOrReplaceChild("h_bang_6", CubeListBuilder.create().texOffs(40, 61).addBox(-0.68F, 0F, -0.38F, 1.36F, 2.6F, 0.76F), PartPose.offsetAndRotation(3.55F, -8.3F, -4.45F, -0.14F, 0F, -0.08F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(0, 61).addBox(-1.9F, -1.2F, -0.5F, 2.3F, 2.6F, 1F).texOffs(28, 66).addBox(-3.3F, -1.8F, -0.42F, 1.5F, 2F, 0.84F).texOffs(46, 66).addBox(-4.6F, -2.5F, -0.32F, 1.4F, 1.3F, 0.64F), PartPose.offsetAndRotation(-3.6F, -3.6F, 0.6F, 0.05F, 0.38F, -0.36F));
        PartDefinition p_circlet = p_head.addOrReplaceChild("circlet", CubeListBuilder.create().texOffs(0, 27).addBox(-4.72F, -0.4F, -4.72F, 9.44F, 0.75F, 9.44F), PartPose.offsetAndRotation(0F, -7.2F, 0F, 0F, 0F, 0F));
        PartDefinition p_tiara_gem = p_head.addOrReplaceChild("tiara_gem", CubeListBuilder.create().texOffs(34, 66).addBox(-0.8F, -0.8F, -0.35F, 1.6F, 1.6F, 0.6F), PartPose.offsetAndRotation(0F, -7.35F, -4.95F, 0F, 0F, 0.78F));
        PartDefinition p_earring_r = p_head.addOrReplaceChild("earring_r", CubeListBuilder.create().texOffs(58, 66).addBox(-0.25F, 0F, -0.25F, 0.5F, 1.3F, 0.5F), PartPose.offsetAndRotation(-6.6F, -2.4F, 0.4F, 0F, 0F, 0F));
        PartDefinition p_h_side_l = p_head.addOrReplaceChild("h_side_l", CubeListBuilder.create().texOffs(18, 0).addBox(-0.5F, 0F, -3.2F, 1.5F, 5.9F, 6.6F), PartPose.offsetAndRotation(4.35F, -6.8F, 0F, 0F, 0F, -0.07F));
        PartDefinition p_h_frame_l = p_head.addOrReplaceChild("h_frame_l", CubeListBuilder.create().texOffs(7, 51).addBox(-0.65F, 0F, -0.65F, 1.7F, 6.9F, 1.2F), PartPose.offsetAndRotation(3.7F, -7.6F, -3.9F, -0.05F, 0F, -0.06F));
        PartDefinition p_h_flare_l = p_head.addOrReplaceChild("h_flare_l", CubeListBuilder.create().texOffs(44, 39).addBox(-0.2F, 0F, -3.3F, 1.3F, 1.5F, 6.6F), PartPose.offsetAndRotation(4.75F, -1.6F, 0.2F, 0F, 0F, -0.4F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(8, 61).addBox(-0.4F, -1.2F, -0.5F, 2.3F, 2.6F, 1F).texOffs(40, 66).addBox(1.8F, -1.8F, -0.42F, 1.5F, 2F, 0.84F).texOffs(52, 66).addBox(3.2F, -2.5F, -0.32F, 1.4F, 1.3F, 0.64F), PartPose.offsetAndRotation(3.6F, -3.6F, 0.6F, 0.05F, -0.38F, 0.36F));
        PartDefinition p_earring_l = p_head.addOrReplaceChild("earring_l", CubeListBuilder.create().texOffs(61, 66).addBox(-0.25F, 0F, -0.25F, 0.5F, 1.3F, 0.5F), PartPose.offsetAndRotation(6.6F, -2.4F, 0.4F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeHairShiori(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("h_top", m.get("head").getChild("h_top"));
        m.put("h_back", m.get("head").getChild("h_back"));
        m.put("h_side_r", m.get("head").getChild("h_side_r"));
        m.put("h_frame_r", m.get("head").getChild("h_frame_r"));
        m.put("h_flare_r", m.get("head").getChild("h_flare_r"));
        m.put("h_flare_back", m.get("head").getChild("h_flare_back"));
        m.put("h_tuft_a", m.get("head").getChild("h_tuft_a"));
        m.put("h_tuft_b", m.get("head").getChild("h_tuft_b"));
        m.put("h_crown", m.get("head").getChild("h_crown"));
        m.put("h_bang_0", m.get("head").getChild("h_bang_0"));
        m.put("h_bang_1", m.get("head").getChild("h_bang_1"));
        m.put("h_bang_2", m.get("head").getChild("h_bang_2"));
        m.put("h_bang_3", m.get("head").getChild("h_bang_3"));
        m.put("h_bang_4", m.get("head").getChild("h_bang_4"));
        m.put("h_bang_5", m.get("head").getChild("h_bang_5"));
        m.put("h_bang_6", m.get("head").getChild("h_bang_6"));
        m.put("ear_r", m.get("head").getChild("ear_r"));
        m.put("circlet", m.get("head").getChild("circlet"));
        m.put("tiara_gem", m.get("head").getChild("tiara_gem"));
        m.put("earring_r", m.get("head").getChild("earring_r"));
        m.put("h_side_l", m.get("head").getChild("h_side_l"));
        m.put("h_frame_l", m.get("head").getChild("h_frame_l"));
        m.put("h_flare_l", m.get("head").getChild("h_flare_l"));
        m.put("ear_l", m.get("head").getChild("ear_l"));
        m.put("earring_l", m.get("head").getChild("earring_l"));
        return m;
    }

}
