package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
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

/** Aura 3D: dos capas de energía brillante que fluyen hacia arriba, con llamas que se elevan sobre la cabeza. */
public class AuraLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura.png");
    private static final String[] LIMBS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private final ModelPart root;
    private final ModelPart[] flames = new ModelPart[8];

    public AuraLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(ClientModEvents.AURA_LAYER);
        for (int i = 0; i < flames.length; i++) flames[i] = root.getChild("body").getChild("flame" + i);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        CubeDeformation big = new CubeDeformation(1.4F);
        CubeDeformation mid = new CubeDeformation(1.1F);
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, big), PartPose.ZERO);
        PartDefinition body = r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4, big), PartPose.ZERO);
        r.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4, mid), PartPose.offset(-5, 2, 0));
        r.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -2, 4, 12, 4, mid), PartPose.offset(5, 2, 0));
        r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4, mid), PartPose.offset(-1.9F, 12, 0));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4, mid), PartPose.offset(1.9F, 12, 0));
        // llamas del aura: nacen alrededor de los hombros y suben por encima de la cabeza
        float[][] fl = {{-5.5F, 0, -1, -0.25F}, {5.5F, 0, -1, 0.25F}, {-3.5F, 0, 2.5F, -0.12F}, {3.5F, 0, 2.5F, 0.12F},
                {0, 0, 3.2F, 0}, {-6.5F, 6, 0, -0.35F}, {6.5F, 6, 0, 0.35F}, {0, 0, -3.2F, 0}};
        for (int i = 0; i < fl.length; i++) {
            body.addOrReplaceChild("flame" + i, CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, -16, -1.5F, 3, 16, 3, new CubeDeformation(0.3F)),
                    PartPose.offsetAndRotation(fl[i][0], fl[i][1], fl[i][2], 0, 0, fl[i][3]));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        SaiyanState s = ClientStates.get(player.getId());
        boolean forced = s.charging || s.powering > 0;
        if (!s.saiyan || !(s.aura || forced)) return;
        Form f = s.visibleForm();
        int k = s.transformed || s.powering > 0 ? s.kaioken : 0;
        float r, g, b;
        if (f == Form.BASE) {
            if (k > 0) { r = 1.0F; g = 0.2F; b = 0.12F; } else { r = 0.75F; g = 0.9F; b = 1.0F; }
        } else {
            r = f.ar(); g = f.ag(); b = f.ab();
            if (k > 0) { r = Math.min(1, r * 0.5F + 0.6F); g *= 0.45F; b *= 0.45F; }      // Kaioken tiñe el aura de rojo
        }
        float progress = s.powering > 0 ? 1.0F - (float) s.powering / Math.max(1, s.poweringTotal) : 1.0F;
        float power = (f == Form.BASE ? 0.35F : 0.5F + f.ordinal() * 0.07F) + k / 400.0F;
        if (s.charging) power += 0.25F;
        if (s.powering > 0) power *= 0.5F + progress;
        float size = f.auraSize * (s.charging ? 1.15F : 1.0F) * (s.powering > 0 ? 0.7F + progress * 0.5F : 1.0F);

        PlayerModel<AbstractClientPlayer> m = getParentModel();
        root.getChild("head").copyFrom(m.head);
        root.getChild("body").copyFrom(m.body);
        root.getChild("right_arm").copyFrom(m.rightArm);
        root.getChild("left_arm").copyFrom(m.leftArm);
        root.getChild("right_leg").copyFrom(m.rightLeg);
        root.getChild("left_leg").copyFrom(m.leftLeg);
        float height = (0.55F + power * 0.5F) * size;
        for (int i = 0; i < flames.length; i++) {
            ModelPart fp = flames[i];
            fp.resetPose();
            fp.yScale = height * (0.75F + 0.35F * Mth.sin(age * 0.45F + i * 1.7F));
            fp.xScale = fp.zScale = (0.8F + 0.25F * Mth.sin(age * 0.3F + i)) * (0.8F + size * 0.25F);
        }
        float t = age + partial;
        float a = Math.min(1.0F, 0.45F + power * 0.35F);
        // capa interior
        pose.pushPose();
        scaleAround(pose, 0.92F + size * 0.08F);
        renderAll(pose, buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.004F) % 1.0F, (-t * 0.03F) % 1.0F)), r * a, g * a, b * a);
        pose.popPose();
        // capa exterior: más grande (la fase 1 tiene el aura más grande) y tenue
        pose.pushPose();
        float pulse = (1.08F + 0.03F * Mth.sin(age * 0.5F) + power * 0.04F) * size;
        scaleAround(pose, pulse);
        renderAll(pose, buf.getBuffer(RenderType.energySwirl(TEX, (t * -0.003F) % 1.0F, (-t * 0.045F) % 1.0F)), r * a * 0.5F, g * a * 0.5F, b * a * 0.5F);
        pose.popPose();
        if (size > 1.3F) {
            // tercera capa para auras grandes
            pose.pushPose();
            scaleAround(pose, pulse * 1.15F);
            renderAll(pose, buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.002F) % 1.0F, (-t * 0.06F) % 1.0F)), r * a * 0.28F, g * a * 0.28F, b * a * 0.28F);
            pose.popPose();
        }
        // ondas del aura: anillos que se expanden desde los pies y anillos que suben por el cuerpo
        drawWaves(pose, buf, t, r * a, g * a, b * a, size, power, s.charging || s.powering > 0);
        // rayos eléctricos (fases 2 y 3, morados en Ultra Ego)
        if (f.lightningAura() && (s.transformed || progress > 0.5F)) {
            drawLightning(pose, buf, player.getId(), age, f == Form.SSJ3 || f == Form.ULTRA_EGO ? 4 : 3, size, f.lightningColor());
        }
    }

    private static void scaleAround(PoseStack pose, float sc) {
        pose.translate(0, 0.75F, 0);
        pose.scale(sc, sc, sc);
        pose.translate(0, -0.75F, 0);
    }

    /** Rayos en zigzag que aparecen y desaparecen alrededor del cuerpo. */
    /** Ondas: anillos de energía brillantes. */
    private static void drawWaves(PoseStack pose, MultiBufferSource buf, float t, float r, float g, float b, float size, float power, boolean strong) {
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.01F) % 1.0F, (t * 0.02F) % 1.0F));
        PoseStack.Pose last = pose.last();
        float period = strong ? 14.0F : 24.0F;
        int rings = strong ? 3 : 2;
        // anillos en el suelo (y = 1.5 en el espacio del modelo = los pies)
        for (int i = 0; i < rings; i++) {
            float ph = ((t / period) + (float) i / rings) % 1.0F;
            float radius = (0.5F + ph * (1.6F + power)) * size;
            float fade = (1.0F - ph) * (strong ? 0.9F : 0.6F);
            ring(vc, last, 1.48F, radius, radius * 0.78F, r * fade, g * fade, b * fade);
        }
        // anillos que suben alrededor del cuerpo
        for (int i = 0; i < 2; i++) {
            float ph = ((t / (period * 0.9F)) + i * 0.5F) % 1.0F;
            float y = 1.5F - ph * 2.3F * size;
            float radius = (0.62F + 0.12F * (float) Math.sin(ph * Math.PI)) * size;
            float fade = (float) Math.sin(ph * Math.PI) * 0.45F;
            ring(vc, last, y, radius, radius * 0.86F, r * fade, g * fade, b * fade);
        }
    }

    private static void ring(VertexConsumer vc, PoseStack.Pose pose, float y, float outer, float inner, float r, float g, float b) {
        org.joml.Matrix4f m = pose.pose();
        org.joml.Matrix3f n = pose.normal();
        int seg = 28;
        for (int i = 0; i < seg; i++) {
            double a0 = i * Math.PI * 2 / seg, a1 = (i + 1) * Math.PI * 2 / seg;
            float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float u0 = (float) i / seg, u1 = (float) (i + 1) / seg;
            v(vc, m, n, c0 * inner, y, s0 * inner, r, g, b, u0, 0);
            v(vc, m, n, c0 * outer, y, s0 * outer, r, g, b, u0, 1);
            v(vc, m, n, c1 * outer, y, s1 * outer, r, g, b, u1, 1);
            v(vc, m, n, c1 * inner, y, s1 * inner, r, g, b, u1, 0);
        }
    }

    private static void v(VertexConsumer vc, org.joml.Matrix4f m, org.joml.Matrix3f n, float x, float y, float z,
                          float r, float g, float b, float u, float vv) {
        vc.vertex(m, x, y, z).color(r, g, b, 1.0F).uv(u, vv).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, 0, -1, 0).endVertex();
    }

    private static void drawLightning(PoseStack pose, MultiBufferSource buf, int id, float age, int bolts, float size, int color) {
        VertexConsumer vc = buf.getBuffer(RenderType.lightning());
        org.joml.Matrix4f mat = pose.last().pose();
        int frame = (int) (age / 2);
        for (int i = 0; i < bolts; i++) {
            java.util.Random rnd = new java.util.Random(frame * 7919L + i * 104729L + id * 31L);
            if (rnd.nextFloat() < 0.35F) continue;                 // parpadeo
            double ang = rnd.nextDouble() * Math.PI * 2;
            double rad = (0.45 + rnd.nextDouble() * 0.25) * size;
            float x = (float) (Math.cos(ang) * rad), z = (float) (Math.sin(ang) * rad);
            float y = -0.7F + rnd.nextFloat() * 0.6F;                // empieza cerca de la cabeza (y hacia abajo)
            int segs = 5 + rnd.nextInt(3);
            for (int sgm = 0; sgm < segs; sgm++) {
                float nx = x + (rnd.nextFloat() - 0.5F) * 0.35F;
                float nz = z + (rnd.nextFloat() - 0.5F) * 0.35F;
                float ny = y + 0.18F + rnd.nextFloat() * 0.22F;
                quad(vc, mat, x, y, z, nx, ny, nz, 0.035F, color);
                if (rnd.nextFloat() < 0.3F) {                         // ramita
                    quad(vc, mat, nx, ny, nz, nx + (rnd.nextFloat() - 0.5F) * 0.3F, ny + 0.15F, nz + (rnd.nextFloat() - 0.5F) * 0.3F, 0.02F, color);
                }
                x = nx; y = ny; z = nz;
            }
        }
    }

    private static void quad(VertexConsumer vc, org.joml.Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float w, int color) {
        float r = ((color >> 16) & 255) / 255F, g = ((color >> 8) & 255) / 255F, b = (color & 255) / 255F, al = 0.9F;
        // dos planos cruzados para que el rayo se vea desde cualquier ángulo
        vc.vertex(m, x1 - w, y1, z1).color(r, g, b, al).endVertex();
        vc.vertex(m, x1 + w, y1, z1).color(r, g, b, al).endVertex();
        vc.vertex(m, x2 + w, y2, z2).color(r, g, b, al).endVertex();
        vc.vertex(m, x2 - w, y2, z2).color(r, g, b, al).endVertex();
        vc.vertex(m, x1, y1, z1 - w).color(r, g, b, al).endVertex();
        vc.vertex(m, x1, y1, z1 + w).color(r, g, b, al).endVertex();
        vc.vertex(m, x2, y2, z2 + w).color(r, g, b, al).endVertex();
        vc.vertex(m, x2, y2, z2 - w).color(r, g, b, al).endVertex();
    }

    private void renderAll(PoseStack pose, VertexConsumer vc, float r, float g, float b) {
        for (String n : LIMBS) root.getChild(n).render(pose, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }
}
