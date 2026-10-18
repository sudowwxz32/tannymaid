package com.tanny.tannymaid.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ordena los cofres y barriles cercanos: junta todo, combina pilas y lo reparte
 * agrupado por tipo (bloques, comida, equipo, objetos de Cobblemon, varios).
 */
public final class ChestOrganizer {

    private ChestOrganizer() {}

    private record Slotted(BlockPos pos, Container container) {}

    public static int organize(ServerLevel level, BlockPos center, int radius) {
        List<Slotted> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, 3, radius))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) {
                found.add(new Slotted(pos.immutable(), (Container) be));
            }
        }
        if (found.isEmpty()) return 0;

        // Orden estable de los cofres: de arriba hacia abajo, luego X y Z.
        found.sort(Comparator.<Slotted>comparingInt(s -> -s.pos().getY())
                .thenComparingInt(s -> s.pos().getX())
                .thenComparingInt(s -> s.pos().getZ()));

        // 1) Sacar todo.
        List<ItemStack> all = new ArrayList<>();
        for (Slotted s : found) {
            Container c = s.container();
            for (int i = 0; i < c.getContainerSize(); i++) {
                ItemStack stack = c.removeItemNoUpdate(i);
                if (!stack.isEmpty()) all.add(stack);
            }
        }

        // 2) Ordenar por categoría y nombre.
        all.sort(Comparator.comparingInt(ChestOrganizer::category).thenComparing(ChestOrganizer::sortName));

        // 3) Combinar pilas iguales.
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : all) {
            for (ItemStack existing : merged) {
                if (stack.isEmpty()) break;
                if (ItemStack.isSameItemSameTags(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                    int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(move);
                    stack.shrink(move);
                }
            }
            if (!stack.isEmpty()) merged.add(stack);
        }

        // 4) Repartir en orden (siempre cabe: combinar solo reduce el número de pilas).
        int idx = 0;
        for (Slotted s : found) {
            Container c = s.container();
            for (int i = 0; i < c.getContainerSize(); i++) {
                c.setItem(i, idx < merged.size() ? merged.get(idx++) : ItemStack.EMPTY);
            }
            c.setChanged();
        }
        // Seguridad: si algo sobrara, se suelta en el centro.
        while (idx < merged.size()) {
            net.minecraft.world.Containers.dropItemStack(level, center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, merged.get(idx++));
        }
        return found.size();
    }

    private static int category(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id != null && "cobblemon".equals(id.getNamespace())) return 3;
        if (stack.isEdible()) return 1;
        if (stack.isDamageableItem()) return 2;
        if (item instanceof BlockItem) return 0;
        return 4;
    }

    private static String sortName(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }
}
