package com.linterna.data;

import com.linterna.registry.ModRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Menú de la ranura del anillo: una ranura solo para el Anillo Verde y tu inventario. */
public class RingMenu extends AbstractContainerMenu {
    public static final int SLOT_X = 116, SLOT_Y = 34;

    public RingMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(1));
    }

    public RingMenu(int id, Inventory inv, Container ring) {
        super(ModRegistry.RING_MENU.get(), id);
        addSlot(new Slot(ring, 0, SLOT_X, SLOT_Y) {
            @Override public boolean mayPlace(ItemStack s) { return s.is(ModRegistry.RING.get()); }
            @Override public int getMaxStackSize() { return 1; }
        });
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, 142));
    }

    @Override
    public boolean stillValid(Player p) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack st = slot.getItem(), copy = st.copy();
        if (index == 0) {
            if (!moveItemStackTo(st, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (st.is(ModRegistry.RING.get()) && !slots.get(0).hasItem()) {
            if (!moveItemStackTo(st, 0, 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (st.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
