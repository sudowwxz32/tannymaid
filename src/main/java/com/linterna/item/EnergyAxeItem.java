package com.linterna.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Hacha de energía verde (holograma): corta madera al instante y pega muy fuerte. */
public class EnergyAxeItem extends AxeItem {
    public EnergyAxeItem(Properties props) {
        super(EnergyPickItem.ENERGY, 8.0F, -2.9F, props.durability(0));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_AXE) ? 40.0F : 2.0F;
    }
}
