package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Cola de mono peluda de la Fase 4: cuelga y se balancea. */
public class TailLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/tail.png");
    private static final ResourceLocation BROWN = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/tail_brown.png");
    private static final int SEGMENTS = 7;
    private final ModelPart root;
    private final ModelPart[] seg = new ModelPart[SEGMENTS];

    public TailLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        root = models.bakeLayer(ClientModEvents.TAIL_LAYER);
        ModelPart cur = root.getChild("body");
        for (int i = 0; i < SEGMENTS; i++) {
            cur = cur.getChild("t" + i);
            seg[i] = cur;
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition cur = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < SEGMENTS; i++) {
            float s = 2.2F - i * 0.18F;
            cur = cur.addOrReplaceChild("t" + i, CubeListBuilder.create().texOffs(0, i * 4).addBox(-s / 2, -s / 2, 0, s, s, 3.4F),
                    i == 0 ? PartPose.offsetAndRotation(0, 10.5F, 1.6F, -0.9F, 0, 0) : PartPose.offsetAndRotation(0, 0, 3.0F, 0.22F, 0, 0));
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        SaiyanState s = ClientStates.get(player.getId());
        if (!s.saiyan || s.ozaru) return;
        boolean ssj4 = s.visibleForm() == Form.SSJ4 && (s.transformed || s.powering > 0);
        if (!ssj4 && !s.tail) return;                                  // cola café elegida en /raza saiyajin
        root.getAllParts().forEach(ModelPart::resetPose);
        root.getChild("body").copyFrom(getParentModel().body);
        for (int i = 0; i < SEGMENTS; i++) {
            seg[i].yRot += Mth.sin(age * 0.12F - i * 0.55F) * (0.18F + limbAmount * 0.2F);
            seg[i].xRot += Mth.sin(age * 0.09F - i * 0.4F) * 0.05F + (i > 3 ? -0.08F : 0);
        }
        root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(ssj4 ? TEX : BROWN)), light, LivingEntityRenderer.getOverlayCoords(player, 0.0F));
    }
}
