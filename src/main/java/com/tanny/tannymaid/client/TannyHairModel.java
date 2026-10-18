package com.tanny.tannymaid.client;

import com.tanny.tannymaid.TannyMaid;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Cabello 3D de Tanny: bob corto con volumen, flequillo en mechones, mechones que
 * enmarcan la cara, pelitos desordenados arriba y la cofia de maid.
 * Coordenadas en píxeles relativas al pivote de la cabeza (cabeza = caja de -4..4, -8..0, -4..4).
 */
public class TannyHairModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(TannyMaid.MODID, "tanny"), "hair");

    public final ModelPart root;
    public final ModelPart back;
    public final ModelPart sideLeft;
    public final ModelPart sideRight;
    public final ModelPart lockLeft;
    public final ModelPart lockRight;
    public final ModelPart[] bangs;

    public TannyHairModel(ModelPart layerRoot) {
        this.root = layerRoot.getChild("hair");
        this.back = root.getChild("back");
        this.sideLeft = root.getChild("side_left");
        this.sideRight = root.getChild("side_right");
        this.lockLeft = root.getChild("lock_left");
        this.lockRight = root.getChild("lock_right");
        this.bangs = new ModelPart[] {
                root.getChild("bang_0"), root.getChild("bang_1"), root.getChild("bang_2"), root.getChild("bang_3")
        };
    }

    // Mechones del flequillo: x central, ancho, largo, inclinación lateral
    static final float[][] BANGS = {
            {-2.9F, 2.3F, 3.3F, 0.07F},
            {-0.85F, 2.0F, 2.7F, 0.02F},
            {1.15F, 2.1F, 3.1F, -0.03F},
            {3.05F, 1.8F, 2.6F, -0.09F}
    };
    static final float BANG_TILT = -0.14F;
    static final float SIDE_SPLAY = 0.07F;
    static final float BACK_TILT = 0.07F;

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition hair = mesh.getRoot().addOrReplaceChild("hair", CubeListBuilder.create(), PartPose.ZERO);

        // Coronilla con volumen
        hair.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.5F, -8.6F, -4.5F, 9.0F, 2.5F, 9.0F), PartPose.ZERO);

        // Pelitos desordenados arriba
        hair.addOrReplaceChild("tuft_0", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.5F, -1.6F, -0.5F, 1.0F, 1.6F, 1.6F),
                PartPose.offsetAndRotation(-1.2F, -8.4F, -0.5F, -0.35F, 0.3F, -0.45F));
        hair.addOrReplaceChild("tuft_1", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.5F, -1.2F, -0.5F, 1.0F, 1.2F, 1.2F),
                PartPose.offsetAndRotation(1.6F, -8.4F, 1.5F, 0.3F, -0.2F, 0.5F));

        // Parte de atrás del bob (un poco abombada hacia afuera en las puntas)
        hair.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.75F, 0.0F, -1.25F, 9.5F, 7.5F, 2.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, 4.0F, BACK_TILT, 0.0F, 0.0F));

        // Laterales del bob, hasta la mandíbula
        hair.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.25F, 0.0F, -4.0F, 1.25F, 7.5F, 8.0F),
                PartPose.offsetAndRotation(4.0F, -7.0F, 0.25F, 0.0F, 0.0F, -SIDE_SPLAY));
        hair.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, 0.0F, -4.0F, 1.25F, 7.5F, 8.0F),
                PartPose.offsetAndRotation(-4.0F, -7.0F, 0.25F, 0.0F, 0.0F, SIDE_SPLAY));

        // Mechones que enmarcan la cara
        hair.addOrReplaceChild("lock_left", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.6F, 0.0F, -0.6F, 1.2F, 6.9F, 1.2F),
                PartPose.offsetAndRotation(3.65F, -6.6F, -4.0F, -0.05F, 0.0F, -0.05F));
        hair.addOrReplaceChild("lock_right", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-0.6F, 0.0F, -0.6F, 1.2F, 6.9F, 1.2F),
                PartPose.offsetAndRotation(-3.65F, -6.6F, -4.0F, -0.05F, 0.0F, 0.05F));

        // Flequillo suave en mechones
        for (int i = 0; i < BANGS.length; i++) {
            float[] b = BANGS[i];
            hair.addOrReplaceChild("bang_" + i, CubeListBuilder.create().texOffs(0, 0)
                            .addBox(-b[1] / 2.0F, 0.0F, -0.6F, b[1], b[2], 0.8F),
                    PartPose.offsetAndRotation(b[0], -8.0F, -4.2F, BANG_TILT, 0.0F, b[3]));
        }

        // Cofia de maid: diadema blanca con volante
        hair.addOrReplaceChild("headband", CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-4.9F, -0.6F, -0.7F, 9.8F, 1.0F, 1.4F)
                        .texOffs(0, 56).addBox(-4.2F, -1.5F, -0.3F, 8.4F, 1.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, -8.3F, -1.2F, -0.25F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
