package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Aspecto "Noa" en estilo de bloques: bob negro, orejas de elfa, chaqueta celeste, enterizo negro y botas blancas. */
public class NoaRenderer extends HumanoidMobRenderer<WaifuEntity, NoaRenderer.Model> {
    public static final ModelLayerLocation BODY = new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, "noa"), "body");
    public static final ModelLayerLocation FACE = new ModelLayerLocation(new ResourceLocation(TotallyWaifu.MODID, "noa"), "face");
    private static final ResourceLocation TEX = new ResourceLocation(TotallyWaifu.MODID, "textures/entity/noa/body.png");

    public NoaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new Model(ctx.bakeLayer(BODY)), 0.45F);
        addLayer(new Face(this, ctx.bakeLayer(FACE).getChild("head")));
    }

    @Override
    public ResourceLocation getTextureLocation(WaifuEntity e) {
        return TEX;
    }

    @Override
    protected void scale(WaifuEntity e, PoseStack pose, float partial) {
        pose.scale(0.97F, 0.97F, 0.97F);
    }

    /** Pose neutra: brazos un poco levantados, sosteniendo algo frente al pecho cuando está quieta. */
    public static class Model extends HumanoidModel<WaifuEntity> {
        public Model(ModelPart root) {
            super(root);
            NoaModels.bake(root);
        }

        @Override
        public void prepareMobModel(WaifuEntity e, float limbSwing, float amount, float partial) {
            super.prepareMobModel(e, limbSwing, amount, partial);
            rightArmPose = e.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
            leftArmPose = ArmPose.EMPTY;
        }

        @Override
        public void setupAnim(WaifuEntity e, float limbSwing, float amount, float age, float headYaw, float headPitch) {
            super.setupAnim(e, limbSwing, amount, age, headYaw, headPitch);
            float breath = Mth.sin(age * 0.09F);
            if (amount < 0.05F && attackTime <= 0 && !e.isBusy()) {
                rightArm.xRot = -0.75F + breath * 0.03F; rightArm.yRot = -0.35F; rightArm.zRot = 0.05F;
                leftArm.xRot = -0.75F + breath * 0.03F; leftArm.yRot = 0.35F; leftArm.zRot = -0.05F;
            }
            head.y = breath * 0.15F;
            if (e.isHappy()) head.zRot = Mth.sin(age * 0.3F) * 0.1F;
            hat.copyFrom(head);
        }
    }

    /** La cara con su expresión. */
    static class Face extends RenderLayer<WaifuEntity, Model> {
        private final ModelPart head;
        private final ResourceLocation normal, blink, happy, surprised, focused, tired;

        Face(RenderLayerParent<WaifuEntity, Model> parent, ModelPart head) {
            super(parent);
            this.head = head;
            normal = tex("face");
            blink = tex("face_blink");
            happy = tex("face_happy");
            surprised = tex("face_surprised");
            focused = tex("face_focused");
            tired = tex("face_tired");
        }

        private static ResourceLocation tex(String n) {
            return new ResourceLocation(TotallyWaifu.MODID, "textures/entity/noa/" + n + ".png");
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, WaifuEntity e, float limbSwing, float limbAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.isInvisible()) return;
            ResourceLocation t;
            long k = e.tickCount + e.getId() * 53L;
            if (e.hurtTime > 0) t = surprised;
            else if (e.getHealth() < e.getMaxHealth() * 0.3F) t = tired;
            else if (k % 90 < 3 || (k % 270 >= 6 && k % 270 < 9)) t = blink;
            else if (e.isHappy()) t = happy;
            else if (e.isBusy() || e.isAggressive()) t = focused;
            else t = normal;
            head.copyFrom(getParentModel().head);
            head.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(t)), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F));
        }
    }
}
