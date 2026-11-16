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
    private boolean sitting;
    private final Map<ModelPart, Float> skirtFront = new HashMap<>(), skirtBack = new HashMap<>(), skirtSide = new HashMap<>();

    public WaifuModel(ModelPart root, WaifuKind kind) {
        super(root);
        this.kind = kind;
        Map<String, ModelPart> parts = WaifuModels.bake(kind.id(), root);
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
        sitting = e.isSitting();
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
        if (sitting) {                    // Yume sentada: piernas al frente, te acaricia el cabello
            rightLeg.xRot = leftLeg.xRot = -1.45F;
            rightLeg.yRot = 0.12F;
            leftLeg.yRot = -0.12F;
            rightLeg.zRot = leftLeg.zRot = 0;
            head.xRot = 0.6F;
            rightArm.xRot = -0.75F + Mth.sin(age * 0.08F) * 0.18F;
            rightArm.yRot = -0.15F;
            rightArm.zRot = 0.1F;
            leftArm.xRot = -0.45F;
            leftArm.yRot = 0.3F;
            leftArm.zRot = -0.1F;
            head.zRot = Mth.sin(age * 0.04F) * 0.08F;
            hat.copyFrom(head);
            skirtFront.forEach((p, b) -> p.xRot = b - 1.0F);
            skirtSide.forEach((p, b) -> p.xRot = b - 0.4F);
            skirtBack.forEach((p, b) -> p.xRot = b + 0.2F);
            return;
        }
        if (e.isVehicle()) {             // Brisa carga a alguien: brazos atrás, sujetándolo
            rightArm.xRot = leftArm.xRot = 0.7F;
            rightArm.zRot = 0.3F;
            leftArm.zRot = -0.3F;
        }
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
                case ZOMBIE -> {              // brazos al frente, como zombi (pero tierna)
                    rightArm.xRot = -1.35F + breath * 0.04F; leftArm.xRot = -1.35F - breath * 0.04F;
                    rightArm.yRot = -0.1F; leftArm.yRot = 0.1F;
                    tilt = 0.12F;
                }
                case BRUJA -> {               // mano en el sombrero
                    rightArm.xRot = -0.5F; rightArm.zRot = 0.25F;
                    leftArm.xRot = 0.1F; leftArm.zRot = -0.55F; leftArm.yRot = 0.35F;
                }
                case GATO, OVEJA, GALLINA -> { // manitas juntas
                    rightArm.xRot = -0.55F; rightArm.yRot = 0.4F;
                    leftArm.xRot = -0.55F; leftArm.yRot = -0.4F;
                    tilt = Mth.sin(age * 0.06F) * 0.08F;
                }
                case GOLEM, ESQUELETO -> {    // firmes
                    rightArm.zRot = 0.08F; leftArm.zRot = -0.08F;
                }
                case ENDERMAN -> {            // brazos largos, relajados
                    rightArm.zRot = 0.12F; leftArm.zRot = -0.12F;
                    tilt = Mth.sin(age * 0.04F) * 0.1F;
                }
                case DINA -> {                // la mayor: manos en la cintura, segura de sí misma
                    rightArm.xRot = 0.1F; rightArm.zRot = 0.5F; rightArm.yRot = -0.3F;
                    leftArm.xRot = 0.1F; leftArm.zRot = -0.5F; leftArm.yRot = 0.3F;
                    tilt = -0.06F;
                }
                case MITA -> {                // la menor: manos atrás, juguetona
                    rightArm.xRot = 0.3F + breath * 0.03F; rightArm.zRot = 0.12F;
                    leftArm.xRot = 0.3F - breath * 0.03F; leftArm.zRot = -0.12F;
                    tilt = 0.1F + Mth.sin(age * 0.06F) * 0.05F;
                }
                case WARDEN -> {              // guardiana: brazos abiertos, alerta
                    rightArm.zRot = 0.3F; leftArm.zRot = -0.3F;
                    rightArm.xRot = leftArm.xRot = -0.15F;
                }
                case BLAZE, GHAST, ALLAY -> { // flotan: brazos abiertos y suaves
                    rightArm.zRot = 0.45F + breath * 0.08F; leftArm.zRot = -0.45F - breath * 0.08F;
                    tilt = Mth.sin(age * 0.05F) * 0.08F;
                }
                case CAMA -> {                // abraza su almohada imaginaria, con sueño
                    rightArm.xRot = -0.6F; rightArm.yRot = 0.5F;
                    leftArm.xRot = -0.6F; leftArm.yRot = -0.5F;
                    tilt = 0.18F + Mth.sin(age * 0.03F) * 0.05F;
                }
                case CONEJO, ABEJA, AJOLOTE, RANA -> {   // saltitos
                    rightArm.xRot = leftArm.xRot = -0.3F;
                    rightArm.zRot = 0.2F; leftArm.zRot = -0.2F;
                    tilt = Mth.sin(age * 0.12F) * 0.1F;
                }
                case PIGLIN, WITHER, PANDA, CABALLO -> { // brazos cruzados
                    rightArm.xRot = -0.95F; rightArm.yRot = -0.6F; rightArm.zRot = 0.1F;
                    leftArm.xRot = -0.85F; leftArm.yRot = 0.6F; leftArm.zRot = -0.1F;
                }
                default -> {                  // una mano en la cintura
                    leftArm.xRot = 0.1F; leftArm.zRot = -0.55F; leftArm.yRot = 0.35F;
                    rightArm.zRot = 0.06F;
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
