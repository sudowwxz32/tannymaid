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
 * Accesorios 3D del vestido de Tanny: busto discreto (parte del vestido), falda con vuelo,
 * moño rojo en el cuello, lazo grande en la espalda y mangas abullonadas.
 * Coordenadas en píxeles relativas al pivote de cada parte (torso: -4..4, 0..12, -2..2).
 */
public class TannyOutfitModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(TannyMaid.MODID, "tanny"), "outfit");

    static final float SKIRT_FLARE = 0.22F;

    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart skirtFront;
    public final ModelPart skirtBack;
    public final ModelPart skirtLeft;
    public final ModelPart skirtRight;
    public final ModelPart bowTailLeft;
    public final ModelPart bowTailRight;

    public TannyOutfitModel(ModelPart root) {
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.skirtFront = body.getChild("skirt_front");
        this.skirtBack = body.getChild("skirt_back");
        this.skirtLeft = body.getChild("skirt_left");
        this.skirtRight = body.getChild("skirt_right");
        this.bowTailLeft = body.getChild("bow_tail_left");
        this.bowTailRight = body.getChild("bow_tail_right");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);

        // Busto discreto: forma parte del vestido (pechera blanca del delantal)
        body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.5F, 0.0F, -0.9F, 7.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, -2.0F, -0.28F, 0.0F, 0.0F));

        // Moño rojo en el cuello
        body.addOrReplaceChild("collar_knot", CubeListBuilder.create().texOffs(0, 8)
                        .addBox(-0.5F, -0.5F, -0.6F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 1.0F, -2.3F));
        body.addOrReplaceChild("collar_left", CubeListBuilder.create().texOffs(0, 11)
                        .addBox(0.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.4F, 1.0F, -2.3F, 0.0F, 0.0F, -0.3F));
        body.addOrReplaceChild("collar_right", CubeListBuilder.create().texOffs(8, 11)
                        .addBox(-2.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-0.4F, 1.0F, -2.3F, 0.0F, 0.0F, 0.3F));

        // Lazo grande en la espalda (delantal)
        body.addOrReplaceChild("bow_knot", CubeListBuilder.create().texOffs(16, 0)
                        .addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 7.5F, 2.5F));
        body.addOrReplaceChild("bow_left", CubeListBuilder.create().texOffs(24, 0)
                        .addBox(0.0F, -1.5F, -0.5F, 4.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.8F, 7.5F, 2.4F, 0.0F, -0.2F, -0.35F));
        body.addOrReplaceChild("bow_right", CubeListBuilder.create().texOffs(24, 4)
                        .addBox(-4.0F, -1.5F, -0.5F, 4.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-0.8F, 7.5F, 2.4F, 0.0F, 0.2F, 0.35F));
        body.addOrReplaceChild("bow_tail_left", CubeListBuilder.create().texOffs(36, 0)
                        .addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.7F, 8.0F, 2.6F, 0.15F, 0.0F, -0.15F));
        body.addOrReplaceChild("bow_tail_right", CubeListBuilder.create().texOffs(42, 0)
                        .addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-0.7F, 8.0F, 2.6F, 0.15F, 0.0F, 0.15F));

        // Falda con vuelo (cuatro paneles que se abren desde la cintura)
        body.addOrReplaceChild("skirt_front", CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-4.5F, 0.0F, -0.5F, 9.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, -2.2F, -SKIRT_FLARE, 0.0F, 0.0F));
        body.addOrReplaceChild("skirt_back", CubeListBuilder.create().texOffs(20, 32)
                        .addBox(-4.5F, 0.0F, -0.5F, 9.0F, 8.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 2.2F, SKIRT_FLARE, 0.0F, 0.0F));
        body.addOrReplaceChild("skirt_left", CubeListBuilder.create().texOffs(40, 32)
                        .addBox(-0.5F, 0.0F, -2.5F, 1.0F, 8.0F, 5.0F),
                PartPose.offsetAndRotation(4.2F, 8.0F, 0.0F, 0.0F, 0.0F, -SKIRT_FLARE));
        body.addOrReplaceChild("skirt_right", CubeListBuilder.create().texOffs(52, 32)
                        .addBox(-0.5F, 0.0F, -2.5F, 1.0F, 8.0F, 5.0F),
                PartPose.offsetAndRotation(-4.2F, 8.0F, 0.0F, 0.0F, 0.0F, SKIRT_FLARE));

        // Mangas abullonadas (brazos delgados: derecho -2..1, izquierdo -1..2)
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 48)
                .addBox(-2.5F, -2.4F, -2.5F, 4.0F, 4.0F, 5.0F), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(18, 48)
                .addBox(-1.5F, -2.4F, -2.5F, 4.0F, 4.0F, 5.0F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }
}
