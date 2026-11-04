package com.linterna.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Espada de energía verde (creada en el catálogo del anillo; se desvanece a los 3 minutos). */
public class EnergySwordItem extends SwordItem {
    private static final Tier TIER = new Tier() {
        @Override public int getUses() { return 0; }
        @Override public float getSpeed() { return 8.0F; }
        @Override public float getAttackDamageBonus() { return 7.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 0; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public EnergySwordItem(Properties props) {
        super(TIER, 4, -2.2F, props.durability(0));
    }

    @Override
    public boolean isDamageable(net.minecraft.world.item.ItemStack stack) {
        return false;
    }
}
