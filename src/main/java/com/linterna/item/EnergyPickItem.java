package com.linterna.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Pico holograma verde del anillo: pica muy rápido y no se gasta. */
public class EnergyPickItem extends PickaxeItem {
    public static final Tier ENERGY = new Tier() {
        @Override public int getUses() { return 0; }
        @Override public float getSpeed() { return 40.0F; }
        @Override public float getAttackDamageBonus() { return 3.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 0; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public EnergyPickItem(Properties props) {
        super(ENERGY, 1, -2.4F, props.durability(0));
    }

    @Override
    public boolean isDamageable(net.minecraft.world.item.ItemStack stack) {
        return false;
    }

    @Override
    public boolean isCorrectToolForDrops(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public float getDestroySpeed(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.block.state.BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL) ? 40.0F : 2.0F;
    }
}
