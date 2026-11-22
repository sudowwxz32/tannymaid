package com.totallywaifu.client;

import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inari como chica: elegante y tranquila (manos juntas al frente), juguetona a ratos (inclina la cabeza, mueve la cola rápido),
 * relajada (manos a la espalda) o tímida. Cola enorme y esponjosa que ondula, orejas de zorro que se mueven y mechones que se mecen.
 */
public class InariGirlModel extends HumanoidModel<WaifuEntity> {
    private final List<ModelPart> tail = new ArrayList<>(), locks = new ArrayList<>();
    private final List<Float> tailBase = new ArrayList<>(), lockBase = new ArrayList<>();
    private final ModelPart earR, earL;
    private final float earRz, earLz, earRx;
    private final Map<ModelPart, Float> skirtFront = new HashMap<>(), skirtBack = new HashMap<>(), skirtSide = new HashMap<>();

    public InariGirlModel(ModelPart root) {
        super(root);
        Map<String, ModelPart> p = InariModels.bakeGirl(root);
        for (int i = 0; p.containsKey("tail" + i); i++) {
            tail.add(p.get("tail" + i));
            tailBase.add(p.get("tail" + i).xRot);
        }
        for (int i = 0; p.containsKey("lock" + i); i++) {
            locks.add(p.get("lock" + i));
            lockBase.add(p.get("lock" + i).xRot);
        }
        earR = p.get("iear_r");
        earL = p.get("iear_l");
        earRz = earR.zRot;
        earLz = earL.zRot;
        earRx = earR.xRot;
        p.forEach((n, q) -> {
            switch (n) {
                case "skirt_front" -> skirtFront.put(q, q.xRot);
                case "skirt_back" -> skirtBack.put(q, q.xRot);
                case "skirt_right", "skirt_left" -> skirtSide.put(q, q.xRot);
                default -> { }
            }
        });
    }

    @Override
    public void prepareMobModel(WaifuEntity e, float limbSwing, float amount, float partial) {
        super.prepareMobModel(e, limbSwing, amount, partial);
        rightArmPose = ArmPose.EMPTY;
        leftArmPose = ArmPose.EMPTY;
    }

