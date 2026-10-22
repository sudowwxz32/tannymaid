package com.bdsz.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Último Entrenamiento: ingrediente para craftear Dominio Total. Solo se craftea con todo desbloqueado hasta Ultra Ego. */
public class UltimateTrainingItem extends Item {
    public UltimateTrainingItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§7Ponlo en el centro de la mesa,"));
        tip.add(Component.literal("§7rodeado de 7 Minerales de Ki y 1 bloque de oro."));
        tip.add(Component.literal("§d→ Dominio Total"));
        tip.add(Component.literal("§8Requiere: todo desbloqueado hasta Ultra Ego"));
    }
}
