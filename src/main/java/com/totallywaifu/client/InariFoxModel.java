package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Inari como zorro dios gigante: camina, galopa (al galopar se estira y encoge la espalda), corre por los cielos
 * con las patas recogidas y la cola larguísima flotando detrás, respira, mueve las orejas, muerde y embiste.
 */
public class InariFoxModel extends EntityModel<WaifuEntity> {
    final ModelPart root, fox, torso, neck, head, jaw, earR, earL, flegR, flegL, blegR, blegL;
    private final List<ModelPart> tail = new ArrayList<>();
    private final List<Float> tailBase = new ArrayList<>();
    private final float foxY, headY, headZ, neckX, earRz, earLz;

    public InariFoxModel(ModelPart root) {
        this.root = root;
        Map<String, ModelPart> p = InariModels.bakeFox(root);
        fox = p.get("fox");
        torso = p.get("torso");
        neck = p.get("neck");
        head = p.get("head");
        jaw = p.get("jaw");
        earR = p.get("ear_r");
        earL = p.get("ear_l");
        flegR = p.get("fleg_r");
        flegL = p.get("fleg_l");
        blegR = p.get("bleg_r");
        blegL = p.get("bleg_l");
        for (int i = 0; p.containsKey("tail" + i); i++) {
            tail.add(p.get("tail" + i));
            tailBase.add(p.get("tail" + i).xRot);
        }
        foxY = fox.y;
        headY = head.y;
        headZ = head.z;
        neckX = neck.xRot;
        earRz = earR.zRot;
        earLz = earL.zRot;
    }

    @Override
    public void setupAnim(WaifuEntity e, float pos, float amount, float age, float headYaw, float headPitch) {
        fox.y = foxY;
        fox.xRot = 0;
        torso.xRot = 0;
        neck.xRot = neckX;
        head.y = headY;
        head.z = headZ;
        jaw.xRot = 0;
        for (ModelPart l : new ModelPart[]{flegR, flegL, blegR, blegL}) l.xRot = l.yRot = l.zRot = 0;
        earR.xRot = earL.xRot = 0;
        headYaw = Mth.clamp(Mth.wrapDegrees(headYaw), -50.0F, 50.0F);
        headPitch = Mth.clamp(Mth.wrapDegrees(headPitch), -30.0F, 30.0F);
        head.yRot = headYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        head.zRot = 0;
        boolean air = !e.onGround();
        float sp = Mth.clamp(amount, 0, 1);
        float tailLift = 0, tailWave = 0.14F, tailSpeed = 0.07F, earBack = 0;
        if (sp > 0.08F) {
            if (sp < 0.5F && !air) {                                     // paso (patas en diagonal)
                float w = pos * 0.7F;
                flegR.xRot = blegL.xRot = Mth.cos(w) * 0.7F * sp * 2;
                flegL.xRot = blegR.xRot = Mth.cos(w + Mth.PI) * 0.7F * sp * 2;
                fox.y = foxY - Math.abs(Mth.sin(w)) * 0.5F;
                head.y = headY + Mth.sin(w * 2) * 0.3F;
                tailLift = 0.15F;
            } else {                                                     // galope / correr por el cielo
                float f = pos * 0.42F;
                float k = Math.min(1, sp * 1.3F);
                flegR.xRot = Mth.sin(f) * 1.15F * k;
                flegL.xRot = Mth.sin(f + 0.35F) * 1.15F * k;
                blegR.xRot = Mth.sin(f + Mth.PI) * 1.05F * k;
                blegL.xRot = Mth.sin(f + Mth.PI + 0.35F) * 1.05F * k;
                torso.xRot = Mth.sin(f) * 0.1F * k;                     // la espalda se estira y se encoge
                fox.y = foxY - Math.abs(Mth.cos(f)) * 1.6F * k;
                head.y = headY + Mth.cos(f) * 0.6F * k;
                head.xRot -= 0.12F;
                neck.xRot = neckX + 0.1F;
                earBack = 0.55F * k;
                tailLift = 0.5F * k;                                    // la cola se estira hacia atrás
                tailWave = 0.08F;
                tailSpeed = 0.2F;
                if (air) fox.xRot = -0.08F;
            }
        } else if (air) {                                                // flota: patas recogidas, cola que ondula en el aire
            flegR.xRot = flegL.xRot = -0.55F;
            blegR.xRot = blegL.xRot = 0.65F;
            fox.y = foxY + Mth.sin(age * 0.07F) * 0.6F;
            tailWave = 0.22F;
            tailSpeed = 0.05F;
            tailLift = 0.25F;
        } else {                                                         // de pie: respira tranquila
            fox.y = foxY + Mth.sin(age * 0.06F) * 0.15F;
            head.xRot += Mth.sin(age * 0.03F) * 0.05F;
        }
        if (e.isAggressive()) {
            earBack = Math.max(earBack, 0.2F);
            tailLift += 0.2F;
            tailSpeed = 0.16F;
        }
        if (e.isHappy()) {
            tailSpeed = 0.22F;
            tailWave = 0.3F;
        }
        if (attackTime > 0) {                                            // mordisco y zarpazo
            float a = Mth.sin(attackTime * Mth.PI);
            head.z = headZ - a * 2.0F;
            head.xRot += 0.3F * a;
            jaw.xRot = 0.7F * a;
            flegR.xRot = -1.3F * a;
        }
        if (e.isVehicle() && sp < 0.08F) {                               // te espera mientras la montas
            tailWave = 0.18F;
        }
        // orejas
        long t = e.tickCount + e.getId() * 17L;
        float tw = t % 100 < 6 ? Mth.sin((t % 100) * 1.0F) * 0.25F : 0;
        earR.zRot = earRz - earBack * 0.3F + tw;
        earL.zRot = earLz + earBack * 0.3F;
        earR.xRot = earL.xRot = earBack;
        // cola enorme y esponjosa
        for (int i = 0; i < tail.size(); i++) {
            ModelPart q = tail.get(i);
            float w = Mth.sin(age * tailSpeed - i * 0.6F);
            q.zRot = w * tailWave * (0.3F + i * 0.16F);
            q.yRot = Mth.cos(age * tailSpeed * 0.6F - i * 0.5F) * 0.06F * i;
            q.xRot = tailBase.get(i) + (i == 0 ? tailLift : tailLift * 0.25F + Mth.cos(age * tailSpeed * 0.7F - i * 0.4F) * 0.05F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buf, int light, int overlay, float r, float g, float b, float a) {
        root.render(pose, buf, light, overlay, r, g, b, a);
    }
}
