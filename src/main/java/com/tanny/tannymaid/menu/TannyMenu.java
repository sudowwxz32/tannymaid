package com.tanny.tannymaid.menu;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;

/**
 * Inventario de Tanny: barra de herramientas (pico, pala, hacha, espada, escudo),
 * 27 espacios de mochila y el inventario del jugador.
 */
public class TannyMenu extends AbstractContainerMenu {
    public static final int TOOL_COUNT = 5;
    public static final int TOOL_X = 62;
    public static final int TOOL_Y = 54;
    public static final int BAG_Y = 84;
    public static final int PLAYER_Y = 152;
    public static final int HOTBAR_Y = 210;

    private static final int BAG_START = TOOL_COUNT;
    private static final int PLAYER_START = BAG_START + 27;
    private static final int END = PLAYER_START + 36;

    public static final ToolAction[] TOOL_ACTIONS = {
            ToolActions.PICKAXE_DIG, ToolActions.SHOVEL_DIG, ToolActions.AXE_DIG,
            ToolActions.SWORD_DIG, ToolActions.SHIELD_BLOCK
    };

    @Nullable private final TannyEntity tanny;

    /** Cliente */
    public TannyMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, new SimpleContainer(27), new SimpleContainer(TOOL_COUNT), findTanny(playerInv, buf.readVarInt()));
    }

    /** Servidor */
    public TannyMenu(int id, Inventory playerInv, Container bag, Container tools, @Nullable TannyEntity tanny) {
        super(ModMenus.TANNY.get(), id);
        this.tanny = tanny;
        checkContainerSize(bag, 27);
        checkContainerSize(tools, TOOL_COUNT);
        bag.startOpen(playerInv.player);

        for (int i = 0; i < TOOL_COUNT; i++) {
            addSlot(new ToolSlot(tools, i, TOOL_X + i * 18 + 1, TOOL_Y + 1));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(bag, col + row * 9, 8 + col * 18, BAG_Y + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    @Nullable
    private static TannyEntity findTanny(Inventory inv, int entityId) {
        Entity e = inv.player.level().getEntity(entityId);
        return e instanceof TannyEntity t ? t : null;
    }

    @Nullable
    public TannyEntity getTanny() {
        return tanny;
    }

    public static boolean fitsToolSlot(int index, ItemStack stack) {
        return !stack.isEmpty() && stack.canPerformAction(TOOL_ACTIONS[index]);
    }

    @Override
    public boolean stillValid(Player player) {
        return tanny == null || (tanny.isAlive() && !tanny.isRemoved());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, END, true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = 0; i < TOOL_COUNT && !moved; i++) {
                if (fitsToolSlot(i, stack) && !this.slots.get(i).hasItem()) {
                    moved = moveItemStackTo(stack, i, i + 1, false);
                }
            }
            if (!moved && !moveItemStackTo(stack, BAG_START, PLAYER_START, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return result;
    }

    /** Ranura que solo acepta su tipo de herramienta. */
    public static class ToolSlot extends Slot {
        private final int type;

        public ToolSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
            this.type = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return fitsToolSlot(type, stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
