package com.cz.client;

import com.cz.ChakraZorro;
import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Fases 4 y 5: el manto se fusiona con el cuerpo; todo el jugador se tiñe de rojo, ojos blancos y colmillos. */
public class BeastLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(ChakraZorro.MODID, "textures/entity/beast_skin.png");
    private static final ResourceLocation TEX_SLIM = new ResourceLocation(ChakraZorro.MODID, "textures/entity/beast_skin_slim.png");
    private static final ResourceLocation GLOW = new ResourceLocation(ChakraZorro.MODID, "textures/entity/beast_glow.png");
    private static final ResourceLocation GLOW_SLIM = new ResourceLocation(ChakraZorro.MODID, "textures/entity/beast_glow_slim.png");
    private final PlayerModel<AbstractClientPlayer> wide, slim;

    public BeastLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        wide = new PlayerModel<>(models.bakeLayer(ClientModEvents.BEAST), false);
        slim = new PlayerModel<>(models.bakeLayer(ClientModEvents.BEAST_SLIM), true);
    }

    public static LayerDefinition createWide() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.03F), false), 64, 64);
    }

    public static LayerDefinition createSlim() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.03F), true), 64, 64);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        ZorroState s = ClientStates.get(player.getId());
        Phase v = s.visible();
        if (!s.awakened || !v.beast || (s.powering > 0 && s.progress() < 0.5F)) return;
        boolean isSlim = "slim".equals(player.getModelName());
        PlayerModel<AbstractClientPlayer> m = isSlim ? slim : wide;
        PlayerModel<AbstractClientPlayer> p = getParentModel();
        p.copyPropertiesTo(m);
        copy(p.head, m.head);
        copy(p.body, m.body);
        copy(p.rightArm, m.rightArm);
        copy(p.leftArm, m.leftArm);
        copy(p.rightLeg, m.rightLeg);
        copy(p.leftLeg, m.leftLeg);
        m.setAllVisible(false);
        m.head.visible = m.body.visible = m.rightArm.visible = m.leftArm.visible = m.rightLeg.visible = m.leftLeg.visible = true;
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        m.renderToBuffer(pose, buf.getBuffer(RenderType.entityCutoutNoCull(isSlim ? TEX_SLIM : TEX)), light, overlay, 1, 1, 1, 1);
        // ojos y colmillos que brillan en la oscuridad
        m.setAllVisible(false);
        m.head.visible = true;
        m.renderToBuffer(pose, buf.getBuffer(RenderType.eyes(isSlim ? GLOW_SLIM : GLOW)), 0xF000F0, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }

    private static void copy(ModelPart from, ModelPart to) {
        to.copyFrom(from);
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }
}
