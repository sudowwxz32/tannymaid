package com.cz.client;

import com.cz.ChakraZorro;
import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Manto de chakra del zorro: una capa de chakra que sigue la forma del cuerpo y hierve, con orejas de zorro
 * y de una a cinco colas que se mueven como llamas.
 */
public class CloakLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(ChakraZorro.MODID, "textures/entity/cloak.png");
    private static final String[] LIMBS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private final ModelPart root;

    public CloakLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        root = models.bakeLayer(ClientModEvents.CLOAK);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        CubeDeformation hd = new CubeDeformation(0.9F), bd = new CubeDeformation(0.75F), lm = new CubeDeformation(0.6F);
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, hd), PartPose.ZERO);
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4, bd), PartPose.ZERO);
        r.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4, lm), PartPose.offset(-5, 2, 0));
        r.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -2, 4, 12, 4, lm), PartPose.offset(5, 2, 0));
        r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4, lm), PartPose.offset(-1.9F, 12, 0));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4, lm), PartPose.offset(1.9F, 12, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        ZorroState s = ClientStates.get(player.getId());
        if (!s.awakened) return;
        Phase v = s.visible();
        if (v == Phase.NONE) return;
        float pr = s.progress();
        boolean powering = s.powering > 0;
        float grow = powering ? Mth.clamp((pr - 0.15F) / 0.7F, 0, 1) : 1.0F;      // el manto aparece durante la transformación
        if (grow <= 0) return;
        float t = age;
        float boil = v.burns ? 0.12F * Mth.sin(t * 1.3F) + 0.08F * Mth.sin(t * 3.1F + 1.0F) : 0.05F * Mth.sin(t * 0.9F);
        float a = (0.55F + v.ordinal() * 0.06F + boil) * grow;
        float r = v.r(), g = v.g(), b = v.b();

        PlayerModel<AbstractClientPlayer> m = getParentModel();
        copy(root.getChild("head"), m.head);
        copy(root.getChild("body"), m.body);
        copy(root.getChild("right_arm"), m.rightArm);
        copy(root.getChild("left_arm"), m.leftArm);
        copy(root.getChild("right_leg"), m.rightLeg);
        copy(root.getChild("left_leg"), m.leftLeg);

        // capa interior pegada al cuerpo y capa exterior que ondea (más grande y oscura)
        VertexConsumer inner = buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.006F) % 1.0F, (-t * (v.burns ? 0.05F : 0.025F)) % 1.0F));
        renderAll(pose, inner, r * a, g * a, b * a);
        pose.pushPose();
        float wobble = 1.06F + 0.02F * Mth.sin(t * 0.6F) + (v.burns ? 0.03F * Mth.sin(t * 2.2F) : 0);
        scaleAround(pose, wobble);
        VertexConsumer outer = buf.getBuffer(RenderType.energySwirl(TEX, (-t * 0.004F) % 1.0F, (-t * 0.07F) % 1.0F));
        float oa = a * 0.55F;
        renderAll(pose, outer, Math.min(1, r * oa * 1.1F), g * oa * 0.6F, b * oa * 0.5F);
        pose.popPose();

        // orejas de zorro y colas, también de chakra
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.01F) % 1.0F, (-t * 0.06F) % 1.0F));
        pose.pushPose();
        m.head.translateAndRotate(pose);
        if (!v.skeleton || powering && pr < 0.7F) {
            for (int side = -1; side <= 1; side += 2) {
                float twitch = Mth.sin(t * 0.21F + side) * 0.08F;
                tube(vc, pose.last().pose(), pose.last().normal(), side * 2.6F, -8.2F, 0.4F, 5, 4.6F * grow, 1.9F, 0.0F,
                        -0.08F + twitch, side * 0.32F, 0.0F, 0, r * a, g * a, b * a, 4);
            }
        }
        pose.popPose();

        int tails = v.tails;
        pose.pushPose();
        m.body.translateAndRotate(pose);
        Matrix4f mat = pose.last().pose();
        Matrix3f nm = pose.last().normal();
        for (int i = 0; i < tails; i++) {
            float spread = (i - (tails - 1) / 2.0F) * 0.42F;
            // la cola nueva de esta fase crece durante la transformación
            float len = (16.0F + v.ordinal() * 1.6F) * (powering && i == tails - 1 ? grow : 1.0F);
            if (len < 1.0F) continue;
            float swayPh = t * 0.12F + i * 1.3F;
            tube(vc, mat, nm, 0, 10.8F, 2.2F, 12, len, 2.4F + v.ordinal() * 0.12F, 0.25F, 1.7F, spread, swayPh, 1, r * a, g * a, b * a, 6);
        }
        pose.popPose();
    }

    /**
     * Tubo de chakra que se afina hacia la punta y se curva: se usa para las colas (curve = 1) y las orejas (curve = 0).
     * La dirección empieza hacia atrás (+z) y gira hacia arriba (-y) a lo largo del largo.
     */
    private static void tube(VertexConsumer vc, Matrix4f m, Matrix3f n, float x0, float y0, float z0, int segs, float len, float radius,
                             float pitch0, float pitch1, float yaw, float swayPh, int curve, float r, float g, float b, int sides) {
        float px = x0, py = y0, pz = z0;
        float[][] ring = new float[sides][3], prev = null;
        float step = len / segs;
        for (int k = 0; k <= segs; k++) {
            float s = (float) k / segs;
            float th, ph;
            if (curve == 1) {
                th = pitch0 + (pitch1 - pitch0) * s;                              // de atrás hacia arriba
                ph = yaw + Mth.sin(swayPh - s * 3.0F) * 0.35F * s;                // ondea como una llama
                th += Mth.cos(swayPh * 0.8F - s * 2.5F) * 0.15F * s;
            } else {
                th = Mth.HALF_PI + pitch0;                                        // oreja: hacia arriba
                ph = yaw + pitch1;                                                // inclinación al lado + movimiento
            }
            float dx = Mth.sin(ph) * Mth.cos(th), dy = -Mth.sin(th), dz = Mth.cos(ph) * Mth.cos(th);
            if (curve == 0) {
                // oreja: inclinación hacia el lado y un poco atrás
                dx = Mth.sin(ph);
                dy = -Mth.cos(ph);
                dz = 0.15F;
                float l = Mth.sqrt(dx * dx + dy * dy + dz * dz);
                dx /= l; dy /= l; dz /= l;
            }
            float rad = curve == 1 ? radius * (float) Math.pow(1 - s, 0.7) * (1 + 0.35F * Mth.sin(s * Mth.PI)) + 0.25F
                    : radius * (1 - s) + 0.05F;
            // marco perpendicular a la dirección
            float rx = Math.abs(dy) < 0.9F ? 0 : 1, ry = Math.abs(dy) < 0.9F ? 1 : 0, rz = 0;
            float ux = dy * rz - dz * ry, uy = dz * rx - dx * rz, uz = dx * ry - dy * rx;
            float ul = Mth.sqrt(ux * ux + uy * uy + uz * uz);
            ux /= ul; uy /= ul; uz /= ul;
            float wx = dy * uz - dz * uy, wy = dz * ux - dx * uz, wz = dx * uy - dy * ux;
            for (int j = 0; j < sides; j++) {
                float a = j * Mth.TWO_PI / sides;
                float c = Mth.cos(a), sn = Mth.sin(a);
                ring[j][0] = px + (ux * c + wx * sn) * rad;
                ring[j][1] = py + (uy * c + wy * sn) * rad;
                ring[j][2] = pz + (uz * c + wz * sn) * rad;
            }
            if (prev != null) {
                float fade = 1.0F - s * 0.45F;
                for (int j = 0; j < sides; j++) {
                    int jj = (j + 1) % sides;
                    float u0 = (float) j / sides, u1 = (float) (j + 1) / sides, v0 = (s - 1.0F / segs), v1 = s;
                    vert(vc, m, n, prev[j], u0, v0, r, g, b, fade);
                    vert(vc, m, n, prev[jj], u1, v0, r, g, b, fade);
                    vert(vc, m, n, ring[jj], u1, v1, r, g, b, fade);
                    vert(vc, m, n, ring[j], u0, v1, r, g, b, fade);
                }
            }
            if (prev == null) prev = new float[sides][3];
            for (int j = 0; j < sides; j++) System.arraycopy(ring[j], 0, prev[j], 0, 3);
            px += dx * step;
            py += dy * step;
            pz += dz * step;
        }
    }

    private static void vert(VertexConsumer vc, Matrix4f m, Matrix3f n, float[] p, float u, float v, float r, float g, float b, float f) {
        vc.vertex(m, p[0] / 16.0F, p[1] / 16.0F, p[2] / 16.0F).color(r * f, g * f, b * f, 1.0F).uv(u, v * 2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 1, 0).endVertex();
    }

    private static void copy(ModelPart to, ModelPart from) {
        to.copyFrom(from);
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }

    private static void scaleAround(PoseStack pose, float sc) {
        pose.translate(0, 0.75F, 0);
        pose.scale(sc, sc, sc);
        pose.translate(0, -0.75F, 0);
    }

    private void renderAll(PoseStack pose, VertexConsumer vc, float r, float g, float b) {
        for (String n : LIMBS) root.getChild(n).render(pose, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }
}
