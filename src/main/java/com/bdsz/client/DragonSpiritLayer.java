package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Dragón espiritual dorado, largo como una serpiente, que se enrosca alrededor del jugador (Puño del Dragón). */
public class DragonSpiritLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation SCALES = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/spirit_dragon.png");
    private static final ResourceLocation AURA = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/aura.png");
    private static final int SEGMENTS = 34;
    private final ModelPart segment;
    private final ModelPart head;

    public DragonSpiritLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        ModelPart root = models.bakeLayer(ClientModEvents.SPIRIT_LAYER);
        segment = root.getChild("segment");
        head = root.getChild("head");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("segment", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -5, 8, 8, 10)
                .texOffs(36, 0).addBox(-1, -6.5F, -3, 2, 3, 5), PartPose.ZERO);   // cuerpo + aleta dorsal
        PartDefinition h = r.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-5, -5, -8, 10, 9, 10)          // cráneo
                .texOffs(0, 37).addBox(-3.5F, -2, -16, 7, 5, 8)        // hocico
                .texOffs(30, 37).addBox(-3, 3, -15, 6, 2, 8), PartPose.ZERO); // mandíbula
        h.addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(40, 18).addBox(-1, -1, 0, 2, 2, 10), PartPose.offsetAndRotation(-3, -5, 0, 0.6F, -0.3F, 0));
        h.addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(40, 18).addBox(-1, -1, 0, 2, 2, 10), PartPose.offsetAndRotation(3, -5, 0, 0.6F, 0.3F, 0));
        h.addOrReplaceChild("whisker_r", CubeListBuilder.create().texOffs(0, 52).addBox(-14, -0.5F, -0.5F, 14, 1, 1), PartPose.offsetAndRotation(-3.5F, 0, -14, 0, -0.5F, 0.3F));
        h.addOrReplaceChild("whisker_l", CubeListBuilder.create().texOffs(0, 52).addBox(0, -0.5F, -0.5F, 14, 1, 1), PartPose.offsetAndRotation(3.5F, 0, -14, 0, 0.5F, -0.3F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        SaiyanState s = ClientStates.get(player.getId());
        if (!s.saiyan || s.dragonFist <= 0) return;
        float t = age + partial;
        float appear = Math.min(1.0F, (ClientStates.ticksSinceDragon(player.getId()) + partial) / 25.0F);   // el dragón se va formando
        VertexConsumer solid = buf.getBuffer(RenderType.entityTranslucentEmissive(SCALES));
        VertexConsumer glow = buf.getBuffer(RenderType.energySwirl(AURA, (t * 0.01F) % 1, (-t * 0.04F) % 1));
        // espiral: de los pies hacia arriba, girando alrededor del cuerpo (en el modelo, y crece hacia abajo)
        double spin = t * 0.06;
        float prevX = 0, prevY = 0, prevZ = 0;
        int shown = (int) (SEGMENTS * appear);
        for (int i = 0; i <= shown; i++) {
            float k = (float) i / SEGMENTS;
            double ang = k * Math.PI * 4.2 + spin;
            float rad = 1.15F - k * 0.25F + 0.06F * Mth.sin(t * 0.2F + i);
            float x = (float) Math.cos(ang) * rad;
            float z = (float) Math.sin(ang) * rad;
            float y = 1.45F - k * 3.4F + 0.05F * Mth.sin(t * 0.3F + i * 0.5F);
            if (i == 0) { prevX = x; prevY = y; prevZ = z; continue; }
            float dx = x - prevX, dy = y - prevY, dz = z - prevZ;
            float yawRad = (float) Math.atan2(dx, dz);
            float pitchRad = (float) Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz));
            float size = (i == shown ? 1.0F : 0.55F - k * 0.15F + 0.25F * Math.min(1, i / 6.0F)) * 0.06F;
            pose.pushPose();
            pose.translate(x, y, z);
            pose.mulPose(Axis.YP.rotation(yawRad));
            pose.mulPose(Axis.XP.rotation(pitchRad));
            pose.mulPose(Axis.YP.rotationDegrees(180));
            if (i == shown) {
                pose.scale(0.075F * 16, 0.075F * 16, 0.075F * 16);
                head.render(pose, solid, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 0.85F, 0.35F, 1);
                pose.scale(1.15F, 1.15F, 1.15F);
                head.render(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.6F, 0.45F, 0.1F, 1);
            } else {
                pose.scale(size * 16, size * 16, size * 16);
                segment.render(pose, solid, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 0.8F, 0.3F, 1);
                pose.scale(1.2F, 1.2F, 1.2F);
                segment.render(pose, glow, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0.55F, 0.4F, 0.08F, 1);
            }
            pose.popPose();
            prevX = x; prevY = y; prevZ = z;
        }
    }
}
