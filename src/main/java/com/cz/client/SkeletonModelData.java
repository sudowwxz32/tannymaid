// GENERADO por tools/gen_assets.py — no editar a mano.
package com.cz.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.HashMap;
import java.util.Map;

public final class SkeletonModelData {
    private SkeletonModelData() {}

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("body", mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO));
        d.put("head", mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO));
        d.put("spine0", d.get("body").addOrReplaceChild("spine0", CubeListBuilder.create().texOffs(22, 47).addBox(-0.9F, -0.6F, -0.4F, 1.8F, 1.3F, 1.6F).texOffs(53, 55).addBox(-0.4F, -0.9F, 0.9F, 0.8F, 0.8F, 1.2F), PartPose.offsetAndRotation(0F, 0.5F, 2.4F, 0.05F, 0F, 0F)));
        d.put("spine1", d.get("body").addOrReplaceChild("spine1", CubeListBuilder.create().texOffs(30, 47).addBox(-0.9F, -0.6F, -0.4F, 1.8F, 1.3F, 1.6F).texOffs(58, 55).addBox(-0.4F, -0.9F, 0.9F, 0.8F, 0.8F, 1.2F), PartPose.offsetAndRotation(0F, 2.25F, 2.4F, 0.05F, 0F, 0F)));
        d.put("spine2", d.get("body").addOrReplaceChild("spine2", CubeListBuilder.create().texOffs(38, 47).addBox(-0.9F, -0.6F, -0.4F, 1.8F, 1.3F, 1.6F).texOffs(0, 58).addBox(-0.4F, -0.9F, 0.9F, 0.8F, 0.8F, 1.2F), PartPose.offsetAndRotation(0F, 4F, 2.4F, 0.05F, 0F, 0F)));
        d.put("spine3", d.get("body").addOrReplaceChild("spine3", CubeListBuilder.create().texOffs(46, 47).addBox(-0.9F, -0.6F, -0.4F, 1.8F, 1.3F, 1.6F).texOffs(5, 58).addBox(-0.4F, -0.9F, 0.9F, 0.8F, 0.8F, 1.2F), PartPose.offsetAndRotation(0F, 5.75F, 2.4F, 0.05F, 0F, 0F)));
        d.put("spine4", d.get("body").addOrReplaceChild("spine4", CubeListBuilder.create().texOffs(54, 47).addBox(-0.9F, -0.6F, -0.4F, 1.8F, 1.3F, 1.6F).texOffs(10, 58).addBox(-0.4F, -0.9F, 0.9F, 0.8F, 0.8F, 1.2F), PartPose.offsetAndRotation(0F, 7.5F, 2.4F, 0.05F, 0F, 0F)));
        d.put("spine5", d.get("body").addOrReplaceChild("spine5", CubeListBuilder.create().texOffs(0, 51).addBox(-0.9F, -0.6F, -0.4F, 1.8F, 1.3F, 1.6F).texOffs(15, 58).addBox(-0.4F, -0.9F, 0.9F, 0.8F, 0.8F, 1.2F), PartPose.offsetAndRotation(0F, 9.25F, 2.4F, 0.05F, 0F, 0F)));
        d.put("spine6", d.get("body").addOrReplaceChild("spine6", CubeListBuilder.create().texOffs(8, 51).addBox(-0.9F, -0.6F, -0.4F, 1.8F, 1.3F, 1.6F).texOffs(20, 58).addBox(-0.4F, -0.9F, 0.9F, 0.8F, 0.8F, 1.2F), PartPose.offsetAndRotation(0F, 11F, 2.4F, 0.05F, 0F, 0F)));
        d.put("rib0_r", d.get("body").addOrReplaceChild("rib0_r", CubeListBuilder.create().texOffs(26, 51).addBox(-3.9F, -0.35F, -0.5F, 3.9F, 0.8F, 1F).texOffs(18, 34).addBox(-4.4F, -0.35F, -4.3F, 0.8F, 0.8F, 4.1F), PartPose.offsetAndRotation(-0.6F, 2.6F, 2.5F, 0F, 0F, 0.12F)));
        d.put("rib1_r", d.get("body").addOrReplaceChild("rib1_r", CubeListBuilder.create().texOffs(37, 51).addBox(-3.75F, -0.35F, -0.5F, 3.75F, 0.8F, 1F).texOffs(29, 34).addBox(-4.25F, -0.35F, -4.3F, 0.8F, 0.8F, 4.1F), PartPose.offsetAndRotation(-0.6F, 4.4F, 2.5F, 0F, 0F, 0.16F)));
        d.put("rib2_r", d.get("body").addOrReplaceChild("rib2_r", CubeListBuilder.create().texOffs(48, 51).addBox(-3.6F, -0.35F, -0.5F, 3.6F, 0.8F, 1F).texOffs(40, 34).addBox(-4.1F, -0.35F, -4.3F, 0.8F, 0.8F, 4.1F), PartPose.offsetAndRotation(-0.6F, 6.2F, 2.5F, 0F, 0F, 0.2F)));
        d.put("rib3_r", d.get("body").addOrReplaceChild("rib3_r", CubeListBuilder.create().texOffs(33, 55).addBox(-3.45F, -0.35F, -0.5F, 3.45F, 0.8F, 1F).texOffs(51, 34).addBox(-3.95F, -0.35F, -4.3F, 0.8F, 0.8F, 4.1F), PartPose.offsetAndRotation(-0.6F, 8F, 2.5F, 0F, 0F, 0.24F)));
        d.put("blade_r", d.get("body").addOrReplaceChild("blade_r", CubeListBuilder.create().texOffs(44, 41).addBox(-1.8F, -0.4F, -0.2F, 2.6F, 3.6F, 0.8F), PartPose.offsetAndRotation(-2.2F, 1.6F, 2.4F, 0.15F, 0.15F, 0.2F)));
        d.put("pelvis", d.get("body").addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 26).addBox(-4.3F, -0.8F, -2.4F, 8.6F, 1.6F, 4.8F), PartPose.offsetAndRotation(0F, 11F, 0F, 0F, 0F, 0F)));
        d.put("skull", d.get("head").addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 0).addBox(-4.7F, -9F, -4.7F, 9.4F, 3.2F, 9.4F).texOffs(0, 47).addBox(-4.75F, -6.2F, -4.75F, 9.5F, 2.2F, 0.6F).texOffs(39, 0).addBox(-4.75F, -6F, -4.2F, 0.6F, 3.4F, 7.4F).texOffs(0, 14).addBox(4.15F, -6F, -4.2F, 0.6F, 3.4F, 7.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("snout", d.get("head").addOrReplaceChild("snout", CubeListBuilder.create().texOffs(17, 14).addBox(-2.4F, -1.6F, -5.4F, 4.8F, 2.4F, 5.6F).texOffs(48, 26).addBox(-1F, -2F, -5F, 2F, 0.6F, 4.8F), PartPose.offsetAndRotation(0F, -4F, -4.6F, 0.08F, 0F, 0F)));
        d.put("jaw", d.get("head").addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(28, 26).addBox(-2.1F, 0F, -4.8F, 4.2F, 1.2F, 5F), PartPose.offsetAndRotation(0F, -1.2F, -4.4F, 0.25F, 0F, 0F)));
        d.put("ear_r", d.get("head").addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(0, 34).addBox(-1.2F, -4F, -0.8F, 2.4F, 4.2F, 1.6F).texOffs(16, 51).addBox(-0.6F, -5.6F, -0.4F, 1.2F, 1.8F, 0.8F), PartPose.offsetAndRotation(-3F, -8.8F, 0.6F, -0.15F, 0F, -0.3F)));
        d.put("rib0_l", d.get("body").addOrReplaceChild("rib0_l", CubeListBuilder.create().texOffs(0, 55).addBox(0F, -0.35F, -0.5F, 3.9F, 0.8F, 1F).texOffs(0, 41).addBox(3.6F, -0.35F, -4.3F, 0.8F, 0.8F, 4.1F), PartPose.offsetAndRotation(0.6F, 2.6F, 2.5F, 0F, 0F, -0.12F)));
        d.put("rib1_l", d.get("body").addOrReplaceChild("rib1_l", CubeListBuilder.create().texOffs(11, 55).addBox(0F, -0.35F, -0.5F, 3.75F, 0.8F, 1F).texOffs(11, 41).addBox(3.45F, -0.35F, -4.3F, 0.8F, 0.8F, 4.1F), PartPose.offsetAndRotation(0.6F, 4.4F, 2.5F, 0F, 0F, -0.16F)));
        d.put("rib2_l", d.get("body").addOrReplaceChild("rib2_l", CubeListBuilder.create().texOffs(22, 55).addBox(0F, -0.35F, -0.5F, 3.6F, 0.8F, 1F).texOffs(22, 41).addBox(3.3F, -0.35F, -4.3F, 0.8F, 0.8F, 4.1F), PartPose.offsetAndRotation(0.6F, 6.2F, 2.5F, 0F, 0F, -0.2F)));
        d.put("rib3_l", d.get("body").addOrReplaceChild("rib3_l", CubeListBuilder.create().texOffs(43, 55).addBox(0F, -0.35F, -0.5F, 3.45F, 0.8F, 1F).texOffs(33, 41).addBox(3.15F, -0.35F, -4.3F, 0.8F, 0.8F, 4.1F), PartPose.offsetAndRotation(0.6F, 8F, 2.5F, 0F, 0F, -0.24F)));
        d.put("blade_l", d.get("body").addOrReplaceChild("blade_l", CubeListBuilder.create().texOffs(52, 41).addBox(-0.8F, -0.4F, -0.2F, 2.6F, 3.6F, 0.8F), PartPose.offsetAndRotation(2.2F, 1.6F, 2.4F, 0.15F, -0.15F, -0.2F)));
        d.put("ear_l", d.get("head").addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(9, 34).addBox(-1.2F, -4F, -0.8F, 2.4F, 4.2F, 1.6F).texOffs(21, 51).addBox(-0.6F, -5.6F, -0.4F, 1.2F, 1.8F, 0.8F), PartPose.offsetAndRotation(3F, -8.8F, 0.6F, -0.15F, 0F, 0.3F)));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
