package com.linterna.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

/** Candelabro de Poder: en la mano, clic izquierdo recarga el anillo. También se puede colocar. */
public class CandelabraItem extends BlockItem {
    public CandelabraItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§aClic izquierdo con él en la mano: recarga el Anillo Verde"));
        tip.add(Component.literal("§7Colocado: clic izquierdo sobre él también lo recarga"));
    }
}
