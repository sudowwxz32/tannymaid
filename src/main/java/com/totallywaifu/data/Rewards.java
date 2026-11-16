package com.totallywaifu.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** Recompensas por completar la colección y los extras (también las muestra la pantalla). */
public final class Rewards {
    private Rewards() {}

    public static final int COLLECTION_LEVELS = 30, EXTRAS_LEVELS = 50;
    public static final String PERK_COLLECTION = "Bendición ♥: todas tus waifus tienen +20 de vida y +4 de ataque para siempre";
    public static final String PERK_EXTRAS = "Bendición ✦: Suerte y Héroe de la aldea siempre, y te curas cuando tienes 3 o más waifus cerca";

    public static List<ItemStack> collection() {
        return List.of(new ItemStack(Items.DIAMOND, 16), new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2), new ItemStack(Items.TOTEM_OF_UNDYING),
                new ItemStack(Items.GOLDEN_CARROT, 32), new ItemStack(Items.EXPERIENCE_BOTTLE, 32), new ItemStack(Items.EMERALD_BLOCK, 4));
    }

    public static List<ItemStack> extras() {
        return List.of(new ItemStack(Items.NETHERITE_INGOT, 4), new ItemStack(Items.ELYTRA), new ItemStack(Items.NETHER_STAR),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 3), new ItemStack(Items.TOTEM_OF_UNDYING, 2), new ItemStack(Items.HEART_OF_THE_SEA),
                new ItemStack(Items.BEACON));
    }

    public static void give(ServerPlayer p, List<ItemStack> l, int levels) {
        for (ItemStack s : l) {
            ItemStack c = s.copy();
            if (!p.getInventory().add(c)) p.drop(c, false);
        }
        p.giveExperienceLevels(levels);
    }
}
