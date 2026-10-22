package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Map;

/** Dibuja al jugador como un Ozaru gigante (en lugar del modelo de jugador). */
public final class OzaruRenderer {
    private static final ResourceLocation TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/ozaru.png");
    /** Escala del modelo: el Ozaru mide ~36 px; así llena su caja de choque gigante. */
    private static final float MODEL_SCALE = SaiyanState.OZARU_SCALE * 0.8F;
    private static ModelPart root;
    private static Map<String, ModelPart> p;

    private OzaruRenderer() {}

    private static void init() {
        if (root == null) {
            root = Minecraft.getInstance().getEntityModels().bakeLayer(ClientModEvents.OZARU_LAYER);
            p = OzaruModelData.bake(root);
        }
    }

    /** Reinicia el modelo (cuando se recargan los recursos). */
    public static void reset() {
        root = null;
    }

    public static void render(AbstractClientPlayer pl, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        init();
        long since = ClientStates.ticksSinceOzaru(pl.getId());
        float t = since == Long.MAX_VALUE ? 1000.0F : since + partial;
        float grow = Mth.clamp(t / 40.0F, 0.0F, 1.0F);
        grow = 1.0F - (1.0F - grow) * (1.0F - grow);                 // crece rápido y frena al final
        float scale = Mth.lerp(grow, 1.0F / 2.0F, 1.0F) * MODEL_SCALE;

        float bodyYaw = Mth.rotLerp(partial, pl.yBodyRotO, pl.yBodyRot);
        float headYaw = Mth.rotLerp(partial, pl.yHeadRotO, pl.yHeadRot) - bodyYaw;
        float pitch = Mth.lerp(partial, pl.xRotO, pl.getXRot());
        float age = pl.tickCount + partial;
        float swing = pl.walkAnimation.position(partial);
        float amount = Math.min(1.0F, pl.walkAnimation.speed(partial));
        float attack = pl.getAttackAnim(partial);

        root.getAllParts().forEach(ModelPart::resetPose);
        animate(t, age, swing, amount, attack, headYaw, pitch);

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        pose.scale(-scale, -scale, scale);
        pose.translate(0.0F, -1.501F, 0.0F);
        int overlay = LivingEntityRenderer.getOverlayCoords(pl, 0.0F);
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        p.get("eyes").visible = false;
        root.render(pose, vc, light, overlay);
        // ojos rojos que brillan en la oscuridad
        p.get("eyes").visible = true;
        pose.pushPose();
        p.get("body").translateAndRotate(pose);
        p.get("chest").translateAndRotate(pose);
        p.get("head").translateAndRotate(pose);
        p.get("eyes").render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(TEX)), LightTexture.FULL_BRIGHT, overlay);
        pose.popPose();
        pose.popPose();
    }

    private static void animate(float t, float age, float swing, float amount, float attack, float headYaw, float pitch) {
        ModelPart body = p.get("body"), chest = p.get("chest"), head = p.get("head"), jaw = p.get("jaw");
        ModelPart armR = p.get("arm_r"), armL = p.get("arm_l"), foreR = p.get("fore_r"), foreL = p.get("fore_l");
        ModelPart legR = p.get("leg_r"), legL = p.get("leg_l"), shinR = p.get("shin_r"), shinL = p.get("shin_l");
        float walk = swing * 0.45F;
        // respiración pesada
        float breath = Mth.sin(age * 0.08F);
        chest.xRot += breath * 0.02F;
        // caminar: piernas alternadas, brazos que se balancean como un gorila y el cuerpo que se mece
        legR.xRot = Mth.cos(walk) * 0.75F * amount;
        legL.xRot = Mth.cos(walk + Mth.PI) * 0.75F * amount;
        shinR.xRot = Math.max(0, -Mth.sin(walk)) * 0.5F * amount;
        shinL.xRot = Math.max(0, Mth.sin(walk)) * 0.5F * amount;
        armR.xRot += Mth.cos(walk + Mth.PI) * 0.6F * amount + breath * 0.03F;
        armL.xRot += Mth.cos(walk) * 0.6F * amount - breath * 0.03F;
        foreR.xRot -= 0.2F * amount;
        foreL.xRot -= 0.2F * amount;
        body.zRot += Mth.cos(walk) * 0.06F * amount;
        body.y += Math.abs(Mth.sin(walk)) * 1.0F * amount;
        // la cabeza sigue la mirada
        head.yRot += headYaw * Mth.DEG_TO_RAD;
        head.xRot += pitch * Mth.DEG_TO_RAD * 0.8F;
        jaw.xRot = 0.05F + Math.max(0, Mth.sin(age * 0.05F)) * 0.08F;
        // golpe: el brazo derecho cae como un mazo
        if (attack > 0) {
            float a = Mth.sin(attack * Mth.PI);
            armR.xRot -= a * 2.2F;
            armR.zRot -= a * 0.3F;
            chest.yRot -= a * 0.35F;
        }
        // cola que se mece
        for (int i = 0; i < 6; i++) {
            ModelPart seg = p.get("tail" + i);
            seg.yRot += Mth.sin(age * 0.1F - i * 0.6F) * 0.18F;
            seg.xRot += Mth.sin(age * 0.07F - i * 0.5F) * 0.05F;
        }
        // rugido al transformarse: cabeza al cielo, boca abierta, se golpea el pecho
        if (t < 70) {
            float r = t < 10 ? t / 10.0F : t > 55 ? (70 - t) / 15.0F : 1.0F;
            head.xRot -= 0.7F * r;
            jaw.xRot = 0.7F * r;
            boolean beat = ((int) (t / 5)) % 2 == 0;
            armR.xRot = Mth.lerp(r, armR.xRot, beat ? -1.5F : -0.9F);
            armL.xRot = Mth.lerp(r, armL.xRot, beat ? -0.9F : -1.5F);
            armR.zRot = Mth.lerp(r, armR.zRot, -0.15F);
            armL.zRot = Mth.lerp(r, armL.zRot, 0.15F);
            foreR.xRot = Mth.lerp(r, foreR.xRot, -1.2F);
            foreL.xRot = Mth.lerp(r, foreL.xRot, -1.2F);
            body.xRot -= 0.25F * r;
        }
    }
}
