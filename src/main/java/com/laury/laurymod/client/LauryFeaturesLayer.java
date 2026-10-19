package com.laury.laurymod.client;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.entity.LauryEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Map;

/** Cuernos, orejas, cabello, alas, cola, busto, caderas y botas en 3D, animados. */
public class LauryFeaturesLayer extends RenderLayer<LauryEntity, LauryHumanModel> {
    private static final ResourceLocation TEX = new ResourceLocation(LauryMod.MODID, "textures/entity/laury_features.png");
    private static final String[] ANCHORS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};

    private final ModelPart root;
    private final Map<String, ModelPart> p;

    public LauryFeaturesLayer(RenderLayerParent<LauryEntity, LauryHumanModel> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(ClientSetup.FEATURES);
        this.p = LauryGeneratedModels.bakeFeatures(root);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, LauryEntity e, float limbSwing, float limbAmount,
                       float partial, float age, float headYaw, float headPitch) {
        if (e.isInvisible()) return;
        root.getAllParts().forEach(ModelPart::resetPose);
        LauryHumanModel m = getParentModel();
        p.get("head").copyFrom(m.head);
        p.get("body").copyFrom(m.body);
        p.get("right_arm").copyFrom(m.rightArm);
        p.get("left_arm").copyFrom(m.leftArm);
        p.get("right_leg").copyFrom(m.rightLeg);
        p.get("left_leg").copyFrom(m.leftLeg);

        // Alas: respiran suave, se abren un poco al caminar y mucho al pelear
        float flap = Mth.sin(age * 0.09F) * 0.06F + limbAmount * 0.12F + (e.isAggressive() ? 0.25F + Mth.sin(age * 0.5F) * 0.15F : 0.0F);
        if (e.isWeakened()) flap = -0.35F;
        add(p.get("wing_r"), 0, -flap * 0.4F, flap);
        add(p.get("wing_l"), 0, flap * 0.4F, -flap);
        add(p.get("wing_r2"), 0, 0, flap * 0.5F);
        add(p.get("wing_l2"), 0, 0, -flap * 0.5F);

        // Cola: se mece al caminar
        float sway = 1.0F + limbAmount * 2.0F;
        for (int i = 1; i <= 5; i++) {
            ModelPart t = p.get("tail" + i);
            t.yRot += Mth.sin(age * 0.1F - i * 0.6F) * 0.12F * sway;
        }
        p.get("tail1").xRot += limbAmount * 0.25F + (e.isWeakened() ? 0.6F : 0.0F);

        // Orejas: pequeño movimiento ocasional
        long tw = (e.tickCount + e.getId() * 13L) % 140;
        if (tw < 4) {
            p.get("ear_r").zRot += 0.25F;
            p.get("ear_l").zRot -= 0.25F;
        }

        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        int overlay = LivingEntityRenderer.getOverlayCoords(e, 0.0F);
        for (String a : ANCHORS) p.get(a).render(pose, vc, light, overlay);
    }

    private static void add(ModelPart part, float x, float y, float z) {
        part.xRot += x;
        part.yRot += y;
        part.zRot += z;
    }
}
