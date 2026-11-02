package com.bdsz.client.kaori;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.companion.KaoriEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Kaori: chaqueta oliva, top blanco, cinturón de cápsulas, shorts cargo, botas, gafas de mecánica y coleta alta. */
public class KaoriRenderer extends HumanoidMobRenderer<KaoriEntity, KaoriModel> {
    public enum Face { NORMAL, BLINK, HAPPY, SURPRISED, FOCUSED, TIRED }

    private static final ResourceLocation NORMAL = tex("kaori"), BLINK = tex("kaori_blink"), HAPPY = tex("kaori_happy"),
            SURPRISED = tex("kaori_surprised"), FOCUSED = tex("kaori_focused"), TIRED = tex("kaori_tired");

    public KaoriRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new KaoriModel(ctx.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.45F);
        // armadura un poco más holgada para que cubra la ropa 3D y no se vea nada por fuera
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(ctx.bakeLayer(KaoriLayers.ARMOR_INNER)),
                new HumanoidArmorModel<>(ctx.bakeLayer(KaoriLayers.ARMOR_OUTER)), ctx.getModelManager()));
        addLayer(new KaoriLayers.Outfit(this, ctx.getModelSet()));
        addLayer(new KaoriLayers.Head(this, ctx.getModelSet()));
    }

    static ResourceLocation tex(String n) {
        return new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/kaori/" + n + ".png");
    }

    /** Expresión: pestañea cada pocos segundos (a veces dos veces seguidas). */
    public static Face face(KaoriEntity e) {
        if (e.isWeakened()) return Face.TIRED;
        if (e.hurtTime > 0) return Face.SURPRISED;
        long t = e.tickCount + e.getId() * 53L;
        if (t % 90 < 3 || (t % 270 >= 6 && t % 270 < 9)) return Face.BLINK;
        if (e.isTalking()) return Face.HAPPY;
        if (e.isAggressive()) return Face.FOCUSED;
        return Face.NORMAL;
    }

    @Override
    public ResourceLocation getTextureLocation(KaoriEntity e) {
        return switch (face(e)) {
            case TIRED -> TIRED;
            case SURPRISED -> SURPRISED;
            case BLINK -> BLINK;
            case HAPPY -> HAPPY;
            case FOCUSED -> FOCUSED;
            default -> NORMAL;
        };
    }

    /** Al volar rápido se inclina hacia adelante (estilo Superman). */
    @Override
    protected void setupRotations(KaoriEntity e, PoseStack pose, float age, float yaw, float partial) {
        super.setupRotations(e, pose, age, yaw, partial);
        if (e.isFlying()) {
            // igual que el jugador al volar: el cuerpo sigue la dirección del vuelo, boca abajo y la cabeza al frente
            float k = flySpeed(e, partial);
            double vx = e.getX() - e.xo, vy = e.getY() - e.yo, vz = e.getZ() - e.zo, h = Math.sqrt(vx * vx + vz * vz);
            float elev = (float) Math.toDegrees(Math.atan2(-vy, Math.max(1e-4, h)));      // + = bajando
            float tilt = (90.0F + Mth.clamp(elev, -80.0F, 60.0F)) * k;
            float t = TILT.getOrDefault(e.getId(), 0.0F);
            t += (tilt - t) * 0.15F;                                                     // suave
            TILT.put(e.getId(), t);
            pose.translate(0, 0.9, 0);
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-t));
            pose.translate(0, -0.9, 0);
        } else TILT.remove(e.getId());
    }

    private static final java.util.Map<Integer, Float> TILT = new java.util.HashMap<>();

    /** 0 = suspendida en el aire, 1 = volando a toda velocidad (con la velocidad real, no la del servidor). */
    public static float flySpeed(KaoriEntity e, float partial) {
        double vx = e.getX() - e.xo, vy = e.getY() - e.yo, vz = e.getZ() - e.zo;
        return (float) Mth.clamp((Math.sqrt(vx * vx + vy * vy + vz * vz) - 0.08) / 0.4, 0.0, 1.0);
    }

    @Override
    protected void scale(KaoriEntity e, PoseStack pose, float partial) {
        pose.scale(0.93F, 0.93F, 0.93F);
        if (e.isWeakened()) pose.translate(0.0F, 0.6F, 0.0F);
    }
}