    @Override
    public void setupAnim(WaifuEntity e, float limbSwing, float amount, float age, float headYaw, float headPitch) {
        headYaw = Mth.clamp(Mth.wrapDegrees(headYaw), -70.0F, 70.0F);
        headPitch = Mth.clamp(Mth.wrapDegrees(headPitch), -40.0F, 40.0F);
        head.zRot = 0;
        head.x = head.z = 0;
        body.zRot = 0;
        rightArm.yRot = leftArm.yRot = 0;
        rightLeg.yRot = leftLeg.yRot = rightLeg.zRot = leftLeg.zRot = 0;
        super.setupAnim(e, limbSwing, amount, age, headYaw, headPitch);
        head.zRot = 0;
        float c = Mth.cos(body.yRot) * WaifuModels.ARM_X, s = Mth.sin(body.yRot) * WaifuModels.ARM_X;
        rightArm.x = -c;
        rightArm.z = s;
        leftArm.x = c;
        leftArm.z = -s;
        rightArm.y = leftArm.y = WaifuModels.ARM_Y;
        rightLeg.y = leftLeg.y = WaifuModels.LEG_Y;
        rightLeg.x = -WaifuModels.LEG_X;
        leftLeg.x = WaifuModels.LEG_X;
        // caminar con gracia (pasos cortos, brazos suaves)
        rightLeg.xRot *= 0.8F;
        leftLeg.xRot *= 0.8F;
        rightArm.xRot *= 0.45F;
        leftArm.xRot *= 0.45F;
        rightArm.zRot = 0.06F;
        leftArm.zRot = -0.06F;
        float breath = Mth.sin(age * 0.08F);
        head.y = breath * 0.15F;
        float tilt = 0, tailSpeed = 0.06F, tailWave = 0.16F, tailLift = 0, earPerk = 0;
        int phase = InariRenderer.phase(e);
        boolean idle = amount < 0.05F && attackTime <= 0;
        if (idle) {
            switch (phase) {
                case 3 -> {                                        // relajada: manos a la espalda
                    rightArm.xRot = 0.4F; rightArm.zRot = 0.18F;
                    leftArm.xRot = 0.4F; leftArm.zRot = -0.18F;
                    tilt = Mth.sin(age * 0.03F) * 0.06F;
                    tailSpeed = 0.035F;
                    tailWave = 0.1F;
                }
                case 4 -> {                                        // juguetona: mano en la mejilla, cabecita de lado, cola rápida
                    rightArm.xRot = -2.1F + Mth.sin(age * 0.2F) * 0.08F; rightArm.yRot = -0.3F; rightArm.zRot = -0.35F;
                    leftArm.xRot = 0.1F; leftArm.zRot = -0.45F; leftArm.yRot = 0.3F;
                    tilt = 0.22F + Mth.sin(age * 0.15F) * 0.06F;
                    tailSpeed = 0.18F;
                    tailWave = 0.3F;
                    earPerk = 0.15F;
                }
                case 6 -> {                                        // te saluda con la mano, guiñando
                    rightArm.xRot = -2.5F; rightArm.zRot = 0.25F + Mth.sin(age * 0.35F) * 0.3F;
                    tilt = 0.12F;
                    tailSpeed = 0.14F;
                    tailWave = 0.24F;
                }
                case 7 -> {                                        // tímida: manos juntas en el pecho, mirada abajo, orejas caídas
                    rightArm.xRot = -1.0F; rightArm.yRot = 0.55F;
                    leftArm.xRot = -1.0F; leftArm.yRot = -0.55F;
                    head.xRot += 0.25F;
                    tilt = 0.1F;
                    earPerk = -0.35F;
                    tailLift = -0.2F;
                }
                default -> {                                       // elegante: manos juntas al frente, como una sacerdotisa
                    rightArm.xRot = -0.55F + breath * 0.02F; rightArm.yRot = 0.5F;
                    leftArm.xRot = -0.55F + breath * 0.02F; leftArm.yRot = -0.5F;
                    tilt = Mth.sin(age * 0.025F) * 0.04F;
                }
            }
            body.zRot = Mth.sin(age * 0.03F) * 0.015F;
        }
        if (e.isHappy()) {
            tilt = Mth.sin(age * 0.25F) * 0.12F;
            tailSpeed = 0.2F;
            tailWave = 0.34F;
            earPerk = 0.2F;
        }
        if (e.isAggressive()) {                                    // en guardia: orejas al frente, cola alta
            earPerk = 0.3F;
            tailLift = 0.35F;
            tailSpeed = 0.12F;
        }
        if (attackTime > 0) {                                      // lanza fuego de zorro con la mano
            float a = Mth.sin(attackTime * Mth.PI);
            rightArm.xRot = -1.6F * a - 0.2F;
            rightArm.yRot = -0.2F * a;
            body.yRot += a * 0.25F;
        }
        if (e.transform() > 0) {                                   // brazos abiertos al transformarse
            rightArm.zRot = 1.2F;
            leftArm.zRot = -1.2F;
            head.xRot = -0.3F;
        }
        head.zRot = Mth.clamp(tilt, -0.35F, 0.35F);
        hat.copyFrom(head);
        // orejas: se mueven, se paran y se agachan según lo que siente
        long t = e.tickCount + e.getId() * 31L;
        float tw = t % 120 < 8 ? Mth.sin((t % 120) * 0.9F) * 0.25F : 0;
        float tw2 = (t + 47) % 170 < 8 ? Mth.sin(((t + 47) % 170) * 0.9F) * 0.25F : 0;
        earR.zRot = earRz - earPerk * 0.6F + tw;
        earL.zRot = earLz + earPerk * 0.6F - tw2;
        earR.xRot = earL.xRot = earRx - earPerk * 0.4F;
        // cola enorme: onda que viaja por los segmentos (se levanta al caminar y al estar alerta)
        for (int i = 0; i < tail.size(); i++) {
            ModelPart q = tail.get(i);
            float w = Mth.sin(age * tailSpeed - i * 0.55F);
            q.zRot = w * tailWave * (0.35F + i * 0.12F);
            q.yRot = Mth.cos(age * tailSpeed * 0.7F - i * 0.5F) * 0.05F * i;
            q.xRot = tailBase.get(i) + (i == 0 ? tailLift - amount * 0.25F : Mth.cos(age * tailSpeed * 0.8F - i * 0.4F) * 0.04F);
        }
        // mechones largos
        for (int i = 0; i < locks.size(); i++)
            locks.get(i).xRot = lockBase.get(i) + Mth.sin(age * 0.06F - i * 0.6F) * 0.03F + amount * 0.12F * (i == 0 ? 1 : 0.4F);
        float fwd = Math.max(0, -Math.min(rightLeg.xRot, leftLeg.xRot)), back = Math.max(0, Math.max(rightLeg.xRot, leftLeg.xRot));
        float wave = Mth.sin(age * 0.1F) * 0.02F;
        skirtFront.forEach((q, b) -> q.xRot = b - fwd * 0.6F + wave);
        skirtBack.forEach((q, b) -> q.xRot = b - back * 0.6F + wave);
        skirtSide.forEach((q, b) -> q.xRot = b - amount * 0.15F + wave);
    }
}
