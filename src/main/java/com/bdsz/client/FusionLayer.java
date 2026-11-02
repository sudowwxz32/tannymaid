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
    public static final ModelLayerLocation EARRINGS = new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "fusion_earrings"), "main");
    private static final ResourceLocation EAR_TEX = tex("earring");

    private final PlayerModel<AbstractClientPlayer> wide, slim;
    private final ModelPart earRoot, earHead, earR, earL;

    public FusionLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        wide = new PlayerModel<>(models.bakeLayer(WIDE), false);
        slim = new PlayerModel<>(models.bakeLayer(SLIM), true);
        earRoot = models.bakeLayer(EARRINGS);
        earHead = earRoot.getChild("head");
        earR = earHead.getChild("ear_r");
        earL = earHead.getChild("ear_l");
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
            PlayerModel<AbstractClientPlayer> m = "slim".equals(p.getModelName()) ? slim : wide;
            parent.copyPropertiesTo(m);
            copyScale(parent, m);
            String name = f.name().toLowerCase(java.util.Locale.ROOT);
            m.renderToBuffer(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex(name))), light, overlay, 1, 1, 1, 1);
            if (f == Fusion.ENDERMAN || f == Fusion.WARDEN || f == Fusion.GOLEM) {
                float pulse = f == Fusion.WARDEN ? 0.7F + 0.3F * net.minecraft.util.Mth.sin(age * 0.15F) : 1.0F;
                m.renderToBuffer(pose, buf.getBuffer(RenderType.eyes(tex(name + "_glow"))), LightTexture.FULL_BRIGHT, overlay, pulse, pulse, pulse, 1);
            }
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
