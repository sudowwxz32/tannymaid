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

/**
 * Aura 3D.
 *  - Fases dramáticas: capas de energía (interior y exterior de distinto color) y lenguas de fuego que nacen
 *    en la cintura y los hombros, suben, se estiran y se desvanecen; ondas en el suelo y rayos.
 *  - Ultra Instinto: un brillo plateado fino y sereno.
 */
public class AuraLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura.png");
    private static final String[] LIMBS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private static final int FLAMES = 14;
    private final ModelPart root;
    private final ModelPart fbody;
    private final ModelPart[] flames = new ModelPart[FLAMES];
    private final float[] fx = new float[FLAMES], fy = new float[FLAMES], fz = new float[FLAMES], fa = new float[FLAMES];

    public AuraLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(ClientModEvents.AURA_LAYER);
        this.fbody = root.getChild("fbody");
        for (int i = 0; i < FLAMES; i++) {
            flames[i] = fbody.getChild("flame" + i);
            double a = i * Math.PI * 2 / FLAMES + (i % 2) * 0.2;
            fa[i] = (float) a;
            fx[i] = (float) Math.cos(a) * 6.5F;
            fz[i] = (float) Math.sin(a) * 4.5F;
            fy[i] = i % 2 == 0 ? 12.0F : 5.0F;           // cintura y hombros
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        CubeDeformation big = new CubeDeformation(1.4F);
        CubeDeformation mid = new CubeDeformation(1.1F);
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, big), PartPose.ZERO);
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4, big), PartPose.ZERO);
        r.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4, mid), PartPose.offset(-5, 2, 0));
        r.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1, -2, -2, 4, 12, 4, mid), PartPose.offset(5, 2, 0));
        r.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4, mid), PartPose.offset(-1.9F, 12, 0));
        r.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4, mid), PartPose.offset(1.9F, 12, 0));
        // lenguas de fuego: base ancha, cuerpo y punta fina
        PartDefinition fb = r.addOrReplaceChild("fbody", CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < FLAMES; i++) {
            PartDefinition f = fb.addOrReplaceChild("flame" + i, CubeListBuilder.create().texOffs(32, 0).addBox(-2, -8, -2, 4, 8, 4), PartPose.ZERO);
            PartDefinition m = f.addOrReplaceChild("m", CubeListBuilder.create().texOffs(32, 0).addBox(-1.4F, -7, -1.4F, 2.8F, 7, 2.8F), PartPose.offset(0, -7.5F, 0));
            m.addOrReplaceChild("t", CubeListBuilder.create().texOffs(32, 0).addBox(-0.7F, -6, -0.7F, 1.4F, 6, 1.4F), PartPose.offset(0, -6.5F, 0));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        SaiyanState s = ClientStates.get(player.getId());
        boolean forced = s.charging || s.powering > 0;
        if (!s.saiyan || s.ozaru || !(s.aura || forced)) return;
        Form f = s.visibleForm();
        int k = s.transformed || s.powering > 0 ? s.kaioken : 0;
        float r, g, b;
        int oc;
        if (f == Form.BASE) {
            if (k > 0) { r = 1.0F; g = 0.2F; b = 0.12F; oc = 0x7A0A06; } else { r = 0.75F; g = 0.9F; b = 1.0F; oc = 0x5A90C8; }
        } else {
            r = f.ar(); g = f.ag(); b = f.ab();
            oc = f.outerColor();
            if (k > 0) { r = Math.min(1, r * 0.5F + 0.6F); g *= 0.45F; b *= 0.45F; oc = 0x8A0A0A; }      // Kaioken tiñe el aura de rojo
        }
        float or = ((oc >> 16) & 255) / 255F, og = ((oc >> 8) & 255) / 255F, ob = (oc & 255) / 255F;
        float progress = s.powering > 0 ? 1.0F - (float) s.powering / Math.max(1, s.poweringTotal) : 1.0F;
        float t = age + partial;

        PlayerModel<AbstractClientPlayer> m = getParentModel();
        root.getChild("head").copyFrom(m.head);
        root.getChild("body").copyFrom(m.body);
        root.getChild("right_arm").copyFrom(m.rightArm);
        root.getChild("left_arm").copyFrom(m.leftArm);
        root.getChild("right_leg").copyFrom(m.rightLeg);
        root.getChild("left_leg").copyFrom(m.leftLeg);

        if (f.calm() && k == 0) {
            renderCalm(pose, buf, t, progress, s.powering > 0);
            return;
        }

        float drama = Math.max(f.drama(), k > 0 ? 0.45F : 0.25F);
        float power = (f == Form.BASE ? 0.35F : 0.5F + f.ordinal() * 0.06F) + k / 400.0F;
        if (s.charging) power += 0.25F;
        if (s.powering > 0) power *= 0.5F + progress;
        float size = f.auraSize * (s.charging ? 1.15F : 1.0F) * (s.powering > 0 ? 0.7F + progress * 0.5F : 1.0F);
        float a = Math.min(1.0F, 0.45F + power * 0.35F);

        // capa interior
        pose.pushPose();
        scaleAround(pose, 0.92F + size * 0.08F);
        renderAll(pose, buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.004F) % 1.0F, (-t * 0.035F) % 1.0F)), r * a, g * a, b * a);
        pose.popPose();
        // capa exterior: color propio (rosa oscuro en el Rosé), más grande y tenue
        pose.pushPose();
        float pulse = (1.08F + 0.03F * Mth.sin(age * 0.5F) + power * 0.04F) * size;
        scaleAround(pose, pulse);
        renderAll(pose, buf.getBuffer(RenderType.energySwirl(TEX, (t * -0.003F) % 1.0F, (-t * 0.05F) % 1.0F)), or * a * 0.6F, og * a * 0.6F, ob * a * 0.6F);
        pose.popPose();
        if (size > 1.3F) {
            pose.pushPose();
            scaleAround(pose, pulse * 1.15F);
            renderAll(pose, buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.002F) % 1.0F, (-t * 0.065F) % 1.0F)), or * a * 0.3F, og * a * 0.3F, ob * a * 0.3F);
            pose.popPose();
        }

        // lenguas de fuego
        renderFlames(pose, buf, t, size, drama * (s.powering > 0 ? 0.4F + progress * 0.8F : 1.0F) * (forced ? 1.25F : 1.0F), r, g, b, or, og, ob, a);

        // ondas del aura
        int wc = k > 0 && f == Form.BASE ? 0xFF3020 : f.waveColor();
        float wr = ((wc >> 16) & 255) / 255F, wg = ((wc >> 8) & 255) / 255F, wb = (wc & 255) / 255F;
        drawWaves(pose, buf, t, wr * a, wg * a, wb * a, size, power, forced, f == Form.BEAST ? 1.5F : 1.0F);

        // rayos
        if (f.lightningAura() && (s.transformed || progress > 0.5F)) {
            int bolts = 3 + (int) (drama * 4);
            drawLightning(pose, buf, player.getId(), age, bolts, size, f.lightningColor(), f.tier() == 6 ? 0.05F : 0.035F);
            if (f == Form.ROSE) drawLightning(pose, buf, player.getId() + 777, age, 2, size * 1.1F, 0x9C0E5C, 0.03F);
        }
    }

    /** Ultra Instinto: una sola capa plateada, fina, que fluye muy despacio. */
    private void renderCalm(PoseStack pose, MultiBufferSource buf, float t, float progress, boolean powering) {
        float a = 0.28F + 0.06F * Mth.sin(t * 0.06F);
        if (powering) a *= 0.5F + progress * 0.5F;
        pose.pushPose();
        scaleAround(pose, 1.0F + 0.02F * Mth.sin(t * 0.05F));
        renderAll(pose, buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.001F) % 1.0F, (-t * 0.006F) % 1.0F)), 0.82F * a, 0.86F * a, 1.0F * a);
        pose.popPose();
        pose.pushPose();
        scaleAround(pose, 1.12F);
        renderAll(pose, buf.getBuffer(RenderType.energySwirl(TEX, (t * -0.001F) % 1.0F, (-t * 0.004F) % 1.0F)), 0.5F * a, 0.55F * a, 0.7F * a);
        pose.popPose();
        // un solo anillo suave en el suelo
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(TEX, 0, (t * 0.005F) % 1.0F));
        float ph = (t / 60.0F) % 1.0F;
        float rad = 0.6F + ph * 1.2F;
        float fade = (1.0F - ph) * 0.35F;
        ring(vc, pose.last(), 1.48F, rad, rad * 0.9F, 0.85F * fade, 0.9F * fade, fade);
    }

    private void renderFlames(PoseStack pose, MultiBufferSource buf, float t, float size, float drama,
                              float r, float g, float b, float or, float og, float ob, float a) {
        if (drama <= 0.05F) return;
        pose.pushPose();
        ModelPart body = root.getChild("body");
        fbody.copyFrom(body);
        fbody.translateAndRotate(pose);
        float height = (0.55F + drama * 0.75F) * size;
        VertexConsumer core = buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.01F) % 1.0F, (-t * 0.09F) % 1.0F));
        VertexConsumer glow = buf.getBuffer(RenderType.energySwirl(TEX, (t * -0.01F) % 1.0F, (-t * 0.07F) % 1.0F));
        for (int i = 0; i < FLAMES; i++) {
            ModelPart fl = flames[i];
            ModelPart mid = fl.getChild("m"), tip = mid.getChild("t");
            float c = (t * (0.035F + drama * 0.03F) + i * 0.618F) % 1.0F;        // ciclo de vida de cada llama
            float life = Mth.sin(c * Mth.PI);
            float sway = Mth.sin(t * 0.35F + i * 1.3F);
            fl.resetPose();
            mid.resetPose();
            tip.resetPose();
            fl.x = fx[i] * (0.9F + size * 0.15F);
            fl.z = fz[i] * (0.9F + size * 0.15F);
            fl.y = fy[i] - c * 7.0F * size;                                        // sube mientras vive
            fl.zRot = Mth.cos(fa[i]) * 0.35F + sway * 0.12F;                       // se abre hacia afuera
            fl.xRot = -Mth.sin(fa[i]) * 0.3F + Mth.cos(t * 0.3F + i) * 0.08F;
            mid.zRot = sway * 0.22F;
            tip.zRot = sway * 0.35F;
            mid.xRot = Mth.cos(t * 0.4F + i) * 0.15F;
            float hs = height * (0.45F + life * 0.85F);
            fl.yScale = hs;
            fl.xScale = fl.zScale = (0.7F + life * 0.5F) * (0.85F + size * 0.2F);
            float bright = life * a;
            fl.render(pose, core, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r * bright, g * bright, b * bright, 1.0F);
            // resplandor exterior del color secundario
            float xs = fl.xScale;
            fl.xScale = fl.zScale = xs * 1.45F;
            fl.yScale = hs * 1.12F;
            fl.render(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, or * bright * 0.6F, og * bright * 0.6F, ob * bright * 0.6F, 1.0F);
        }
        pose.popPose();
    }

    private static void scaleAround(PoseStack pose, float sc) {
        pose.translate(0, 0.75F, 0);
        pose.scale(sc, sc, sc);
        pose.translate(0, -0.75F, 0);
    }

    /** Ondas: anillos de energía que se expanden desde los pies y suben por el cuerpo. */
    private static void drawWaves(PoseStack pose, MultiBufferSource buf, float t, float r, float g, float b, float size, float power,
                                  boolean strong, float boost) {
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.01F) % 1.0F, (t * 0.02F) % 1.0F));
        PoseStack.Pose last = pose.last();
        float period = strong ? 12.0F : 22.0F;
        int rings = strong ? 3 : 2;
        for (int i = 0; i < rings; i++) {
            float ph = ((t / period) + (float) i / rings) % 1.0F;
            float radius = (0.5F + ph * (1.6F + power) * boost) * size;
            float fade = (1.0F - ph) * (strong ? 0.9F : 0.6F) * Math.min(1.3F, boost);
            ring(vc, last, 1.48F, radius, radius * 0.78F, r * fade, g * fade, b * fade);
        }
        for (int i = 0; i < 2; i++) {
            float ph = ((t / (period * 0.9F)) + i * 0.5F) % 1.0F;
            float y = 1.5F - ph * 2.3F * size;
            float radius = (0.62F + 0.12F * (float) Math.sin(ph * Math.PI)) * size;
            float fade = (float) Math.sin(ph * Math.PI) * 0.45F * boost;
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

    /** Rayos en zigzag que aparecen y desaparecen alrededor del cuerpo. */
    private static void drawLightning(PoseStack pose, MultiBufferSource buf, int id, float age, int bolts, float size, int color, float width) {
        VertexConsumer vc = buf.getBuffer(RenderType.lightning());
        org.joml.Matrix4f mat = pose.last().pose();
        int frame = (int) (age / 2);
        for (int i = 0; i < bolts; i++) {
            java.util.Random rnd = new java.util.Random(frame * 7919L + i * 104729L + id * 31L);
            if (rnd.nextFloat() < 0.35F) continue;                 // parpadeo
            double ang = rnd.nextDouble() * Math.PI * 2;
            double rad = (0.45 + rnd.nextDouble() * 0.3) * size;
            float x = (float) (Math.cos(ang) * rad), z = (float) (Math.sin(ang) * rad);
            float y = -0.8F + rnd.nextFloat() * 0.7F;
            int segs = 5 + rnd.nextInt(4);
            for (int sgm = 0; sgm < segs; sgm++) {
                float nx = x + (rnd.nextFloat() - 0.5F) * 0.38F;
                float nz = z + (rnd.nextFloat() - 0.5F) * 0.38F;
                float ny = y + 0.18F + rnd.nextFloat() * 0.24F;
                quad(vc, mat, x, y, z, nx, ny, nz, width, color);
                if (rnd.nextFloat() < 0.35F) {                        // ramita
                    quad(vc, mat, nx, ny, nz, nx + (rnd.nextFloat() - 0.5F) * 0.32F, ny + 0.15F, nz + (rnd.nextFloat() - 0.5F) * 0.32F, width * 0.6F, color);
                }
                x = nx; y = ny; z = nz;
            }
        }
    }

    private static void quad(VertexConsumer vc, org.joml.Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float w, int color) {
        float r = ((color >> 16) & 255) / 255F, g = ((color >> 8) & 255) / 255F, b = (color & 255) / 255F, al = 0.9F;
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
