// GENERADO por tools/gen_waifus.py — no editar a mano.
package com.totallywaifu.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class WaifuModels {
    private WaifuModels() {}

    public static LayerDefinition headKozue() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bandana = p_head.addOrReplaceChild("bandana", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6F, -1.4F, -4.6F, 9.2F, 1.6F, 9.2F), PartPose.offsetAndRotation(0F, -6.6F, 0F, 0F, 0F, 0F));
        PartDefinition p_knot = p_head.addOrReplaceChild("knot", CubeListBuilder.create().texOffs(54, 0).addBox(-1F, -1F, -0.5F, 2F, 2F, 1F).texOffs(49, 0).addBox(-1.6F, 0.5F, 0F, 1.2F, 2.6F, 0.5F).texOffs(0, 12).addBox(0.4F, 0.5F, 0F, 1.2F, 2.4F, 0.5F), PartPose.offsetAndRotation(0F, -6.4F, 4.7F, 0.3F, 0F, 0F));
        PartDefinition p_pencil = p_head.addOrReplaceChild("pencil", CubeListBuilder.create().texOffs(38, 0).addBox(-0.3F, -0.3F, -2.2F, 0.6F, 0.6F, 4.4F), PartPose.offsetAndRotation(-4.6F, -4.2F, -0.4F, 0.25F, 0F, 0.2F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static Map<String, ModelPart> bakeHeadKozue(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("bandana", m.get("head").getChild("bandana"));
        m.put("knot", m.get("head").getChild("knot"));
        m.put("pencil", m.get("head").getChild("pencil"));
        return m;
    }

    public static LayerDefinition outfitKozue() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_shirt = p_body.addOrReplaceChild("shirt", CubeListBuilder.create().texOffs(0, 0).addBox(-4.25F, -0.3F, -2.25F, 8.5F, 8.4F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(0, 68).addBox(-3.3F, 0F, -0.9F, 6.6F, 3F, 1F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_hips = p_body.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(0, 26).addBox(-4.55F, 8.6F, -2.45F, 9.1F, 3.6F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bib = p_body.addOrReplaceChild("bib", CubeListBuilder.create().texOffs(40, 60).addBox(-3F, 3.4F, -2.55F, 6F, 5F, 0.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_strap_r = p_body.addOrReplaceChild("strap_r", CubeListBuilder.create().texOffs(20, 14).addBox(-0.6F, -0.4F, -2.6F, 1.2F, 4.2F, 5.2F), PartPose.offsetAndRotation(-2.4F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_button_r = p_body.addOrReplaceChild("button_r", CubeListBuilder.create().texOffs(44, 77).addBox(-0.45F, -0.45F, -0.2F, 0.9F, 0.9F, 0.4F), PartPose.offsetAndRotation(-2.4F, 3.5F, -2.85F, 0F, 0F, 0F));
        PartDefinition p_skirt_front = p_body.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(31, 36).addBox(-4.4F, 0F, -0.5F, 8.8F, 5.2F, 1F).texOffs(12, 73).addBox(-4.25F, 4.8F, -0.25F, 8.5F, 0.9F, 0.7F), PartPose.offsetAndRotation(0F, 8.4F, -2.3F, -0.3F, 0F, 0F));
        PartDefinition p_skirt_back = p_body.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(0, 44).addBox(-4.4F, 0F, -0.5F, 8.8F, 5.2F, 1F).texOffs(32, 73).addBox(-4.25F, 4.8F, -0.25F, 8.5F, 0.9F, 0.7F), PartPose.offsetAndRotation(0F, 8.4F, 2.3F, -0.3F, 3.1416F, 0F));
        PartDefinition p_skirt_right = p_body.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(34, 52).addBox(-2.5F, 0F, -0.5F, 5F, 5.2F, 1F).texOffs(52, 73).addBox(-2.35F, 4.8F, -0.25F, 4.7F, 0.9F, 0.7F), PartPose.offsetAndRotation(-4.35F, 8.4F, 0F, -0.3F, 1.5708F, 0F));
        PartDefinition p_skirt_left = p_body.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(47, 52).addBox(-2.5F, 0F, -0.5F, 5F, 5.2F, 1F).texOffs(0, 77).addBox(-2.35F, 4.8F, -0.25F, 4.7F, 0.9F, 0.7F), PartPose.offsetAndRotation(4.35F, 8.4F, 0F, -0.3F, -1.5708F, 0F));
        PartDefinition p_skirt_fr = p_body.addOrReplaceChild("skirt_fr", CubeListBuilder.create().texOffs(0, 60).addBox(-1.55F, 0F, -0.5F, 3.1F, 5.2F, 1F).texOffs(12, 77).addBox(-1.4F, 4.8F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.4F, -1.85F, -0.3F, 0.7854F, 0F));
        PartDefinition p_skirt_fl = p_body.addOrReplaceChild("skirt_fl", CubeListBuilder.create().texOffs(10, 60).addBox(-1.55F, 0F, -0.5F, 3.1F, 5.2F, 1F).texOffs(20, 77).addBox(-1.4F, 4.8F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.4F, -1.85F, -0.3F, -0.7854F, 0F));
        PartDefinition p_skirt_br = p_body.addOrReplaceChild("skirt_br", CubeListBuilder.create().texOffs(20, 60).addBox(-1.55F, 0F, -0.5F, 3.1F, 5.2F, 1F).texOffs(28, 77).addBox(-1.4F, 4.8F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.4F, 1.85F, -0.3F, 2.3562F, 0F));
        PartDefinition p_skirt_bl = p_body.addOrReplaceChild("skirt_bl", CubeListBuilder.create().texOffs(30, 60).addBox(-1.55F, 0F, -0.5F, 3.1F, 5.2F, 1F).texOffs(36, 77).addBox(-1.4F, 4.8F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.4F, 1.85F, -0.3F, -2.3562F, 0F));
        PartDefinition p_toolbelt = p_body.addOrReplaceChild("toolbelt", CubeListBuilder.create().texOffs(0, 36).addBox(-4.7F, 7.6F, -2.75F, 9.4F, 1.3F, 5.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_hammer = p_body.addOrReplaceChild("hammer", CubeListBuilder.create().texOffs(58, 60).addBox(-0.3F, 0F, -0.3F, 0.6F, 3.8F, 0.6F).texOffs(17, 68).addBox(-0.7F, 3.6F, -1.2F, 1.4F, 1.1F, 2.4F), PartPose.offsetAndRotation(4.9F, 8.2F, -0.6F, 0F, 0F, -0.15F));
        PartDefinition p_pouch_r = p_body.addOrReplaceChild("pouch_r", CubeListBuilder.create().texOffs(26, 68).addBox(-1.1F, 0F, -1F, 2.2F, 2.4F, 1.2F), PartPose.offsetAndRotation(-3.4F, 8.3F, -2.7F, 0F, 0.2F, 0F));
        PartDefinition p_ruler = p_body.addOrReplaceChild("ruler", CubeListBuilder.create().texOffs(54, 60).addBox(-0.25F, 0F, -0.5F, 0.5F, 4.2F, 1F), PartPose.offsetAndRotation(-4.85F, 7.9F, 0.9F, 0F, 0F, 0.1F));
        PartDefinition p_sleeve_r = p_right_arm.addOrReplaceChild("sleeve_r", CubeListBuilder.create().texOffs(29, 26).addBox(-2.35F, -2.4F, -2.35F, 3.7F, 4.3F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_roll_r = p_right_arm.addOrReplaceChild("roll_r", CubeListBuilder.create().texOffs(21, 44).addBox(-2.5F, 1.6F, -2.5F, 4F, 1.3F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_glove_r = p_right_arm.addOrReplaceChild("glove_r", CubeListBuilder.create().texOffs(0, 52).addBox(-2.2F, 7.6F, -2.2F, 3.4F, 2.6F, 4.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_r = p_right_leg.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(27, 0).addBox(-2.3F, 6.6F, -2.3F, 4.6F, 5.6F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_toe_r = p_right_leg.addOrReplaceChild("toe_r", CubeListBuilder.create().texOffs(42, 68).addBox(-2.3F, 10.2F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_strap_l = p_body.addOrReplaceChild("strap_l", CubeListBuilder.create().texOffs(34, 14).addBox(-0.6F, -0.4F, -2.6F, 1.2F, 4.2F, 5.2F), PartPose.offsetAndRotation(2.4F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_button_l = p_body.addOrReplaceChild("button_l", CubeListBuilder.create().texOffs(48, 77).addBox(-0.45F, -0.45F, -0.2F, 0.9F, 0.9F, 0.4F), PartPose.offsetAndRotation(2.4F, 3.5F, -2.85F, 0F, 0F, 0F));
        PartDefinition p_pouch_l = p_body.addOrReplaceChild("pouch_l", CubeListBuilder.create().texOffs(34, 68).addBox(-1.1F, 0F, -1F, 2.2F, 2.4F, 1.2F), PartPose.offsetAndRotation(3.4F, 8.3F, -2.7F, 0F, -0.2F, 0F));
        PartDefinition p_sleeve_l = p_left_arm.addOrReplaceChild("sleeve_l", CubeListBuilder.create().texOffs(47, 26).addBox(-1.35F, -2.4F, -2.35F, 3.7F, 4.3F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_roll_l = p_left_arm.addOrReplaceChild("roll_l", CubeListBuilder.create().texOffs(40, 44).addBox(-1.5F, 1.6F, -2.5F, 4F, 1.3F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_glove_l = p_left_arm.addOrReplaceChild("glove_l", CubeListBuilder.create().texOffs(17, 52).addBox(-1.2F, 7.6F, -2.2F, 3.4F, 2.6F, 4.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_l = p_left_leg.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(0, 14).addBox(-2.3F, 6.6F, -2.3F, 4.6F, 5.6F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_toe_l = p_left_leg.addOrReplaceChild("toe_l", CubeListBuilder.create().texOffs(0, 73).addBox(-2.3F, 10.2F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeOutfitKozue(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("shirt", m.get("body").getChild("shirt"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("hips", m.get("body").getChild("hips"));
        m.put("bib", m.get("body").getChild("bib"));
        m.put("strap_r", m.get("body").getChild("strap_r"));
        m.put("button_r", m.get("body").getChild("button_r"));
        m.put("skirt_front", m.get("body").getChild("skirt_front"));
        m.put("skirt_back", m.get("body").getChild("skirt_back"));
        m.put("skirt_right", m.get("body").getChild("skirt_right"));
        m.put("skirt_left", m.get("body").getChild("skirt_left"));
        m.put("skirt_fr", m.get("body").getChild("skirt_fr"));
        m.put("skirt_fl", m.get("body").getChild("skirt_fl"));
        m.put("skirt_br", m.get("body").getChild("skirt_br"));
        m.put("skirt_bl", m.get("body").getChild("skirt_bl"));
        m.put("toolbelt", m.get("body").getChild("toolbelt"));
        m.put("hammer", m.get("body").getChild("hammer"));
        m.put("pouch_r", m.get("body").getChild("pouch_r"));
        m.put("ruler", m.get("body").getChild("ruler"));
        m.put("sleeve_r", m.get("right_arm").getChild("sleeve_r"));
        m.put("roll_r", m.get("right_arm").getChild("roll_r"));
        m.put("glove_r", m.get("right_arm").getChild("glove_r"));
        m.put("boot_r", m.get("right_leg").getChild("boot_r"));
        m.put("toe_r", m.get("right_leg").getChild("toe_r"));
        m.put("strap_l", m.get("body").getChild("strap_l"));
        m.put("button_l", m.get("body").getChild("button_l"));
        m.put("pouch_l", m.get("body").getChild("pouch_l"));
        m.put("sleeve_l", m.get("left_arm").getChild("sleeve_l"));
        m.put("roll_l", m.get("left_arm").getChild("roll_l"));
        m.put("glove_l", m.get("left_arm").getChild("glove_l"));
        m.put("boot_l", m.get("left_leg").getChild("boot_l"));
        m.put("toe_l", m.get("left_leg").getChild("toe_l"));
        return m;
    }

    public static LayerDefinition headHomura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_clip_r = p_head.addOrReplaceChild("clip_r", CubeListBuilder.create().texOffs(18, 0).addBox(-0.8F, -1.8F, -0.4F, 1.6F, 2.4F, 0.8F).texOffs(30, 0).addBox(-0.4F, -2.6F, -0.3F, 0.8F, 1F, 0.6F), PartPose.offsetAndRotation(-3.4F, -7.4F, -2.4F, 0F, 0F, -0.35F));
        PartDefinition p_tie_r = p_head.addOrReplaceChild("tie_r", CubeListBuilder.create().texOffs(0, 0).addBox(-0.9F, -0.9F, -0.9F, 1.8F, 1.8F, 1.8F), PartPose.offsetAndRotation(-3.6F, -6F, 3.3F, 0F, 0F, 0F));
        PartDefinition p_clip_l = p_head.addOrReplaceChild("clip_l", CubeListBuilder.create().texOffs(24, 0).addBox(-0.8F, -1.8F, -0.4F, 1.6F, 2.4F, 0.8F).texOffs(34, 0).addBox(-0.4F, -2.6F, -0.3F, 0.8F, 1F, 0.6F), PartPose.offsetAndRotation(3.4F, -7.4F, -2.4F, 0F, 0F, 0.35F));
        PartDefinition p_tie_l = p_head.addOrReplaceChild("tie_l", CubeListBuilder.create().texOffs(9, 0).addBox(-0.9F, -0.9F, -0.9F, 1.8F, 1.8F, 1.8F), PartPose.offsetAndRotation(3.6F, -6F, 3.3F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static Map<String, ModelPart> bakeHeadHomura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("clip_r", m.get("head").getChild("clip_r"));
        m.put("tie_r", m.get("head").getChild("tie_r"));
        m.put("clip_l", m.get("head").getChild("clip_l"));
        m.put("tie_l", m.get("head").getChild("tie_l"));
        return m;
    }

    public static LayerDefinition outfitHomura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_dress = p_body.addOrReplaceChild("dress", CubeListBuilder.create().texOffs(0, 0).addBox(-4.25F, -0.3F, -2.25F, 8.5F, 8.6F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(48, 66).addBox(-3.3F, 0F, -0.9F, 6.6F, 3F, 1F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_hips = p_body.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(20, 15).addBox(-4.55F, 8.6F, -2.45F, 9.1F, 3.6F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_collar = p_body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(0, 66).addBox(-4.4F, -0.5F, -2.4F, 8.8F, 1.2F, 4.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_skirt_front = p_body.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(18, 38).addBox(-4.4F, 0F, -0.5F, 8.8F, 7F, 1F).texOffs(24, 73).addBox(-4.25F, 6.6F, -0.25F, 8.5F, 0.9F, 0.7F), PartPose.offsetAndRotation(0F, 8.4F, -2.3F, -0.24F, 0F, 0F));
        PartDefinition p_skirt_back = p_body.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(39, 38).addBox(-4.4F, 0F, -0.5F, 8.8F, 7F, 1F).texOffs(44, 73).addBox(-4.25F, 6.6F, -0.25F, 8.5F, 0.9F, 0.7F), PartPose.offsetAndRotation(0F, 8.4F, 2.3F, -0.24F, 3.1416F, 0F));
        PartDefinition p_skirt_right = p_body.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(0, 48).addBox(-2.5F, 0F, -0.5F, 5F, 7F, 1F).texOffs(0, 77).addBox(-2.35F, 6.6F, -0.25F, 4.7F, 0.9F, 0.7F), PartPose.offsetAndRotation(-4.35F, 8.4F, 0F, -0.24F, 1.5708F, 0F));
        PartDefinition p_skirt_left = p_body.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(13, 48).addBox(-2.5F, 0F, -0.5F, 5F, 7F, 1F).texOffs(12, 77).addBox(-2.35F, 6.6F, -0.25F, 4.7F, 0.9F, 0.7F), PartPose.offsetAndRotation(4.35F, 8.4F, 0F, -0.24F, -1.5708F, 0F));
        PartDefinition p_skirt_fr = p_body.addOrReplaceChild("skirt_fr", CubeListBuilder.create().texOffs(26, 48).addBox(-1.55F, 0F, -0.5F, 3.1F, 7F, 1F).texOffs(24, 77).addBox(-1.4F, 6.6F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.4F, -1.85F, -0.24F, 0.7854F, 0F));
        PartDefinition p_skirt_fl = p_body.addOrReplaceChild("skirt_fl", CubeListBuilder.create().texOffs(36, 48).addBox(-1.55F, 0F, -0.5F, 3.1F, 7F, 1F).texOffs(32, 77).addBox(-1.4F, 6.6F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.4F, -1.85F, -0.24F, -0.7854F, 0F));
        PartDefinition p_skirt_br = p_body.addOrReplaceChild("skirt_br", CubeListBuilder.create().texOffs(46, 48).addBox(-1.55F, 0F, -0.5F, 3.1F, 7F, 1F).texOffs(40, 77).addBox(-1.4F, 6.6F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.4F, 1.85F, -0.24F, 2.3562F, 0F));
        PartDefinition p_skirt_bl = p_body.addOrReplaceChild("skirt_bl", CubeListBuilder.create().texOffs(0, 57).addBox(-1.55F, 0F, -0.5F, 3.1F, 7F, 1F).texOffs(48, 77).addBox(-1.4F, 6.6F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.4F, 1.85F, -0.24F, -2.3562F, 0F));
        PartDefinition p_apron_top = p_body.addOrReplaceChild("apron_top", CubeListBuilder.create().texOffs(29, 66).addBox(-2.8F, 4.6F, -2.75F, 5.6F, 4F, 0.35F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_apron = p_skirt_front.addOrReplaceChild("apron", CubeListBuilder.create().texOffs(40, 57).addBox(-3F, 0.1F, -0.4F, 6F, 6F, 0.4F), PartPose.offsetAndRotation(0F, 0F, -0.55F, 0F, 0F, 0F));
        PartDefinition p_apron_strap = p_body.addOrReplaceChild("apron_strap", CubeListBuilder.create().texOffs(10, 57).addBox(-4.45F, 7.6F, -2.6F, 8.9F, 0.9F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_tongs = p_body.addOrReplaceChild("tongs", CubeListBuilder.create().texOffs(42, 66).addBox(-0.25F, 0F, -0.5F, 0.5F, 4.4F, 0.4F).texOffs(45, 66).addBox(-0.25F, 0F, 0.15F, 0.5F, 4.4F, 0.4F), PartPose.offsetAndRotation(4.9F, 8F, 0.2F, 0F, 0F, -0.12F));
        PartDefinition p_sleeve_r = p_right_arm.addOrReplaceChild("sleeve_r", CubeListBuilder.create().texOffs(0, 28).addBox(-2.5F, -2.45F, -2.5F, 4F, 4F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_glove_r = p_right_arm.addOrReplaceChild("glove_r", CubeListBuilder.create().texOffs(38, 28).addBox(-2.3F, 6.4F, -2.3F, 3.6F, 3.8F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_r = p_right_leg.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(27, 0).addBox(-2.3F, 5.4F, -2.3F, 4.6F, 6.8F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_toe_r = p_right_leg.addOrReplaceChild("toe_r", CubeListBuilder.create().texOffs(0, 73).addBox(-2.3F, 10.2F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_sleeve_l = p_left_arm.addOrReplaceChild("sleeve_l", CubeListBuilder.create().texOffs(19, 28).addBox(-1.5F, -2.45F, -2.5F, 4F, 4F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_glove_l = p_left_arm.addOrReplaceChild("glove_l", CubeListBuilder.create().texOffs(0, 38).addBox(-1.3F, 6.4F, -2.3F, 3.6F, 3.8F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_l = p_left_leg.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(0, 15).addBox(-2.3F, 5.4F, -2.3F, 4.6F, 6.8F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_toe_l = p_left_leg.addOrReplaceChild("toe_l", CubeListBuilder.create().texOffs(12, 73).addBox(-2.3F, 10.2F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeOutfitHomura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("dress", m.get("body").getChild("dress"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("hips", m.get("body").getChild("hips"));
        m.put("collar", m.get("body").getChild("collar"));
        m.put("skirt_front", m.get("body").getChild("skirt_front"));
        m.put("skirt_back", m.get("body").getChild("skirt_back"));
        m.put("skirt_right", m.get("body").getChild("skirt_right"));
        m.put("skirt_left", m.get("body").getChild("skirt_left"));
        m.put("skirt_fr", m.get("body").getChild("skirt_fr"));
        m.put("skirt_fl", m.get("body").getChild("skirt_fl"));
        m.put("skirt_br", m.get("body").getChild("skirt_br"));
        m.put("skirt_bl", m.get("body").getChild("skirt_bl"));
        m.put("apron_top", m.get("body").getChild("apron_top"));
        m.put("apron", m.get("skirt_front").getChild("apron"));
        m.put("apron_strap", m.get("body").getChild("apron_strap"));
        m.put("tongs", m.get("body").getChild("tongs"));
        m.put("sleeve_r", m.get("right_arm").getChild("sleeve_r"));
        m.put("glove_r", m.get("right_arm").getChild("glove_r"));
        m.put("boot_r", m.get("right_leg").getChild("boot_r"));
        m.put("toe_r", m.get("right_leg").getChild("toe_r"));
        m.put("sleeve_l", m.get("left_arm").getChild("sleeve_l"));
        m.put("glove_l", m.get("left_arm").getChild("glove_l"));
        m.put("boot_l", m.get("left_leg").getChild("boot_l"));
        m.put("toe_l", m.get("left_leg").getChild("toe_l"));
        return m;
    }

    public static LayerDefinition headKura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_headdress = p_head.addOrReplaceChild("headdress", CubeListBuilder.create().texOffs(24, 0).addBox(-3.8F, -1.6F, -0.4F, 7.6F, 1.8F, 0.8F), PartPose.offsetAndRotation(0F, -8.1F, -1.4F, -0.35F, 0F, 0F));
        PartDefinition p_headband = p_head.addOrReplaceChild("headband", CubeListBuilder.create().texOffs(0, 0).addBox(-4.55F, -0.5F, -1.2F, 9.1F, 0.9F, 2.4F), PartPose.offsetAndRotation(0F, -7.6F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static Map<String, ModelPart> bakeHeadKura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("headdress", m.get("head").getChild("headdress"));
        m.put("headband", m.get("head").getChild("headband"));
        return m;
    }

    public static LayerDefinition outfitKura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_dress = p_body.addOrReplaceChild("dress", CubeListBuilder.create().texOffs(0, 0).addBox(-4.25F, -0.3F, -2.25F, 8.5F, 8.6F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(14, 77).addBox(-3.3F, 0F, -0.9F, 6.6F, 3F, 1F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_hips = p_body.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(0, 26).addBox(-4.55F, 8.6F, -2.45F, 9.1F, 3.6F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_collar = p_body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(0, 63).addBox(-4.3F, -0.4F, -2.35F, 8.6F, 1.1F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_latch = p_body.addOrReplaceChild("latch", CubeListBuilder.create().texOffs(31, 77).addBox(-0.9F, -0.4F, -0.45F, 1.8F, 2.2F, 0.6F).texOffs(32, 87).addBox(-0.35F, 0.4F, -0.6F, 0.7F, 0.7F, 0.2F), PartPose.offsetAndRotation(0F, 1.4F, -2.7F, 0F, 0F, 0F));
        PartDefinition p_skirt_front = p_body.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(29, 26).addBox(-4.4F, 0F, -0.5F, 8.8F, 6.6F, 1F).texOffs(37, 77).addBox(-4.25F, 6.2F, -0.25F, 8.5F, 0.9F, 0.7F), PartPose.offsetAndRotation(0F, 8.4F, -2.3F, -0.27F, 0F, 0F));
        PartDefinition p_skirt_back = p_body.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(0, 36).addBox(-4.4F, 0F, -0.5F, 8.8F, 6.6F, 1F).texOffs(0, 84).addBox(-4.25F, 6.2F, -0.25F, 8.5F, 0.9F, 0.7F), PartPose.offsetAndRotation(0F, 8.4F, 2.3F, -0.27F, 3.1416F, 0F));
        PartDefinition p_skirt_right = p_body.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(0, 45).addBox(-2.5F, 0F, -0.5F, 5F, 6.6F, 1F).texOffs(36, 84).addBox(-2.35F, 6.2F, -0.25F, 4.7F, 0.9F, 0.7F), PartPose.offsetAndRotation(-4.35F, 8.4F, 0F, -0.27F, 1.5708F, 0F));
        PartDefinition p_skirt_left = p_body.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(13, 45).addBox(-2.5F, 0F, -0.5F, 5F, 6.6F, 1F).texOffs(48, 84).addBox(-2.35F, 6.2F, -0.25F, 4.7F, 0.9F, 0.7F), PartPose.offsetAndRotation(4.35F, 8.4F, 0F, -0.27F, -1.5708F, 0F));
        PartDefinition p_skirt_fr = p_body.addOrReplaceChild("skirt_fr", CubeListBuilder.create().texOffs(26, 45).addBox(-1.55F, 0F, -0.5F, 3.1F, 6.6F, 1F).texOffs(0, 87).addBox(-1.4F, 6.2F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.4F, -1.85F, -0.27F, 0.7854F, 0F));
        PartDefinition p_skirt_fl = p_body.addOrReplaceChild("skirt_fl", CubeListBuilder.create().texOffs(36, 45).addBox(-1.55F, 0F, -0.5F, 3.1F, 6.6F, 1F).texOffs(8, 87).addBox(-1.4F, 6.2F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.4F, -1.85F, -0.27F, -0.7854F, 0F));
        PartDefinition p_skirt_br = p_body.addOrReplaceChild("skirt_br", CubeListBuilder.create().texOffs(46, 45).addBox(-1.55F, 0F, -0.5F, 3.1F, 6.6F, 1F).texOffs(16, 87).addBox(-1.4F, 6.2F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.4F, 1.85F, -0.27F, 2.3562F, 0F));
        PartDefinition p_skirt_bl = p_body.addOrReplaceChild("skirt_bl", CubeListBuilder.create().texOffs(0, 54).addBox(-1.55F, 0F, -0.5F, 3.1F, 6.6F, 1F).texOffs(24, 87).addBox(-1.4F, 6.2F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.4F, 1.85F, -0.27F, -2.3562F, 0F));
        PartDefinition p_apron = p_skirt_front.addOrReplaceChild("apron", CubeListBuilder.create().texOffs(0, 77).addBox(-3F, 0.2F, -0.4F, 6F, 5.6F, 0.4F).texOffs(20, 84).addBox(-3.3F, 5.6F, -0.45F, 6.6F, 0.9F, 0.5F), PartPose.offsetAndRotation(0F, 0F, -0.55F, 0F, 0F, 0F));
        PartDefinition p_sash = p_body.addOrReplaceChild("sash", CubeListBuilder.create().texOffs(10, 54).addBox(-4.4F, 7.4F, -2.4F, 8.8F, 1F, 4.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_backpack = p_body.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(0, 15).addBox(-3.4F, 0F, 0F, 6.8F, 6.2F, 3.2F), PartPose.offsetAndRotation(0F, 1.2F, 2.3F, 0.05F, 0F, 0F));
        PartDefinition p_pack_strap_r = p_body.addOrReplaceChild("pack_strap_r", CubeListBuilder.create().texOffs(27, 0).addBox(-0.5F, -0.45F, -2.45F, 1F, 5.4F, 4.9F), PartPose.offsetAndRotation(-2.3F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_puff_r = p_right_arm.addOrReplaceChild("puff_r", CubeListBuilder.create().texOffs(21, 15).addBox(-2.55F, -2.45F, -2.55F, 4.1F, 4.1F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cuff_r = p_right_arm.addOrReplaceChild("cuff_r", CubeListBuilder.create().texOffs(19, 70).addBox(-2.25F, 7.4F, -2.25F, 3.5F, 1.3F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_frill_r = p_right_leg.addOrReplaceChild("frill_r", CubeListBuilder.create().texOffs(28, 63).addBox(-2.25F, 4.6F, -2.25F, 4.5F, 0.9F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_shoe_r = p_right_leg.addOrReplaceChild("shoe_r", CubeListBuilder.create().texOffs(21, 36).addBox(-2.2F, 9.9F, -2.75F, 4.4F, 2.15F, 4.95F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_pack_strap_l = p_body.addOrReplaceChild("pack_strap_l", CubeListBuilder.create().texOffs(40, 0).addBox(-0.5F, -0.45F, -2.45F, 1F, 5.4F, 4.9F), PartPose.offsetAndRotation(2.3F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_puff_l = p_left_arm.addOrReplaceChild("puff_l", CubeListBuilder.create().texOffs(41, 15).addBox(-1.55F, -2.45F, -2.55F, 4.1F, 4.1F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cuff_l = p_left_arm.addOrReplaceChild("cuff_l", CubeListBuilder.create().texOffs(36, 70).addBox(-1.25F, 7.4F, -2.25F, 3.5F, 1.3F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_frill_l = p_left_leg.addOrReplaceChild("frill_l", CubeListBuilder.create().texOffs(0, 70).addBox(-2.25F, 4.6F, -2.25F, 4.5F, 0.9F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_shoe_l = p_left_leg.addOrReplaceChild("shoe_l", CubeListBuilder.create().texOffs(41, 36).addBox(-2.2F, 9.9F, -2.75F, 4.4F, 2.15F, 4.95F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeOutfitKura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("dress", m.get("body").getChild("dress"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("hips", m.get("body").getChild("hips"));
        m.put("collar", m.get("body").getChild("collar"));
        m.put("latch", m.get("body").getChild("latch"));
        m.put("skirt_front", m.get("body").getChild("skirt_front"));
        m.put("skirt_back", m.get("body").getChild("skirt_back"));
        m.put("skirt_right", m.get("body").getChild("skirt_right"));
        m.put("skirt_left", m.get("body").getChild("skirt_left"));
        m.put("skirt_fr", m.get("body").getChild("skirt_fr"));
        m.put("skirt_fl", m.get("body").getChild("skirt_fl"));
        m.put("skirt_br", m.get("body").getChild("skirt_br"));
        m.put("skirt_bl", m.get("body").getChild("skirt_bl"));
        m.put("apron", m.get("skirt_front").getChild("apron"));
        m.put("sash", m.get("body").getChild("sash"));
        m.put("backpack", m.get("body").getChild("backpack"));
        m.put("pack_strap_r", m.get("body").getChild("pack_strap_r"));
        m.put("puff_r", m.get("right_arm").getChild("puff_r"));
        m.put("cuff_r", m.get("right_arm").getChild("cuff_r"));
        m.put("frill_r", m.get("right_leg").getChild("frill_r"));
        m.put("shoe_r", m.get("right_leg").getChild("shoe_r"));
        m.put("pack_strap_l", m.get("body").getChild("pack_strap_l"));
        m.put("puff_l", m.get("left_arm").getChild("puff_l"));
        m.put("cuff_l", m.get("left_arm").getChild("cuff_l"));
        m.put("frill_l", m.get("left_leg").getChild("frill_l"));
        m.put("shoe_l", m.get("left_leg").getChild("shoe_l"));
        return m;
    }

    public static LayerDefinition headShiori() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_circlet = p_head.addOrReplaceChild("circlet", CubeListBuilder.create().texOffs(0, 0).addBox(-4.55F, -0.45F, -4.55F, 9.1F, 0.8F, 9.1F), PartPose.offsetAndRotation(0F, -6.9F, 0F, 0F, 0F, 0F));
        PartDefinition p_tiara_gem = p_head.addOrReplaceChild("tiara_gem", CubeListBuilder.create().texOffs(38, 0).addBox(-0.8F, -0.8F, -0.35F, 1.6F, 1.6F, 0.6F), PartPose.offsetAndRotation(0F, -7.1F, -4.75F, 0F, 0F, 0.78F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static Map<String, ModelPart> bakeHeadShiori(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("circlet", m.get("head").getChild("circlet"));
        m.put("tiara_gem", m.get("head").getChild("tiara_gem"));
        return m;
    }

    public static LayerDefinition outfitShiori() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_robe = p_body.addOrReplaceChild("robe", CubeListBuilder.create().texOffs(21, 0).addBox(-4.25F, -0.3F, -2.25F, 8.5F, 8.6F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(29, 75).addBox(-3.3F, 0F, -0.9F, 6.6F, 3F, 1F), PartPose.offsetAndRotation(0F, 2.9F, -2.2F, -0.3F, 0F, 0F));
        PartDefinition p_hips = p_body.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(10, 55).addBox(-4.55F, 8.6F, -2.45F, 9.1F, 3.6F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_collar = p_body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(23, 66).addBox(-4.4F, -0.6F, -2.45F, 8.8F, 1.4F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_brooch = p_body.addOrReplaceChild("brooch", CubeListBuilder.create().texOffs(44, 86).addBox(-0.7F, -0.7F, -0.4F, 1.4F, 1.4F, 0.6F), PartPose.offsetAndRotation(0F, 1F, -2.75F, 0F, 0F, 0.78F));
        PartDefinition p_sash = p_body.addOrReplaceChild("sash", CubeListBuilder.create().texOffs(0, 75).addBox(-4.4F, 7.2F, -2.4F, 8.8F, 1.2F, 4.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_skirt_front = p_body.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(20, 32).addBox(-4.3F, 0F, -0.5F, 8.6F, 8.6F, 1F).texOffs(12, 82).addBox(-4.15F, 8.2F, -0.25F, 8.3F, 0.9F, 0.7F), PartPose.offsetAndRotation(0F, 8.4F, -2.3F, -0.2F, 0F, 0F));
        PartDefinition p_skirt_back = p_body.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(41, 32).addBox(-4.3F, 0F, -0.5F, 8.6F, 8.6F, 1F).texOffs(31, 82).addBox(-4.15F, 8.2F, -0.25F, 8.3F, 0.9F, 0.7F), PartPose.offsetAndRotation(0F, 8.4F, 2.3F, -0.2F, 3.1416F, 0F));
        PartDefinition p_skirt_right = p_body.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(0, 44).addBox(-2.5F, 0F, -0.5F, 5F, 8.6F, 1F).texOffs(50, 82).addBox(-2.35F, 8.2F, -0.25F, 4.7F, 0.9F, 0.7F), PartPose.offsetAndRotation(-4.35F, 8.4F, 0F, -0.2F, 1.5708F, 0F));
        PartDefinition p_skirt_left = p_body.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(13, 44).addBox(-2.5F, 0F, -0.5F, 5F, 8.6F, 1F).texOffs(0, 86).addBox(-2.35F, 8.2F, -0.25F, 4.7F, 0.9F, 0.7F), PartPose.offsetAndRotation(4.35F, 8.4F, 0F, -0.2F, -1.5708F, 0F));
        PartDefinition p_skirt_fr = p_body.addOrReplaceChild("skirt_fr", CubeListBuilder.create().texOffs(26, 44).addBox(-1.55F, 0F, -0.5F, 3.1F, 8.6F, 1F).texOffs(12, 86).addBox(-1.4F, 8.2F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.4F, -1.85F, -0.2F, 0.7854F, 0F));
        PartDefinition p_skirt_fl = p_body.addOrReplaceChild("skirt_fl", CubeListBuilder.create().texOffs(36, 44).addBox(-1.55F, 0F, -0.5F, 3.1F, 8.6F, 1F).texOffs(20, 86).addBox(-1.4F, 8.2F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.4F, -1.85F, -0.2F, -0.7854F, 0F));
        PartDefinition p_skirt_br = p_body.addOrReplaceChild("skirt_br", CubeListBuilder.create().texOffs(46, 44).addBox(-1.55F, 0F, -0.5F, 3.1F, 8.6F, 1F).texOffs(28, 86).addBox(-1.4F, 8.2F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(-3.7F, 8.4F, 1.85F, -0.2F, 2.3562F, 0F));
        PartDefinition p_skirt_bl = p_body.addOrReplaceChild("skirt_bl", CubeListBuilder.create().texOffs(0, 55).addBox(-1.55F, 0F, -0.5F, 3.1F, 8.6F, 1F).texOffs(36, 86).addBox(-1.4F, 8.2F, -0.25F, 2.8F, 0.9F, 0.7F), PartPose.offsetAndRotation(3.7F, 8.4F, 1.85F, -0.2F, -2.3562F, 0F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6F, 0F, 0F, 9.2F, 15.5F, 0.5F), PartPose.offsetAndRotation(0F, -0.2F, 2.45F, 0.12F, 0F, 0F));
        PartDefinition p_sleeve_r = p_right_arm.addOrReplaceChild("sleeve_r", CubeListBuilder.create().texOffs(0, 17).addBox(-2.55F, -2.45F, -2.55F, 4.1F, 8.4F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bell_r = p_right_arm.addOrReplaceChild("bell_r", CubeListBuilder.create().texOffs(39, 55).addBox(-2.9F, 5.6F, -2.9F, 4.8F, 1.6F, 5.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_r = p_right_leg.addOrReplaceChild("boot_r", CubeListBuilder.create().texOffs(40, 17).addBox(-2.3F, 6F, -2.3F, 4.6F, 6.2F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_toe_r = p_right_leg.addOrReplaceChild("toe_r", CubeListBuilder.create().texOffs(46, 75).addBox(-2.3F, 10.2F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_sleeve_l = p_left_arm.addOrReplaceChild("sleeve_l", CubeListBuilder.create().texOffs(20, 17).addBox(-1.55F, -2.45F, -2.55F, 4.1F, 8.4F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bell_l = p_left_arm.addOrReplaceChild("bell_l", CubeListBuilder.create().texOffs(0, 66).addBox(-1.9F, 5.6F, -2.9F, 4.8F, 1.6F, 5.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_boot_l = p_left_leg.addOrReplaceChild("boot_l", CubeListBuilder.create().texOffs(0, 32).addBox(-2.3F, 6F, -2.3F, 4.6F, 6.2F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_toe_l = p_left_leg.addOrReplaceChild("toe_l", CubeListBuilder.create().texOffs(0, 82).addBox(-2.3F, 10.2F, -3.05F, 4.6F, 2F, 0.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeOutfitShiori(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("robe", m.get("body").getChild("robe"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("hips", m.get("body").getChild("hips"));
        m.put("collar", m.get("body").getChild("collar"));
        m.put("brooch", m.get("body").getChild("brooch"));
        m.put("sash", m.get("body").getChild("sash"));
        m.put("skirt_front", m.get("body").getChild("skirt_front"));
        m.put("skirt_back", m.get("body").getChild("skirt_back"));
        m.put("skirt_right", m.get("body").getChild("skirt_right"));
        m.put("skirt_left", m.get("body").getChild("skirt_left"));
        m.put("skirt_fr", m.get("body").getChild("skirt_fr"));
        m.put("skirt_fl", m.get("body").getChild("skirt_fl"));
        m.put("skirt_br", m.get("body").getChild("skirt_br"));
        m.put("skirt_bl", m.get("body").getChild("skirt_bl"));
        m.put("cape", m.get("body").getChild("cape"));
        m.put("sleeve_r", m.get("right_arm").getChild("sleeve_r"));
        m.put("bell_r", m.get("right_arm").getChild("bell_r"));
        m.put("boot_r", m.get("right_leg").getChild("boot_r"));
        m.put("toe_r", m.get("right_leg").getChild("toe_r"));
        m.put("sleeve_l", m.get("left_arm").getChild("sleeve_l"));
        m.put("bell_l", m.get("left_arm").getChild("bell_l"));
        m.put("boot_l", m.get("left_leg").getChild("boot_l"));
        m.put("toe_l", m.get("left_leg").getChild("toe_l"));
        return m;
    }

}
