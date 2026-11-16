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

import java.util.function.Supplier;

/** Capas de las waifus de bloques: la cara (una textura por expresión) y el libro flotante de Shiori. */
public final class WaifuLayers {
    private WaifuLayers() {}

    public static final ModelLayerLocation FACE = new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, "waifu"), "face");

    public static ModelLayerLocation body(WaifuKind k) {
        return new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, k.id()), "body");
    }

    public static Supplier<LayerDefinition> bodyDef(WaifuKind k) {
        return () -> WaifuModels.layer(k.id());
    }

    // ------------------------------------------------------------------ cara
    public static class Face extends RenderLayer<WaifuEntity, WaifuModel> {
        private final ModelPart head;
        private final ResourceLocation normal, blink, happy, surprised, focused, tired;

        public Face(RenderLayerParent<WaifuEntity, WaifuModel> parent, EntityModelSet models, WaifuKind k) {
            super(parent);
            head = models.bakeLayer(FACE).getChild("head");
            normal = WaifuRenderer.tex(k, "face");
            blink = WaifuRenderer.tex(k, "face_blink");
            happy = WaifuRenderer.tex(k, "face_happy");
            surprised = WaifuRenderer.tex(k, "face_surprised");
            focused = WaifuRenderer.tex(k, "face_focused");
            tired = WaifuRenderer.tex(k, "face_tired");
        }

        private ResourceLocation expression(WaifuEntity e) {
            if (e.hurtTime > 0) return surprised;
            if (e.getHealth() < e.getMaxHealth() * 0.3F) return tired;
            long t = e.tickCount + e.getId() * 53L;
            if (t % 90 < 3 || (t % 270 >= 6 && t % 270 < 9)) return blink;
            if (e.isHappy()) return happy;
            if (e.isBusy() || e.isAggressive()) return focused;
            return normal;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            head.copyFrom(getParentModel().head);
            head.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(expression(e))), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F));
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
            pose.translate(0.6F, -0.05F + bob, -0.15F);
            pose.mulPose(Axis.YP.rotationDegrees(-90.0F + Mth.sin(age * 0.03F) * 20.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(80.0F));
            pose.scale(0.7F, 0.7F, 0.7F);
            float flip = age * 0.05F;
            float r = Mth.frac(flip + 0.25F) * 1.6F - 0.3F, l = Mth.frac(flip + 0.75F) * 1.6F - 0.3F;
            book.setupAnim(age, Mth.clamp(r, 0, 1), Mth.clamp(l, 0, 1), e.isAggressive() || e.attackAnim > 0 ? 1.0F : 0.85F);
            book.renderToBuffer(pose, buf.getBuffer(RenderType.entityCutout(TEX)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            pose.popPose();
        }
    }
}
