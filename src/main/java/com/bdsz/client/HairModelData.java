// GENERADO por tools/gen_hair.py — no editar a mano.
package com.bdsz.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.HashMap;
import java.util.Map;

public final class HairModelData {
    private HairModelData() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_c_cap = p_head.addOrReplaceChild("c_cap", CubeListBuilder.create().texOffs(0, 22).addBox(-4.6F, -8.8F, -4.6F, 9.2F, 2.6F, 9.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p_c_back = p_head.addOrReplaceChild("c_back", CubeListBuilder.create().texOffs(38, 35).addBox(-4.7F, 0F, -1F, 9.4F, 6.5F, 1.6F), PartPose.offsetAndRotation(0F, -7F, 4F, 0.08F, 0F, 0F));
        PartDefinition p_c_side_r = p_head.addOrReplaceChild("c_side_r", CubeListBuilder.create().texOffs(38, 22).addBox(-0.8F, 0F, -3.4F, 1.3F, 4F, 7.2F), PartPose.offsetAndRotation(-4.2F, -7.4F, -0.5F, 0F, 0F, 0.05F));
        PartDefinition p_s_top = p_head.addOrReplaceChild("s_top", CubeListBuilder.create().texOffs(0, 48).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F), PartPose.offsetAndRotation(0F, -8.5F, -1F, -0.25F, 0F, 0F));
        PartDefinition p_s_top_r = p_head.addOrReplaceChild("s_top_r", CubeListBuilder.create().texOffs(26, 48).addBox(-1.35F, -5.4F, -1.35F, 2.7F, 5.4F, 2.7F), PartPose.offsetAndRotation(-2.5F, -8.4F, -0.5F, -0.2F, 0F, -0.45F));
        PartDefinition p_s_side_r = p_head.addOrReplaceChild("s_side_r", CubeListBuilder.create().texOffs(40, 58).addBox(-1.25F, -4.5F, -1.25F, 2.5F, 4.5F, 2.5F), PartPose.offsetAndRotation(-3.6F, -7.6F, 1.2F, 0.1F, 0F, -1F));
        PartDefinition p_s_back = p_head.addOrReplaceChild("s_back", CubeListBuilder.create().texOffs(13, 48).addBox(-1.45F, -5.4F, -1.45F, 2.9F, 5.4F, 2.9F), PartPose.offsetAndRotation(0F, -8.3F, 2.4F, 0.6F, 0F, 0F));
        PartDefinition p_s_back_r = p_head.addOrReplaceChild("s_back_r", CubeListBuilder.create().texOffs(50, 48).addBox(-1.25F, -4.8F, -1.25F, 2.5F, 4.8F, 2.5F), PartPose.offsetAndRotation(-2.3F, -7.8F, 3F, 0.65F, 0F, -0.45F));
        PartDefinition p_s_low = p_head.addOrReplaceChild("s_low", CubeListBuilder.create().texOffs(51, 58).addBox(-1.25F, -3.9F, -1.25F, 2.5F, 3.9F, 2.5F), PartPose.offsetAndRotation(0F, -5.8F, 4.2F, 1.25F, 0F, 0F));
        PartDefinition p_s_low_r = p_head.addOrReplaceChild("s_low_r", CubeListBuilder.create().texOffs(29, 67).addBox(-1.15F, -3.6F, -1.15F, 2.3F, 3.6F, 2.3F), PartPose.offsetAndRotation(-2.8F, -5.5F, 3.8F, 1.15F, 0F, -0.6F));
        PartDefinition p_s_front = p_head.addOrReplaceChild("s_front", CubeListBuilder.create().texOffs(0, 67).addBox(-1.15F, -3.9F, -1.15F, 2.3F, 3.9F, 2.3F), PartPose.offsetAndRotation(1.2F, -8.6F, -3.4F, -0.75F, 0F, 0.25F));
        PartDefinition p_s_bang_r = p_head.addOrReplaceChild("s_bang_r", CubeListBuilder.create().texOffs(35, 89).addBox(-0.7F, 0F, -0.6F, 1.4F, 3.2F, 1F), PartPose.offsetAndRotation(-1.8F, -8F, -4.3F, -0.15F, 0F, 0.3F));
        PartDefinition p_l_back = p_head.addOrReplaceChild("l_back", CubeListBuilder.create().texOffs(0, 0).addBox(-4.7F, 0F, -1.2F, 9.4F, 18F, 2.6F), PartPose.offsetAndRotation(0F, -7.6F, 3.8F, 0.16F, 0F, 0F));
        PartDefinition p_l_back2 = p_l_back.addOrReplaceChild("l_back2", CubeListBuilder.create().texOffs(18, 35).addBox(-3.6F, 0F, -1F, 7.2F, 8F, 2.2F), PartPose.offsetAndRotation(0F, 17.5F, 0F, 0.08F, 0F, 0F));
        PartDefinition p_l_tip = p_l_back2.addOrReplaceChild("l_tip", CubeListBuilder.create().texOffs(27, 58).addBox(-2F, 0F, -0.8F, 4F, 4.5F, 1.8F), PartPose.offsetAndRotation(0F, 7.6F, 0F, 0.05F, 0F, 0F));
        PartDefinition p_l_side_r = p_head.addOrReplaceChild("l_side_r", CubeListBuilder.create().texOffs(25, 0).addBox(-1.3F, 0F, -3.2F, 1.6F, 10F, 6.4F), PartPose.offsetAndRotation(-4.3F, -7.2F, 0.2F, 0F, 0F, 0.12F));
        PartDefinition p_l_top = p_head.addOrReplaceChild("l_top", CubeListBuilder.create().texOffs(11, 58).addBox(-1.8F, -2.7F, -1.8F, 3.6F, 2.7F, 3.6F), PartPose.offsetAndRotation(0F, -8.6F, 0.5F, 0.7F, 0F, 0F));
        PartDefinition p_l_top_r = p_head.addOrReplaceChild("l_top_r", CubeListBuilder.create().texOffs(22, 82).addBox(-1.3F, -2.4F, -1.3F, 2.6F, 2.4F, 2.6F), PartPose.offsetAndRotation(-2.4F, -8.3F, 0.8F, 0.65F, 0F, -0.35F));
        PartDefinition p_l_bang = p_head.addOrReplaceChild("l_bang", CubeListBuilder.create().texOffs(22, 67).addBox(-0.8F, 0F, -0.6F, 1.6F, 5.5F, 1F), PartPose.offsetAndRotation(1.4F, -8F, -4.3F, -0.1F, 0F, -0.08F));
        PartDefinition p_g_top = p_head.addOrReplaceChild("g_top", CubeListBuilder.create().texOffs(40, 67).addBox(-1.25F, -3.3F, -1.25F, 2.5F, 3.3F, 2.5F), PartPose.offsetAndRotation(0F, -8.5F, -0.5F, -0.45F, 0F, 0F));
        PartDefinition p_g_top_r = p_head.addOrReplaceChild("g_top_r", CubeListBuilder.create().texOffs(51, 67).addBox(-1.15F, -3F, -1.15F, 2.3F, 3F, 2.3F), PartPose.offsetAndRotation(-2.4F, -8.3F, 0F, -0.25F, 0F, -0.55F));
        PartDefinition p_g_back = p_head.addOrReplaceChild("g_back", CubeListBuilder.create().texOffs(0, 75).addBox(-1.25F, -3.48F, -1.25F, 2.5F, 3.48F, 2.5F), PartPose.offsetAndRotation(0F, -8F, 2.6F, 0.85F, 0F, 0F));
        PartDefinition p_g_back_r = p_head.addOrReplaceChild("g_back_r", CubeListBuilder.create().texOffs(11, 75).addBox(-1.15F, -3F, -1.15F, 2.3F, 3F, 2.3F), PartPose.offsetAndRotation(-2.4F, -7.4F, 3F, 0.9F, 0F, -0.5F));
        PartDefinition p_g_bang_r = p_head.addOrReplaceChild("g_bang_r", CubeListBuilder.create().texOffs(56, 95).addBox(-0.7F, 0F, -0.6F, 1.4F, 2.6F, 1F), PartPose.offsetAndRotation(-1.6F, -8F, -4.3F, -0.12F, 0F, 0.25F));
        PartDefinition p_s_top_tip = p_s_top.addOrReplaceChild("s_top_tip", CubeListBuilder.create().texOffs(55, 75).addBox(-0.825F, -4.3F, -0.825F, 1.65F, 4.3F, 1.65F), PartPose.offsetAndRotation(0F, -5.7F, 0F, -0.062F, 0F, 0F));
        PartDefinition p_s_top_r_tip = p_s_top_r.addOrReplaceChild("s_top_r_tip", CubeListBuilder.create().texOffs(8, 82).addBox(-0.743F, -3.9F, -0.743F, 1.485F, 3.9F, 1.485F), PartPose.offsetAndRotation(0F, -5.1F, 0F, -0.05F, 0F, -0.113F));
        PartDefinition p_s_side_r_tip = p_s_side_r.addOrReplaceChild("s_side_r_tip", CubeListBuilder.create().texOffs(55, 82).addBox(-0.688F, -3.3F, -0.688F, 1.375F, 3.3F, 1.375F), PartPose.offsetAndRotation(0F, -4.2F, 0F, 0.025F, 0F, -0.25F));
        PartDefinition p_s_back_tip = p_s_back.addOrReplaceChild("s_back_tip", CubeListBuilder.create().texOffs(0, 82).addBox(-0.797F, -3.9F, -0.797F, 1.595F, 3.9F, 1.595F), PartPose.offsetAndRotation(0F, -5.1F, 0F, 0.15F, 0F, 0F));
        PartDefinition p_s_back_r_tip = p_s_back_r.addOrReplaceChild("s_back_r_tip", CubeListBuilder.create().texOffs(0, 89).addBox(-0.688F, -3.5F, -0.688F, 1.375F, 3.5F, 1.375F), PartPose.offsetAndRotation(0F, -4.5F, 0F, 0.163F, 0F, -0.113F));
        PartDefinition p_s_low_tip = p_s_low.addOrReplaceChild("s_low_tip", CubeListBuilder.create().texOffs(7, 89).addBox(-0.688F, -2.9F, -0.688F, 1.375F, 2.9F, 1.375F), PartPose.offsetAndRotation(0F, -3.6F, 0F, 0.312F, 0F, 0F));
        PartDefinition p_s_low_r_tip = p_s_low_r.addOrReplaceChild("s_low_r_tip", CubeListBuilder.create().texOffs(47, 89).addBox(-0.632F, -2.7F, -0.632F, 1.265F, 2.7F, 1.265F), PartPose.offsetAndRotation(0F, -3.3F, 0F, 0.287F, 0F, -0.15F));
        PartDefinition p_s_front_tip = p_s_front.addOrReplaceChild("s_front_tip", CubeListBuilder.create().texOffs(14, 89).addBox(-0.632F, -2.9F, -0.632F, 1.265F, 2.9F, 1.265F), PartPose.offsetAndRotation(0F, -3.6F, 0F, -0.188F, 0F, 0.062F));
        PartDefinition p_l_top_tip = p_l_top.addOrReplaceChild("l_top_tip", CubeListBuilder.create().texOffs(46, 82).addBox(-0.99F, -2.1F, -0.99F, 1.98F, 2.1F, 1.98F), PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.175F, 0F, 0F));
        PartDefinition p_l_top_r_tip = p_l_top_r.addOrReplaceChild("l_top_r_tip", CubeListBuilder.create().texOffs(54, 89).addBox(-0.715F, -1.9F, -0.715F, 1.43F, 1.9F, 1.43F), PartPose.offsetAndRotation(0F, -2.1F, 0F, 0.163F, 0F, -0.087F));
        PartDefinition p_g_top_tip = p_g_top.addOrReplaceChild("g_top_tip", CubeListBuilder.create().texOffs(0, 95).addBox(-0.688F, -2.5F, -0.688F, 1.375F, 2.5F, 1.375F), PartPose.offsetAndRotation(0F, -3F, 0F, -0.113F, 0F, 0F));
        PartDefinition p_g_top_r_tip = p_g_top_r.addOrReplaceChild("g_top_r_tip", CubeListBuilder.create().texOffs(7, 95).addBox(-0.632F, -2.3F, -0.632F, 1.265F, 2.3F, 1.265F), PartPose.offsetAndRotation(0F, -2.7F, 0F, -0.062F, 0F, -0.138F));
        PartDefinition p_g_back_tip = p_g_back.addOrReplaceChild("g_back_tip", CubeListBuilder.create().texOffs(14, 95).addBox(-0.688F, -2.62F, -0.688F, 1.375F, 2.62F, 1.375F), PartPose.offsetAndRotation(0F, -3.18F, 0F, 0.212F, 0F, 0F));
        PartDefinition p_g_back_r_tip = p_g_back_r.addOrReplaceChild("g_back_r_tip", CubeListBuilder.create().texOffs(21, 95).addBox(-0.632F, -2.3F, -0.632F, 1.265F, 2.3F, 1.265F), PartPose.offsetAndRotation(0F, -2.7F, 0F, 0.225F, 0F, -0.125F));
        PartDefinition p_c_side_l = p_head.addOrReplaceChild("c_side_l", CubeListBuilder.create().texOffs(0, 35).addBox(-0.5F, 0F, -3.4F, 1.3F, 4F, 7.2F), PartPose.offsetAndRotation(4.2F, -7.4F, -0.5F, 0F, 0F, -0.05F));
        PartDefinition p_s_top_l = p_head.addOrReplaceChild("s_top_l", CubeListBuilder.create().texOffs(38, 48).addBox(-1.35F, -5.4F, -1.35F, 2.7F, 5.4F, 2.7F), PartPose.offsetAndRotation(2.5F, -8.4F, -0.5F, -0.2F, 0F, 0.45F));
        PartDefinition p_s_side_l = p_head.addOrReplaceChild("s_side_l", CubeListBuilder.create().texOffs(11, 67).addBox(-1.25F, -4.5F, -1.25F, 2.5F, 4.5F, 2.5F), PartPose.offsetAndRotation(3.6F, -7.6F, 1.2F, 0.1F, 0F, 1F));
        PartDefinition p_s_back_l = p_head.addOrReplaceChild("s_back_l", CubeListBuilder.create().texOffs(0, 58).addBox(-1.25F, -4.8F, -1.25F, 2.5F, 4.8F, 2.5F), PartPose.offsetAndRotation(2.3F, -7.8F, 3F, 0.65F, 0F, 0.45F));
        PartDefinition p_s_low_l = p_head.addOrReplaceChild("s_low_l", CubeListBuilder.create().texOffs(22, 75).addBox(-1.15F, -3.6F, -1.15F, 2.3F, 3.6F, 2.3F), PartPose.offsetAndRotation(2.8F, -5.5F, 3.8F, 1.15F, 0F, 0.6F));
        PartDefinition p_s_bang_l = p_head.addOrReplaceChild("s_bang_l", CubeListBuilder.create().texOffs(41, 89).addBox(-0.7F, 0F, -0.6F, 1.4F, 3.2F, 1F), PartPose.offsetAndRotation(1.8F, -8F, -4.3F, -0.15F, 0F, -0.3F));
        PartDefinition p_l_side_l = p_head.addOrReplaceChild("l_side_l", CubeListBuilder.create().texOffs(42, 0).addBox(-0.3F, 0F, -3.2F, 1.6F, 10F, 6.4F), PartPose.offsetAndRotation(4.3F, -7.2F, 0.2F, 0F, 0F, -0.12F));
        PartDefinition p_l_top_l = p_head.addOrReplaceChild("l_top_l", CubeListBuilder.create().texOffs(34, 82).addBox(-1.3F, -2.4F, -1.3F, 2.6F, 2.4F, 2.6F), PartPose.offsetAndRotation(2.4F, -8.3F, 0.8F, 0.65F, 0F, 0.35F));
        PartDefinition p_g_top_l = p_head.addOrReplaceChild("g_top_l", CubeListBuilder.create().texOffs(33, 75).addBox(-1.15F, -3F, -1.15F, 2.3F, 3F, 2.3F), PartPose.offsetAndRotation(2.4F, -8.3F, 0F, -0.25F, 0F, 0.55F));
        PartDefinition p_g_back_l = p_head.addOrReplaceChild("g_back_l", CubeListBuilder.create().texOffs(44, 75).addBox(-1.15F, -3F, -1.15F, 2.3F, 3F, 2.3F), PartPose.offsetAndRotation(2.4F, -7.4F, 3F, 0.9F, 0F, 0.5F));
        PartDefinition p_g_bang_l = p_head.addOrReplaceChild("g_bang_l", CubeListBuilder.create().texOffs(0, 100).addBox(-0.7F, 0F, -0.6F, 1.4F, 2.6F, 1F), PartPose.offsetAndRotation(1.6F, -8F, -4.3F, -0.12F, 0F, -0.25F));
        PartDefinition p_s_top_l_tip = p_s_top_l.addOrReplaceChild("s_top_l_tip", CubeListBuilder.create().texOffs(15, 82).addBox(-0.743F, -3.9F, -0.743F, 1.485F, 3.9F, 1.485F), PartPose.offsetAndRotation(0F, -5.1F, 0F, -0.05F, 0F, 0.113F));
        PartDefinition p_s_side_l_tip = p_s_side_l.addOrReplaceChild("s_side_l_tip", CubeListBuilder.create().texOffs(21, 89).addBox(-0.688F, -3.3F, -0.688F, 1.375F, 3.3F, 1.375F), PartPose.offsetAndRotation(0F, -4.2F, 0F, 0.025F, 0F, 0.25F));
        PartDefinition p_s_back_l_tip = p_s_back_l.addOrReplaceChild("s_back_l_tip", CubeListBuilder.create().texOffs(28, 89).addBox(-0.688F, -3.5F, -0.688F, 1.375F, 3.5F, 1.375F), PartPose.offsetAndRotation(0F, -4.5F, 0F, 0.163F, 0F, 0.113F));
        PartDefinition p_s_low_l_tip = p_s_low_l.addOrReplaceChild("s_low_l_tip", CubeListBuilder.create().texOffs(28, 95).addBox(-0.632F, -2.7F, -0.632F, 1.265F, 2.7F, 1.265F), PartPose.offsetAndRotation(0F, -3.3F, 0F, 0.287F, 0F, 0.15F));
        PartDefinition p_l_top_l_tip = p_l_top_l.addOrReplaceChild("l_top_l_tip", CubeListBuilder.create().texOffs(35, 95).addBox(-0.715F, -1.9F, -0.715F, 1.43F, 1.9F, 1.43F), PartPose.offsetAndRotation(0F, -2.1F, 0F, 0.163F, 0F, 0.087F));
        PartDefinition p_g_top_l_tip = p_g_top_l.addOrReplaceChild("g_top_l_tip", CubeListBuilder.create().texOffs(42, 95).addBox(-0.632F, -2.3F, -0.632F, 1.265F, 2.3F, 1.265F), PartPose.offsetAndRotation(0F, -2.7F, 0F, -0.062F, 0F, 0.138F));
        PartDefinition p_g_back_l_tip = p_g_back_l.addOrReplaceChild("g_back_l_tip", CubeListBuilder.create().texOffs(49, 95).addBox(-0.632F, -2.3F, -0.632F, 1.265F, 2.3F, 1.265F), PartPose.offsetAndRotation(0F, -2.7F, 0F, 0.225F, 0F, 0.125F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static Map<String, ModelPart> bake(ModelPart root) {
        Map<String, ModelPart> m = new HashMap<>();
        m.put("root", root);
        m.put("head", root.getChild("head"));
        m.put("c_cap", m.get("head").getChild("c_cap"));
        m.put("c_back", m.get("head").getChild("c_back"));
        m.put("c_side_r", m.get("head").getChild("c_side_r"));
        m.put("s_top", m.get("head").getChild("s_top"));
        m.put("s_top_r", m.get("head").getChild("s_top_r"));
        m.put("s_side_r", m.get("head").getChild("s_side_r"));
        m.put("s_back", m.get("head").getChild("s_back"));
        m.put("s_back_r", m.get("head").getChild("s_back_r"));
        m.put("s_low", m.get("head").getChild("s_low"));
        m.put("s_low_r", m.get("head").getChild("s_low_r"));
        m.put("s_front", m.get("head").getChild("s_front"));
        m.put("s_bang_r", m.get("head").getChild("s_bang_r"));
        m.put("l_back", m.get("head").getChild("l_back"));
        m.put("l_back2", m.get("l_back").getChild("l_back2"));
        m.put("l_tip", m.get("l_back2").getChild("l_tip"));
        m.put("l_side_r", m.get("head").getChild("l_side_r"));
        m.put("l_top", m.get("head").getChild("l_top"));
        m.put("l_top_r", m.get("head").getChild("l_top_r"));
        m.put("l_bang", m.get("head").getChild("l_bang"));
        m.put("g_top", m.get("head").getChild("g_top"));
        m.put("g_top_r", m.get("head").getChild("g_top_r"));
        m.put("g_back", m.get("head").getChild("g_back"));
        m.put("g_back_r", m.get("head").getChild("g_back_r"));
        m.put("g_bang_r", m.get("head").getChild("g_bang_r"));
        m.put("s_top_tip", m.get("s_top").getChild("s_top_tip"));
        m.put("s_top_r_tip", m.get("s_top_r").getChild("s_top_r_tip"));
        m.put("s_side_r_tip", m.get("s_side_r").getChild("s_side_r_tip"));
        m.put("s_back_tip", m.get("s_back").getChild("s_back_tip"));
        m.put("s_back_r_tip", m.get("s_back_r").getChild("s_back_r_tip"));
        m.put("s_low_tip", m.get("s_low").getChild("s_low_tip"));
        m.put("s_low_r_tip", m.get("s_low_r").getChild("s_low_r_tip"));
        m.put("s_front_tip", m.get("s_front").getChild("s_front_tip"));
        m.put("l_top_tip", m.get("l_top").getChild("l_top_tip"));
        m.put("l_top_r_tip", m.get("l_top_r").getChild("l_top_r_tip"));
        m.put("g_top_tip", m.get("g_top").getChild("g_top_tip"));
        m.put("g_top_r_tip", m.get("g_top_r").getChild("g_top_r_tip"));
        m.put("g_back_tip", m.get("g_back").getChild("g_back_tip"));
        m.put("g_back_r_tip", m.get("g_back_r").getChild("g_back_r_tip"));
        m.put("c_side_l", m.get("head").getChild("c_side_l"));
        m.put("s_top_l", m.get("head").getChild("s_top_l"));
        m.put("s_side_l", m.get("head").getChild("s_side_l"));
        m.put("s_back_l", m.get("head").getChild("s_back_l"));
        m.put("s_low_l", m.get("head").getChild("s_low_l"));
        m.put("s_bang_l", m.get("head").getChild("s_bang_l"));
        m.put("l_side_l", m.get("head").getChild("l_side_l"));
        m.put("l_top_l", m.get("head").getChild("l_top_l"));
        m.put("g_top_l", m.get("head").getChild("g_top_l"));
        m.put("g_back_l", m.get("head").getChild("g_back_l"));
        m.put("g_bang_l", m.get("head").getChild("g_bang_l"));
        m.put("s_top_l_tip", m.get("s_top_l").getChild("s_top_l_tip"));
        m.put("s_side_l_tip", m.get("s_side_l").getChild("s_side_l_tip"));
        m.put("s_back_l_tip", m.get("s_back_l").getChild("s_back_l_tip"));
        m.put("s_low_l_tip", m.get("s_low_l").getChild("s_low_l_tip"));
        m.put("l_top_l_tip", m.get("l_top_l").getChild("l_top_l_tip"));
        m.put("g_top_l_tip", m.get("g_top_l").getChild("g_top_l_tip"));
        m.put("g_back_l_tip", m.get("g_back_l").getChild("g_back_l_tip"));
        return m;
    }
}
