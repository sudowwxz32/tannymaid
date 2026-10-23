package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
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
import net.minecraft.resources.ResourceLocation;

/** Fase 4: pelaje rojo en los brazos, los costados y la espalda (el pecho queda libre) y brazaletes azules. */
public class Ssj4FurLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/ssj4_fur.png");
    private static final ResourceLocation TEX_SLIM = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/ssj4_fur_slim.png");
    private final PlayerModel<AbstractClientPlayer> wide, slim;

    public Ssj4FurLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        wide = new PlayerModel<>(models.bakeLayer(ClientModEvents.SSJ4_FUR), false);
        slim = new PlayerModel<>(models.bakeLayer(ClientModEvents.SSJ4_FUR_SLIM), true);
    }

    public static LayerDefinition createWide() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.3F), false), 64, 64);
    }

    public static LayerDefinition createSlim() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.3F), true), 64, 64);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        SaiyanState s = ClientStates.get(player.getId());
        if (!s.saiyan || s.ozaru || s.visibleForm() != Form.SSJ4) return;
        if (!s.transformed && (s.powering <= 0 || s.powering > s.poweringTotal / 2)) return;   // aparece a mitad de la carga
        boolean isSlim = "slim".equals(player.getModelName());
        PlayerModel<AbstractClientPlayer> m = isSlim ? slim : wide;
        PlayerModel<AbstractClientPlayer> p = getParentModel();
        p.copyPropertiesTo(m);
        copy(p.body, m.body);
        copy(p.rightArm, m.rightArm);
        copy(p.leftArm, m.leftArm);
        m.setAllVisible(false);
        m.body.visible = true;
        m.rightArm.visible = true;
        m.leftArm.visible = true;
        m.renderToBuffer(pose, buf.getBuffer(RenderType.entityCutoutNoCull(isSlim ? TEX_SLIM : TEX)), light,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void copy(ModelPart from, ModelPart to) {
        to.copyFrom(from);
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }
}
