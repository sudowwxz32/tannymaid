package com.cz.client;

import com.cz.ChakraZorro;
import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/** Fase 5: un esqueleto de zorro (columna, costillas, omóplatos y cráneo) sale de la espalda y cubre al jugador como armadura. */
public class SkeletonLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(ChakraZorro.MODID, "textures/entity/bones.png");
    private final ModelPart root, body, head;

    public SkeletonLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        root = models.bakeLayer(ClientModEvents.SKELETON);
        body = root.getChild("body");
        head = root.getChild("head");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        ZorroState s = ClientStates.get(player.getId());
        Phase v = s.visible();
        if (!s.awakened || !v.skeleton) return;
        float grow = s.powering > 0 ? (s.progress() - 0.6F) / 0.35F : 1.0F;        // los huesos brotan de la espalda
        if (grow <= 0) return;
        grow = Math.min(1.0F, grow);
        PlayerModel<AbstractClientPlayer> p = getParentModel();
        body.copyFrom(p.body);
        body.xScale = p.body.xScale * grow;
        body.yScale = p.body.yScale;
        body.zScale = p.body.zScale * grow;
        head.copyFrom(p.head);
        head.xScale = head.yScale = head.zScale = grow;
        root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(TEX)), light, LivingEntityRenderer.getOverlayCoords(player, 0.0F));
    }
}
