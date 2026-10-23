// GENERADO por tools/gen_enemies.py — no editar a mano.
package com.bdsz.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.HashMap;
import java.util.Map;

public final class EnemyModels {
    private EnemyModels() {}

    public static LayerDefinition create(String id) {
        return switch (id) {
            case "androide" -> androide();
            case "robot_guardian" -> robot_guardian();
            case "soldado_imperial" -> soldado_imperial();
            case "engendro_bio" -> engendro_bio();
            case "bio_titan" -> bio_titan();
            case "demonio_arcano" -> demonio_arcano();
            case "emperador_galactico" -> emperador_galactico();
            default -> throw new IllegalArgumentException(id);
        };
    }

    private static LayerDefinition androide() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("root", mesh.getRoot());
        d.put("head", d.get("root").addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("hat", d.get("root").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("body", d.get("root").addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("right_arm", d.get("root").addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 17).addBox(-3F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("left_arm", d.get("root").addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(17, 17).addBox(-1F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("right_leg", d.get("root").addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(34, 17).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("left_leg", d.get("root").addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("ear_r", d.get("head").addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(28, 51).addBox(-0.8F, -1F, -1F, 0.8F, 2F, 2F), PartPose.offsetAndRotation(-4.2F, -4.5F, 0F, 0F, 0F, 0F)));
        d.put("ear_l", d.get("head").addOrReplaceChild("ear_l", CubeListBuilder.create().texOffs(35, 51).addBox(0F, -1F, -1F, 0.8F, 2F, 2F), PartPose.offsetAndRotation(4.2F, -4.5F, 0F, 0F, 0F, 0F)));
        d.put("pad_r", d.get("right_arm").addOrReplaceChild("pad_r", CubeListBuilder.create().texOffs(17, 34).addBox(-3.5F, -2.6F, -2.5F, 4.6F, 2.6F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("pad_l", d.get("left_arm").addOrReplaceChild("pad_l", CubeListBuilder.create().texOffs(38, 34).addBox(-1.1F, -2.6F, -2.5F, 4.6F, 2.6F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("collar", d.get("body").addOrReplaceChild("collar", CubeListBuilder.create().texOffs(0, 51).addBox(-4.3F, -0.3F, -2.3F, 8.6F, 1.6F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static LayerDefinition robot_guardian() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("root", mesh.getRoot());
        d.put("head", d.get("root").addOrReplaceChild("head", CubeListBuilder.create().texOffs(22, 41).addBox(-3F, -5F, -3.5F, 6F, 5F, 7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("hat", d.get("root").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("body", d.get("root").addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6F, -1.5F, -3.5F, 12F, 13.5F, 7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("right_arm", d.get("root").addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(39, 0).addBox(-6.5F, -3F, -3F, 6F, 12F, 6F), PartPose.offsetAndRotation(-5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("left_arm", d.get("root").addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 22).addBox(0.5F, -3F, -3F, 6F, 12F, 6F), PartPose.offsetAndRotation(5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("right_leg", d.get("root").addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(25, 22).addBox(-2.6F, 0F, -2.6F, 5.2F, 12F, 5.2F), PartPose.offsetAndRotation(-1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("left_leg", d.get("root").addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 41).addBox(-2.6F, 0F, -2.6F, 5.2F, 12F, 5.2F), PartPose.offsetAndRotation(1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("antenna", d.get("head").addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(56, 84).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F), PartPose.offsetAndRotation(0F, -5F, 0F, 0F, 0F, 0F)));
        d.put("cannon", d.get("right_arm").addOrReplaceChild("cannon", CubeListBuilder.create().texOffs(0, 60).addBox(-6F, 8F, -4.5F, 5F, 4.5F, 7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("claw", d.get("left_arm").addOrReplaceChild("claw", CubeListBuilder.create().texOffs(25, 60).addBox(1F, 9F, -3.5F, 5F, 3.5F, 7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("foot_r", d.get("right_leg").addOrReplaceChild("foot_r", CubeListBuilder.create().texOffs(0, 84).addBox(-3F, 10F, -4.5F, 6F, 2F, 7.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("foot_l", d.get("left_leg").addOrReplaceChild("foot_l", CubeListBuilder.create().texOffs(28, 84).addBox(-3F, 10F, -4.5F, 6F, 2F, 7.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("shoulder_r", d.get("right_arm").addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(0, 73).addBox(-7F, -4F, -3.5F, 7F, 3F, 7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("shoulder_l", d.get("left_arm").addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(29, 73).addBox(0F, -4F, -3.5F, 7F, 3F, 7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        return LayerDefinition.create(mesh, 64, 128);
    }

    private static LayerDefinition soldado_imperial() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("root", mesh.getRoot());
        d.put("head", d.get("root").addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("hat", d.get("root").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("body", d.get("root").addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("right_arm", d.get("root").addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 17).addBox(-3F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("left_arm", d.get("root").addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(17, 17).addBox(-1F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("right_leg", d.get("root").addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(34, 17).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("left_leg", d.get("root").addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("helmet", d.get("head").addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(0, 51).addBox(-4.5F, -8.6F, -4.5F, 9F, 3.4F, 9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("visor", d.get("head").addOrReplaceChild("visor", CubeListBuilder.create().texOffs(24, 65).addBox(-4.2F, -5.6F, -4.65F, 8.4F, 1.2F, 0.4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("chest", d.get("body").addOrReplaceChild("chest", CubeListBuilder.create().texOffs(17, 34).addBox(-4.6F, -0.4F, -2.6F, 9.2F, 8F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("pauldron_r", d.get("right_arm").addOrReplaceChild("pauldron_r", CubeListBuilder.create().texOffs(37, 51).addBox(-4F, -3F, -3F, 5.4F, 3.4F, 6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("pauldron_l", d.get("left_arm").addOrReplaceChild("pauldron_l", CubeListBuilder.create().texOffs(0, 65).addBox(-1.4F, -3F, -3F, 5.4F, 3.4F, 6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        return LayerDefinition.create(mesh, 64, 128);
    }

    private static LayerDefinition engendro_bio() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("root", mesh.getRoot());
        d.put("head", d.get("root").addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 34).addBox(-4F, -7F, -4.5F, 8F, 7F, 8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("hat", d.get("root").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("body", d.get("root").addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("right_arm", d.get("root").addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(25, 0).addBox(-3F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("left_arm", d.get("root").addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(42, 0).addBox(-1F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("right_leg", d.get("root").addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 17).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("left_leg", d.get("root").addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(17, 17).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("mandible_r", d.get("head").addOrReplaceChild("mandible_r", CubeListBuilder.create().texOffs(46, 50).addBox(-1F, 0F, -1.5F, 1.4F, 2.2F, 1.4F), PartPose.offsetAndRotation(-2.5F, -1F, -4.5F, 0.2F, 0F, 0.2F)));
        d.put("crest", d.get("head").addOrReplaceChild("crest", CubeListBuilder.create().texOffs(33, 34).addBox(-1F, -4F, -1F, 2F, 4F, 2F), PartPose.offsetAndRotation(0F, -7F, 1F, -0.6F, 0F, 0F)));
        d.put("crest_r", d.get("head").addOrReplaceChild("crest_r", CubeListBuilder.create().texOffs(6, 50).addBox(-0.8F, -3F, -0.8F, 1.6F, 3F, 1.6F), PartPose.offsetAndRotation(-2.4F, -6.5F, 1.5F, -0.8F, 0F, -0.4F)));
        d.put("spine0", d.get("body").addOrReplaceChild("spine0", CubeListBuilder.create().texOffs(14, 50).addBox(-0.8F, -3F, -0.8F, 1.6F, 3F, 1.6F), PartPose.offsetAndRotation(0F, 2F, 2F, -1F, 0F, 0F)));
        d.put("spine1", d.get("body").addOrReplaceChild("spine1", CubeListBuilder.create().texOffs(22, 50).addBox(-0.8F, -3F, -0.8F, 1.6F, 3F, 1.6F), PartPose.offsetAndRotation(0F, 5.2F, 2F, -1F, 0F, 0F)));
        d.put("spine2", d.get("body").addOrReplaceChild("spine2", CubeListBuilder.create().texOffs(30, 50).addBox(-0.8F, -3F, -0.8F, 1.6F, 3F, 1.6F), PartPose.offsetAndRotation(0F, 8.4F, 2F, -1F, 0F, 0F)));
        d.put("mandible_l", d.get("head").addOrReplaceChild("mandible_l", CubeListBuilder.create().texOffs(53, 50).addBox(-0.4F, 0F, -1.5F, 1.4F, 2.2F, 1.4F), PartPose.offsetAndRotation(2.5F, -1F, -4.5F, 0.2F, 0F, -0.2F)));
        d.put("crest_l", d.get("head").addOrReplaceChild("crest_l", CubeListBuilder.create().texOffs(38, 50).addBox(-0.8F, -3F, -0.8F, 1.6F, 3F, 1.6F), PartPose.offsetAndRotation(2.4F, -6.5F, 1.5F, -0.8F, 0F, 0.4F)));
        d.put("claw_r", d.get("right_arm").addOrReplaceChild("claw_r", CubeListBuilder.create().texOffs(42, 34).addBox(-3.5F, 9.5F, -3F, 1.2F, 4F, 1.2F).texOffs(48, 34).addBox(-1.5F, 9.5F, -3F, 1.2F, 4F, 1.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("claw_l", d.get("left_arm").addOrReplaceChild("claw_l", CubeListBuilder.create().texOffs(54, 34).addBox(0.3F, 9.5F, -3F, 1.2F, 4F, 1.2F).texOffs(0, 50).addBox(2.3F, 9.5F, -3F, 1.2F, 4F, 1.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static LayerDefinition bio_titan() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("root", mesh.getRoot());
        d.put("head", d.get("root").addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("hat", d.get("root").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("body", d.get("root").addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("right_arm", d.get("root").addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 17).addBox(-3F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("left_arm", d.get("root").addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(17, 17).addBox(-1F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("right_leg", d.get("root").addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(34, 17).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("left_leg", d.get("root").addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("horn_r", d.get("head").addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(42, 51).addBox(-0.9F, -6F, -0.9F, 1.8F, 6F, 1.8F), PartPose.offsetAndRotation(-3F, -7F, 0F, 0.7F, 0F, -0.35F)));
        d.put("horn_tip_r", d.get("horn_r").addOrReplaceChild("horn_tip_r", CubeListBuilder.create().texOffs(46, 62).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F), PartPose.offsetAndRotation(0F, -5.8F, 0F, 0.6F, 0F, 0F)));
        d.put("chestplate", d.get("body").addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(17, 34).addBox(-4.6F, -0.4F, -2.6F, 9.2F, 6.5F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("spike_r", d.get("right_arm").addOrReplaceChild("spike_r", CubeListBuilder.create().texOffs(0, 62).addBox(-1F, -4.5F, -1F, 2F, 4.5F, 2F), PartPose.offsetAndRotation(-1.5F, -2.5F, 0F, 0F, 0F, -0.5F)));
        d.put("guard_r", d.get("right_arm").addOrReplaceChild("guard_r", CubeListBuilder.create().texOffs(0, 51).addBox(-3.5F, 5F, -2.5F, 5F, 5F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("claw_r", d.get("right_arm").addOrReplaceChild("claw_r", CubeListBuilder.create().texOffs(51, 62).addBox(-3.2F, 10F, -2.8F, 1F, 3.5F, 1F).texOffs(56, 62).addBox(-1F, 10F, -2.8F, 1F, 3.5F, 1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("knee_r", d.get("right_leg").addOrReplaceChild("knee_r", CubeListBuilder.create().texOffs(18, 62).addBox(-2.5F, 4F, -2.8F, 5F, 3F, 1.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("horn_l", d.get("head").addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(51, 51).addBox(-0.9F, -6F, -0.9F, 1.8F, 6F, 1.8F), PartPose.offsetAndRotation(3F, -7F, 0F, 0.7F, 0F, 0.35F)));
        d.put("horn_tip_l", d.get("horn_l").addOrReplaceChild("horn_tip_l", CubeListBuilder.create().texOffs(0, 70).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F), PartPose.offsetAndRotation(0F, -5.8F, 0F, 0.6F, 0F, 0F)));
        d.put("spike_l", d.get("left_arm").addOrReplaceChild("spike_l", CubeListBuilder.create().texOffs(9, 62).addBox(-1F, -4.5F, -1F, 2F, 4.5F, 2F), PartPose.offsetAndRotation(1.5F, -2.5F, 0F, 0F, 0F, 0.5F)));
        d.put("guard_l", d.get("left_arm").addOrReplaceChild("guard_l", CubeListBuilder.create().texOffs(21, 51).addBox(-1.5F, 5F, -2.5F, 5F, 5F, 5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("claw_l", d.get("left_arm").addOrReplaceChild("claw_l", CubeListBuilder.create().texOffs(5, 70).addBox(2.2F, 10F, -2.8F, 1F, 3.5F, 1F).texOffs(10, 70).addBox(0F, 10F, -2.8F, 1F, 3.5F, 1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("knee_l", d.get("left_leg").addOrReplaceChild("knee_l", CubeListBuilder.create().texOffs(32, 62).addBox(-2.5F, 4F, -2.8F, 5F, 3F, 1.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        return LayerDefinition.create(mesh, 64, 128);
    }

    private static LayerDefinition demonio_arcano() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("root", mesh.getRoot());
        d.put("head", d.get("root").addOrReplaceChild("head", CubeListBuilder.create().texOffs(21, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("hat", d.get("root").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("body", d.get("root").addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 23).addBox(-4F, 0F, -2F, 8F, 12F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("right_arm", d.get("root").addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(25, 23).addBox(-3F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("left_arm", d.get("root").addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(42, 23).addBox(-1F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("right_leg", d.get("root").addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 40).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("left_leg", d.get("root").addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(17, 40).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("horn_r", d.get("head").addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(0, 57).addBox(-0.9F, -3.5F, -0.9F, 1.8F, 3.5F, 1.8F), PartPose.offsetAndRotation(-3F, -7.5F, -1F, 0.2F, 0F, -0.6F)));
        d.put("horn2_r", d.get("horn_r").addOrReplaceChild("horn2_r", CubeListBuilder.create().texOffs(18, 57).addBox(-0.6F, -3.5F, -0.6F, 1.2F, 3.5F, 1.2F), PartPose.offsetAndRotation(0F, -3.3F, 0F, 0.7F, 0F, 0.5F)));
        d.put("cape", d.get("body").addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 0).addBox(-4.6F, -0.2F, 0F, 9.2F, 21F, 0.7F), PartPose.offsetAndRotation(0F, 0F, 2.1F, 0.12F, 0F, 0F)));
        d.put("collar", d.get("body").addOrReplaceChild("collar", CubeListBuilder.create().texOffs(34, 40).addBox(-4.8F, -1.2F, -2.6F, 9.6F, 2.2F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("horn_l", d.get("head").addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(9, 57).addBox(-0.9F, -3.5F, -0.9F, 1.8F, 3.5F, 1.8F), PartPose.offsetAndRotation(3F, -7.5F, -1F, 0.2F, 0F, 0.6F)));
        d.put("horn2_l", d.get("horn_l").addOrReplaceChild("horn2_l", CubeListBuilder.create().texOffs(24, 57).addBox(-0.6F, -3.5F, -0.6F, 1.2F, 3.5F, 1.2F), PartPose.offsetAndRotation(0F, -3.3F, 0F, 0.7F, 0F, -0.5F)));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static LayerDefinition emperador_galactico() {
        MeshDefinition mesh = new MeshDefinition();
        Map<String, PartDefinition> d = new HashMap<>();
        d.put("root", mesh.getRoot());
        d.put("head", d.get("root").addOrReplaceChild("head", CubeListBuilder.create().texOffs(23, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("hat", d.get("root").addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("body", d.get("root").addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 24).addBox(-4F, 0F, -2F, 8F, 12F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("right_arm", d.get("root").addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(25, 24).addBox(-3F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("left_arm", d.get("root").addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(42, 24).addBox(-1F, -2F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(5F, 2F, 0F, 0F, 0F, 0F)));
        d.put("right_leg", d.get("root").addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 41).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(-1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("left_leg", d.get("root").addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(17, 41).addBox(-2F, 0F, -2F, 4F, 12F, 4F), PartPose.offsetAndRotation(1.9F, 12F, 0F, 0F, 0F, 0F)));
        d.put("crown", d.get("head").addOrReplaceChild("crown", CubeListBuilder.create().texOffs(0, 58).addBox(-4.5F, -9F, -4.5F, 9F, 2.6F, 9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("point0", d.get("crown").addOrReplaceChild("point0", CubeListBuilder.create().texOffs(37, 71).addBox(-0.6F, -2.5F, -0.6F, 1.2F, 2.5F, 1.2F), PartPose.offsetAndRotation(-3.2F, -9F, -3.8F, 0.15F, 0F, 0F)));
        d.put("point1", d.get("crown").addOrReplaceChild("point1", CubeListBuilder.create().texOffs(25, 71).addBox(-0.6F, -3.7F, -0.6F, 1.2F, 3.7F, 1.2F), PartPose.offsetAndRotation(-1.1F, -9F, -3.8F, 0.15F, 0F, 0F)));
        d.put("point2", d.get("crown").addOrReplaceChild("point2", CubeListBuilder.create().texOffs(31, 71).addBox(-0.6F, -3.7F, -0.6F, 1.2F, 3.7F, 1.2F), PartPose.offsetAndRotation(1.1F, -9F, -3.8F, 0.15F, 0F, 0F)));
        d.put("point3", d.get("crown").addOrReplaceChild("point3", CubeListBuilder.create().texOffs(43, 71).addBox(-0.6F, -2.5F, -0.6F, 1.2F, 2.5F, 1.2F), PartPose.offsetAndRotation(3.2F, -9F, -3.8F, 0.15F, 0F, 0F)));
        d.put("crown_gem", d.get("crown").addOrReplaceChild("crown_gem", CubeListBuilder.create().texOffs(49, 71).addBox(-0.8F, -8.4F, -4.75F, 1.6F, 1.4F, 0.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("chest", d.get("body").addOrReplaceChild("chest", CubeListBuilder.create().texOffs(34, 41).addBox(-4.6F, -0.4F, -2.6F, 9.2F, 7F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("pauldron_r", d.get("right_arm").addOrReplaceChild("pauldron_r", CubeListBuilder.create().texOffs(37, 58).addBox(-4.2F, -3.2F, -3F, 5.8F, 3.6F, 6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("pauldron_l", d.get("left_arm").addOrReplaceChild("pauldron_l", CubeListBuilder.create().texOffs(0, 71).addBox(-1.6F, -3.2F, -3F, 5.8F, 3.6F, 6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F)));
        d.put("cape", d.get("body").addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 0).addBox(-5F, -0.4F, 0F, 10F, 22F, 0.7F), PartPose.offsetAndRotation(0F, 0F, 2.2F, 0.1F, 0F, 0F)));
        return LayerDefinition.create(mesh, 64, 128);
    }

}
