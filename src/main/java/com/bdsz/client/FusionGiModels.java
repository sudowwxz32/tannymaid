// GENERADO por tools/gen_fusion_gi.py — no editar a mano.
package com.bdsz.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public final class FusionGiModels {
    private FusionGiModels() {}

    /** Piezas (hijas de head, body, brazos o piernas). Las que empiezan con el nombre del mob solo se ven con esa fusión. */
    public static final String[][] PARTS = {
            {"body", "chest"},
            {"body", "obi"},
            {"body", "obi_knot"},
            {"body", "obi_tail_a"},
            {"body", "obi_tail_b"},
            {"body", "hips"},
            {"right_leg", "pant_r"},
            {"right_leg", "ankle_r"},
            {"left_leg", "pant_l"},
            {"left_leg", "ankle_l"},
            {"right_arm", "shoulder_r"},
            {"right_arm", "glove_r"},
            {"left_arm", "shoulder_l"},
            {"left_arm", "glove_l"},
            {"body", "skeleton_rib0"},
            {"body", "skeleton_rib1"},
            {"body", "skeleton_rib2"},
            {"body", "skeleton_rib3"},
            {"body", "skeleton_sternum"},
            {"body", "skeleton_spine"},
            {"body", "golem_plate"},
            {"body", "golem_vine0"},
            {"body", "golem_vine1"},
            {"body", "golem_vine2"},
            {"body", "golem_vine3"},
            {"head", "warden_tendril_r"},
            {"head", "warden_tendril_l"},
            {"body", "zombie_rag_a"},
            {"body", "zombie_rag_b"},
            {"right_arm", "enderman_spike_r"},
            {"left_arm", "enderman_spike_l"},
            {"head", "dragon_horn_r"},
            {"body", "dragon_wing_r"},
            {"head", "dragon_horn_l"},
            {"body", "dragon_wing_l"},
            {"body", "dragon_spike0"},
            {"body", "dragon_spike1"},
            {"body", "dragon_spike2"},
            {"body", "dragon_spike3"},
            {"body", "dragon_tail"},
            {"body", "creeper_hood"},
    };

    public static LayerDefinition createWide() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        p_body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 16).addBox(-4.35F, -0.3F, -2.35F, 8.7F, 7.6F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("obi", CubeListBuilder.create().texOffs(0, 65).addBox(-4.6F, 7F, -2.6F, 9.2F, 1.5F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("obi_knot", CubeListBuilder.create().texOffs(41, 80).addBox(-0.8F, -0.8F, -0.6F, 1.6F, 1.6F, 0.9F), PartPose.offsetAndRotation(-1.2F, 7.8F, -2.7F, 0F, 0F, 0.2F));
        p_body.addOrReplaceChild("obi_tail_a", CubeListBuilder.create().texOffs(27, 80).addBox(-0.55F, 0F, -0.25F, 1.1F, 3.6F, 0.4F), PartPose.offsetAndRotation(-1.5F, 8.4F, -2.75F, -0.08F, 0F, 0.25F));
        p_body.addOrReplaceChild("obi_tail_b", CubeListBuilder.create().texOffs(31, 80).addBox(-0.5F, 0F, -0.25F, 1F, 3F, 0.4F), PartPose.offsetAndRotation(-0.6F, 8.4F, -2.75F, -0.05F, 0F, -0.1F));
        p_body.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(20, 44).addBox(-4.45F, 8.3F, -2.45F, 8.9F, 3.9F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_right_leg.addOrReplaceChild("pant_r", CubeListBuilder.create().texOffs(0, 0).addBox(-2.65F, -0.3F, -2.65F, 5.3F, 9.6F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_right_leg.addOrReplaceChild("ankle_r", CubeListBuilder.create().texOffs(37, 65).addBox(-2.25F, 9.2F, -2.25F, 4.5F, 1.3F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_left_leg.addOrReplaceChild("pant_l", CubeListBuilder.create().texOffs(23, 0).addBox(-2.65F, -0.3F, -2.65F, 5.3F, 9.6F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_left_leg.addOrReplaceChild("ankle_l", CubeListBuilder.create().texOffs(0, 73).addBox(-2.25F, 9.2F, -2.25F, 4.5F, 1.3F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_right_arm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(34, 30).addBox(-3.3F, -2.3F, -2.3F, 4.6F, 5F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_right_arm.addOrReplaceChild("glove_r", CubeListBuilder.create().texOffs(0, 55).addBox(-3.3F, 6.3F, -2.3F, 4.6F, 3.9F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_left_arm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(0, 44).addBox(-1.3F, -2.3F, -2.3F, 4.6F, 5F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_left_arm.addOrReplaceChild("glove_l", CubeListBuilder.create().texOffs(20, 55).addBox(-1.3F, 6.3F, -2.3F, 4.6F, 3.9F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_rib0", CubeListBuilder.create().texOffs(28, 86).addBox(-3.6F, -0.3F, -0.3F, 7.2F, 0.6F, 0.55F), PartPose.offsetAndRotation(0F, 1.4F, -2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_rib1", CubeListBuilder.create().texOffs(45, 86).addBox(-3.45F, -0.3F, -0.3F, 6.9F, 0.6F, 0.55F), PartPose.offsetAndRotation(0F, 2.8F, -2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_rib2", CubeListBuilder.create().texOffs(0, 90).addBox(-3.3F, -0.3F, -0.3F, 6.6F, 0.6F, 0.55F), PartPose.offsetAndRotation(0F, 4.2F, -2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_rib3", CubeListBuilder.create().texOffs(16, 90).addBox(-3.15F, -0.3F, -0.3F, 6.3F, 0.6F, 0.55F), PartPose.offsetAndRotation(0F, 5.6F, -2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_sternum", CubeListBuilder.create().texOffs(30, 65).addBox(-0.45F, 0F, -0.3F, 0.9F, 5.6F, 0.55F), PartPose.offsetAndRotation(0F, 0.8F, -2.6F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_spine", CubeListBuilder.create().texOffs(40, 55).addBox(-0.5F, 0F, -0.25F, 1F, 6.9F, 0.6F), PartPose.offsetAndRotation(0F, 0.3F, 2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("golem_plate", CubeListBuilder.create().texOffs(0, 80).addBox(-3.2F, 0F, -0.5F, 6.4F, 4.4F, 0.6F), PartPose.offsetAndRotation(0F, 1F, -2.55F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("golem_vine0", CubeListBuilder.create().texOffs(34, 65).addBox(-0.3F, 0F, -0.2F, 0.6F, 6F, 0.4F), PartPose.offsetAndRotation(-3.6F, -0.2F, -2.5F, 0.05F, 0F, -0.06F));
        p_body.addOrReplaceChild("golem_vine1", CubeListBuilder.create().texOffs(35, 80).addBox(-0.3F, 0F, -0.2F, 0.6F, 3.6F, 0.4F), PartPose.offsetAndRotation(-1.2F, -0.2F, 2.5F, 0.05F, 0F, -0.02F));
        p_body.addOrReplaceChild("golem_vine2", CubeListBuilder.create().texOffs(31, 73).addBox(-0.3F, 0F, -0.2F, 0.6F, 5.2F, 0.4F), PartPose.offsetAndRotation(2.4F, -0.2F, -2.5F, 0.05F, 0F, 0.02F));
        p_body.addOrReplaceChild("golem_vine3", CubeListBuilder.create().texOffs(38, 80).addBox(-0.3F, 0F, -0.2F, 0.6F, 2.8F, 0.4F), PartPose.offsetAndRotation(3.8F, -0.2F, 2.5F, 0.05F, 0F, 0.06F));
        p_head.addOrReplaceChild("warden_tendril_r", CubeListBuilder.create().texOffs(19, 73).addBox(-0.6F, -4.2F, -0.6F, 1.2F, 4.4F, 1.2F), PartPose.offsetAndRotation(-3.6F, -7.6F, 0.4F, -0.2F, 0F, -0.85F));
        p_head.addOrReplaceChild("warden_tendril_l", CubeListBuilder.create().texOffs(25, 73).addBox(-0.6F, -4.2F, -0.6F, 1.2F, 4.4F, 1.2F), PartPose.offsetAndRotation(3.6F, -7.6F, 0.4F, -0.2F, 0F, 0.85F));
        p_body.addOrReplaceChild("zombie_rag_a", CubeListBuilder.create().texOffs(47, 80).addBox(-1F, 0F, -0.15F, 2F, 2.6F, 0.3F), PartPose.offsetAndRotation(-2.2F, 6.6F, -2.5F, 0.12F, 0F, 0.18F));
        p_body.addOrReplaceChild("zombie_rag_b", CubeListBuilder.create().texOffs(53, 80).addBox(-1.1F, 0F, -0.15F, 2.2F, 2.2F, 0.3F), PartPose.offsetAndRotation(2.4F, 6.4F, 2.5F, -0.1F, 0F, -0.15F));
        p_right_arm.addOrReplaceChild("enderman_spike_r", CubeListBuilder.create().texOffs(18, 86).addBox(-0.5F, -1.8F, -0.5F, 1F, 1.9F, 1F), PartPose.offsetAndRotation(-1F, -2.4F, 0F, 0F, 0F, 0F));
        p_left_arm.addOrReplaceChild("enderman_spike_l", CubeListBuilder.create().texOffs(23, 86).addBox(-0.5F, -1.8F, -0.5F, 1F, 1.9F, 1F), PartPose.offsetAndRotation(1F, -2.4F, 0F, 0F, 0F, 0F));
        p_head.addOrReplaceChild("dragon_horn_r", CubeListBuilder.create().texOffs(15, 80).addBox(-0.6F, -3.4F, -0.6F, 1.2F, 3.6F, 1.2F), PartPose.offsetAndRotation(-2.6F, -8F, 1.2F, -0.65F, 0F, -0.25F));
        p_body.addOrReplaceChild("dragon_wing_r", CubeListBuilder.create().texOffs(28, 16).addBox(-11F, -6F, 0F, 11F, 12F, 0.4F), PartPose.offsetAndRotation(-1.6F, 1.2F, 2.5F, 0.2F, -0.45F, -0.35F));
        p_head.addOrReplaceChild("dragon_horn_l", CubeListBuilder.create().texOffs(21, 80).addBox(-0.6F, -3.4F, -0.6F, 1.2F, 3.6F, 1.2F), PartPose.offsetAndRotation(2.6F, -8F, 1.2F, -0.65F, 0F, 0.25F));
        p_body.addOrReplaceChild("dragon_wing_l", CubeListBuilder.create().texOffs(0, 30).addBox(0F, -6F, 0F, 11F, 12F, 0.4F), PartPose.offsetAndRotation(1.6F, 1.2F, 2.5F, 0.2F, 0.45F, 0.35F));
        p_body.addOrReplaceChild("dragon_spike0", CubeListBuilder.create().texOffs(59, 80).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 1.5F), PartPose.offsetAndRotation(0F, 0.8F, 2.4F, 0.35F, 0F, 0F));
        p_body.addOrReplaceChild("dragon_spike1", CubeListBuilder.create().texOffs(0, 86).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 1.5F), PartPose.offsetAndRotation(0F, 2.8F, 2.4F, 0.35F, 0F, 0F));
        p_body.addOrReplaceChild("dragon_spike2", CubeListBuilder.create().texOffs(6, 86).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 1.5F), PartPose.offsetAndRotation(0F, 4.8F, 2.4F, 0.35F, 0F, 0F));
        p_body.addOrReplaceChild("dragon_spike3", CubeListBuilder.create().texOffs(12, 86).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 1.5F), PartPose.offsetAndRotation(0F, 6.8F, 2.4F, 0.35F, 0F, 0F));
        p_body.addOrReplaceChild("dragon_tail", CubeListBuilder.create().texOffs(24, 30).addBox(-1.1F, 0F, -1.1F, 2.2F, 8F, 2.2F), PartPose.offsetAndRotation(0F, 10F, 2.2F, 0.75F, 0F, 0F));
        p_body.addOrReplaceChild("creeper_hood", CubeListBuilder.create().texOffs(34, 73).addBox(-3.6F, -0.8F, 0F, 7.2F, 3F, 1.6F), PartPose.offsetAndRotation(0F, 0.2F, 2.4F, 0.3F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }

    public static LayerDefinition createSlim() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        p_body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 16).addBox(-4.35F, -0.3F, -2.35F, 8.7F, 7.6F, 4.7F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("obi", CubeListBuilder.create().texOffs(0, 65).addBox(-4.6F, 7F, -2.6F, 9.2F, 1.5F, 5.2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("obi_knot", CubeListBuilder.create().texOffs(41, 80).addBox(-0.8F, -0.8F, -0.6F, 1.6F, 1.6F, 0.9F), PartPose.offsetAndRotation(-1.2F, 7.8F, -2.7F, 0F, 0F, 0.2F));
        p_body.addOrReplaceChild("obi_tail_a", CubeListBuilder.create().texOffs(27, 80).addBox(-0.55F, 0F, -0.25F, 1.1F, 3.6F, 0.4F), PartPose.offsetAndRotation(-1.5F, 8.4F, -2.75F, -0.08F, 0F, 0.25F));
        p_body.addOrReplaceChild("obi_tail_b", CubeListBuilder.create().texOffs(31, 80).addBox(-0.5F, 0F, -0.25F, 1F, 3F, 0.4F), PartPose.offsetAndRotation(-0.6F, 8.4F, -2.75F, -0.05F, 0F, -0.1F));
        p_body.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(20, 44).addBox(-4.45F, 8.3F, -2.45F, 8.9F, 3.9F, 4.9F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_right_leg.addOrReplaceChild("pant_r", CubeListBuilder.create().texOffs(0, 0).addBox(-2.65F, -0.3F, -2.65F, 5.3F, 9.6F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_right_leg.addOrReplaceChild("ankle_r", CubeListBuilder.create().texOffs(37, 65).addBox(-2.25F, 9.2F, -2.25F, 4.5F, 1.3F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_left_leg.addOrReplaceChild("pant_l", CubeListBuilder.create().texOffs(23, 0).addBox(-2.65F, -0.3F, -2.65F, 5.3F, 9.6F, 5.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_left_leg.addOrReplaceChild("ankle_l", CubeListBuilder.create().texOffs(0, 73).addBox(-2.25F, 9.2F, -2.25F, 4.5F, 1.3F, 4.5F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_right_arm.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(34, 30).addBox(-2.3F, -2.3F, -2.3F, 3.6F, 5F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_right_arm.addOrReplaceChild("glove_r", CubeListBuilder.create().texOffs(0, 55).addBox(-2.3F, 6.3F, -2.3F, 3.6F, 3.9F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_left_arm.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(0, 44).addBox(-1.3F, -2.3F, -2.3F, 3.6F, 5F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_left_arm.addOrReplaceChild("glove_l", CubeListBuilder.create().texOffs(20, 55).addBox(-1.3F, 6.3F, -2.3F, 3.6F, 3.9F, 4.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_rib0", CubeListBuilder.create().texOffs(28, 86).addBox(-3.6F, -0.3F, -0.3F, 7.2F, 0.6F, 0.55F), PartPose.offsetAndRotation(0F, 1.4F, -2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_rib1", CubeListBuilder.create().texOffs(45, 86).addBox(-3.45F, -0.3F, -0.3F, 6.9F, 0.6F, 0.55F), PartPose.offsetAndRotation(0F, 2.8F, -2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_rib2", CubeListBuilder.create().texOffs(0, 90).addBox(-3.3F, -0.3F, -0.3F, 6.6F, 0.6F, 0.55F), PartPose.offsetAndRotation(0F, 4.2F, -2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_rib3", CubeListBuilder.create().texOffs(16, 90).addBox(-3.15F, -0.3F, -0.3F, 6.3F, 0.6F, 0.55F), PartPose.offsetAndRotation(0F, 5.6F, -2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_sternum", CubeListBuilder.create().texOffs(30, 65).addBox(-0.45F, 0F, -0.3F, 0.9F, 5.6F, 0.55F), PartPose.offsetAndRotation(0F, 0.8F, -2.6F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("skeleton_spine", CubeListBuilder.create().texOffs(40, 55).addBox(-0.5F, 0F, -0.25F, 1F, 6.9F, 0.6F), PartPose.offsetAndRotation(0F, 0.3F, 2.45F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("golem_plate", CubeListBuilder.create().texOffs(0, 80).addBox(-3.2F, 0F, -0.5F, 6.4F, 4.4F, 0.6F), PartPose.offsetAndRotation(0F, 1F, -2.55F, 0F, 0F, 0F));
        p_body.addOrReplaceChild("golem_vine0", CubeListBuilder.create().texOffs(34, 65).addBox(-0.3F, 0F, -0.2F, 0.6F, 6F, 0.4F), PartPose.offsetAndRotation(-3.6F, -0.2F, -2.5F, 0.05F, 0F, -0.06F));
        p_body.addOrReplaceChild("golem_vine1", CubeListBuilder.create().texOffs(35, 80).addBox(-0.3F, 0F, -0.2F, 0.6F, 3.6F, 0.4F), PartPose.offsetAndRotation(-1.2F, -0.2F, 2.5F, 0.05F, 0F, -0.02F));
        p_body.addOrReplaceChild("golem_vine2", CubeListBuilder.create().texOffs(31, 73).addBox(-0.3F, 0F, -0.2F, 0.6F, 5.2F, 0.4F), PartPose.offsetAndRotation(2.4F, -0.2F, -2.5F, 0.05F, 0F, 0.02F));
        p_body.addOrReplaceChild("golem_vine3", CubeListBuilder.create().texOffs(38, 80).addBox(-0.3F, 0F, -0.2F, 0.6F, 2.8F, 0.4F), PartPose.offsetAndRotation(3.8F, -0.2F, 2.5F, 0.05F, 0F, 0.06F));
        p_head.addOrReplaceChild("warden_tendril_r", CubeListBuilder.create().texOffs(19, 73).addBox(-0.6F, -4.2F, -0.6F, 1.2F, 4.4F, 1.2F), PartPose.offsetAndRotation(-3.6F, -7.6F, 0.4F, -0.2F, 0F, -0.85F));
        p_head.addOrReplaceChild("warden_tendril_l", CubeListBuilder.create().texOffs(25, 73).addBox(-0.6F, -4.2F, -0.6F, 1.2F, 4.4F, 1.2F), PartPose.offsetAndRotation(3.6F, -7.6F, 0.4F, -0.2F, 0F, 0.85F));
        p_body.addOrReplaceChild("zombie_rag_a", CubeListBuilder.create().texOffs(47, 80).addBox(-1F, 0F, -0.15F, 2F, 2.6F, 0.3F), PartPose.offsetAndRotation(-2.2F, 6.6F, -2.5F, 0.12F, 0F, 0.18F));
        p_body.addOrReplaceChild("zombie_rag_b", CubeListBuilder.create().texOffs(53, 80).addBox(-1.1F, 0F, -0.15F, 2.2F, 2.2F, 0.3F), PartPose.offsetAndRotation(2.4F, 6.4F, 2.5F, -0.1F, 0F, -0.15F));
        p_right_arm.addOrReplaceChild("enderman_spike_r", CubeListBuilder.create().texOffs(18, 86).addBox(-0.5F, -1.8F, -0.5F, 1F, 1.9F, 1F), PartPose.offsetAndRotation(-0.5F, -2.4F, 0F, 0F, 0F, 0F));
        p_left_arm.addOrReplaceChild("enderman_spike_l", CubeListBuilder.create().texOffs(23, 86).addBox(-0.5F, -1.8F, -0.5F, 1F, 1.9F, 1F), PartPose.offsetAndRotation(0.5F, -2.4F, 0F, 0F, 0F, 0F));
        p_head.addOrReplaceChild("dragon_horn_r", CubeListBuilder.create().texOffs(15, 80).addBox(-0.6F, -3.4F, -0.6F, 1.2F, 3.6F, 1.2F), PartPose.offsetAndRotation(-2.6F, -8F, 1.2F, -0.65F, 0F, -0.25F));
        p_body.addOrReplaceChild("dragon_wing_r", CubeListBuilder.create().texOffs(28, 16).addBox(-11F, -6F, 0F, 11F, 12F, 0.4F), PartPose.offsetAndRotation(-1.6F, 1.2F, 2.5F, 0.2F, -0.45F, -0.35F));
        p_head.addOrReplaceChild("dragon_horn_l", CubeListBuilder.create().texOffs(21, 80).addBox(-0.6F, -3.4F, -0.6F, 1.2F, 3.6F, 1.2F), PartPose.offsetAndRotation(2.6F, -8F, 1.2F, -0.65F, 0F, 0.25F));
        p_body.addOrReplaceChild("dragon_wing_l", CubeListBuilder.create().texOffs(0, 30).addBox(0F, -6F, 0F, 11F, 12F, 0.4F), PartPose.offsetAndRotation(1.6F, 1.2F, 2.5F, 0.2F, 0.45F, 0.35F));
        p_body.addOrReplaceChild("dragon_spike0", CubeListBuilder.create().texOffs(59, 80).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 1.5F), PartPose.offsetAndRotation(0F, 0.8F, 2.4F, 0.35F, 0F, 0F));
        p_body.addOrReplaceChild("dragon_spike1", CubeListBuilder.create().texOffs(0, 86).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 1.5F), PartPose.offsetAndRotation(0F, 2.8F, 2.4F, 0.35F, 0F, 0F));
        p_body.addOrReplaceChild("dragon_spike2", CubeListBuilder.create().texOffs(6, 86).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 1.5F), PartPose.offsetAndRotation(0F, 4.8F, 2.4F, 0.35F, 0F, 0F));
        p_body.addOrReplaceChild("dragon_spike3", CubeListBuilder.create().texOffs(12, 86).addBox(-0.45F, -0.45F, 0F, 0.9F, 0.9F, 1.5F), PartPose.offsetAndRotation(0F, 6.8F, 2.4F, 0.35F, 0F, 0F));
        p_body.addOrReplaceChild("dragon_tail", CubeListBuilder.create().texOffs(24, 30).addBox(-1.1F, 0F, -1.1F, 2.2F, 8F, 2.2F), PartPose.offsetAndRotation(0F, 10F, 2.2F, 0.75F, 0F, 0F));
        p_body.addOrReplaceChild("creeper_hood", CubeListBuilder.create().texOffs(34, 73).addBox(-3.6F, -0.8F, 0F, 7.2F, 3F, 1.6F), PartPose.offsetAndRotation(0F, 0.2F, 2.4F, 0.3F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 128);
    }
}
