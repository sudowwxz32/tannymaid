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
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Map;

/** Cabello saiyajin 3D, brillante y del color de la fase. */
public class SaiyanHairLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/saiyan_hair.png");
    private final ModelPart root;
    private final Map<String, ModelPart> parts;

    public SaiyanHairLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(ClientModEvents.HAIR);
        this.parts = HairModelData.bake(root);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        SaiyanState s = ClientStates.get(player.getId());
        Form f = s.activeForm();
        if (!s.saiyan || f.hairStyle == 0) return;

        String prefix = switch (f.hairStyle) {
            case 2 -> "l_";
            case 3 -> "g_";
            default -> "s_";
        };
        boolean extraBang = f.hairStyle == 4;
        root.getAllParts().forEach(ModelPart::resetPose);
        for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
            String n = en.getKey();
            if (n.equals("root") || n.equals("head")) continue;
            en.getValue().visible = n.startsWith("c_") || n.startsWith(prefix) || (extraBang && n.startsWith("x_"));
        }
        // el cabello vibra con la energía (fase 2 y Blue chisporrotean más)
        float jitter = (f == Form.SSJ2 || f == Form.BLUE) ? 0.04F : 0.015F;
        for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
            if (en.getKey().startsWith("s_") || en.getKey().startsWith("g_")) {
                en.getValue().zRot += Mth.sin(age * 0.9F + en.getKey().hashCode()) * jitter;
            }
        }
        // el cabello largo de la fase 3 se mece al caminar
        if (f.hairStyle == 2) {
            parts.get("l_back").xRot += limbAmount * 0.35F + Mth.sin(age * 0.08F) * 0.03F;
            parts.get("l_back2").xRot += limbAmount * 0.2F;
        }

        ModelPart head = parts.get("head");
        head.copyFrom(getParentModel().head);
        float pulse = 0.9F + Mth.sin(age * 0.25F) * 0.1F;
        float r = Math.min(1.0F, f.r() * pulse + 0.05F), g = Math.min(1.0F, f.g() * pulse + 0.05F), b = Math.min(1.0F, f.b() * pulse + 0.05F);
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        head.render(pose, vc, LightTexture.FULL_BRIGHT, LivingEntityRenderer.getOverlayCoords(player, 0.0F), r, g, b, 1.0F);
    }
}
