package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Capas de las waifus: ropa 3D, cabello en bloques (con orejas de elfa y accesorios), objeto en las manos y el libro de Shiori. */
public final class WaifuLayers {
    private WaifuLayers() {}

    public static final ModelLayerLocation ARMOR_INNER = new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, "waifu"), "armor_inner");
    public static final ModelLayerLocation ARMOR_OUTER = new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, "waifu"), "armor_outer");

    public static LayerDefinition armorInner() {
        return LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(0.7F)), 64, 32);
    }

    public static LayerDefinition armorOuter() {
        return LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.15F)), 64, 32);
    }

    public static ModelLayerLocation clothes(WaifuKind k) {
        return new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, k.id()), "clothes");
    }

    public static ModelLayerLocation hair(WaifuKind k) {
        return new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, k.id()), "hair");
    }

    public static Supplier<LayerDefinition> clothesDef(WaifuKind k) {
        return switch (k) {
            case KOZUE -> WaifuModels::clothesKozue;
            case HOMURA -> WaifuModels::clothesHomura;
            case KURA -> WaifuModels::clothesKura;
            case SHIORI -> WaifuModels::clothesShiori;
        };
    }

    public static Supplier<LayerDefinition> hairDef(WaifuKind k) {
        return switch (k) {
            case KOZUE -> WaifuModels::hairKozue;
            case HOMURA -> WaifuModels::hairHomura;
            case KURA -> WaifuModels::hairKura;
            case SHIORI -> WaifuModels::hairShiori;
        };
    }

    private static Map<String, ModelPart> bakeClothes(WaifuKind k, ModelPart root) {
        return switch (k) {
            case KOZUE -> WaifuModels.bakeClothesKozue(root);
            case HOMURA -> WaifuModels.bakeClothesHomura(root);
            case KURA -> WaifuModels.bakeClothesKura(root);
            case SHIORI -> WaifuModels.bakeClothesShiori(root);
        };
    }

    private static Map<String, ModelPart> bakeHair(WaifuKind k, ModelPart root) {
        return switch (k) {
            case KOZUE -> WaifuModels.bakeHairKozue(root);
            case HOMURA -> WaifuModels.bakeHairHomura(root);
            case KURA -> WaifuModels.bakeHairKura(root);
            case SHIORI -> WaifuModels.bakeHairShiori(root);
        };
    }

    // ------------------------------------------------------------------ ropa (se esconde lo que tapa la armadura)
    public static class Clothes extends RenderLayer<WaifuEntity, WaifuModel> {
        private final ResourceLocation tex;
        private final ModelPart root, body, rightArm, leftArm, rightLeg, leftLeg;
        private final Map<String, ModelPart> parts;
        private final Map<ModelPart, Float> front = new HashMap<>(), back = new HashMap<>(), side = new HashMap<>();

        public Clothes(RenderLayerParent<WaifuEntity, WaifuModel> parent, EntityModelSet models, WaifuKind k) {
            super(parent);
            tex = WaifuRenderer.tex(k, "clothes");
            root = models.bakeLayer(clothes(k));
            parts = bakeClothes(k, root);
            body = parts.get("body");
            rightArm = parts.get("right_arm");
            leftArm = parts.get("left_arm");
            rightLeg = parts.get("right_leg");
            leftLeg = parts.get("left_leg");
            for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
                String n = en.getKey();
                if (!n.contains("skirt")) continue;
                ModelPart p = en.getValue();
                if (n.endsWith("skirt_front") || n.endsWith("skirt_fr") || n.endsWith("skirt_fl")) front.put(p, p.xRot);
                else if (n.endsWith("skirt_back") || n.endsWith("skirt_br") || n.endsWith("skirt_bl")) back.put(p, p.xRot);
                else side.put(p, p.xRot);
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
            boolean chest = !e.getItemBySlot(EquipmentSlot.CHEST).isEmpty(), legs = !e.getItemBySlot(EquipmentSlot.LEGS).isEmpty(),
                    feet = !e.getItemBySlot(EquipmentSlot.FEET).isEmpty();
            for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
                String n = en.getKey();
                if (n.startsWith("c_")) en.getValue().visible = !chest;
                else if (n.startsWith("l_")) en.getValue().visible = !legs;
                else if (n.startsWith("f_")) en.getValue().visible = !feet;
            }
            float fwd = Math.max(0, -Math.min(m.rightLeg.xRot, m.leftLeg.xRot)), bk = Math.max(0, Math.max(m.rightLeg.xRot, m.leftLeg.xRot));
            float wave = Mth.sin(age * 0.1F) * 0.02F;
            front.forEach((p, b) -> p.xRot = b - fwd * 0.55F + wave);
            back.forEach((p, b) -> p.xRot = b - bk * 0.55F + wave);
            side.forEach((p, b) -> p.xRot = b - limbAmount * 0.12F + wave);
            root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex)), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F));
        }
    }

    // ------------------------------------------------------------------ cabello en bloques, orejas de elfa y accesorios
    public static class Hair extends RenderLayer<WaifuEntity, WaifuModel> {
        private final ResourceLocation tex;
        private final ModelPart root, head;
        private final Map<String, ModelPart> parts;

        public Hair(RenderLayerParent<WaifuEntity, WaifuModel> parent, EntityModelSet models, WaifuKind k) {
            super(parent);
            tex = WaifuRenderer.tex(k, "hair");
            root = models.bakeLayer(hair(k));
            parts = bakeHair(k, root);
            head = parts.get("head");
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            boolean helmet = !e.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
            for (Map.Entry<String, ModelPart> en : parts.entrySet()) {
                String n = en.getKey();
                if (n.equals("head")) continue;
                // con casco solo asoman las orejas y las puntas del cabello
                en.getValue().visible = !helmet || n.startsWith("ear") || n.startsWith("h_flare") || n.startsWith("h_back") || n.startsWith("h_side");
            }
            head.copyFrom(getParentModel().head);
            root.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex)), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F));
        }
    }

    // ------------------------------------------------------------------ el objeto que sostiene frente al pecho
    public static class Held extends RenderLayer<WaifuEntity, WaifuModel> {
        private final ItemStack stack;

        public Held(RenderLayerParent<WaifuEntity, WaifuModel> parent, WaifuKind k) {
            super(parent);
            stack = new ItemStack(switch (k) {
                case KOZUE -> Items.CRAFTING_TABLE;
                case HOMURA -> Items.FURNACE;
                case KURA -> Items.CHEST;
                case SHIORI -> Items.ENCHANTED_BOOK;
            });
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible() || !WaifuModel.holding(e, limbAmount) || getParentModel().attackTime > 0) return;
            pose.pushPose();
            getParentModel().body.translateAndRotate(pose);
            pose.translate(0.0F, 6.6F / 16F + Mth.sin(age * 0.09F) * 0.004F, -7.2F / 16F);
            if (stack.is(Items.ENCHANTED_BOOK)) {
                pose.mulPose(Axis.XP.rotationDegrees(-20));
                pose.scale(0.5F, 0.5F, 0.5F);
            } else {
                pose.mulPose(Axis.YP.rotationDegrees(180));
                pose.scale(0.42F, 0.42F, 0.42F);
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, buf,
                    e.level(), e.getId());
            pose.popPose();
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
            pose.translate(0.8F, -0.15F + bob, 0.1F);
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
