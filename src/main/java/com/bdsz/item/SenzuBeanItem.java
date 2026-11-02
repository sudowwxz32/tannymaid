package com.bdsz.item;

import com.bdsz.data.PowerLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

/** Semilla del ermitaño: se come (lo recupera todo) o se planta en tierra de cultivo. */
public class SenzuBeanItem extends ItemNameBlockItem {

    public SenzuBeanItem(Block crop, Properties props) {
        super(crop, props);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        boolean gift = com.bdsz.companion.KaoriActions.isGift(stack);
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer p) {
            PowerLogic.senzu(p);
            if (gift) com.bdsz.companion.KaoriActions.onGiftEaten(p);
        }
        return result;
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext ctx) {
        // las semillas regaladas por Kaori se comen, no se plantan
        if (com.bdsz.companion.KaoriActions.isGift(ctx.getItemInHand())) return net.minecraft.world.InteractionResult.PASS;
        return super.useOn(ctx);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 8;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§aRecupera al instante vida, hambre y ki, y cura todo."));
        tip.add(Component.literal("§7Se puede plantar en tierra de cultivo."));
    }
}
