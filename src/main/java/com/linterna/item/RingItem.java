package com.linterna.item;

import com.linterna.data.Holding;
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

/** El Anillo Verde. En la mano: clic derecho levanta lo que miras (otro clic lo lanza). */
public class RingItem extends Item {
    public RingItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) Holding.use(sp);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§aEn la mano: clic derecho levanta lo que miras, otro clic lo lanza"));
        tip.add(Component.literal("§7L volar · Y armadura · Alt+P pico · C intangible"));
        tip.add(Component.literal("§7V armas · Alt+M martillo · Alt+T traje · M catálogo"));
        tip.add(Component.literal("§2Recárgalo con el Candelabro de Poder (clic izquierdo)"));
    }
}
