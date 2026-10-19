package com.laury.laurymod.client;

import com.laury.laurymod.entity.LauryEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Elige el renderizador según la forma: humana o dragón. */
public class LauryRenderer extends EntityRenderer<LauryEntity> {
    private final LauryHumanRenderer human;
    private final LauryDragonRenderer dragon;

    public LauryRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.human = new LauryHumanRenderer(ctx);
        this.dragon = new LauryDragonRenderer(ctx);
    }

    @Override
    public void render(LauryEntity e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        this.shadowRadius = e.isDragon() ? 1.8F : 0.45F;
        if (e.isDragon()) dragon.render(e, yaw, partial, pose, buf, light);
        else human.render(e, yaw, partial, pose, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(LauryEntity e) {
        return e.isDragon() ? dragon.getTextureLocation(e) : human.getTextureLocation(e);
    }

    /** "Laury ❤ 432/500" sobre su cabeza. */
    public static Component healthTag(LauryEntity e) {
        String state = e.isWeakened() ? " §7(debilitada)" : "";
        int hp = (int) Math.ceil(e.getHealth());
        int max = (int) e.getMaxHealth();
        String color = e.isWeakened() ? "§7" : hp > max / 2 ? "§a" : hp > max / 4 ? "§e" : "§c";
        return Component.literal("§9Laury" + state + " " + color + "❤ " + hp + "/" + max);
    }
}
