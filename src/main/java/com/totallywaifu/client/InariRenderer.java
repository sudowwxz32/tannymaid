package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Inari: chica zorro diosa o zorro dios gigante. Al transformarse, una forma se encoge y la otra crece entre luz dorada.
 * El círculo dorado de su divinidad brilla en su espalda (y sobre sus hombros en forma de zorro).
 */
public class InariRenderer extends EntityRenderer<WaifuEntity> {
    public static final ModelLayerLocation GIRL = new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, "inari"), "girl");
    public static final ModelLayerLocation FOX = new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, "inari"), "fox");
    public static final ModelLayerLocation FOX_FACE = new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, "inari"), "fox_face");

    static final String[] FACES = {"normal", "blink", "happy", "smile", "surprised", "focused", "relaxed", "love", "playful", "shy", "sleep", "wink"};
    static final String[] FOX_FACES = {"normal", "blink", "happy", "focused"};
    static final ResourceLocation HALO = tex("halo");
    static final float FOX_SCALE = 2.2F;

    static ResourceLocation tex(String n) {
        return new ResourceLocation(TotallyWaifu.MODID, "textures/entity/diosa/" + n + ".png");
    }

    static float extraScale = 1.0F;
    private final GirlR girl;
    private final FoxR fox;

    public InariRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        girl = new GirlR(ctx);
        fox = new FoxR(ctx);
        shadowRadius = 0.45F;
    }

    /** Su "momento" del día: 0-1 tranquila, 2 sonríe, 3 relajada, 4 juguetona, 5 sonríe, 6 saluda y guiña, 7 tímida. */
    static int phase(WaifuEntity e) {
        return (int) (((e.tickCount + e.getId() * 97L) / 160) % 8);
    }

    static int expression(WaifuEntity e) {
        long t = e.tickCount + e.getId() * 53L;
        if (e.transform() > 0 || e.hurtTime > 0) return 4;               // sorprendida
        if (e.isAggressive()) return 5;                                    // concentrada: ojos dorados que brillan
        if (t % 100 < 3 || (t % 330 >= 8 && t % 330 < 11)) return 1;       // parpadea
        if (e.isHappy()) return (t / 40) % 3 == 0 ? 7 : 2;                 // enamorada / feliz
        if (e.isVehicle()) return 3;
        return switch (phase(e)) {
            case 2, 5 -> 3;
            case 3 -> 6;
            case 4 -> (t / 60) % 2 == 0 ? 8 : 2;
            case 6 -> (t % 160) < 40 ? 11 : 3;
            case 7 -> 9;
            default -> 0;
        };
    }

    @Override
    public void render(WaifuEntity e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        if (e.actor) light = 0xF000F0;                                     // en el cielo, brilla
        int t = e.transform();
        boolean isFox = e.isFox();
        shadowRadius = isFox ? 1.4F : 0.45F;
        if (t > 0) {
            float k = (t - partial) / 30.0F;                               // 1 → 0
            if (k > 0.5F) {
                extraScale = Mth.clamp((k - 0.5F) * 2, 0.05F, 1);
                (isFox ? (EntityRenderer<WaifuEntity>) girl : fox).render(e, yaw, partial, pose, buf, light);
            } else {
                extraScale = Mth.clamp(1 - k * 2, 0.05F, 1);
                (isFox ? (EntityRenderer<WaifuEntity>) fox : girl).render(e, yaw, partial, pose, buf, light);
            }
            extraScale = 1.0F;
        } else {
            (isFox ? (EntityRenderer<WaifuEntity>) fox : girl).render(e, yaw, partial, pose, buf, light);
        }
        super.render(e, yaw, partial, pose, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(WaifuEntity e) {
        return e.isFox() ? tex("fox") : tex("girl");
    }

    /** Un círculo dorado translúcido (dos caras), que gira despacio. */
    static void halo(PoseStack pose, MultiBufferSource buf, float size, float age, float alpha) {
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucentEmissive(HALO));
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotation(age * 0.012F));
        float h = size * (1 + Mth.sin(age * 0.08F) * 0.03F);
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        float[][] v = {{-h, -h, 0, 0}, {h, -h, 1, 0}, {h, h, 1, 1}, {-h, h, 0, 1}};
        for (int side = 0; side < 2; side++) {
            for (int i = 0; i < 4; i++) {
                float[] q = v[side == 0 ? i : 3 - i];
                vc.vertex(m, q[0], q[1], 0).color(1.0F, 1.0F, 1.0F, alpha).uv(q[2], q[3]).overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(0xF000F0).normal(n, 0, 0, side == 0 ? 1 : -1).endVertex();
            }
        }
        pose.popPose();
    }

    // ================================================================== chica
    static class GirlR extends MobRenderer<WaifuEntity, InariGirlModel> {
        GirlR(EntityRendererProvider.Context ctx) {
            super(ctx, new InariGirlModel(ctx.bakeLayer(GIRL)), 0.45F);
            addLayer(new GirlFace(this, ctx.bakeLayer(WaifuLayers.FACE).getChild("head")));
            addLayer(new GirlHalo(this));
        }

        @Override
        public ResourceLocation getTextureLocation(WaifuEntity e) {
            return tex("girl");
        }

        @Override
        protected void scale(WaifuEntity e, PoseStack pose, float partial) {
            float s = 0.98F * e.kind.scale * extraScale;
            pose.scale(s, s, s);
        }

        @Override
        protected boolean shouldShowName(WaifuEntity e) {
            return e.hasCustomName() && super.shouldShowName(e);
        }
    }

    static class GirlFace extends RenderLayer<WaifuEntity, InariGirlModel> {
        private final ModelPart head;
        private final ResourceLocation[] faces = new ResourceLocation[FACES.length];

        GirlFace(RenderLayerParent<WaifuEntity, InariGirlModel> parent, ModelPart head) {
            super(parent);
            this.head = head;
            for (int i = 0; i < FACES.length; i++) faces[i] = tex("face_" + FACES[i]);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float ls, float la, float partial, float age, float hy, float hp) {
            if (e.isInvisible()) return;
            head.copyFrom(getParentModel().head);
            head.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(faces[expression(e)])), light, LivingEntityRenderer.getOverlayCoords(e, 0));
        }
    }

    static class GirlHalo extends RenderLayer<WaifuEntity, InariGirlModel> {
        GirlHalo(RenderLayerParent<WaifuEntity, InariGirlModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float ls, float la, float partial, float age, float hy, float hp) {
            if (e.isInvisible()) return;
            pose.pushPose();
            getParentModel().body.translateAndRotate(pose);
            pose.translate(0, 0.28F, 0.5F);                                 // detrás de la espalda
            halo(pose, buf, 0.62F, age, 0.85F);
            pose.popPose();
        }
    }

    // ================================================================== zorro dios gigante
    static class FoxR extends MobRenderer<WaifuEntity, InariFoxModel> {
        FoxR(EntityRendererProvider.Context ctx) {
            super(ctx, new InariFoxModel(ctx.bakeLayer(FOX)), 1.4F);
            addLayer(new FoxFace(this, ctx.bakeLayer(FOX_FACE).getChild("face")));
            addLayer(new FoxHalo(this));
        }

        @Override
        public ResourceLocation getTextureLocation(WaifuEntity e) {
            return tex("fox");
        }

        @Override
        protected void scale(WaifuEntity e, PoseStack pose, float partial) {
            float s = FOX_SCALE * extraScale;
            pose.scale(s, s, s);
        }

        @Override
        protected boolean shouldShowName(WaifuEntity e) {
            return false;
        }
    }

    static class FoxFace extends RenderLayer<WaifuEntity, InariFoxModel> {
        private final ModelPart face;
        private final ResourceLocation[] faces = new ResourceLocation[FOX_FACES.length];

        FoxFace(RenderLayerParent<WaifuEntity, InariFoxModel> parent, ModelPart face) {
            super(parent);
            this.face = face;
            for (int i = 0; i < FOX_FACES.length; i++) faces[i] = tex("fox_face_" + FOX_FACES[i]);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float ls, float la, float partial, float age, float hy, float hp) {
            if (e.isInvisible()) return;
            int x = expression(e);
            int f = switch (x) {
                case 1, 10 -> 1;
                case 2, 3, 6, 7, 8, 9, 11 -> 2;
                case 5 -> 3;
                default -> 0;
            };
            if (x == 3 || x == 6) f = 0;
            InariFoxModel m = getParentModel();
            pose.pushPose();
            m.root.translateAndRotate(pose);
            m.fox.translateAndRotate(pose);
            m.head.translateAndRotate(pose);
            face.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(faces[f])), light, LivingEntityRenderer.getOverlayCoords(e, 0));
            if (f == 3) face.render(pose, buf.getBuffer(RenderType.eyes(faces[f])), 0xF000F0, OverlayTexture.NO_OVERLAY);   // ojos que brillan
            pose.popPose();
        }
    }

    static class FoxHalo extends RenderLayer<WaifuEntity, InariFoxModel> {
        FoxHalo(RenderLayerParent<WaifuEntity, InariFoxModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float ls, float la, float partial, float age, float hy, float hp) {
            if (e.isInvisible()) return;
            InariFoxModel m = getParentModel();
            pose.pushPose();
            m.root.translateAndRotate(pose);
            m.fox.translateAndRotate(pose);
            m.torso.translateAndRotate(pose);
            pose.translate(0, -0.62F, -0.2F);                               // sobre los hombros, detrás de la cabeza
            halo(pose, buf, 0.58F, age, 0.8F);
            pose.popPose();
        }
    }
}
