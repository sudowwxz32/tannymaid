// GENERADO por tools/gen_smooth.py — no editar a mano.
package com.totallywaifu.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** Accesorios (cubos pequeños) y huesos; el cuerpo liso está en WaifuShapes. */
public final class WaifuModels {
    private WaifuModels() {}

    public static final float ARM_X = 3.75F, ARM_Y = 1.3F, LEG_X = 1.75F, LEG_Y = 10F, HEAD_SCALE = 0.86F;

    public static LayerDefinition bodyKozue() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-3.75F, 1.3F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(3.75F, 1.3F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.75F, 10F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.75F, 10F, 0F));
        PartDefinition p_hammer = p_body.addOrReplaceChild("hammer", CubeListBuilder.create().texOffs(10, 0).addBox(-0.28F, 0F, -0.28F, 0.56F, 3.6F, 0.56F).texOffs(14, 0).addBox(-0.65F, 3.4F, -1.1F, 1.3F, 1F, 2.2F), PartPose.offsetAndRotation(3.45F, 7.6F, -0.4F, 0F, 0F, -0.18F));
        PartDefinition p_pouch_r = p_body.addOrReplaceChild("pouch_r", CubeListBuilder.create().texOffs(22, 0).addBox(-1F, 0F, -0.9F, 2F, 2F, 1.1F), PartPose.offsetAndRotation(-2.5F, 7.7F, -1.8F, 0F, 0.35F, 0F));
        PartDefinition p_knot = p_head.addOrReplaceChild("knot", CubeListBuilder.create().texOffs(8, 6).addBox(-0.8F, -0.8F, -0.45F, 1.6F, 1.6F, 0.9F).texOffs(14, 6).addBox(-1.5F, 0.3F, 0F, 1F, 2.4F, 0.4F).texOffs(18, 6).addBox(0.4F, 0.3F, 0F, 1F, 2.1F, 0.4F), PartPose.offsetAndRotation(1.8F, -6.1F, 4F, 0.3F, 0F, 0.3F));
        PartDefinition p_pencil = p_head.addOrReplaceChild("pencil", CubeListBuilder.create().texOffs(0, 0).addBox(-0.25F, -0.25F, -2F, 0.5F, 0.5F, 4F), PartPose.offsetAndRotation(-4.3F, -3.6F, -0.2F, 0.25F, 0F, 0.2F));
        PartDefinition p_pouch_l = p_body.addOrReplaceChild("pouch_l", CubeListBuilder.create().texOffs(0, 6).addBox(-1F, 0F, -0.9F, 2F, 2F, 1.1F), PartPose.offsetAndRotation(2.5F, 7.7F, -1.8F, 0F, -0.35F, 0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static Map<String, ModelPart> bakeBodyKozue(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("hat", root.getChild("hat"));
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("hammer", m.get("body").getChild("hammer"));
        m.put("pouch_r", m.get("body").getChild("pouch_r"));
        m.put("knot", m.get("head").getChild("knot"));
        m.put("pencil", m.get("head").getChild("pencil"));
        m.put("pouch_l", m.get("body").getChild("pouch_l"));
        return m;
    }

    public static LayerDefinition bodyHomura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-3.75F, 1.3F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(3.75F, 1.3F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.75F, 10F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.75F, 10F, 0F));
        PartDefinition p_apron = p_body.addOrReplaceChild("apron", CubeListBuilder.create().texOffs(0, 0).addBox(-2.4F, 0F, -0.3F, 4.8F, 4.8F, 0.3F), PartPose.offsetAndRotation(0F, 7.4F, -2F, -0.3F, 0F, 0F));
        PartDefinition p_ember = p_body.addOrReplaceChild("ember", CubeListBuilder.create().texOffs(20, 12).addBox(-0.4F, -0.4F, -0.3F, 0.8F, 0.8F, 0.45F), PartPose.offsetAndRotation(0F, -0.7F, -1.05F, 0F, 0F, 0.78F));
        PartDefinition p_goggles = p_head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(0, 12).addBox(-4.3F, -0.4F, -0.3F, 8.6F, 0.8F, 0.6F), PartPose.offsetAndRotation(0F, -7.6F, -2.6F, -0.4F, 0F, 0F));
        PartDefinition p_lens_r = p_goggles.addOrReplaceChild("lens_r", CubeListBuilder.create().texOffs(12, 0).addBox(-1.2F, -1F, -0.6F, 2.4F, 2F, 1.2F), PartPose.offsetAndRotation(-1.7F, 0F, -0.5F, 0F, 0.1F, 0F));
        PartDefinition p_clip_r = p_head.addOrReplaceChild("clip_r", CubeListBuilder.create().texOffs(18, 7).addBox(-0.7F, -1.7F, -0.35F, 1.4F, 2.2F, 0.7F), PartPose.offsetAndRotation(-3.3F, -6.3F, -2.5F, 0F, 0F, -0.35F));
        PartDefinition p_tie_r = p_head.addOrReplaceChild("tie_r", CubeListBuilder.create().texOffs(21, 0).addBox(-0.9F, -0.9F, -0.9F, 1.8F, 1.8F, 1.8F), PartPose.offsetAndRotation(-3.2F, -6.2F, 3.2F, 0F, 0F, 0F));
        PartDefinition p_lens_l = p_goggles.addOrReplaceChild("lens_l", CubeListBuilder.create().texOffs(0, 7).addBox(-1.2F, -1F, -0.6F, 2.4F, 2F, 1.2F), PartPose.offsetAndRotation(1.7F, 0F, -0.5F, 0F, -0.1F, 0F));
        PartDefinition p_clip_l = p_head.addOrReplaceChild("clip_l", CubeListBuilder.create().texOffs(24, 7).addBox(-0.7F, -1.7F, -0.35F, 1.4F, 2.2F, 0.7F), PartPose.offsetAndRotation(3.3F, -6.3F, -2.5F, 0F, 0F, 0.35F));
        PartDefinition p_tie_l = p_head.addOrReplaceChild("tie_l", CubeListBuilder.create().texOffs(9, 7).addBox(-0.9F, -0.9F, -0.9F, 1.8F, 1.8F, 1.8F), PartPose.offsetAndRotation(3.2F, -6.2F, 3.2F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static Map<String, ModelPart> bakeBodyHomura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("hat", root.getChild("hat"));
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("apron", m.get("body").getChild("apron"));
        m.put("ember", m.get("body").getChild("ember"));
        m.put("goggles", m.get("head").getChild("goggles"));
        m.put("lens_r", m.get("goggles").getChild("lens_r"));
        m.put("clip_r", m.get("head").getChild("clip_r"));
        m.put("tie_r", m.get("head").getChild("tie_r"));
        m.put("lens_l", m.get("goggles").getChild("lens_l"));
        m.put("clip_l", m.get("head").getChild("clip_l"));
        m.put("tie_l", m.get("head").getChild("tie_l"));
        return m;
    }

    public static LayerDefinition bodyKura() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-3.75F, 1.3F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(3.75F, 1.3F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.75F, 10F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.75F, 10F, 0F));
        PartDefinition p_latch = p_body.addOrReplaceChild("latch", CubeListBuilder.create().texOffs(23, 14).addBox(-0.75F, -0.4F, -0.35F, 1.5F, 1.8F, 0.5F).texOffs(5, 22).addBox(-0.28F, 0.25F, -0.5F, 0.56F, 0.56F, 0.2F), PartPose.offsetAndRotation(0F, 1.4F, -2.45F, 0.15F, 0F, 0F));
        PartDefinition p_back_bow_knot = p_body.addOrReplaceChild("back_bow_knot", CubeListBuilder.create().texOffs(17, 14).addBox(-0.715F, -0.715F, -0.52F, 1.43F, 1.43F, 1.04F), PartPose.offsetAndRotation(0F, 6.6F, 1.65F, 0F, 0F, 0F));
        PartDefinition p_back_bow_wing_r = p_back_bow_knot.addOrReplaceChild("back_bow_wing_r", CubeListBuilder.create().texOffs(0, 9).addBox(-2.6F, -1.17F, -0.455F, 2.6F, 2.34F, 0.91F), PartPose.offsetAndRotation(-0.52F, 0F, 0F, 0F, 0.2F, 0.3F));
        PartDefinition p_back_bow_wing_l = p_back_bow_knot.addOrReplaceChild("back_bow_wing_l", CubeListBuilder.create().texOffs(9, 9).addBox(0F, -1.17F, -0.455F, 2.6F, 2.34F, 0.91F), PartPose.offsetAndRotation(0.52F, 0F, 0F, 0F, -0.2F, -0.3F));
        PartDefinition p_back_bow_tail_r = p_back_bow_knot.addOrReplaceChild("back_bow_tail_r", CubeListBuilder.create().texOffs(18, 9).addBox(-0.585F, 0F, -0.325F, 1.17F, 3.12F, 0.65F), PartPose.offsetAndRotation(-0.26F, 0.52F, 0F, 0F, 0F, 0.25F));
        PartDefinition p_back_bow_tail_l = p_back_bow_knot.addOrReplaceChild("back_bow_tail_l", CubeListBuilder.create().texOffs(23, 9).addBox(-0.585F, 0F, -0.325F, 1.17F, 3.12F, 0.65F), PartPose.offsetAndRotation(0.26F, 0.52F, 0F, 0F, 0F, -0.25F));
        PartDefinition p_apron = p_body.addOrReplaceChild("apron", CubeListBuilder.create().texOffs(17, 0).addBox(-2.4F, 0F, -0.3F, 4.8F, 4.4F, 0.3F).texOffs(4, 18).addBox(-2.6F, 4.3F, -0.35F, 5.2F, 0.7F, 0.4F), PartPose.offsetAndRotation(0F, 7.2F, -2.05F, -0.32F, 0F, 0F));
        PartDefinition p_backpack = p_body.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(0, 0).addBox(-2.6F, 0F, 0F, 5.2F, 4.8F, 2.5F), PartPose.offsetAndRotation(0F, 0.9F, 1.6F, 0.06F, 0F, 0F));
        PartDefinition p_headdress = p_head.addOrReplaceChild("headdress", CubeListBuilder.create().texOffs(0, 14).addBox(-3.4F, -1.5F, -0.4F, 6.8F, 1.7F, 0.8F), PartPose.offsetAndRotation(0F, -7.6F, -1.5F, -0.45F, 0F, 0F));
        PartDefinition p_hair_bow_knot = p_head.addOrReplaceChild("hair_bow_knot", CubeListBuilder.create().texOffs(0, 22).addBox(-0.44F, -0.44F, -0.32F, 0.88F, 0.88F, 0.64F), PartPose.offsetAndRotation(3.2F, -7F, 1.6F, 0F, -0.6F, 0F));
        PartDefinition p_hair_bow_wing_r = p_hair_bow_knot.addOrReplaceChild("hair_bow_wing_r", CubeListBuilder.create().texOffs(17, 18).addBox(-1.6F, -0.72F, -0.28F, 1.6F, 1.44F, 0.56F), PartPose.offsetAndRotation(-0.32F, 0F, 0F, 0F, 0.2F, 0.3F));
        PartDefinition p_hair_bow_wing_l = p_hair_bow_knot.addOrReplaceChild("hair_bow_wing_l", CubeListBuilder.create().texOffs(23, 18).addBox(0F, -0.72F, -0.28F, 1.6F, 1.44F, 0.56F), PartPose.offsetAndRotation(0.32F, 0F, 0F, 0F, -0.2F, -0.3F));
        PartDefinition p_hair_bow_tail_r = p_hair_bow_knot.addOrReplaceChild("hair_bow_tail_r", CubeListBuilder.create().texOffs(28, 14).addBox(-0.36F, 0F, -0.2F, 0.72F, 1.92F, 0.4F), PartPose.offsetAndRotation(-0.16F, 0.32F, 0F, 0F, 0F, 0.25F));
        PartDefinition p_hair_bow_tail_l = p_hair_bow_knot.addOrReplaceChild("hair_bow_tail_l", CubeListBuilder.create().texOffs(0, 18).addBox(-0.36F, 0F, -0.2F, 0.72F, 1.92F, 0.4F), PartPose.offsetAndRotation(0.16F, 0.32F, 0F, 0F, 0F, -0.25F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static Map<String, ModelPart> bakeBodyKura(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("hat", root.getChild("hat"));
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("latch", m.get("body").getChild("latch"));
        m.put("back_bow_knot", m.get("body").getChild("back_bow_knot"));
        m.put("back_bow_wing_r", m.get("back_bow_knot").getChild("back_bow_wing_r"));
        m.put("back_bow_wing_l", m.get("back_bow_knot").getChild("back_bow_wing_l"));
        m.put("back_bow_tail_r", m.get("back_bow_knot").getChild("back_bow_tail_r"));
        m.put("back_bow_tail_l", m.get("back_bow_knot").getChild("back_bow_tail_l"));
        m.put("apron", m.get("body").getChild("apron"));
        m.put("backpack", m.get("body").getChild("backpack"));
        m.put("headdress", m.get("head").getChild("headdress"));
        m.put("hair_bow_knot", m.get("head").getChild("hair_bow_knot"));
        m.put("hair_bow_wing_r", m.get("hair_bow_knot").getChild("hair_bow_wing_r"));
        m.put("hair_bow_wing_l", m.get("hair_bow_knot").getChild("hair_bow_wing_l"));
        m.put("hair_bow_tail_r", m.get("hair_bow_knot").getChild("hair_bow_tail_r"));
        m.put("hair_bow_tail_l", m.get("hair_bow_knot").getChild("hair_bow_tail_l"));
        return m;
    }

    public static LayerDefinition bodyShiori() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-3.75F, 1.3F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(3.75F, 1.3F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.75F, 10F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.75F, 10F, 0F));
        PartDefinition p_brooch = p_body.addOrReplaceChild("brooch", CubeListBuilder.create().texOffs(22, 0).addBox(-0.65F, -0.65F, -0.35F, 1.3F, 1.3F, 0.55F), PartPose.offsetAndRotation(0F, 0.7F, -1.85F, 0F, 0F, 0.78F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, 0F, 0F, 7F, 15.4F, 0.4F), PartPose.offsetAndRotation(0F, -0.4F, 1.7F, 0.1F, 0F, 0F));
        PartDefinition p_tiara_gem = p_head.addOrReplaceChild("tiara_gem", CubeListBuilder.create().texOffs(16, 0).addBox(-0.8F, -0.8F, -0.35F, 1.6F, 1.6F, 0.6F), PartPose.offsetAndRotation(0F, -6.9F, -4.35F, 0F, 0F, 0.78F));
        PartDefinition p_earring_r = p_head.addOrReplaceChild("earring_r", CubeListBuilder.create().texOffs(27, 0).addBox(-0.25F, 0F, -0.25F, 0.5F, 1.3F, 0.5F), PartPose.offsetAndRotation(-4.25F, -2.6F, -0.6F, 0F, 0F, 0F));
        PartDefinition p_earring_l = p_head.addOrReplaceChild("earring_l", CubeListBuilder.create().texOffs(30, 0).addBox(-0.25F, 0F, -0.25F, 0.5F, 1.3F, 0.5F), PartPose.offsetAndRotation(4.25F, -2.6F, -0.6F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static Map<String, ModelPart> bakeBodyShiori(ModelPart root) {
        Map<String, ModelPart> m = new LinkedHashMap<>();
        m.put("head", root.getChild("head"));
        m.put("hat", root.getChild("hat"));
        m.put("body", root.getChild("body"));
        m.put("right_arm", root.getChild("right_arm"));
        m.put("left_arm", root.getChild("left_arm"));
        m.put("right_leg", root.getChild("right_leg"));
        m.put("left_leg", root.getChild("left_leg"));
        m.put("brooch", m.get("body").getChild("brooch"));
        m.put("cape", m.get("body").getChild("cape"));
        m.put("tiara_gem", m.get("head").getChild("tiara_gem"));
        m.put("earring_r", m.get("head").getChild("earring_r"));
        m.put("earring_l", m.get("head").getChild("earring_l"));
        return m;
    }

}
