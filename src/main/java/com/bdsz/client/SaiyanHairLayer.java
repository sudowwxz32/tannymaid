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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cabello saiyajin 3D, brillante y del color de la fase.
 * Cada mechón es una cadena de 4 segmentos: se mueve solo, con un efecto de látigo que crece hacia la punta.
 */
public class SaiyanHairLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/saiyan_hair.png");
    private final ModelPart root;
    private final ModelPart head;
    private final List<Seg> segs = new ArrayList<>();
    private final List<Seg> roots = new ArrayList<>();

    /** Un segmento de mechón con sus datos de animación precalculados. */
    private record Seg(ModelPart part, String group, int depth, float seed, boolean hanging) {}

    public SaiyanHairLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(ClientModEvents.HAIR);
        Map<String, ModelPart> parts = HairModelData.bake(root);
        this.head = parts.get("head");
        for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
            String n = en.getKey();
            if (n.equals("root") || n.equals("head")) continue;
            int depth = n.endsWith("_m") || n.endsWith("_e") ? 1 : n.endsWith("_t") ? 2 : n.endsWith("_p") ? 3 : 0;
            String base = depth == 0 ? n : n.substring(0, n.length() - 2);
            String group = n.substring(0, n.indexOf('_') + 1);
            float seed = (base.hashCode() & 0xFFFF) * 0.0137F;
            Seg s = new Seg(en.getValue(), group, depth, seed, base.contains("_h"));
            segs.add(s);
            if (depth == 0) roots.add(s);
        }
    }

    private static String prefix(int style) {
        return switch (style) {
            case 2 -> "l_";
            case 3 -> "g_";
            case 5 -> "f_";
            case 6 -> "e_";
            case 7 -> "b_";
            case 8 -> "k_";
            case 9 -> "r_";
            default -> "s_";
        };
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        SaiyanState s = ClientStates.get(player.getId());
        Form f = s.visibleForm();   // durante la carga ya aparece el cabello de la fase destino
        if (!s.saiyan || f.hairStyle == 0 || s.ozaru) return;

        String prefix = prefix(f.hairStyle);
        boolean extraBang = f.hairStyle == 4;
        root.getAllParts().forEach(ModelPart::resetPose);
        for (Seg r : roots) {
            r.part.visible = r.group.equals("c_") || r.group.equals(prefix) || (extraBang && r.group.equals("x_"));
        }

        // ---- movimiento: cada mechón con su propio ritmo; fuerte al recargar o transformarse, suave en Ultra Instinto
        long since = ClientStates.ticksSinceTransform(player.getId());
        boolean intense = s.charging || s.powering > 0 || since < 25;
        boolean calm = f == Form.ULTRA;
        float growth = s.powering > 0 ? 0.25F + 0.75F * (1.0F - (float) s.powering / Math.max(1, s.poweringTotal)) : 1.0F;
        float amp = calm ? (intense ? 0.03F : 0.012F) : intense ? 0.085F + f.ordinal() * 0.003F : 0.022F;
        float freq = calm ? 0.25F : intense ? 1.5F : 0.55F;
        boolean flying = s.flight && player.getAbilities().flying;
        float wind = flying ? (float) Math.min(1.0, player.getDeltaMovement().length() * 1.5) : 0.0F;
        float t = age;
        for (Seg sg : segs) {
            if (!sg.group.equals(prefix) && !sg.group.equals("c_") && !(extraBang && sg.group.equals("x_"))) continue;
            if (sg.group.equals("c_") && sg.depth == 0 && !intense) continue;      // el cráneo casi no se mueve
            float k = 1.0F + sg.depth * 0.75F;                // látigo: la punta se mueve más que la raíz
            float ph = sg.seed + sg.depth * 0.65F;
            sg.part.zRot += Mth.sin(t * freq + ph) * amp * k;
            sg.part.xRot += Mth.cos(t * freq * 0.83F + ph * 1.3F) * amp * 0.7F * k;
            if (sg.hanging) {
                // la melena se mece al caminar y vuela hacia atrás con el viento
                if (sg.depth == 0) sg.part.xRot += limbAmount * 0.35F + wind * 0.55F;
                sg.part.xRot += Mth.sin(t * 0.12F + ph) * 0.03F * k + Mth.sin(limbSwing * 0.6662F + ph) * limbAmount * 0.04F * k;
            } else if (sg.depth == 0) {
                if (intense && !calm) sg.part.xRot -= 0.04F;   // la energía levanta el cabello
                sg.part.xRot -= wind * 0.18F;                      // el viento lo empuja hacia atrás al volar
                if (s.powering > 0 && !sg.group.equals("c_")) sg.part.yScale = growth;   // crece mientras se transforma
            }
        }

        head.copyFrom(getParentModel().head);
        float pulse = calm ? 0.95F + Mth.sin(age * 0.08F) * 0.05F
                : intense ? 0.95F + Mth.sin(age * 0.9F) * 0.15F : 0.9F + Mth.sin(age * 0.25F) * 0.1F;
        float r = Math.min(1.0F, f.r() * pulse + 0.05F), g = Math.min(1.0F, f.g() * pulse + 0.05F), b = Math.min(1.0F, f.b() * pulse + 0.05F);
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(TEX));
        head.render(pose, vc, f == Form.SSJ4 ? light : LightTexture.FULL_BRIGHT, LivingEntityRenderer.getOverlayCoords(player, 0.0F), r, g, b, 1.0F);
    }
}
