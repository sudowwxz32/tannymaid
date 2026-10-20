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
        boolean flying = !e.onGround();
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

        // ---- estado de vuelo interpolado
        float pt = age - e.tickCount;
        float phase = Mth.lerp(pt, e.prevFlapPhase, e.flapPhase);
        float pitchDeg = Mth.lerp(pt, e.prevFlightPitch, e.flightPitch);
        float rollDeg = Mth.lerp(pt, e.prevFlightRoll, e.flightRoll);
        float dive = e.diveAmount;
        float str = e.flapStrength;

        // alas
        ModelPart wr = p.get("dwing_r"), wl = p.get("dwing_l"), wr2 = p.get("dwing_r2"), wl2 = p.get("dwing_l2");
        if (flying && !weak) {
            float flap = Mth.sin(phase) * 0.8F * str;
            float lag = Mth.sin(phase - 0.9F) * 0.55F * str;
            float glide = (1.0F - str) * 0.12F;                       // planeo: alas un poco hacia arriba
            // en el giro, el ala de dentro baja y la de fuera sube
            float bank = rollDeg * Mth.DEG_TO_RAD * 0.25F;
            wr.zRot += flap + glide - bank - dive * 0.35F;
            wl.zRot -= flap + glide + bank - dive * 0.35F;
            wr2.zRot += lag + dive * 0.2F;
            wl2.zRot -= lag + dive * 0.2F;
            // picada: alas hacia atrás, pegadas al cuerpo
            wr.yRot += dive * 0.95F;
            wl.yRot -= dive * 0.95F;
            wr2.yRot -= dive * 1.3F;
            wl2.yRot += dive * 1.3F;
        } else {
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

        // cola: ondula, se abre hacia fuera en las curvas y sigue la inclinación
        float sway = flying ? 0.6F + str * 0.4F : 1.0F + limbAmount;
        for (int i = 1; i <= 5; i++) {
            ModelPart t = p.get("dtail" + i);
            t.yRot += Mth.sin(age * 0.08F - i * 0.7F) * 0.1F * sway + rollDeg * Mth.DEG_TO_RAD * 0.06F;
            if (flying) {
                t.xRot += 0.04F + Mth.sin(phase - i * 0.6F) * 0.05F * str - pitchDeg * Mth.DEG_TO_RAD * 0.05F;
            }
        }

        // cuello y cabeza compensan la inclinación para mirar al frente
        if (flying && !weak) {
            p.get("neck1").xRot -= pitchDeg * Mth.DEG_TO_RAD * 0.35F;
            p.get("dhead").xRot -= pitchDeg * Mth.DEG_TO_RAD * 0.15F;
            p.get("neck1").zRot -= rollDeg * Mth.DEG_TO_RAD * 0.25F;
            p.get("neck1").xRot += Mth.sin(phase + 0.5F) * 0.06F * str;
        }

        // debilitada: tumbada en el suelo
        if (weak) {
            p.get("dbody").y += 6.0F;
            for (String leg : new String[] {"dleg_fr", "dleg_fl"}) p.get(leg).xRot -= 1.2F;
            for (String leg : new String[] {"dleg_br", "dleg_bl"}) p.get(leg).xRot += 1.2F;
            p.get("neck1").xRot += 0.6F;
            p.get("dhead").xRot -= 0.3F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, vc, light, overlay, r, g, b, a);
    }
}
