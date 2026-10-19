package com.laury.laurymod.client;

import com.laury.laurymod.entity.LauryEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.Map;

/** Dragón azul: alas que aletean al volar, cola ondulante, cuello y cabeza que miran, mandíbula que escupe fuego. */
public class LauryDragonModel extends EntityModel<LauryEntity> {
    private final ModelPart root;
    private final Map<String, ModelPart> p;

    public LauryDragonModel(ModelPart root) {
        this.root = root;
        this.p = LauryGeneratedModels.bakeDragon(root);
    }

    @Override
    public void setupAnim(LauryEntity e, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        boolean flying = !e.onGround() || e.isNoGravity();
        boolean weak = e.isWeakened();

        // cabeza y cuello siguen la mirada
        float yaw = headYaw * Mth.DEG_TO_RAD;
        float pitch = headPitch * Mth.DEG_TO_RAD;
        p.get("neck1").yRot += yaw * 0.35F;
        p.get("neck2").yRot += yaw * 0.35F;
        p.get("dhead").yRot += yaw * 0.3F;
        p.get("dhead").xRot += pitch * 0.5F;
        p.get("neck1").xRot += Mth.sin(age * 0.07F) * 0.03F;

        // mandíbula: abierta al escupir fuego o al pelear
        int sinceBreath = e.ticksSinceBreath();
        float jaw = sinceBreath >= 0 && sinceBreath < 20 ? 0.7F : e.isAggressive() ? 0.25F + Mth.sin(age * 0.4F) * 0.15F : 0.0F;
        if (e.isTalking()) jaw = Math.max(jaw, Math.abs(Mth.sin(age * 0.6F)) * 0.3F);
        p.get("djaw").xRot += jaw;

        // patas
        float walk = Mth.cos(limbSwing * 0.5F) * 0.9F * limbAmount;
        if (flying) {
            for (String leg : new String[] {"dleg_fr", "dleg_fl", "dleg_br", "dleg_bl"}) p.get(leg).xRot += 0.7F;
        } else {
            p.get("dleg_fr").xRot += walk;
            p.get("dleg_bl").xRot += walk;
            p.get("dleg_fl").xRot -= walk;
            p.get("dleg_br").xRot -= walk;
        }

        // alas
        ModelPart wr = p.get("dwing_r"), wl = p.get("dwing_l"), wr2 = p.get("dwing_r2"), wl2 = p.get("dwing_l2");
        if (flying && !weak) {
            float flap = Mth.sin(age * 0.28F);
            wr.zRot += flap * 0.75F;
            wl.zRot -= flap * 0.75F;
            wr2.zRot += Mth.sin(age * 0.28F - 0.8F) * 0.5F;
            wl2.zRot -= Mth.sin(age * 0.28F - 0.8F) * 0.5F;
        } else {
            // plegadas sobre el lomo
            float breath = Mth.sin(age * 0.06F) * 0.04F;
            wr.zRot += 0.55F + breath;
            wl.zRot -= 0.55F + breath;
            wr.yRot += 1.05F;
            wl.yRot -= 1.05F;
            wr2.zRot -= 0.35F;
            wl2.zRot += 0.35F;
            wr2.yRot -= 2.1F;
            wl2.yRot += 2.1F;
        }

        // cola
        float sway = flying ? 0.5F : 1.0F + limbAmount;
        for (int i = 1; i <= 5; i++) {
            p.get("dtail" + i).yRot += Mth.sin(age * 0.08F - i * 0.7F) * 0.1F * sway;
            if (flying) p.get("dtail" + i).xRot += 0.04F;
        }

        // debilitada: tumbada en el suelo
        if (weak) {
            p.get("dbody").y += 6.0F;
            for (String leg : new String[] {"dleg_fr", "dleg_fl"}) p.get(leg).xRot -= 1.2F;
            for (String leg : new String[] {"dleg_br", "dleg_bl"}) p.get(leg).xRot += 1.2F;
            p.get("neck1").xRot += 0.6F;
            p.get("dhead").xRot -= 0.3F;
        } else if (flying) {
            p.get("dbody").xRot += Mth.clamp(-e.getXRot() * Mth.DEG_TO_RAD * 0.6F, -0.5F, 0.5F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, vc, light, overlay, r, g, b, a);
    }
}
