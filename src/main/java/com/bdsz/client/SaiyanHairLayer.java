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
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Cabello saiyajin 3D liso: cada mechón es una sola punta continua (malla de 4 lados que se afina hasta un pico),
 * sin cubos apilados. Se dobla de forma fluida: la raíz casi no se mueve y la punta se mueve más.
 */
public class SaiyanHairLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation CAP_TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/saiyan_hair.png");
    private static final ResourceLocation STRAND_TEX = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/hair_strand.png");
    private static final int STEPS = 6;
    private static final float[] WT = new float[STEPS + 1];
    static {
        for (int k = 0; k <= STEPS; k++) WT[k] = (float) Math.pow(1.0 - (double) k / STEPS, 0.85);
    }

    private final ModelPart root;
    private final ModelPart cap;
    // búferes de la malla de un mechón (anillos x esquinas)
    private final float[][] vx = new float[STEPS + 1][4], vy = new float[STEPS + 1][4], vz = new float[STEPS + 1][4];
    private final float[][] nx = new float[STEPS + 1][4], ny = new float[STEPS + 1][4], nz = new float[STEPS + 1][4];

    public SaiyanHairLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.root = models.bakeLayer(ClientModEvents.HAIR);
        this.cap = root.getChild("head");
    }

    /** Casquete: cubre la parte de arriba, la nuca y los lados de la cabeza. */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.3F, -8.6F, -4.3F, 8.6F, 2.0F, 8.6F)
                .texOffs(0, 12).addBox(-4.3F, -8.0F, 3.45F, 8.6F, 6.0F, 0.9F)
                .texOffs(22, 12).addBox(-4.4F, -8.0F, -2.0F, 0.9F, 4.0F, 5.6F)
                .texOffs(36, 12).addBox(3.5F, -8.0F, -2.0F, 0.9F, 4.0F, 5.6F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
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
            case 10 -> "k_";
            case 11 -> "v_";
            default -> "s_";
        };
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        SaiyanState s = ClientStates.get(player.getId());
        if (!s.saiyan || s.ozaru) return;
        Form f = s.visibleForm();   // durante la carga ya aparece el cabello de la fase destino
        com.bdsz.data.HairStyles.Style chosen = com.bdsz.data.HairStyles.get(s.hair);
        boolean base = f == Form.BASE;
        if (base && chosen == null) return;
        if (!base && chosen == null && f.hairStyle == 0) return;

        // grupos visibles
        String main = chosen != null ? chosen.group() : prefix(f.hairStyle);
        boolean skullSpikes = chosen == null || !chosen.smooth();
        String extra = null;
        boolean extraHangOnly = false;
        if (chosen == null) {
            if (f.hairStyle == 4) extra = "x_";
            if (f.hairStyle == 10) extra = "q_";
        } else if (!base) {
            // el peinado elegido se adapta a cada transformación
            if (f == Form.SSJ3) { extra = "l_"; extraHangOnly = true; }          // melena larga por la espalda
            else if (f == Form.SSJ4) { extra = "f_"; extraHangOnly = true; }     // melena salvaje
            else if (f == Form.LEGEND3) extra = "q_";                             // puntas extra erizadas
            else if (f == Form.SSJ2 && !chosen.female()) extra = "x_";
        }
        float rise = chosen != null ? f.hairRise() : 0.0F;
        float lenMul = chosen != null ? f.hairLength() : 1.0F;

        long since = ClientStates.ticksSinceTransform(player.getId());
        boolean intense = !base && (s.charging || s.powering > 0 || since < 25);
        boolean calm = f == Form.ULTRA || base;
        float growth = s.powering > 0 ? 0.25F + 0.75F * (1.0F - (float) s.powering / Math.max(1, s.poweringTotal)) : 1.0F;
        float amp = calm ? (intense ? 0.05F : 0.02F) : intense ? 0.16F : 0.045F;
        float freq = calm ? 0.22F : intense ? 1.3F : 0.5F;
        boolean flying = s.flight && player.getAbilities().flying;
        float wind = flying ? (float) Math.min(1.0, player.getDeltaMovement().length() * 1.5) : 0.0F;

        int color = base ? chosen.color() : f.color;
        float pulse = base ? 1.0F : calm ? 0.95F + Mth.sin(age * 0.08F) * 0.05F
                : intense ? 0.95F + Mth.sin(age * 0.9F) * 0.15F : 0.92F + Mth.sin(age * 0.25F) * 0.08F;
        float cr = ((color >> 16) & 255) / 255F, cg = ((color >> 8) & 255) / 255F, cb = (color & 255) / 255F;
        float r = Math.min(1.0F, cr * pulse + 0.05F), g = Math.min(1.0F, cg * pulse + 0.05F), b = Math.min(1.0F, cb * pulse + 0.05F);
        if (f == Form.BLUE_EVO) {
            // azul metalizado: un brillo blanco que recorre el cabello
            float shine = Math.max(0, Mth.sin(age * 0.18F)) * 0.35F;
            r = Math.min(1, r + shine * 0.8F); g = Math.min(1, g + shine * 0.85F); b = Math.min(1, b + shine);
        }
        boolean dark = base || f == Form.SSJ4 || f == Form.LEGEND1;
        int lt = dark ? light : LightTexture.FULL_BRIGHT;        // el cabello natural / negro no brilla
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);

        // casquete
        cap.copyFrom(getParentModel().head);
        cap.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(CAP_TEX)), lt, overlay, r * 0.85F, g * 0.85F, b * 0.85F, 1.0F);

        // mechones
        pose.pushPose();
        cap.translateAndRotate(pose);
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(STRAND_TEX));
        Matrix4f mat = pose.last().pose();
        Matrix3f nm = pose.last().normal();
        String[] groups = HairSpikes.GROUP;
        float[][] data = HairSpikes.S;
        for (int i = 0; i < data.length; i++) {
            String gr = groups[i];
            float[] d = data[i];
            boolean hanging = d[10] > 0.5F;
            boolean skull = gr.equals("c_");
            boolean isMain = gr.equals(main);
            boolean isExtra = extra != null && gr.equals(extra) && (!extraHangOnly || hanging);
            if (skull ? !skullSpikes : !(isMain || isExtra)) continue;
            float seed = i * 1.37F;
            float k = skull ? 0.4F : 1.0F;
            float swayZ = Mth.sin(age * freq + seed) * amp * 2.2F * k;
            float swayX = Mth.cos(age * freq * 0.83F + seed * 1.3F) * amp * 1.6F * k;
            float baseX = 0, baseZ = 0;
            if (hanging) {
                baseX += limbAmount * 0.35F + wind * 0.55F + Mth.sin(limbSwing * 0.6662F + seed) * limbAmount * 0.08F;
                swayX += Mth.sin(age * 0.12F + seed) * 0.06F;
            } else if (!skull) {
                if (intense && !calm) baseX -= 0.05F;          // la energía levanta el cabello
                baseX -= wind * 0.2F;                            // el viento lo empuja hacia atrás
            }
            float len = d[5];
            float rx = d[3];
            if (isMain && rise > 0 && !hanging && rx < 2.0F) {
                rx *= 1.0F - rise;                               // las puntas se levantan
                len *= lenMul;                                   // y crecen
            }
            if (s.powering > 0 && !skull) len *= growth;
            spike(vc, mat, nm, d, rx, len, baseX, baseZ, swayX, swayZ, r, g, b, lt, overlay);
        }
        pose.popPose();
    }

    /** Construye y dibuja un mechón liso. */
    private void spike(VertexConsumer vc, Matrix4f mat, Matrix3f nm, float[] d, float rx0, float len, float baseX, float baseZ,
                       float swayX, float swayZ, float r, float g, float b, int light, int overlay) {
        float a0 = rx0 + baseX, b0 = d[4] + baseZ;
        float d0x = Mth.cos(a0) * Mth.sin(b0), d0y = -Mth.cos(a0) * Mth.cos(b0), d0z = -Mth.sin(a0);
        // vector de referencia fijo para todo el mechón (evita giros bruscos de la sección)
        float rx = 0, ry = 0, rz = 1;
        if (Math.abs(d0z) > 0.9F) { rx = 1; rz = 0; }
        float cx = d[0] - d0x * 0.6F, cy = d[1] - d0y * 0.6F, cz = d[2] - d0z * 0.6F;
        float width = d[6], flat = d[9];
        for (int k = 0; k <= STEPS; k++) {
            float t = (float) k / STEPS;
            float tw = t * (float) Math.sqrt(t);
            float a = a0 + d[7] * t * t + swayX * tw;
            float bb = b0 + d[8] * t * t + swayZ * tw;
            float dx = Mth.cos(a) * Mth.sin(bb), dy = -Mth.cos(a) * Mth.cos(bb), dz = -Mth.sin(a);
            // u = d x ref, v = d x u
            float ux = dy * rz - dz * ry, uy = dz * rx - dx * rz, uz = dx * ry - dy * rx;
            float ul = Mth.sqrt(ux * ux + uy * uy + uz * uz);
            if (ul < 1.0E-4F) ul = 1.0F;
            ux /= ul; uy /= ul; uz /= ul;
            float wx = dy * uz - dz * uy, wy = dz * ux - dx * uz, wz = dx * uy - dy * ux;
            float w = width * (d[11] > 0 ? 1.0F - d[11] * t : WT[k]);
            for (int j = 0; j < 4; j++) {
                float ph = Mth.PI / 4 + j * Mth.HALF_PI;
                float c = Mth.cos(ph), sn = Mth.sin(ph);
                vx[k][j] = cx + (ux * c + wx * sn * flat) * w;
                vy[k][j] = cy + (uy * c + wy * sn * flat) * w;
                vz[k][j] = cz + (uz * c + wz * sn * flat) * w;
                float px = ux * c + wx * sn * flat, py = uy * c + wy * sn * flat, pz = uz * c + wz * sn * flat;
                float pl = Mth.sqrt(px * px + py * py + pz * pz);
                // la normal se inclina un poco hacia la punta para un brillo suave
                nx[k][j] = px / pl * 0.9F + dx * 0.1F;
                ny[k][j] = py / pl * 0.9F + dy * 0.1F;
                nz[k][j] = pz / pl * 0.9F + dz * 0.1F;
            }
            cx += dx * len / STEPS;
            cy += dy * len / STEPS;
            cz += dz * len / STEPS;
        }
        for (int k = 0; k < STEPS; k++) {
            float v0 = (float) k / STEPS, v1 = (float) (k + 1) / STEPS;
            for (int j = 0; j < 4; j++) {
                int jj = (j + 1) & 3;
                float u0 = j * 0.25F, u1 = u0 + 0.25F;
                vert(vc, mat, nm, k, j, u0, v0, r, g, b, light, overlay);
                vert(vc, mat, nm, k, jj, u1, v0, r, g, b, light, overlay);
                vert(vc, mat, nm, k + 1, jj, u1, v1, r, g, b, light, overlay);
                vert(vc, mat, nm, k + 1, j, u0, v1, r, g, b, light, overlay);
            }
        }
    }

    private void vert(VertexConsumer vc, Matrix4f mat, Matrix3f nm, int k, int j, float u, float v,
                      float r, float g, float b, int light, int overlay) {
        vc.vertex(mat, vx[k][j] / 16.0F, vy[k][j] / 16.0F, vz[k][j] / 16.0F).color(r, g, b, 1.0F).uv(u, v)
                .overlayCoords(overlay).uv2(light).normal(nm, nx[k][j], ny[k][j], nz[k][j]).endVertex();
    }
}
