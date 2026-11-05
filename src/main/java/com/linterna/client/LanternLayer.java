package com.linterna.client;

import com.linterna.Linterna;
import com.linterna.data.LanternState;
import com.linterna.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Lo que se ve del anillo sobre el jugador: el traje (verde y negro, botas y guantes blancos), la armadura de energía
 * semitransparente, la silueta verde al volar o al ser intangible, el anillo en la mano, las armas flotantes y el
 * martillo gigante.
 */
public class LanternLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation ENERGY = new ResourceLocation(Linterna.MODID, "textures/entity/energia.png");
    private static final ResourceLocation SUIT = new ResourceLocation(Linterna.MODID, "textures/entity/traje.png");
    private static final ResourceLocation ARMOR = new ResourceLocation(Linterna.MODID, "textures/entity/armadura_energia.png");
    private final PlayerModel<AbstractClientPlayer> shellW, shellS, suitW, suitS;
    private final net.minecraft.client.model.HumanoidModel<AbstractClientPlayer> armorOut, armorIn;
    private final ModelPart hammer, gun, ring, gem;

    public LanternLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        shellW = new PlayerModel<>(models.bakeLayer(ClientModEvents.SHELL_WIDE), false);
        shellS = new PlayerModel<>(models.bakeLayer(ClientModEvents.SHELL_SLIM), true);
        armorOut = new net.minecraft.client.model.HumanoidModel<>(models.bakeLayer(ClientModEvents.ARMOR_OUTER));
        armorIn = new net.minecraft.client.model.HumanoidModel<>(models.bakeLayer(ClientModEvents.ARMOR_INNER));
        suitW = new PlayerModel<>(models.bakeLayer(ClientModEvents.SUIT_WIDE), false);
        suitS = new PlayerModel<>(models.bakeLayer(ClientModEvents.SUIT_SLIM), true);
        ModelPart c = models.bakeLayer(ClientModEvents.CONSTRUCTS);
        hammer = c.getChild("hammer");
        gun = c.getChild("gun");
        ring = c.getChild("ring");
        gem = c.getChild("gem");
    }

    public static LayerDefinition constructs() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("hammer", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 22.0F, 2.0F)          // mango
                .texOffs(0, 0).addBox(-7.0F, 18.0F, -5.0F, 14.0F, 10.0F, 10.0F)        // cabeza enorme
                .texOffs(0, 0).addBox(-8.0F, 17.0F, -6.0F, 16.0F, 1.0F, 12.0F)
                .texOffs(0, 0).addBox(-8.0F, 28.0F, -6.0F, 16.0F, 1.0F, 12.0F), PartPose.ZERO);
        root.addOrReplaceChild("gun", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F)           // cuerpo
                .texOffs(0, 0).addBox(-1.0F, -1.0F, -10.0F, 2.0F, 2.0F, 7.0F)          // cañón
                .texOffs(0, 0).addBox(-1.5F, 2.0F, -1.0F, 3.0F, 3.0F, 2.0F), PartPose.ZERO);
        // anillo: aro alrededor del dedo + sello con la gema
        root.addOrReplaceChild("ring", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.35F, 0.0F, -1.35F, 2.7F, 0.7F, 0.45F)
                .texOffs(0, 0).addBox(-1.35F, 0.0F, 0.9F, 2.7F, 0.7F, 0.45F)
                .texOffs(0, 0).addBox(-1.35F, 0.0F, -0.9F, 0.45F, 0.7F, 1.8F)
                .texOffs(0, 0).addBox(0.9F, 0.0F, -0.9F, 0.45F, 0.7F, 1.8F)
                .texOffs(0, 0).addBox(-0.9F, -0.25F, -1.85F, 1.8F, 1.2F, 0.5F), PartPose.ZERO);
        root.addOrReplaceChild("gem", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -0.05F, -2.25F, 1.2F, 0.8F, 0.45F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Copia la pose exacta (y la escala) del jugador para que todo encaje con su cuerpo. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copy(PlayerModel<AbstractClientPlayer> from, net.minecraft.client.model.HumanoidModel to) {
        from.copyPropertiesTo(to);
        ModelPart[][] pairs = {{from.head, to.head}, {from.hat, to.hat}, {from.body, to.body}, {from.rightArm, to.rightArm},
                {from.leftArm, to.leftArm}, {from.rightLeg, to.rightLeg}, {from.leftLeg, to.leftLeg}};
        for (ModelPart[] p : pairs) scale(p[0], p[1]);
        if (to instanceof PlayerModel<?> pm) {
            ModelPart[][] outer = {{from.jacket, pm.jacket}, {from.rightSleeve, pm.rightSleeve}, {from.leftSleeve, pm.leftSleeve},
                    {from.rightPants, pm.rightPants}, {from.leftPants, pm.leftPants}};
            for (ModelPart[] p : outer) { p[1].copyFrom(p[0]); scale(p[0], p[1]); }
        }
    }

    private static void scale(ModelPart a, ModelPart b) {
        b.xScale = a.xScale;
        b.yScale = a.yScale;
        b.zScale = a.zScale;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer p, float limbSwing, float limbAmount,
                       float partial, float age, float headYaw, float headPitch) {
        LanternState s = ClientStates.get(p.getId());
        if (!s.bearer()) return;
        PlayerModel<AbstractClientPlayer> parent = getParentModel();
        boolean slim = "slim".equals(p.getModelName());
        int overlay = LivingEntityRenderer.getOverlayCoords(p, 0.0F);
        float pulse = 0.85F + 0.15F * Mth.sin(age * 0.2F);

        // ---- traje verde y negro
        if (s.on(LanternState.SUIT) && !p.isInvisible()) {
            PlayerModel<AbstractClientPlayer> m = slim ? suitS : suitW;
            copy(parent, m);
            m.setAllVisible(true);
            m.head.visible = m.hat.visible = false;
            m.renderToBuffer(pose, buf.getBuffer(RenderType.entityCutoutNoCull(SUIT)), light, overlay, 1, 1, 1, 1);
        }
        // ---- armadura de energía semitransparente (casco, pechera, hombreras, grebas y botas)
        if (s.on(LanternState.ARMOR)) {
            var vc = buf.getBuffer(RenderType.entityTranslucentEmissive(ARMOR));
            copy(parent, armorIn);
            armorIn.setAllVisible(false);
            armorIn.body.visible = armorIn.rightLeg.visible = armorIn.leftLeg.visible = true;
            armorIn.renderToBuffer(pose, vc, LightTexture.FULL_BRIGHT, overlay, 0.35F, 1.0F, 0.45F, 0.55F * pulse);
            copy(parent, armorOut);
            armorOut.setAllVisible(false);
            armorOut.head.visible = armorOut.body.visible = armorOut.rightArm.visible = armorOut.leftArm.visible = true;
            armorOut.rightLeg.visible = armorOut.leftLeg.visible = true;
            armorOut.renderToBuffer(pose, vc, LightTexture.FULL_BRIGHT, overlay, 0.35F, 1.0F, 0.45F, 0.6F * pulse);
        }
        // ---- silueta verde (volando o intangible)
        boolean ghost = s.on(LanternState.GHOST);
        if (ghost || (s.on(LanternState.FLY) && !p.onGround())) {
            PlayerModel<AbstractClientPlayer> m = slim ? shellS : shellW;
            copy(parent, m);
            m.setAllVisible(true);
            m.hat.visible = m.jacket.visible = m.leftSleeve.visible = m.rightSleeve.visible = m.leftPants.visible = m.rightPants.visible = false;
            m.renderToBuffer(pose, buf.getBuffer(RenderType.entityTranslucentEmissive(ENERGY)), LightTexture.FULL_BRIGHT, overlay,
                    0.35F, 1.0F, 0.45F, (ghost ? 0.5F : 0.28F) * pulse);
        }
        var glow = buf.getBuffer(RenderType.entityTranslucentEmissive(ENERGY));
        // ---- el anillo puesto en el dedo (mano derecha), con la gema brillando
        if (s.worn) {
            pose.pushPose();
            parent.rightArm.translateAndRotate(pose);
            pose.translate((slim ? -0.5F : -1.0F) / 16F, 10.2F / 16F, 0);
            ring.render(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.16F, 0.62F, 0.26F, 1.0F);
            gem.render(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.6F, 1.0F, 0.65F, 1.0F);
            pose.scale(1.6F, 1.6F, 1.6F);
            gem.render(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.3F, 1.0F, 0.4F, 0.3F * pulse);
            pose.popPose();
        }
        // ---- armas flotantes
        if (s.on(LanternState.GUNS)) {
            for (int side = -1; side <= 1; side += 2) {
                pose.pushPose();
                parent.body.translateAndRotate(pose);
                pose.translate(side * 9F / 16F, (-4F + Mth.sin(age * 0.15F + side) * 0.8F) / 16F, 0);
                pose.mulPose(Axis.YP.rotation(parent.head.yRot));
                pose.mulPose(Axis.XP.rotation(parent.head.xRot));
                gun.render(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.3F, 1.0F, 0.4F, 0.75F);
                pose.popPose();
            }
        }
        // ---- martillo gigante
        if (s.hammer > 0) {
            float prog = 1.0F - (s.hammer - partial) / 16F;
            float swing = prog < 0.6F ? Mth.lerp(prog / 0.6F, -0.2F, -2.9F) : Mth.lerp((prog - 0.6F) / 0.4F, -2.9F, 0.6F);
            pose.pushPose();
            parent.body.translateAndRotate(pose);
            pose.translate(-5F / 16F, 2F / 16F, 0);
            pose.mulPose(Axis.XP.rotation(swing));
            pose.scale(1.4F, 1.4F, 1.4F);
            hammer.render(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.3F, 1.0F, 0.4F, 0.7F);
            pose.popPose();
        }
    }
}
