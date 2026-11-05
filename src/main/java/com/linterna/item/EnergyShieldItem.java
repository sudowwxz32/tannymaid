package com.linterna.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

/** Escudo holograma verde: bloquea como un escudo normal y nunca se rompe. */
public class EnergyShieldItem extends ShieldItem {
    public EnergyShieldItem(Properties props) {
        super(props.durability(0));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }
}
