package com.bdsz.client;

import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

/**
 * Cuerpo femenino: un poco de busto, con la piel del jugador. Si lleva pechera (de Minecraft o del mod),
 * el busto también se cubre con esa armadura para que se ajuste al cuerpo.
 */
public class FemaleBodyLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ModelPart skinRoot, skin, skinOver, armorRoot, armor;

    public FemaleBodyLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        skinRoot = models.bakeLayer(ClientModEvents.BUST_SKIN);
        skin = skinRoot.getChild("body").getChild("bust");
        skinOver = skinRoot.getChild("body").getChild("bust_over");
        armorRoot = models.bakeLayer(ClientModEvents.BUST_ARMOR);
        armor = armorRoot.getChild("body").getChild("bust");
    }

    private static final PartPose BUST = PartPose.offsetAndRotation(0, 2.7F, -2.0F, -0.17F, 0, 0);

    public static LayerDefinition createSkin() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        // busto sutil: poco volumen y con el borde superior pegado al pecho
        body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(19, 21).addBox(-3.0F, 0, -1.3F, 6, 2.6F, 1.3F), BUST);
        body.addOrReplaceChild("bust_over", CubeListBuilder.create().texOffs(19, 37).addBox(-3.0F, 0, -1.3F, 6, 2.6F, 1.3F, new CubeDeformation(0.25F)), BUST);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createArmor() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("bust", CubeListBuilder.create().texOffs(19, 21).addBox(-3.0F, 0, -1.3F, 6, 2.6F, 1.3F, new CubeDeformation(0.85F)), BUST);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing,
                       float limbAmount, float partial, float age, float headYaw, float headPitch) {
        if (player.isInvisible()) return;
        SaiyanState s = ClientStates.get(player.getId());
        if (!s.saiyan || s.ozaru || s.gender != 2) return;
        ModelPart body = getParentModel().body;
        int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
        skinRoot.getChild("body").copyFrom(body);
        ResourceLocation tex = player.getSkinTextureLocation();
        skin.render(pose, buf.getBuffer(RenderType.entityCutoutNoCull(tex)), light, overlay);
        if (getParentModel().jacket.visible) skinOver.render(pose, buf.getBuffer(RenderType.entityTranslucent(tex)), light, overlay);

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ArmorItem ai && ai.getEquipmentSlot() == EquipmentSlot.CHEST) {
            String mat = ai.getMaterial().getName();
            String domain = "minecraft";
            int idx = mat.indexOf(':');
            if (idx != -1) { domain = mat.substring(0, idx); mat = mat.substring(idx + 1); }
            String path = String.format(java.util.Locale.ROOT, "%s:textures/models/armor/%s_layer_1.png", domain, mat);
            path = net.minecraftforge.client.ForgeHooksClient.getArmorTexture(player, chest, path, EquipmentSlot.CHEST, null);
            ResourceLocation armorTex = new ResourceLocation(path);
            float r = 1, g = 1, b = 1;
            if (ai instanceof DyeableLeatherItem dl) {
                int c = dl.getColor(chest);
                r = ((c >> 16) & 255) / 255F; g = ((c >> 8) & 255) / 255F; b = (c & 255) / 255F;
            }
            armorRoot.getChild("body").copyFrom(body);
            armor.render(pose, buf.getBuffer(RenderType.armorCutoutNoCull(armorTex)), light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            if (chest.hasFoil()) armor.render(pose, buf.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY);
        }
    }
}
