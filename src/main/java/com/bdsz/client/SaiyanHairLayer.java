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
        Form f = s.visibleForm();   // durante la carga ya aparece el cabello de la fase destino
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
        // ---- movimiento del cabello: vibra con la energía y ondea fuerte al recargar o transformarse
        long since = ClientStates.ticksSinceTransform(player.getId());
        boolean intense = s.charging || s.powering > 0 || since < 20;
        float growth = s.powering > 0 ? 0.3F + 0.7F * (1.0F - (float) s.powering / Math.max(1, s.poweringTotal)) : 1.0F;
        float amp = intense ? 0.11F : (f == Form.SSJ2 || f == Form.BLUE) ? 0.04F : 0.015F;
        float freq = intense ? 1.6F : 0.9F;
        for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
            String n = en.getKey();
            ModelPart part = en.getValue();
            boolean spike = n.startsWith("s_") || n.startsWith("g_") || n.startsWith("x_") || n.startsWith("l_");
            if (!spike) continue;
            float seed = n.hashCode() * 0.37F;
            part.zRot += Mth.sin(age * freq + seed) * amp;
            part.xRot += Mth.cos(age * freq * 0.8F + seed) * amp * 0.6F;
            if (intense && !n.endsWith("_m") && !n.endsWith("_t") && !n.endsWith("_e")) {
                part.xRot -= 0.06F;                               // la energía levanta el cabello
                if (s.powering > 0) part.yScale = growth;          // el cabello crece mientras se transforma
            }
        }
        // el cabello largo de la fase 3 se mece al caminar y ondea con la energía
        if (f.hairStyle == 2) {
            parts.get("l_back").xRot += limbAmount * 0.35F + Mth.sin(age * (intense ? 0.9F : 0.08F)) * (intense ? 0.12F : 0.03F);
            parts.get("l_back2").xRot += limbAmount * 0.2F + (intense ? Mth.sin(age * 1.1F + 1) * 0.1F : 0);
        }

        ModelPart head = parts.get("head");
        head.copyFrom(getParentModel().head);
        float pulse = intense ? 0.95F + Mth.sin(age * 0.9F) * 0.15F : 0.9F + Mth.sin(age * 0.25F) * 0.1F;
        float r = Math.min(1.0F, f.r() * pulse + 0.05F), g = Math.min(1.0F, f.g() * pulse + 0.05F), b = Math.min(1.0F, f.b() * pulse + 0.05F);
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        head.render(pose, vc, LightTexture.FULL_BRIGHT, LivingEntityRenderer.getOverlayCoords(player, 0.0F), r, g, b, 1.0F);
    }
}
