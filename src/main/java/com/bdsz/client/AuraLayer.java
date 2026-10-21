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
        if (!s.saiyan || !s.aura) return;
        Form f = s.activeForm();
        int k = s.activeKaioken();
        float r, g, b;
        if (f == Form.BASE) {
            if (k > 0) { r = 1.0F; g = 0.2F; b = 0.12F; } else { r = 0.75F; g = 0.9F; b = 1.0F; }
        } else {
            r = f.r(); g = f.g(); b = f.b();
            if (k > 0) { r = Math.min(1, r * 0.5F + 0.6F); g *= 0.45F; b *= 0.45F; }      // Kaioken tiñe el aura de rojo
        }
        float power = f == Form.BASE ? 0.35F : 0.5F + f.ordinal() * 0.07F + k / 400.0F;
        PlayerModel<AbstractClientPlayer> m = getParentModel();
        root.getChild("head").copyFrom(m.head);
        root.getChild("body").copyFrom(m.body);
        root.getChild("right_arm").copyFrom(m.rightArm);
        root.getChild("left_arm").copyFrom(m.leftArm);
        root.getChild("right_leg").copyFrom(m.rightLeg);
        root.getChild("left_leg").copyFrom(m.leftLeg);
        float height = 0.55F + power * 0.5F;
        for (int i = 0; i < flames.length; i++) {
            ModelPart fp = flames[i];
            fp.resetPose();
            fp.yScale = height * (0.75F + 0.35F * Mth.sin(age * 0.45F + i * 1.7F));
            fp.xScale = fp.zScale = 0.8F + 0.25F * Mth.sin(age * 0.3F + i);
        }
        float t = age + partial;
        VertexConsumer vc = buf.getBuffer(RenderType.energySwirl(TEX, (t * 0.004F) % 1.0F, (-t * 0.03F) % 1.0F));
        float a = Math.min(1.0F, 0.45F + power * 0.35F);
        renderAll(pose, vc, r * a, g * a, b * a);
        // capa exterior más grande y tenue
        pose.pushPose();
        float pulse = 1.08F + 0.03F * Mth.sin(age * 0.5F) + power * 0.04F;
        pose.translate(0, 0.75F, 0);
        pose.scale(pulse, pulse * 1.02F, pulse);
        pose.translate(0, -0.75F, 0);
        VertexConsumer vc2 = buf.getBuffer(RenderType.energySwirl(TEX, (t * -0.003F) % 1.0F, (-t * 0.045F) % 1.0F));
        renderAll(pose, vc2, r * a * 0.5F, g * a * 0.5F, b * a * 0.5F);
        pose.popPose();
    }

    private void renderAll(PoseStack pose, VertexConsumer vc, float r, float g, float b) {
        for (String n : LIMBS) root.getChild(n).render(pose, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }
}
