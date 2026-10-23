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
    private static final ResourceLocation FIRE = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura_fire.png");
    private static final String[] LIMBS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
    private final ModelPart root;

    public AuraLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(ClientModEvents.AURA_LAYER);
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

        // envoltura de fuego en forma de gota, con puntas que suben por encima de la cabeza
        float flame = drama * (s.powering > 0 ? 0.4F + progress * 0.8F : 1.0F) * (forced ? 1.2F : 1.0F);
        renderShell(pose, buf, t, size, flame, 1.0F, r * a * 0.35F, g * a * 0.35F, b * a * 0.35F, player.getId());
        renderShell(pose, buf, t * 0.8F + 40, size * 1.12F, flame * 1.1F, 1.25F, or * a * 0.25F, og * a * 0.25F, ob * a * 0.25F, player.getId() + 3);

        // lenguas de fuego finas que suben por la superficie del aura
        drawWisps(pose, buf, t, size, flame, r * a, g * a, b * a, or * a, og * a, ob * a, player.getId());

        // ondas del aura
        int wc = k > 0 && f == Form.BASE ? 0xFF3020 : f.waveColor();
        float wr = ((wc >> 16) & 255) / 255F, wg = ((wc >> 8) & 255) / 255F, wb = (wc & 255) / 255F;
        // las ondas solo aparecen al cargar ki o al transformarse (no al caminar, volar o estar quieto)
        long since = ClientStates.ticksSinceTransform(player.getId());
        if (forced || since < 30) {
            drawWaves(pose, buf, t, wr * a, wg * a, wb * a, size, power, true, f == Form.BEAST ? 1.5F : 1.0F);
        }

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
        if (!powering) return;
        // un solo anillo suave en el suelo (solo mientras se transforma)
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(TEX, 0, (t * 0.005F) % 1.0F));
        float ph = (t / 60.0F) % 1.0F;
        float rad = 0.6F + ph * 1.2F;
        float fade = (1.0F - ph) * 0.35F;
        ring(vc, pose.last(), 1.48F, rad, rad * 0.9F, 0.85F * fade, 0.9F * fade, fade);
    }

    private static final int SEG = 28, RINGS = 14;
    private final float[][] sx = new float[RINGS + 1][SEG + 1], sy = new float[RINGS + 1][SEG + 1], sz = new float[RINGS + 1][SEG + 1];
    private final float[][] sf = new float[RINGS + 1][SEG + 1];

    /**
     * Aura de fuego: una superficie en forma de gota que envuelve al cuerpo. Es ancha en la cintura,
     * se cierra arriba en varias puntas de fuego de distinto alto que suben y ondulan (no son piezas sueltas).
     */
    private void renderShell(PoseStack pose, MultiBufferSource buf, float t, float size, float flame, float scale,
                             float r, float g, float b, int seed) {
        if (flame <= 0.05F) return;
        float feet = 1.55F;
        float baseH = (1.95F + flame * 0.4F) * (0.85F + size * 0.15F) * (0.94F + 0.06F * scale);
        float radius = (0.48F + flame * 0.1F) * (0.75F + size * 0.25F);
        float sd = seed * 0.71F;
        for (int j = 0; j <= SEG; j++) {
            float th = j * Mth.TWO_PI / SEG;
            // alto de cada columna: crea las puntas de fuego en la parte de arriba
            float tongue = 0.09F * Mth.sin(3 * th + t * 0.23F + sd) + 0.07F * Mth.sin(5 * th - t * 0.37F + sd * 2)
                    + 0.05F * Mth.sin(9 * th + t * 0.61F) + 0.035F * Mth.sin(14 * th - t * 0.83F + sd);
            float hcol = baseH * (1.0F + tongue * (0.6F + flame * 0.6F));
            for (int k = 0; k <= RINGS; k++) {
                float v = (float) k / RINGS;
                // perfil de gota: estrecho en los pies, ancho a media altura, cerrado arriba
                float prof = (float) Math.pow(Math.sin(Math.min(1.0, v * 0.92 + 0.06) * Math.PI), 0.75);
                if (v > 0.55F) prof *= 1.0F - (v - 0.55F) * 0.9F;
                float ripple = 1.0F + 0.08F * Mth.sin(5 * th + v * 9.0F - t * 0.5F + sd) + 0.05F * Mth.sin(3 * th - v * 6.0F + t * 0.3F);
                float rad = radius * prof * ripple;
                sx[k][j] = Mth.cos(th) * rad;
                sz[k][j] = Mth.sin(th) * rad * 0.8F;
                sy[k][j] = feet - v * hcol;
                // brillo: se apaga en los pies y en las puntas
                float fade = Math.min(1.0F, v / 0.18F) * (1.0F - v * 0.55F);
                sf[k][j] = fade;
            }
        }
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(FIRE, (t * 0.004F + seed * 0.13F) % 1.0F, (-t * 0.03F) % 1.0F));
        org.joml.Matrix4f m = pose.last().pose();
        org.joml.Matrix3f n = pose.last().normal();
        for (int k = 0; k < RINGS; k++) {
            float v0 = (float) k / RINGS * 2.0F, v1 = (float) (k + 1) / RINGS * 2.0F;
            for (int j = 0; j < SEG; j++) {
                float u0 = (float) j / SEG * 3.0F, u1 = (float) (j + 1) / SEG * 3.0F;
                sv(vc, m, n, k, j, u0, v0, r, g, b);
                sv(vc, m, n, k, j + 1, u1, v0, r, g, b);
                sv(vc, m, n, k + 1, j + 1, u1, v1, r, g, b);
                sv(vc, m, n, k + 1, j, u0, v1, r, g, b);
            }
        }
    }

    private void sv(VertexConsumer vc, org.joml.Matrix4f m, org.joml.Matrix3f n, int k, int j, float u, float v, float r, float g, float b) {
        float f = sf[k][j];
        vc.vertex(m, sx[k][j], sy[k][j], sz[k][j]).color(r * f, g * f, b * f, 1.0F).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 1, 0).endVertex();
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
            fireRing(vc, last, 1.48F, radius, radius * 0.16F, r * fade, g * fade, b * fade, t, i * 2.1F);
            fireRing(vc, last, 1.47F, radius * 0.97F, radius * 0.05F, Math.min(1, r * fade * 1.6F + 0.15F * fade),
                    Math.min(1, g * fade * 1.6F + 0.15F * fade), Math.min(1, b * fade * 1.6F + 0.15F * fade), t, i * 2.1F);   // borde brillante
        }
        for (int i = 0; i < 2; i++) {
            float ph = ((t / (period * 0.9F)) + i * 0.5F) % 1.0F;
            float y = 1.5F - ph * 2.3F * size;
            float radius = (0.62F + 0.12F * (float) Math.sin(ph * Math.PI)) * size;
            float fade = (float) Math.sin(ph * Math.PI) * 0.45F * boost;
            fireRing(vc, last, y, radius, radius * 0.1F, r * fade, g * fade, b * fade, t * 1.3F, i * 3.3F);
        }
        // coronas de llamas pequeñas en el borde de las ondas del suelo
        VertexConsumer lv = buf.getBuffer(RenderType.lightning());
        org.joml.Matrix4f m = last.pose();
        for (int i = 0; i < rings; i++) {
            float ph = ((t / period) + (float) i / rings) % 1.0F;
            float radius = (0.5F + ph * (1.6F + power) * boost) * size;
            float fade = (1.0F - ph) * (strong ? 0.8F : 0.5F);
            int n = 24;
            for (int j = 0; j < n; j++) {
                float th = j * Mth.TWO_PI / n + i;
                float hgt = (0.1F + 0.12F * (0.5F + 0.5F * Mth.sin(th * 5 + t * 0.7F))) * fade * size;
                float rr = radius * edge(th, t, i * 2.1F);
                float cx = Mth.cos(th) * rr, cz = Mth.sin(th) * rr, w = 0.05F * size;
                float tx = -Mth.sin(th) * w, tz = Mth.cos(th) * w;
                lv.vertex(m, cx - tx, 1.48F, cz - tz).color(r * fade, g * fade, b * fade, 1.0F).endVertex();
                lv.vertex(m, cx + tx, 1.48F, cz + tz).color(r * fade, g * fade, b * fade, 1.0F).endVertex();
                lv.vertex(m, cx + Mth.cos(th) * 0.03F, 1.48F - hgt, cz + Mth.sin(th) * 0.03F).color(0, 0, 0, 1.0F).endVertex();
                lv.vertex(m, cx + Mth.cos(th) * 0.03F, 1.48F - hgt, cz + Mth.sin(th) * 0.03F).color(0, 0, 0, 1.0F).endVertex();
            }
        }
    }

    /** Ondulación del borde de una onda (como el filo de una llama). */
    private static float edge(float th, float t, float seed) {
        return 1.0F + 0.07F * Mth.sin(7 * th + t * 0.2F + seed) + 0.04F * Mth.sin(13 * th - t * 0.31F + seed * 2);
    }

    /** Anillo con el borde exterior ondulado como fuego; se desvanece hacia adentro. */
    private static void fireRing(VertexConsumer vc, PoseStack.Pose pose, float y, float radius, float thick,
                                 float r, float g, float b, float t, float seed) {
        org.joml.Matrix4f m = pose.pose();
        org.joml.Matrix3f n = pose.normal();
        int seg = 40;
        for (int i = 0; i < seg; i++) {
            float a0 = i * Mth.TWO_PI / seg, a1 = (i + 1) * Mth.TWO_PI / seg;
            float o0 = radius * edge(a0, t, seed), o1 = radius * edge(a1, t, seed);
            float i0 = o0 - thick, i1 = o1 - thick;
            float u0 = (float) i / seg, u1 = (float) (i + 1) / seg;
            v(vc, m, n, Mth.cos(a0) * i0, y, Mth.sin(a0) * i0, r * 0.2F, g * 0.2F, b * 0.2F, u0, 0);
            v(vc, m, n, Mth.cos(a0) * o0, y, Mth.sin(a0) * o0, r, g, b, u0, 1);
            v(vc, m, n, Mth.cos(a1) * o1, y, Mth.sin(a1) * o1, r, g, b, u1, 1);
            v(vc, m, n, Mth.cos(a1) * i1, y, Mth.sin(a1) * i1, r * 0.2F, g * 0.2F, b * 0.2F, u1, 0);
        }
    }

    /** Lenguas de fuego delgadas: nacen en los pies, suben ondulando por la superficie del aura y se apagan arriba. */
    private static void drawWisps(PoseStack pose, MultiBufferSource buf, float t, float size, float flame,
                                  float r, float g, float b, float or, float og, float ob, int seed) {
        if (flame <= 0.1F) return;
        VertexConsumer vc = buf.getBuffer(RenderType.lightning());
        org.joml.Matrix4f m = pose.last().pose();
        int count = 8 + (int) (flame * 8);
        float radius = (0.48F + flame * 0.1F) * (0.75F + size * 0.25F);
        float height = (1.95F + flame * 0.4F) * (0.85F + size * 0.15F);
        for (int i = 0; i < count; i++) {
            float c = (t * 0.025F + i * 0.618F + seed * 0.1F) % 1.0F;      // ciclo de vida
            float th = i * 2.4F + Mth.sin(t * 0.05F + i) * 0.3F;
            float life = Mth.sin(c * Mth.PI);
            float len = 0.35F * size * (0.6F + flame * 0.4F);
            int segs = 4;
            float px = 0, py = 0, pz = 0;
            for (int k = 0; k <= segs; k++) {
                float v = c * 0.85F + (float) k / segs * len / height;
                if (v > 1.0F) break;
                float prof = (float) Math.pow(Math.sin(Math.min(1.0, v * 0.92 + 0.06) * Math.PI), 0.75);
                if (v > 0.55F) prof *= 1.0F - (v - 0.55F) * 0.9F;
                float wob = Mth.sin(t * 0.4F + i * 1.7F + k * 1.3F) * 0.12F;
                float rad = radius * prof * 0.92F;
                float x = Mth.cos(th + wob) * rad, z = Mth.sin(th + wob) * rad * 0.8F, y = 1.55F - v * height;
                if (k > 0) {
                    float w = 0.03F * size * (1.0F - (float) k / segs) * life + 0.004F;
                    float kk = 1.0F - (float) k / segs;
                    float cr = (r * kk + or * (1 - kk)) * life, cg = (g * kk + og * (1 - kk)) * life, cb = (b * kk + ob * (1 - kk)) * life;
                    vc.vertex(m, px - w, py, pz).color(cr, cg, cb, 1.0F).endVertex();
                    vc.vertex(m, px + w, py, pz).color(cr, cg, cb, 1.0F).endVertex();
                    vc.vertex(m, x + w, y, z).color(cr * 0.7F, cg * 0.7F, cb * 0.7F, 1.0F).endVertex();
                    vc.vertex(m, x - w, y, z).color(cr * 0.7F, cg * 0.7F, cb * 0.7F, 1.0F).endVertex();
                    vc.vertex(m, px, py, pz - w).color(cr, cg, cb, 1.0F).endVertex();
                    vc.vertex(m, px, py, pz + w).color(cr, cg, cb, 1.0F).endVertex();
                    vc.vertex(m, x, y, z + w).color(cr * 0.7F, cg * 0.7F, cb * 0.7F, 1.0F).endVertex();
                    vc.vertex(m, x, y, z - w).color(cr * 0.7F, cg * 0.7F, cb * 0.7F, 1.0F).endVertex();
                }
                px = x; py = y; pz = z;
            }
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
