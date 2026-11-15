package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Capas 3D de las waifus: ropa (con falda que se mueve), cabeza (cabello y accesorios) y el libro flotante de Shiori. */
public final class WaifuLayers {
    private WaifuLayers() {}

    public static ModelLayerLocation head(WaifuKind k) {
        return new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, k.id()), "head");
    }

    public static ModelLayerLocation outfit(WaifuKind k) {
        return new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, k.id()), "outfit");
    }

    public static Supplier<LayerDefinition> headDef(WaifuKind k) {
        return switch (k) {
            case KOZUE -> WaifuModels::headKozue;
            case HOMURA -> WaifuModels::headHomura;
            case KURA -> WaifuModels::headKura;
            case SHIORI -> WaifuModels::headShiori;
        };
    }

    public static Supplier<LayerDefinition> outfitDef(WaifuKind k) {
        return switch (k) {
            case KOZUE -> WaifuModels::outfitKozue;
            case HOMURA -> WaifuModels::outfitHomura;
            case KURA -> WaifuModels::outfitKura;
            case SHIORI -> WaifuModels::outfitShiori;
        };
    }

    private static Map<String, ModelPart> bakeHead(WaifuKind k, ModelPart root) {
        return switch (k) {
            case KOZUE -> WaifuModels.bakeHeadKozue(root);
            case HOMURA -> WaifuModels.bakeHeadHomura(root);
            case KURA -> WaifuModels.bakeHeadKura(root);
            case SHIORI -> WaifuModels.bakeHeadShiori(root);
        };
    }

    private static Map<String, ModelPart> bakeOutfit(WaifuKind k, ModelPart root) {
        return switch (k) {
            case KOZUE -> WaifuModels.bakeOutfitKozue(root);
            case HOMURA -> WaifuModels.bakeOutfitHomura(root);
            case KURA -> WaifuModels.bakeOutfitKura(root);
            case SHIORI -> WaifuModels.bakeOutfitShiori(root);
        };
    }

    private static float[][] locks(WaifuKind k) {
        return switch (k) {
            case KOZUE -> WaifuHair.KOZUE;
            case HOMURA -> WaifuHair.HOMURA;
            case KURA -> WaifuHair.KURA;
            case SHIORI -> WaifuHair.SHIORI;
        };
    }

    private static int hairColor(WaifuKind k) {
        return switch (k) {
            case KOZUE -> WaifuHair.COLOR_KOZUE;
            case HOMURA -> WaifuHair.COLOR_HOMURA;
            case KURA -> WaifuHair.COLOR_KURA;
            case SHIORI -> WaifuHair.COLOR_SHIORI;
        };
    }

    // ------------------------------------------------------------------ ropa
    public static class Outfit extends RenderLayer<WaifuEntity, WaifuModel> {
        private final ResourceLocation tex;
        private final ModelPart root, body, rightArm, leftArm, rightLeg, leftLeg;
        private final Map<ModelPart, Float> skirtFront = new HashMap<>(), skirtBack = new HashMap<>(), skirtSide = new HashMap<>();

        public Outfit(RenderLayerParent<WaifuEntity, WaifuModel> parent, EntityModelSet models, WaifuKind k) {
            super(parent);
            tex = WaifuRenderer.tex(k, "outfit");
            root = models.bakeLayer(outfit(k));
            Map<String, ModelPart> parts = bakeOutfit(k, root);
            body = parts.get("body");
            rightArm = parts.get("right_arm");
            leftArm = parts.get("left_arm");
            rightLeg = parts.get("right_leg");
            leftLeg = parts.get("left_leg");
            for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
                String n = en.getKey();
                if (!n.startsWith("skirt")) continue;
                ModelPart p = en.getValue();
                if (n.equals("skirt_front") || n.equals("skirt_fr") || n.equals("skirt_fl")) skirtFront.put(p, p.xRot);
                else if (n.equals("skirt_back") || n.equals("skirt_br") || n.equals("skirt_bl")) skirtBack.put(p, p.xRot);
                else skirtSide.put(p, p.xRot);
            }
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            WaifuModel m = getParentModel();
            body.copyFrom(m.body);
            rightArm.copyFrom(m.rightArm);
            leftArm.copyFrom(m.leftArm);
            rightLeg.copyFrom(m.rightLeg);
            leftLeg.copyFrom(m.leftLeg);
            // la falda se abre con las piernas al caminar y ondea un poco
            float fwd = Math.max(0, -Math.min(m.rightLeg.xRot, m.leftLeg.xRot)), back = Math.max(0, Math.max(m.rightLeg.xRot, m.leftLeg.xRot));
            float wave = Mth.sin(age * 0.1F) * 0.02F;
            skirtFront.forEach((p, b) -> p.xRot = b - fwd * 0.55F + wave);
            skirtBack.forEach((p, b) -> p.xRot = b - back * 0.55F + wave);
            skirtSide.forEach((p, b) -> p.xRot = b - limbAmount * 0.12F + wave);
            root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex)), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F));
        }
    }

    // ------------------------------------------------------------------ cabeza: cabello en mechones + accesorios
    public static class Head extends RenderLayer<WaifuEntity, WaifuModel> {
        private final ResourceLocation tex, strand;
        private final ModelPart root, head;
        private final WaifuHairMesh hair;
        private final WaifuKind kind;

        public Head(RenderLayerParent<WaifuEntity, WaifuModel> parent, EntityModelSet models, WaifuKind k) {
            super(parent);
            kind = k;
            tex = WaifuRenderer.tex(k, "head");
            strand = WaifuRenderer.tex(k, "hair_strand");
            root = models.bakeLayer(head(k));
            head = bakeHead(k, root).get("head");
            hair = new WaifuHairMesh(locks(k), hairColor(k));
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            ModelPart ph = getParentModel().head;
            int overlay = LivingEntityRenderer.getOverlayCoords(e, 0.0F);
            float breeze = Mth.sin(age * 0.07F) * 0.04F;
            float walk = Mth.sin(limbSwing * 0.6662F) * limbAmount;
            float swayX = 0.08F + breeze + limbAmount * 0.45F - ph.xRot * 0.35F;
            float swayZ = Mth.sin(age * 0.05F) * 0.04F + walk * 0.12F - ph.zRot * 0.6F;
            if (kind == WaifuKind.SHIORI) swayX -= 0.03F + Mth.sin(age * 0.04F) * 0.03F;      // su magia le levanta un poco el cabello
            pose.pushPose();
            ph.translateAndRotate(pose);
            hair.render(buf.getBuffer(RenderType.entityCutoutNoCull(strand)), pose.last().pose(), pose.last().normal(),
                    light, overlay, swayX, swayZ, breeze * 0.5F + limbAmount * 0.1F, Mth.sin(age * 0.2F) * 0.1F, false);
            pose.popPose();
            head.copyFrom(ph);
            root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex)), light, overlay);
        }
    }

    // ------------------------------------------------------------------ libro flotante de Shiori
    public static class Book extends RenderLayer<WaifuEntity, WaifuModel> {
        private static final ResourceLocation TEX = new ResourceLocation("textures/entity/enchanting_table_book.png");
        private final BookModel book;

        public Book(RenderLayerParent<WaifuEntity, WaifuModel> parent, EntityModelSet models) {
            super(parent);
            book = new BookModel(models.bakeLayer(ModelLayers.BOOK));
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            pose.pushPose();
            float bob = Mth.sin(age * 0.1F) * 0.06F;
            pose.translate(0.72F, -0.05F + bob, -0.15F);
            pose.mulPose(Axis.YP.rotationDegrees(-90.0F + Mth.sin(age * 0.03F) * 20.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(80.0F));
            pose.scale(0.75F, 0.75F, 0.75F);
            float flip = age * 0.05F;
            float r = Mth.frac(flip + 0.25F) * 1.6F - 0.3F, l = Mth.frac(flip + 0.75F) * 1.6F - 0.3F;
            book.setupAnim(age, Mth.clamp(r, 0, 1), Mth.clamp(l, 0, 1), e.isAggressive() || e.attackAnim > 0 ? 1.0F : 0.85F);
            book.renderToBuffer(pose, buf.getBuffer(RenderType.entityCutout(TEX)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            pose.popPose();
        }
    }
}
