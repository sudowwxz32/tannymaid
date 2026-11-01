// GENERADO por tools/gen_tanny.py — no editar a mano.
package com.tanny.tannymaid.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TannyGeneratedModels {
    private TannyGeneratedModels() {}

    public static final float BANG_TILT = -0.14F;
    public static final float SKIRT_FLARE = 0.24F;

    public static LayerDefinition createHair() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_headband = p_head.addOrReplaceChild("headband", CubeListBuilder.create().texOffs(0, 0).addBox(-5.1F, -0.6F, -0.75F, 10.2F, 1.1F, 1.5F), PartPose.offsetAndRotation(0F, -8.65F, -1.6F, -0.3F, 0F, 0F));
        PartDefinition p_ruffle_0 = p_headband.addOrReplaceChild("ruffle_0", CubeListBuilder.create().texOffs(25, 0).addBox(-0.72F, -1.6F, -0.3F, 1.44F, 1.6F, 0.55F), PartPose.offsetAndRotation(-3.9F, -0.55F, 0F, 0F, 0F, -0.24F));
        PartDefinition p_ruffle_1 = p_headband.addOrReplaceChild("ruffle_1", CubeListBuilder.create().texOffs(30, 0).addBox(-0.72F, -1.6F, -0.3F, 1.44F, 1.6F, 0.55F), PartPose.offsetAndRotation(-2.6F, -0.55F, 0F, 0F, 0F, -0.16F));
        PartDefinition p_ruffle_2 = p_headband.addOrReplaceChild("ruffle_2", CubeListBuilder.create().texOffs(35, 0).addBox(-0.72F, -1.6F, -0.3F, 1.44F, 1.6F, 0.55F), PartPose.offsetAndRotation(-1.3F, -0.55F, 0F, 0F, 0F, -0.08F));
        PartDefinition p_ruffle_3 = p_headband.addOrReplaceChild("ruffle_3", CubeListBuilder.create().texOffs(40, 0).addBox(-0.72F, -1.6F, -0.3F, 1.44F, 1.6F, 0.55F), PartPose.offsetAndRotation(0F, -0.55F, 0F, 0F, 0F, 0F));
        PartDefinition p_ruffle_4 = p_headband.addOrReplaceChild("ruffle_4", CubeListBuilder.create().texOffs(45, 0).addBox(-0.72F, -1.6F, -0.3F, 1.44F, 1.6F, 0.55F), PartPose.offsetAndRotation(1.3F, -0.55F, 0F, 0F, 0F, 0.08F));
        PartDefinition p_ruffle_5 = p_headband.addOrReplaceChild("ruffle_5", CubeListBuilder.create().texOffs(50, 0).addBox(-0.72F, -1.6F, -0.3F, 1.44F, 1.6F, 0.55F), PartPose.offsetAndRotation(2.6F, -0.55F, 0F, 0F, 0F, 0.16F));
        PartDefinition p_ruffle_6 = p_headband.addOrReplaceChild("ruffle_6", CubeListBuilder.create().texOffs(55, 0).addBox(-0.72F, -1.6F, -0.3F, 1.44F, 1.6F, 0.55F), PartPose.offsetAndRotation(3.9F, -0.55F, 0F, 0F, 0F, 0.24F));
        PartDefinition p_lid_r = p_head.addOrReplaceChild("lid_r", CubeListBuilder.create().texOffs(5, 4).addBox(-0.68F, -0.21F, -0.26F, 1.36F, 0.42F, 0.34F), PartPose.offsetAndRotation(-1.25F, -4.62F, -4.02F, 0F, 0F, 0.19F));
        PartDefinition p_lidb_r = p_head.addOrReplaceChild("lidb_r", CubeListBuilder.create().texOffs(10, 4).addBox(-0.68F, -0.21F, -0.26F, 1.36F, 0.42F, 0.34F), PartPose.offsetAndRotation(-2.55F, -4.65F, -4.02F, 0F, 0F, -0.15F));
        PartDefinition p_lash_r = p_head.addOrReplaceChild("lash_r", CubeListBuilder.create().texOffs(15, 4).addBox(-0.6F, -0.18F, -0.24F, 1.2F, 0.36F, 0.32F), PartPose.offsetAndRotation(-3.62F, -4.1F, -4.04F, 0F, 0.2F, -0.755F));
        PartDefinition p_hb_bow_r = p_headband.addOrReplaceChild("hb_bow_r", CubeListBuilder.create().texOffs(60, 0).addBox(-1F, -0.5F, -0.3F, 1F, 1F, 0.6F), PartPose.offsetAndRotation(-4.9F, -0.05F, 0.3F, 0F, 0.25F, 0.35F));
        PartDefinition p_lid_l = p_head.addOrReplaceChild("lid_l", CubeListBuilder.create().texOffs(20, 4).addBox(-0.68F, -0.21F, -0.26F, 1.36F, 0.42F, 0.34F), PartPose.offsetAndRotation(1.25F, -4.62F, -4.02F, 0F, 0F, -0.19F));
        PartDefinition p_lidb_l = p_head.addOrReplaceChild("lidb_l", CubeListBuilder.create().texOffs(25, 4).addBox(-0.68F, -0.21F, -0.26F, 1.36F, 0.42F, 0.34F), PartPose.offsetAndRotation(2.55F, -4.65F, -4.02F, 0F, 0F, 0.15F));
        PartDefinition p_lash_l = p_head.addOrReplaceChild("lash_l", CubeListBuilder.create().texOffs(30, 4).addBox(-0.6F, -0.18F, -0.24F, 1.2F, 0.36F, 0.32F), PartPose.offsetAndRotation(3.62F, -4.1F, -4.04F, 0F, -0.2F, 0.755F));
        PartDefinition p_hb_bow_l = p_headband.addOrReplaceChild("hb_bow_l", CubeListBuilder.create().texOffs(0, 4).addBox(0F, -0.5F, -0.3F, 1F, 1F, 0.6F), PartPose.offsetAndRotation(4.9F, -0.05F, 0.3F, 0F, -0.25F, -0.35F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static Map<String, ModelPart> bakeHair(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("headband", m.get("head").getChild("headband"));
        m.put("ruffle_0", m.get("headband").getChild("ruffle_0"));
        m.put("ruffle_1", m.get("headband").getChild("ruffle_1"));
        m.put("ruffle_2", m.get("headband").getChild("ruffle_2"));
        m.put("ruffle_3", m.get("headband").getChild("ruffle_3"));
        m.put("ruffle_4", m.get("headband").getChild("ruffle_4"));
        m.put("ruffle_5", m.get("headband").getChild("ruffle_5"));
        m.put("ruffle_6", m.get("headband").getChild("ruffle_6"));
        m.put("lid_r", m.get("head").getChild("lid_r"));
        m.put("lidb_r", m.get("head").getChild("lidb_r"));
        m.put("lash_r", m.get("head").getChild("lash_r"));
        m.put("hb_bow_r", m.get("headband").getChild("hb_bow_r"));
        m.put("lid_l", m.get("head").getChild("lid_l"));
        m.put("lidb_l", m.get("head").getChild("lidb_l"));
        m.put("lash_l", m.get("head").getChild("lash_l"));
        m.put("hb_bow_l", m.get("headband").getChild("hb_bow_l"));
        return m;
    }

    public static LayerDefinition createYunnCap() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cap = p_head.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 0).addBox(-3.4F, -1F, -1.2F, 6.8F, 2.2F, 2.4F), PartPose.offsetAndRotation(0F, -9F, -1.6F, -0.32F, 0F, 0F));
        PartDefinition p_cap_band = p_head.addOrReplaceChild("cap_band", CubeListBuilder.create().texOffs(20, 0).addBox(-4.9F, -0.4F, -0.6F, 9.8F, 0.8F, 1.2F), PartPose.offsetAndRotation(0F, -8.6F, -1.4F, -0.3F, 0F, 0F));
        PartDefinition p_lid_r = p_head.addOrReplaceChild("lid_r", CubeListBuilder.create().texOffs(43, 0).addBox(-0.63F, -0.17F, -0.24F, 1.26F, 0.34F, 0.3F), PartPose.offsetAndRotation(-1.35F, -4F, -4.02F, 0F, 0F, 0.32F));
        PartDefinition p_lidb_r = p_head.addOrReplaceChild("lidb_r", CubeListBuilder.create().texOffs(53, 0).addBox(-0.55F, -0.17F, -0.24F, 1.1F, 0.34F, 0.3F), PartPose.offsetAndRotation(-2.48F, -4.22F, -4.02F, 0F, 0F, 0.05F));
        PartDefinition p_lash_r = p_head.addOrReplaceChild("lash_r", CubeListBuilder.create().texOffs(57, 0).addBox(-0.45F, -0.15F, -0.22F, 0.9F, 0.3F, 0.3F), PartPose.offsetAndRotation(-3.38F, -4.43F, -4.04F, 0F, 0.2F, 0.44F));
        PartDefinition p_lid_l = p_head.addOrReplaceChild("lid_l", CubeListBuilder.create().texOffs(48, 0).addBox(-0.63F, -0.17F, -0.24F, 1.26F, 0.34F, 0.3F), PartPose.offsetAndRotation(1.35F, -4F, -4.02F, 0F, 0F, -0.32F));
        PartDefinition p_lidb_l = p_head.addOrReplaceChild("lidb_l", CubeListBuilder.create().texOffs(61, 0).addBox(-0.55F, -0.17F, -0.24F, 1.1F, 0.34F, 0.3F), PartPose.offsetAndRotation(2.48F, -4.22F, -4.02F, 0F, 0F, -0.05F));
        PartDefinition p_lash_l = p_head.addOrReplaceChild("lash_l", CubeListBuilder.create().texOffs(0, 6).addBox(-0.45F, -0.15F, -0.22F, 0.9F, 0.3F, 0.3F), PartPose.offsetAndRotation(3.38F, -4.43F, -4.04F, 0F, -0.2F, -0.44F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static Map<String, ModelPart> bakeYunnCap(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("cap", m.get("head").getChild("cap"));
        m.put("cap_band", m.get("head").getChild("cap_band"));
        m.put("lid_r", m.get("head").getChild("lid_r"));
        m.put("lidb_r", m.get("head").getChild("lidb_r"));
        m.put("lash_r", m.get("head").getChild("lash_r"));
        m.put("lid_l", m.get("head").getChild("lid_l"));
        m.put("lidb_l", m.get("head").getChild("lidb_l"));
        m.put("lash_l", m.get("head").getChild("lash_l"));
        return m;
    }

    public static LayerDefinition createYunnOutfit() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2.5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2.5F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_shirt = p_body.addOrReplaceChild("shirt", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -0.25F, -2.3F, 8.6F, 7.7F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(42, 34).addBox(-3.4F, 0F, -0.85F, 6.8F, 2.9F, 0.95F), PartPose.offsetAndRotation(0F, 2.9F, -2.25F, -0.28F, 0F, 0F));
        PartDefinition p_shorts = p_body.addOrReplaceChild("shorts", CubeListBuilder.create().texOffs(28, 0).addBox(-4.35F, 7.2F, -2.35F, 8.7F, 4.9F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_sleeve_r = p_right_arm.addOrReplaceChild("sleeve_r", CubeListBuilder.create().texOffs(40, 14).addBox(-2.35F, -2.35F, -2.35F, 3.7F, 4.2F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_glove_r = p_right_arm.addOrReplaceChild("glove_r", CubeListBuilder.create().texOffs(18, 24).addBox(-2.25F, 6.6F, -2.25F, 3.5F, 3.6F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_short_leg_r = p_right_leg.addOrReplaceChild("short_leg_r", CubeListBuilder.create().texOffs(0, 14).addBox(-2.3F, -0.2F, -2.3F, 4.6F, 3.6F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_shoe_r = p_right_leg.addOrReplaceChild("shoe_r", CubeListBuilder.create().texOffs(0, 34).addBox(-2.25F, 9.5F, -2.85F, 4.5F, 2.6F, 5.05F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_sleeve_l = p_left_arm.addOrReplaceChild("sleeve_l", CubeListBuilder.create().texOffs(0, 24).addBox(-1.35F, -2.35F, -2.35F, 3.7F, 4.2F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_glove_l = p_left_arm.addOrReplaceChild("glove_l", CubeListBuilder.create().texOffs(35, 24).addBox(-1.25F, 6.6F, -2.25F, 3.5F, 3.6F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_short_leg_l = p_left_leg.addOrReplaceChild("short_leg_l", CubeListBuilder.create().texOffs(20, 14).addBox(-2.3F, -0.2F, -2.3F, 4.6F, 3.6F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_shoe_l = p_left_leg.addOrReplaceChild("shoe_l", CubeListBuilder.create().texOffs(21, 34).addBox(-2.25F, 9.5F, -2.85F, 4.5F, 2.6F, 5.05F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static Map<String, ModelPart> bakeYunnOutfit(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("shirt", m.get("body").getChild("shirt"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("shorts", m.get("body").getChild("shorts"));
        m.put("sleeve_r", m.get("right_arm").getChild("sleeve_r"));
        m.put("glove_r", m.get("right_arm").getChild("glove_r"));
        m.put("short_leg_r", m.get("right_leg").getChild("short_leg_r"));
        m.put("shoe_r", m.get("right_leg").getChild("shoe_r"));
        m.put("sleeve_l", m.get("left_arm").getChild("sleeve_l"));
        m.put("glove_l", m.get("left_arm").getChild("glove_l"));
        m.put("short_leg_l", m.get("left_leg").getChild("short_leg_l"));
        m.put("shoe_l", m.get("left_leg").getChild("shoe_l"));
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
        PartDefinition p_bust = p_body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(12, 54).addBox(-3.5F, 0F, -0.9F, 7F, 3F, 1F), PartPose.offsetAndRotation(0F, 3F, -2F, -0.28F, 0F, 0F));
        PartDefinition p_collar = p_body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(21, 31).addBox(-4.25F, -0.4F, -2.3F, 8.5F, 1.1F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_collar_tip_r = p_body.addOrReplaceChild("collar_tip_r", CubeListBuilder.create().texOffs(56, 63).addBox(-2.2F, -0.5F, -0.3F, 2.2F, 1.6F, 0.4F), PartPose.offsetAndRotation(-1F, 0.5F, -2.35F, 0F, 0F, 0.55F));
        PartDefinition p_ribbon_knot = p_body.addOrReplaceChild("ribbon_knot", CubeListBuilder.create().texOffs(7, 66).addBox(-0.55F, -0.55F, -0.5F, 1.1F, 1.1F, 0.8F), PartPose.offsetAndRotation(0F, 1F, -2.55F, 0F, 0F, 0F));
        PartDefinition p_ribbon_r = p_body.addOrReplaceChild("ribbon_r", CubeListBuilder.create().texOffs(29, 54).addBox(-2F, -1F, -0.45F, 2F, 2F, 0.7F), PartPose.offsetAndRotation(-0.4F, 1F, -2.5F, 0F, 0.15F, 0.3F));
        PartDefinition p_ribbon_tail_r = p_body.addOrReplaceChild("ribbon_tail_r", CubeListBuilder.create().texOffs(50, 54).addBox(-0.5F, 0F, -0.3F, 0.9F, 2.4F, 0.5F), PartPose.offsetAndRotation(-0.3F, 1.4F, -2.5F, -0.1F, 0F, 0.25F));
        PartDefinition p_sash = p_body.addOrReplaceChild("sash", CubeListBuilder.create().texOffs(0, 40).addBox(-4.35F, 7.2F, -2.35F, 8.7F, 1.1F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_bow_knot = p_body.addOrReplaceChild("bow_knot", CubeListBuilder.create().texOffs(36, 54).addBox(-1F, -1F, -0.4F, 2F, 2F, 1F), PartPose.offsetAndRotation(0F, 7.7F, 2.5F, 0F, 0F, 0F));
        PartDefinition p_bow_r = p_body.addOrReplaceChild("bow_r", CubeListBuilder.create().texOffs(53, 47).addBox(-4.2F, -1.6F, -0.45F, 4.2F, 3.2F, 0.9F), PartPose.offsetAndRotation(-0.8F, 7.7F, 2.5F, 0F, 0.2F, 0.35F));
        PartDefinition p_bow_tail_r = p_body.addOrReplaceChild("bow_tail_r", CubeListBuilder.create().texOffs(58, 21).addBox(-1F, 0F, -0.35F, 2F, 6.4F, 0.7F), PartPose.offsetAndRotation(-0.7F, 8.2F, 2.7F, 0.15F, 0F, 0.15F));
        PartDefinition p_skirt_front = p_body.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(40, 0).addBox(-4.3F, 0F, -0.5F, 8.6F, 8F, 1F).texOffs(0, 60).addBox(-4.15F, 7.6F, -0.25F, 8.3F, 1F, 0.7F), PartPose.offsetAndRotation(0F, 8F, -2.2F, -0.24F, 0F, 0F));
        PartDefinition p_skirt_back = p_body.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(0, 11).addBox(-4.3F, 0F, -0.5F, 8.6F, 8F, 1F).texOffs(19, 60).addBox(-4.15F, 7.6F, -0.25F, 8.3F, 1F, 0.7F), PartPose.offsetAndRotation(0F, 8F, 2.2F, -0.24F, 3.1416F, 0F));
        PartDefinition p_skirt_right = p_body.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(21, 11).addBox(-2.4F, 0F, -0.5F, 4.8F, 8F, 1F).texOffs(0, 63).addBox(-2.25F, 7.6F, -0.25F, 4.5F, 1F, 0.7F), PartPose.offsetAndRotation(-4.2F, 8F, 0F, -0.24F, 1.5708F, 0F));
        PartDefinition p_skirt_left = p_body.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(34, 11).addBox(-2.4F, 0F, -0.5F, 4.8F, 8F, 1F).texOffs(12, 63).addBox(-2.25F, 7.6F, -0.25F, 4.5F, 1F, 0.7F), PartPose.offsetAndRotation(4.2F, 8F, 0F, -0.24F, -1.5708F, 0F));
        PartDefinition p_skirt_fr = p_body.addOrReplaceChild("skirt_fr", CubeListBuilder.create().texOffs(47, 11).addBox(-1.5F, 0F, -0.5F, 3F, 8F, 1F).texOffs(24, 63).addBox(-1.35F, 7.6F, -0.25F, 2.7F, 1F, 0.7F), PartPose.offsetAndRotation(-3.55F, 8F, -1.75F, -0.24F, 0.7854F, 0F));
        PartDefinition p_skirt_fl = p_body.addOrReplaceChild("skirt_fl", CubeListBuilder.create().texOffs(56, 11).addBox(-1.5F, 0F, -0.5F, 3F, 8F, 1F).texOffs(32, 63).addBox(-1.35F, 7.6F, -0.25F, 2.7F, 1F, 0.7F), PartPose.offsetAndRotation(3.55F, 8F, -1.75F, -0.24F, -0.7854F, 0F));
        PartDefinition p_skirt_br = p_body.addOrReplaceChild("skirt_br", CubeListBuilder.create().texOffs(0, 21).addBox(-1.5F, 0F, -0.5F, 3F, 8F, 1F).texOffs(40, 63).addBox(-1.35F, 7.6F, -0.25F, 2.7F, 1F, 0.7F), PartPose.offsetAndRotation(-3.55F, 8F, 1.75F, -0.24F, 2.3562F, 0F));
        PartDefinition p_skirt_bl = p_body.addOrReplaceChild("skirt_bl", CubeListBuilder.create().texOffs(9, 21).addBox(-1.5F, 0F, -0.5F, 3F, 8F, 1F).texOffs(48, 63).addBox(-1.35F, 7.6F, -0.25F, 2.7F, 1F, 0.7F), PartPose.offsetAndRotation(3.55F, 8F, 1.75F, -0.24F, -2.3562F, 0F));
        PartDefinition p_apron = p_skirt_front.addOrReplaceChild("apron", CubeListBuilder.create().texOffs(7, 31).addBox(-3F, 0.2F, -0.4F, 6F, 6.4F, 0.4F).texOffs(38, 60).addBox(-3.3F, 6.5F, -0.45F, 6.6F, 0.9F, 0.5F), PartPose.offsetAndRotation(0F, 0F, -0.55F, 0F, 0F, 0F));
        PartDefinition p_puff_r = p_right_arm.addOrReplaceChild("puff_r", CubeListBuilder.create().texOffs(0, 0).addBox(-2.55F, -2.45F, -2.55F, 4.1F, 4.1F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cuff_r = p_right_arm.addOrReplaceChild("cuff_r", CubeListBuilder.create().texOffs(19, 47).addBox(-2.25F, 7.4F, -2.25F, 3.5F, 1.3F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_frill_r = p_right_leg.addOrReplaceChild("frill_r", CubeListBuilder.create().texOffs(28, 40).addBox(-2.25F, 5F, -2.25F, 4.5F, 0.9F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_shoe_r = p_right_leg.addOrReplaceChild("shoe_r", CubeListBuilder.create().texOffs(18, 21).addBox(-2.2F, 9.9F, -2.75F, 4.4F, 2.15F, 4.95F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_collar_tip_l = p_body.addOrReplaceChild("collar_tip_l", CubeListBuilder.create().texOffs(0, 66).addBox(0F, -0.5F, -0.3F, 2.2F, 1.6F, 0.4F), PartPose.offsetAndRotation(1F, 0.5F, -2.35F, 0F, 0F, -0.55F));
        PartDefinition p_ribbon_l = p_body.addOrReplaceChild("ribbon_l", CubeListBuilder.create().texOffs(43, 54).addBox(0F, -1F, -0.45F, 2F, 2F, 0.7F), PartPose.offsetAndRotation(0.4F, 1F, -2.5F, 0F, -0.15F, -0.3F));
        PartDefinition p_ribbon_tail_l = p_body.addOrReplaceChild("ribbon_tail_l", CubeListBuilder.create().texOffs(54, 54).addBox(-0.4F, 0F, -0.3F, 0.9F, 2.4F, 0.5F), PartPose.offsetAndRotation(0.3F, 1.4F, -2.5F, -0.1F, 0F, -0.25F));
        PartDefinition p_bow_l = p_body.addOrReplaceChild("bow_l", CubeListBuilder.create().texOffs(0, 54).addBox(0F, -1.6F, -0.45F, 4.2F, 3.2F, 0.9F), PartPose.offsetAndRotation(0.8F, 7.7F, 2.5F, 0F, -0.2F, -0.35F));
        PartDefinition p_bow_tail_l = p_body.addOrReplaceChild("bow_tail_l", CubeListBuilder.create().texOffs(0, 31).addBox(-1F, 0F, -0.35F, 2F, 6.4F, 0.7F), PartPose.offsetAndRotation(0.7F, 8.2F, 2.7F, 0.15F, 0F, -0.15F));
        PartDefinition p_puff_l = p_left_arm.addOrReplaceChild("puff_l", CubeListBuilder.create().texOffs(20, 0).addBox(-1.55F, -2.45F, -2.55F, 4.1F, 4.1F, 5.1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_cuff_l = p_left_arm.addOrReplaceChild("cuff_l", CubeListBuilder.create().texOffs(36, 47).addBox(-1.25F, 7.4F, -2.25F, 3.5F, 1.3F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_frill_l = p_left_leg.addOrReplaceChild("frill_l", CubeListBuilder.create().texOffs(0, 47).addBox(-2.25F, 5F, -2.25F, 4.5F, 0.9F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_shoe_l = p_left_leg.addOrReplaceChild("shoe_l", CubeListBuilder.create().texOffs(38, 21).addBox(-2.2F, 9.9F, -2.75F, 4.4F, 2.15F, 4.95F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bakeOutfit(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("bust", m.get("body").getChild("bust"));
        m.put("collar", m.get("body").getChild("collar"));
        m.put("collar_tip_r", m.get("body").getChild("collar_tip_r"));
        m.put("ribbon_knot", m.get("body").getChild("ribbon_knot"));
        m.put("ribbon_r", m.get("body").getChild("ribbon_r"));
        m.put("ribbon_tail_r", m.get("body").getChild("ribbon_tail_r"));
        m.put("sash", m.get("body").getChild("sash"));
        m.put("bow_knot", m.get("body").getChild("bow_knot"));
        m.put("bow_r", m.get("body").getChild("bow_r"));
        m.put("bow_tail_r", m.get("body").getChild("bow_tail_r"));
        m.put("skirt_front", m.get("body").getChild("skirt_front"));
        m.put("skirt_back", m.get("body").getChild("skirt_back"));
        m.put("skirt_right", m.get("body").getChild("skirt_right"));
        m.put("skirt_left", m.get("body").getChild("skirt_left"));
        m.put("skirt_fr", m.get("body").getChild("skirt_fr"));
        m.put("skirt_fl", m.get("body").getChild("skirt_fl"));
        m.put("skirt_br", m.get("body").getChild("skirt_br"));
        m.put("skirt_bl", m.get("body").getChild("skirt_bl"));
        m.put("apron", m.get("skirt_front").getChild("apron"));
        m.put("puff_r", m.get("right_arm").getChild("puff_r"));
        m.put("cuff_r", m.get("right_arm").getChild("cuff_r"));
        m.put("frill_r", m.get("right_leg").getChild("frill_r"));
        m.put("shoe_r", m.get("right_leg").getChild("shoe_r"));
        m.put("collar_tip_l", m.get("body").getChild("collar_tip_l"));
        m.put("ribbon_l", m.get("body").getChild("ribbon_l"));
        m.put("ribbon_tail_l", m.get("body").getChild("ribbon_tail_l"));
        m.put("bow_l", m.get("body").getChild("bow_l"));
        m.put("bow_tail_l", m.get("body").getChild("bow_tail_l"));
        m.put("puff_l", m.get("left_arm").getChild("puff_l"));
        m.put("cuff_l", m.get("left_arm").getChild("cuff_l"));
        m.put("frill_l", m.get("left_leg").getChild("frill_l"));
        m.put("shoe_l", m.get("left_leg").getChild("shoe_l"));
        return m;
    }
}
