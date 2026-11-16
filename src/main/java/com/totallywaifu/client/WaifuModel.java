package com.totallywaifu.client;

import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

/**
 * Cuerpo estilizado de la waifu (modelo propio, no la skin del jugador): cintura estrecha, cadera ancha, piernas largas,
 * cabeza un poco más pequeña. Respira, tiene una pose de reposo con personalidad y la falda se abre al caminar.
 */
public class WaifuModel extends HumanoidModel<WaifuEntity> {
    private final WaifuKind kind;
    private final Map<ModelPart, Float> skirtFront = new HashMap<>(), skirtBack = new HashMap<>(), skirtSide = new HashMap<>();

    public WaifuModel(ModelPart root, WaifuKind kind) {
        super(root);
        this.kind = kind;
        Map<String, ModelPart> parts = switch (kind) {
            case KOZUE -> WaifuModels.bakeBodyKozue(root);
            case HOMURA -> WaifuModels.bakeBodyHomura(root);
            case KURA -> WaifuModels.bakeBodyKura(root);
            case SHIORI -> WaifuModels.bakeBodyShiori(root);
        };
        for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
            String n = en.getKey();
            if (!n.startsWith("skirt")) continue;
            ModelPart p = en.getValue();
            if (n.equals("skirt_front") || n.equals("skirt_fr") || n.equals("skirt_fl")) skirtFront.put(p, p.xRot);
            else if (n.equals("skirt_back") || n.equals("skirt_br") || n.equals("skirt_bl")) skirtBack.put(p, p.xRot);
            else skirtSide.put(p, p.xRot);
        }
    }

    @Override
    public void prepareMobModel(WaifuEntity e, float limbSwing, float amount, float partial) {
        super.prepareMobModel(e, limbSwing, amount, partial);
        rightArmPose = e.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        leftArmPose = ArmPose.EMPTY;
    }

    @Override
    public void setupAnim(WaifuEntity e, float limbSwing, float amount, float age, float headYaw, float headPitch) {
        super.setupAnim(e, limbSwing, amount, age, headYaw, headPitch);
        // proporciones propias (HumanoidModel pone las de Steve cada fotograma)
        float c = Mth.cos(body.yRot) * WaifuModels.ARM_X, sn = Mth.sin(body.yRot) * WaifuModels.ARM_X;
        rightArm.x = -c;
        rightArm.z = sn;
        leftArm.x = c;
        leftArm.z = -sn;
        rightArm.y = leftArm.y = WaifuModels.ARM_Y + (crouching ? 3.2F : 0);
        rightLeg.y = leftLeg.y = WaifuModels.LEG_Y + (crouching ? 0.2F : 0);
        rightLeg.x = -WaifuModels.LEG_X;
        leftLeg.x = WaifuModels.LEG_X;
        head.xScale = head.yScale = head.zScale = WaifuModels.HEAD_SCALE;
        float breath = Mth.sin(age * 0.09F);
        head.y = (crouching ? 4.2F : 0.0F) + breath * 0.15F;
        float tilt = 0;
        boolean idle = amount < 0.05F && attackTime <= 0 && !e.isBusy();
        if (idle) {
            switch (kind) {
                case KOZUE -> {               // manos en la cintura, segura de sí
                    rightArm.xRot = 0.1F; rightArm.zRot = 0.55F; rightArm.yRot = -0.35F;
                    leftArm.xRot = 0.1F; leftArm.zRot = -0.55F; leftArm.yRot = 0.35F;
                }
                case HOMURA -> {              // brazos cruzados, orgullosa
                    rightArm.xRot = -0.95F; rightArm.yRot = -0.6F; rightArm.zRot = 0.1F;
                    leftArm.xRot = -0.85F; leftArm.yRot = 0.6F; leftArm.zRot = -0.1F;
                    head.xRot -= 0.06F;
                    tilt = -0.07F;
                }
                case KURA -> {                // manos juntas al frente, como sirvienta
                    rightArm.xRot = -0.35F; rightArm.yRot = 0.45F; rightArm.zRot = 0.05F;
                    leftArm.xRot = -0.35F; leftArm.yRot = -0.45F; leftArm.zRot = -0.05F;
                    tilt = Mth.sin(age * 0.05F) * 0.05F;
                }
                case SHIORI -> {              // una mano en alto con su magia
                    leftArm.xRot = -0.9F + breath * 0.05F; leftArm.yRot = 0.2F; leftArm.zRot = -0.1F;
                    rightArm.zRot = 0.1F;
                }
            }
            // apoya el peso en una pierna (cadera ladeada, más natural)
            body.zRot = 0.03F;
            rightLeg.zRot = 0.04F;
            leftLeg.zRot = -0.01F;
        } else {
            body.zRot = 0;
        }
        if (e.isHappy()) {
            tilt = Mth.sin(age * 0.3F) * 0.12F;
            head.xRot -= 0.1F;
        }
        if (kind == WaifuKind.SHIORI && attackTime > 0) leftArm.xRot = -1.5F;
        head.zRot = tilt;
        hat.copyFrom(head);
        // la falda se abre con las piernas al caminar y ondea un poco
        float fwd = Math.max(0, -Math.min(rightLeg.xRot, leftLeg.xRot)), back = Math.max(0, Math.max(rightLeg.xRot, leftLeg.xRot));
        float wave = Mth.sin(age * 0.1F) * 0.02F;
        skirtFront.forEach((p, b) -> p.xRot = b - fwd * 0.55F + wave);
        skirtBack.forEach((p, b) -> p.xRot = b - back * 0.55F + wave);
        skirtSide.forEach((p, b) -> p.xRot = b - amount * 0.12F + wave);
    }
}
