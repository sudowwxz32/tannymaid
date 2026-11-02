package com.bdsz.item;

import com.bdsz.data.FusionLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Arete de Fusión dorado: póntelo y lánzale el otro a un mob para fusionarte con él. */
public class FusionEarringItem extends Item {
    public FusionEarringItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) FusionLogic.useEarring(sp, stack);
        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§61) Clic derecho: ponte un arete"));
        tip.add(Component.literal("§62) Clic derecho otra vez: lanza el otro arete a un mob"));
        tip.add(Component.literal("§7Zombi, esqueleto, enderman, creeper, gólem de hierro o warden"));
        tip.add(Component.literal("§7Shift + clic derecho: quitarte el arete · Tecla §eX§7: habilidad de la fusión"));
    }
}
