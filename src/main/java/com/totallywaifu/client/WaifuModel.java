package com.totallywaifu.client;

import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

/** Cuerpo de bloques alto y esbelto (piernas largas): respira, pose de reposo con personalidad y falda que se abre al caminar. */
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
            ModelPart p = en.getValue();
            switch (n) {
                case "skirt_front" -> skirtFront.put(p, p.xRot);
                case "skirt_back" -> skirtBack.put(p, p.xRot);
                case "skirt_right", "skirt_left" -> skirtSide.put(p, p.xRot);
                default -> { }
            }
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
        // proporciones propias (HumanoidModel pone las de Steve en cada fotograma)
        float c = Mth.cos(body.yRot) * WaifuModels.ARM_X, s = Mth.sin(body.yRot) * WaifuModels.ARM_X;
        rightArm.x = -c;
        rightArm.z = s;
        leftArm.x = c;
        leftArm.z = -s;
        rightArm.y = leftArm.y = WaifuModels.ARM_Y + (crouching ? 3.2F : 0);
        rightLeg.y = leftLeg.y = WaifuModels.LEG_Y + (crouching ? 0.2F : 0);
        rightLeg.x = -WaifuModels.LEG_X;
        leftLeg.x = WaifuModels.LEG_X;
        float breath = Mth.sin(age * 0.09F);
        head.y = (crouching ? 4.2F : 0.0F) + breath * 0.15F;
        float tilt = 0;
        boolean idle = amount < 0.05F && attackTime <= 0 && !e.isBusy();
        if (idle) {
            switch (kind) {
                case KOZUE -> {               // manos en la cintura
                    rightArm.xRot = 0.1F; rightArm.zRot = 0.55F; rightArm.yRot = -0.35F;
                    leftArm.xRot = 0.1F; leftArm.zRot = -0.55F; leftArm.yRot = 0.35F;
                }
                case HOMURA -> {              // brazos cruzados
                    rightArm.xRot = -0.95F; rightArm.yRot = -0.6F; rightArm.zRot = 0.1F;
                    leftArm.xRot = -0.85F; leftArm.yRot = 0.6F; leftArm.zRot = -0.1F;
                    tilt = -0.07F;
                }
                case KURA -> {                // manos juntas al frente
                    rightArm.xRot = -0.4F; rightArm.yRot = 0.45F;
                    leftArm.xRot = -0.4F; leftArm.yRot = -0.45F;
                    tilt = Mth.sin(age * 0.05F) * 0.05F;
                }
                case SHIORI -> {              // una mano en alto con magia
                    leftArm.xRot = -0.9F + breath * 0.05F; leftArm.yRot = 0.2F; leftArm.zRot = -0.1F;
                }
            }
            body.zRot = 0.03F;
            rightLeg.zRot = 0.04F;
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
        float fwd = Math.max(0, -Math.min(rightLeg.xRot, leftLeg.xRot)), back = Math.max(0, Math.max(rightLeg.xRot, leftLeg.xRot));
        float wave = Mth.sin(age * 0.1F) * 0.02F;
        skirtFront.forEach((p, b) -> p.xRot = b - fwd * 0.6F + wave);
        skirtBack.forEach((p, b) -> p.xRot = b - back * 0.6F + wave);
        skirtSide.forEach((p, b) -> p.xRot = b - amount * 0.15F + wave);
    }
}
