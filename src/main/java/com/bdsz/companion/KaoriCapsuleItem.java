package com.bdsz.companion;

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

/** Cápsula de Kaori: la invoca (o la trae a tu lado si ya la tienes). No se gasta. */
public class KaoriCapsuleItem extends Item {
    public KaoriCapsuleItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            KaoriActions.summon(sp);
            player.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7Clic derecho: invoca a Kaori o la trae a tu lado"));
        tip.add(Component.literal("§7Clic derecho sobre ella: su menú · Shift: su mochila"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
