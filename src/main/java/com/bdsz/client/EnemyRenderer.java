package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.entity.DbzEnemy;
import com.bdsz.entity.EnemyKind;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Dibuja a un enemigo con su textura, sus partes brillantes (ojos, visores, gemas) y su tamaño. */
public class EnemyRenderer extends MobRenderer<DbzEnemy, EnemyModel> {
    private final EnemyKind kind;
    private final ResourceLocation tex;

    public static ModelLayerLocation layer(EnemyKind k) {
        return new ModelLayerLocation(new ResourceLocation(BlockDragonSuperZ.MODID, "enemy_" + k.id), "main");
    }

    public EnemyRenderer(EntityRendererProvider.Context ctx, EnemyKind kind) {
        super(ctx, new EnemyModel(ctx.bakeLayer(layer(kind))), 0.5F * kind.scale * (kind.width > 1 ? 1.5F : 1.0F));
        this.kind = kind;
        this.tex = new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/enemy/" + kind.id + ".png");
        final RenderType glow = RenderType.eyes(new ResourceLocation(BlockDragonSuperZ.MODID, "textures/entity/enemy/" + kind.id + "_glow.png"));
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return glow;
            }
        });
    }

    @Override
    protected void scale(DbzEnemy e, PoseStack pose, float partial) {
        float s = kind.scale;
        pose.scale(s, s, s);
    }

    @Override
    public void render(DbzEnemy e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        super.render(e, yaw, partial, pose, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(DbzEnemy e) {
        return tex;
    }
}
