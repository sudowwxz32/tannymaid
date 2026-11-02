package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.Fusion;
import com.bdsz.data.FusionLogic;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Apariencia de la fusión: la piel del mob se mezcla con la del jugador (capa encima de la skin, con
 * zonas transparentes), partes que brillan (ojos del enderman, alma del warden) y los aretes dorados.
 */
public class FusionLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelLayerLocation WIDE = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "fusion_wide"), "main");
    public static final ModelLayerLocation SLIM = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "fusion_slim"), "main");
    public static final ModelLayerLocation GI_WIDE = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "fusion_gi_wide"), "main");
    public static final ModelLayerLocation GI_SLIM = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "fusion_gi_slim"), "main");
    public static final ModelLayerLocation EARRINGS = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "fusion_earrings"), "main");
    private static final ResourceLocation EAR_TEX = tex("earring");

    private final PlayerModel<AbstractClientPlayer> wide, slim;
    private final ModelPart earRoot, earHead, earR, earL;
    private final Gi giWide, giSlim;

    /** Traje 3D de la fusión (pantalón de karate, obi, guantes, pecho del mob y sus piezas 3D). */
    private static final class Gi {
        final ModelPart root, head, body, rightArm, leftArm, rightLeg, leftLeg;
        final java.util.Map<String, ModelPart> parts = new java.util.HashMap<>();

        Gi(ModelPart root) {
            this.root = root;
            head = root.getChild("head");
            body = root.getChild("body");
            rightArm = root.getChild("right_arm");
            leftArm = root.getChild("left_arm");
            rightLeg = root.getChild("right_leg");
            leftLeg = root.getChild("left_leg");
            for (String[] pr : FusionGiModels.PARTS) parts.put(pr[1], root.getChild(pr[0]).getChild(pr[1]));
        }

        void pose(PlayerModel<?> m) {
            ModelPart[][] pairs = {{m.head, head}, {m.body, body}, {m.rightArm, rightArm}, {m.leftArm, leftArm},
                    {m.rightLeg, rightLeg}, {m.leftLeg, leftLeg}};
            for (ModelPart[] pr : pairs) {
                pr[1].copyFrom(pr[0]);
                pr[1].xScale = pr[0].xScale;
                pr[1].yScale = pr[0].yScale;
                pr[1].zScale = pr[0].zScale;
            }
        }

        void show(String mob, boolean chestArmor, boolean legArmor) {
            for (java.util.Map.Entry<String, ModelPart> e : parts.entrySet()) {
                String n = e.getKey();
                boolean own = n.startsWith("zombie_") || n.startsWith("skeleton_") || n.startsWith("enderman_")
                        || n.startsWith("creeper_") || n.startsWith("golem_") || n.startsWith("warden_") || n.startsWith("dragon_");
                boolean v = own ? n.startsWith(mob + "_") : true;
                if (chestArmor && (n.equals("chest") || n.startsWith("shoulder_") || n.startsWith("skeleton_") || n.startsWith("golem_")
                        || n.startsWith("zombie_") || n.startsWith("creeper_"))) v = false;
                if (legArmor && (n.equals("hips") || n.startsWith("pant_") || n.startsWith("ankle_") || n.startsWith("obi"))) v = false;
                e.getValue().visible = v;
            }
        }
    }

    public FusionLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        wide = new PlayerModel<>(models.bakeLayer(WIDE), false);
        slim = new PlayerModel<>(models.bakeLayer(SLIM), true);
        earRoot = models.bakeLayer(EARRINGS);
        earHead = earRoot.getChild("head");
        earR = earHead.getChild("ear_r");
        earL = earHead.getChild("ear_l");
        giWide = new Gi(models.bakeLayer(GI_WIDE));
        giSlim = new Gi(models.bakeLayer(GI_SLIM));
    }

    private static ResourceLocation tex(String n) {
        return new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/fusion/" + n + ".png");
    }

    public static LayerDefinition createWide() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.28F), false), 64, 64);
    }

    public static LayerDefinition createSlim() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.28F), true), 64, 64);
    }

    public static LayerDefinition createGiWide() {
        return FusionGiModels.createWide();
    }

    public static LayerDefinition createGiSlim() {
        return FusionGiModels.createSlim();
    }

    public static LayerDefinition createEarrings() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.3F, 0, -0.3F, 0.6F, 0.7F, 0.6F)
                .texOffs(0, 4).addBox(-0.55F, 0.7F, -0.55F, 1.1F, 1.1F, 1.1F), PartPose.offset(-4.35F, -3.1F, 0.3F));
        head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.3F, 0, -0.3F, 0.6F, 0.7F, 0.6F)
                .texOffs(0, 4).addBox(-0.55F, 0.7F, -0.55F, 1.1F, 1.1F, 1.1F), PartPose.offset(4.35F, -3.1F, 0.3F));
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer p, float limbSwing, float limbAmount,
                       float partial, float age, float headYaw, float headPitch) {
        if (p.isInvisible()) return;
        SaiyanState s = ClientStates.get(p.getId());
        if (s.ozaru) return;
        PlayerModel<AbstractClientPlayer> parent = getParentModel();
        int overlay = LivingEntityRenderer.getOverlayCoords(p, 0.0F);
        Fusion f = Fusion.byId(s.fusion);
        if (f != Fusion.NONE && s.fusing == 0) {
            boolean isSlim = "slim".equals(p.getModelName());
            String name = f.name().toLowerCase(java.util.Locale.ROOT);
            // la cara: solo un toque del mob (media cara, ojos), no una plantilla por todo el cuerpo
            PlayerModel<AbstractClientPlayer> m = isSlim ? slim : wide;
            parent.copyPropertiesTo(m);
            copyScale(parent, m);
            m.setAllVisible(false);
            m.head.visible = true;
            m.renderToBuffer(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex(name))), light, overlay, 1, 1, 1, 1);
            float pulse = f == Fusion.WARDEN ? 0.7F + 0.3F * net.minecraft.util.Mth.sin(age * 0.15F) : 1.0F;
            if (f == Fusion.ENDERMAN || f == Fusion.GOLEM || f == Fusion.DRAGON) {
                m.renderToBuffer(pose, buf.getBuffer(RenderType.eyes(tex(name + "_glow"))), LightTexture.FULL_BRIGHT, overlay, 1, 1, 1, 1);
            }
            m.setAllVisible(true);
            // traje 3D: pantalón de karate azul, obi azul, guantes blancos y el pecho del mob con sus piezas 3D
            Gi gi = isSlim ? giSlim : giWide;
            gi.pose(parent);
            gi.show(name, !p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty(),
                    !p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).isEmpty());
            gi.root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex("gi_" + name))), light, overlay);
            if (f == Fusion.WARDEN || f == Fusion.ENDERMAN || f == Fusion.DRAGON) {
                gi.root.render(pose, buf.getBuffer(RenderType.eyes(tex("gi_" + name + "_glow"))), LightTexture.FULL_BRIGHT, overlay, pulse, pulse, pulse, 1);
            }
        }
        if (f == Fusion.SKELETON && s.fusionAnim > 0 && s.fusing == 0) energyBow(pose, buf, parent, s, age);
        if (f == Fusion.GOLEM && s.fusing == 0) {
            float atk = p.getAttackAnim(partial);
            if (atk > 0) golemArms(pose, buf, parent, atk, age);
        }
        // aretes dorados: uno al ponértelo, los dos cuando estás fusionado
        int total = FusionLogic.EAR_ANIM;
        boolean right = s.fusion > 0 || s.fusing > 0 || (s.earring && s.earAnim >= 0 && s.earAnim <= total * 0.4F)
                || (s.earAnim < 0 && -s.earAnim > total * 0.4F);
        boolean left = s.fusion > 0;
        if (right || left) {
            earHead.copyFrom(parent.head);
            earR.visible = right;
            earL.visible = left;
            earRoot.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(EAR_TEX)), light, overlay);
        }
    }

    /** Arco de ki amarillo creado con energía (en la mano izquierda) con su cuerda y la flecha de energía. */
    private static void energyBow(PoseStack pose, MultiBufferSource buf, PlayerModel<?> m, SaiyanState s, float age) {
        com.mojang.blaze3d.vertex.VertexConsumer vc = buf.getBuffer(RenderType.lightning());
        pose.pushPose();
        m.leftArm.translateAndRotate(pose);
        pose.scale(1 / 16F, 1 / 16F, 1 / 16F);
        PoseStack.Pose mat = pose.last();
        float appear = net.minecraft.util.Mth.clamp((24 - s.fusionAnim) / 4.0F, 0, 1) * net.minecraft.util.Mth.clamp(s.fusionAnim / 4.0F, 0, 1);
        float flick = 0.85F + 0.15F * net.minecraft.util.Mth.sin(age * 1.7F);
        float a = appear * flick;
        float hx = 1.0F, hy = 11.0F;
        int seg = 22;
        for (int i = 0; i <= seg; i++) {
            float t = i / (float) seg * 2 - 1;
            float y = hy - 4.0F * t * t;
            float z = t * 10.0F;
            box(vc, mat, hx, y, z, 0.55F, 1.0F, 0.95F, 0.55F, 0.9F * a);      // núcleo
            box(vc, mat, hx, y, z, 1.2F, 1.0F, 0.8F, 0.15F, 0.35F * a);        // brillo
        }
        // cuerda tensada hasta la mano derecha y la flecha de energía
        float px = -4.5F, py = 1.5F;
        for (int side = -1; side <= 1; side += 2) {
            float ty = hy - 4.0F, tz = side * 10.0F;
            for (int i = 0; i <= 10; i++) {
                float t = i / 10F;
                box(vc, mat, hx + (px - hx) * t, ty + (py - ty) * t, tz * (1 - t), 0.22F, 1.0F, 1.0F, 0.8F, 0.8F * a);
            }
        }
        for (int i = 0; i <= 14; i++) {
            float t = i / 14F;
            box(vc, mat, px + (hx - px) * t, py + (hy + 3 - py) * t, 0, 0.35F, 1.0F, 0.95F, 0.5F, 0.9F * a);
        }
        box(vc, mat, hx, hy + 3.5F, 0, 1.4F, 1.0F, 0.9F, 0.2F, 0.6F * a);
        pose.popPose();
    }

    /** Puños de ki con forma de brazos de gólem de hierro al golpear. */
    private static void golemArms(PoseStack pose, MultiBufferSource buf, PlayerModel<?> m, float atk, float age) {
        com.mojang.blaze3d.vertex.VertexConsumer vc = buf.getBuffer(RenderType.lightning());
        float a = 0.25F + 0.4F * net.minecraft.util.Mth.sin(atk * net.minecraft.util.Mth.PI);
        float pulse = 1.0F + 0.06F * net.minecraft.util.Mth.sin(age * 1.3F);
        for (int side = 0; side < 2; side++) {
            ModelPart arm = side == 0 ? m.rightArm : m.leftArm;
            float cx = side == 0 ? -1.0F : 1.0F;
            pose.pushPose();
            arm.translateAndRotate(pose);
            pose.scale(1 / 16F, 1 / 16F, 1 / 16F);
            PoseStack.Pose mat = pose.last();
            // hombro, antebrazo enorme y puño de gólem
            boxes(vc, mat, cx, 0.5F, 0, 6.5F * pulse, 6.0F, 6.5F * pulse, a);
            boxes(vc, mat, cx, 8.0F, 0, 7.5F * pulse, 9.0F, 7.5F * pulse, a);
            boxes(vc, mat, cx, 14.0F, 0, 8.5F * pulse, 4.5F, 8.5F * pulse, a * 1.2F);
            // dedos marcados
            for (int f = -1; f <= 1; f++) box(vc, mat, cx + f * 2.6F, 16.6F, -2.6F, 2.0F, 1.0F, 0.95F, 0.45F, a * 0.8F);
            pose.popPose();
        }
    }

    private static void boxes(com.mojang.blaze3d.vertex.VertexConsumer vc, PoseStack.Pose mat, float cx, float cy, float cz,
                              float w, float h, float d, float a) {
        boxS(vc, mat, cx, cy, cz, w, h, d, 1.0F, 0.9F, 0.3F, a * 0.55F);
        boxS(vc, mat, cx, cy, cz, w + 1.2F, h + 1.2F, d + 1.2F, 1.0F, 0.75F, 0.1F, a * 0.25F);
    }

    private static void box(com.mojang.blaze3d.vertex.VertexConsumer vc, PoseStack.Pose mat, float x, float y, float z, float size,
                            float r, float g, float b, float a) {
        boxS(vc, mat, x, y, z, size, size, size, r, g, b, a);
    }

    /** Caja centrada (x, y, z) de tamaño w×h×d, dibujada por ambos lados. */
    private static void boxS(com.mojang.blaze3d.vertex.VertexConsumer vc, PoseStack.Pose mat, float x, float y, float z,
                             float w, float h, float d, float r, float g, float b, float a) {
        if (a <= 0.01F) return;
        float x0 = x - w / 2, x1 = x + w / 2, y0 = y - h / 2, y1 = y + h / 2, z0 = z - d / 2, z1 = z + d / 2;
        float[][] q = {
                {x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0},
                {x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1},
                {x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1},
                {x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0},
                {x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0},
                {x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1}};
        org.joml.Matrix4f m4 = mat.pose();
        for (float[] f : q) {
            for (int i = 0; i < 4; i++) vc.vertex(m4, f[i * 3], f[i * 3 + 1], f[i * 3 + 2]).color(r, g, b, a).endVertex();
            for (int i = 3; i >= 0; i--) vc.vertex(m4, f[i * 3], f[i * 3 + 1], f[i * 3 + 2]).color(r, g, b, a).endVertex();
        }
    }

    private static void copyScale(PlayerModel<?> a, PlayerModel<?> b) {
        ModelPart[][] pairs = {{a.head, b.head}, {a.body, b.body}, {a.rightArm, b.rightArm}, {a.leftArm, b.leftArm},
                {a.rightLeg, b.rightLeg}, {a.leftLeg, b.leftLeg}, {a.hat, b.hat}, {a.jacket, b.jacket},
                {a.rightSleeve, b.rightSleeve}, {a.leftSleeve, b.leftSleeve}, {a.rightPants, b.rightPants}, {a.leftPants, b.leftPants}};
        for (ModelPart[] pr : pairs) {
            pr[1].xScale = pr[0].xScale;
            pr[1].yScale = pr[0].yScale;
            pr[1].zScale = pr[0].zScale;
            pr[1].copyFrom(pr[0]);
        }
    }
}
