// GENERADO por tools/gen_ozaru.py — no editar a mano.
package com.bdsz.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.HashMap;
import java.util.Map;

public final class OzaruModelData {
    private OzaruModelData() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("root", mesh.getRoot());
        d.put("body", d.get("root").addOrReplaceChild("body", CubeListBuilder.create().texOffs(49, 0).addBox(-5.5F, -9F, -3.5F, 11F, 9.5F, 7F).texOffs(54, 55).addBox(-4F, -8.5F, -3.9F, 8F, 8F, 0.6F), PartPose.offsetAndRotation(0F, 12F, 2F, 0.45F, 0F, 0F)));
        d.put("chest", d.get("body").addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 0).addBox(-7.5F, -11F, -4.5F, 15F, 11.5F, 9F).texOffs(33, 65).addBox(-5.5F, -10F, -5F, 11F, 7F, 0.6F).texOffs(73, 55).addBox(-6F, -12.5F, -2.5F, 12F, 2F, 6F), PartPose.offsetAndRotation(0F, -8F, 0F, 0.15F, 0F, 0F)));
        d.put("head", d.get("chest").addOrReplaceChild("head", CubeListBuilder.create().texOffs(86, 0).addBox(-4.5F, -8F, -5F, 9F, 8.5F, 8F).texOffs(77, 65).addBox(-3.5F, -6.5F, -5.6F, 7F, 5.5F, 0.8F).texOffs(58, 65).addBox(-3F, -3.2F, -7.6F, 6F, 3.4F, 2.8F).texOffs(88, 74).addBox(-4F, -6.9F, -5.9F, 8F, 1.2F, 1F).texOffs(0, 65).addBox(-4.6F, -9.2F, -3.5F, 9.2F, 1.5F, 6.5F), PartPose.offsetAndRotation(0F, -10.5F, -3F, -0.6F, 0F, 0F)));
        d.put("eyes", d.get("head").addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(107, 74).addBox(-2.7F, -5.4F, -5.75F, 1.6F, 1F, 0.2F).texOffs(112, 74).addBox(1.1F, -5.4F, -5.75F, 1.6F, 1F, 0.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("ear_r", d.get("head").addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(70, 74).addBox(-1.6F, -1.3F, -1F, 1.6F, 2.6F, 2F), PartPose.offsetAndRotation(-4.5F, -5.5F, 0F, 0F, 0F, -0.2F)));
        d.put("jaw", d.get("head").addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(51, 74).addBox(-2.8F, 0F, -3F, 5.6F, 1.6F, 3F).texOffs(117, 74).addBox(-2.3F, -1F, -2.8F, 0.8F, 1F, 0.6F).texOffs(121, 74).addBox(1.5F, -1F, -2.8F, 0.8F, 1F, 0.6F), PartPose.offsetAndRotation(0F, 0.1F, -4.6F, 0F, 0F, 0F)));
        d.put("fang_r", d.get("head").addOrReplaceChild("fang_r", CubeListBuilder.create().texOffs(125, 74).addBox(-0.4F, 0F, -0.3F, 0.8F, 1.3F, 0.6F), PartPose.offsetAndRotation(-2F, 0.2F, -7.3F, 0F, 0F, 0F)));
        d.put("arm_r", d.get("chest").addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(0, 22).addBox(-2.9F, -1.5F, -2.9F, 5.8F, 11F, 5.8F), PartPose.offsetAndRotation(-8.6F, -9F, 0F, -0.6F, 0F, 0.12F)));
        d.put("fore_r", d.get("arm_r").addOrReplaceChild("fore_r", CubeListBuilder.create().texOffs(50, 22).addBox(-2.7F, 0F, -2.7F, 5.4F, 10F, 5.4F), PartPose.offsetAndRotation(0F, 9.2F, 0F, 0F, 0F, 0F)));
        d.put("hand_r", d.get("fore_r").addOrReplaceChild("hand_r", CubeListBuilder.create().texOffs(25, 40).addBox(-3F, 0F, -3.3F, 6F, 3F, 6.6F), PartPose.offsetAndRotation(0F, 9.6F, 0F, 0F, 0F, 0F)));
        d.put("leg_r", d.get("root").addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(96, 22).addBox(-2.9F, -1F, -3F, 5.8F, 7.5F, 6F), PartPose.offsetAndRotation(-3.6F, 12F, 2F, 0F, 0F, 0F)));
        d.put("shin_r", d.get("leg_r").addOrReplaceChild("shin_r", CubeListBuilder.create().texOffs(79, 40).addBox(-2.6F, 0F, -2.6F, 5.2F, 4.6F, 5.2F), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F)));
        d.put("foot_r", d.get("shin_r").addOrReplaceChild("foot_r", CubeListBuilder.create().texOffs(0, 55).addBox(-2.9F, 0F, -4.6F, 5.8F, 1.8F, 6.8F), PartPose.offsetAndRotation(0F, 4.2F, 0F, 0F, 0F, 0F)));
        d.put("tail0", d.get("body").addOrReplaceChild("tail0", CubeListBuilder.create().texOffs(94, 65).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 4F), PartPose.offsetAndRotation(0F, -1F, 3.2F, 0.35F, 0F, 0F)));
        d.put("tail1", d.get("tail0").addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(108, 65).addBox(-1.09F, -1.09F, 0F, 2.18F, 2.18F, 4F), PartPose.offsetAndRotation(0F, 0F, 3.6F, 0.3F, 0F, 0F)));
        d.put("tail2", d.get("tail1").addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(0, 74).addBox(-0.98F, -0.98F, 0F, 1.96F, 1.96F, 4F), PartPose.offsetAndRotation(0F, 0F, 3.6F, 0.3F, 0F, 0F)));
        d.put("tail3", d.get("tail2").addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(13, 74).addBox(-0.87F, -0.87F, 0F, 1.74F, 1.74F, 4F), PartPose.offsetAndRotation(0F, 0F, 3.6F, 0.3F, 0F, 0F)));
        d.put("tail4", d.get("tail3").addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(26, 74).addBox(-0.76F, -0.76F, 0F, 1.52F, 1.52F, 4F), PartPose.offsetAndRotation(0F, 0F, 3.6F, 0.3F, 0F, 0F)));
        d.put("tail5", d.get("tail4").addOrReplaceChild("tail5", CubeListBuilder.create().texOffs(39, 74).addBox(-0.65F, -0.65F, 0F, 1.3F, 1.3F, 4F), PartPose.offsetAndRotation(0F, 0F, 3.6F, 0.3F, 0F, 0F)));
        d.put("ear_l", d.get("head").addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(79, 74).addBox(0F, -1.3F, -1F, 1.6F, 2.6F, 2F), PartPose.offsetAndRotation(4.5F, -5.5F, 0F, 0F, 0F, 0.2F)));
        d.put("fang_l", d.get("head").addOrReplaceChild("fang_l", CubeListBuilder.create().texOffs(0, 81).addBox(-0.4F, 0F, -0.3F, 0.8F, 1.3F, 0.6F), PartPose.offsetAndRotation(2F, 0.2F, -7.3F, 0F, 0F, 0F)));
        d.put("arm_l", d.get("chest").addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(25, 22).addBox(-2.9F, -1.5F, -2.9F, 5.8F, 11F, 5.8F), PartPose.offsetAndRotation(8.6F, -9F, 0F, -0.6F, 0F, -0.12F)));
        d.put("fore_l", d.get("arm_l").addOrReplaceChild("fore_l", CubeListBuilder.create().texOffs(73, 22).addBox(-2.7F, 0F, -2.7F, 5.4F, 10F, 5.4F), PartPose.offsetAndRotation(0F, 9.2F, 0F, 0F, 0F, 0F)));
        d.put("hand_l", d.get("fore_l").addOrReplaceChild("hand_l", CubeListBuilder.create().texOffs(52, 40).addBox(-3F, 0F, -3.3F, 6F, 3F, 6.6F), PartPose.offsetAndRotation(0F, 9.6F, 0F, 0F, 0F, 0F)));
        d.put("leg_l", d.get("root").addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(0, 40).addBox(-2.9F, -1F, -3F, 5.8F, 7.5F, 6F), PartPose.offsetAndRotation(3.6F, 12F, 2F, 0F, 0F, 0F)));
        d.put("shin_l", d.get("leg_l").addOrReplaceChild("shin_l", CubeListBuilder.create().texOffs(101, 40).addBox(-2.6F, 0F, -2.6F, 5.2F, 4.6F, 5.2F), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F)));
        d.put("foot_l", d.get("shin_l").addOrReplaceChild("foot_l", CubeListBuilder.create().texOffs(27, 55).addBox(-2.9F, 0F, -4.6F, 5.8F, 1.8F, 6.8F), PartPose.offsetAndRotation(0F, 4.2F, 0F, 0F, 0F, 0F)));
        return LayerDefinition.create(mesh, 128, 128);
    }

    public static Map<String, ModelPart> bake(ModelPart root) {
        Map<String, ModelPart> m = new HashMap<>();
        m.put("root", root);
        m.put("body", m.get("root").getChild("body"));
        m.put("chest", m.get("body").getChild("chest"));
        m.put("head", m.get("chest").getChild("head"));
        m.put("eyes", m.get("head").getChild("eyes"));
        m.put("ear_r", m.get("head").getChild("ear_r"));
        m.put("jaw", m.get("head").getChild("jaw"));
        m.put("fang_r", m.get("head").getChild("fang_r"));
        m.put("arm_r", m.get("chest").getChild("arm_r"));
        m.put("fore_r", m.get("arm_r").getChild("fore_r"));
        m.put("hand_r", m.get("fore_r").getChild("hand_r"));
        m.put("leg_r", m.get("root").getChild("leg_r"));
        m.put("shin_r", m.get("leg_r").getChild("shin_r"));
        m.put("foot_r", m.get("shin_r").getChild("foot_r"));
        m.put("tail0", m.get("body").getChild("tail0"));
        m.put("tail1", m.get("tail0").getChild("tail1"));
        m.put("tail2", m.get("tail1").getChild("tail2"));
        m.put("tail3", m.get("tail2").getChild("tail3"));
        m.put("tail4", m.get("tail3").getChild("tail4"));
        m.put("tail5", m.get("tail4").getChild("tail5"));
        m.put("ear_l", m.get("head").getChild("ear_l"));
        m.put("fang_l", m.get("head").getChild("fang_l"));
        m.put("arm_l", m.get("chest").getChild("arm_l"));
        m.put("fore_l", m.get("arm_l").getChild("fore_l"));
        m.put("hand_l", m.get("fore_l").getChild("hand_l"));
        m.put("leg_l", m.get("root").getChild("leg_l"));
        m.put("shin_l", m.get("leg_l").getChild("shin_l"));
        m.put("foot_l", m.get("shin_l").getChild("foot_l"));
        return m;
    }
}
