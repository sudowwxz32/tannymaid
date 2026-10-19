package com.laury.laurymod.menu;

import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;

/** Inventario de Laury: armadura (4), herramientas (7), mochila (27) y el inventario del jugador. */
public class LauryMenu extends AbstractContainerMenu {
    public static final int TOOL_COUNT = 7;
    public static final int ARMOR_X = 8, ARMOR_Y = 18;
    public static final int TOOL_X = 82, TOOL_Y = 52;
    public static final int BAG_Y = 104, PLAYER_Y = 172, HOTBAR_Y = 230;

    private static final int ARMOR_START = 0, TOOL_START = 4, BAG_START = TOOL_START + TOOL_COUNT;
    private static final int PLAYER_START = BAG_START + 27, END = PLAYER_START + 36;
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @Nullable private final LauryEntity laury;

    public LauryMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, new SimpleContainer(27), new SimpleContainer(TOOL_COUNT), new SimpleContainer(4), find(inv, buf.readVarInt()));
    }

    public LauryMenu(int id, Inventory inv, Container bag, Container tools, Container armor, @Nullable LauryEntity laury) {
        super(ModMenus.LAURY.get(), id);
        this.laury = laury;
        for (int i = 0; i < 4; i++) addSlot(new ArmorSlot(armor, i, ARMOR_X, ARMOR_Y + i * 18, ARMOR[i]));
        for (int i = 0; i < TOOL_COUNT; i++) {
            int col = i % 4, row = i / 4;
            addSlot(new ToolSlot(tools, i, TOOL_X + col * 18 + 1, TOOL_Y + row * 18 + 1));
        }
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(bag, c + r * 9, 8 + c * 18, BAG_Y + r * 18));
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, PLAYER_Y + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, HOTBAR_Y));
    }

    @Nullable
    private static LauryEntity find(Inventory inv, int id) {
        Entity e = inv.player.level().getEntity(id);
        return e instanceof LauryEntity l ? l : null;
    }

    @Nullable public LauryEntity getLaury() { return laury; }

    public static boolean fitsTool(int i, ItemStack s) {
        if (s.isEmpty()) return false;
        return switch (i) {
            case LauryEntity.SLOT_SWORD -> s.getItem() instanceof SwordItem || s.canPerformAction(ToolActions.SWORD_DIG);
            case LauryEntity.SLOT_AXE -> s.canPerformAction(ToolActions.AXE_DIG);
            case LauryEntity.SLOT_PICK -> s.canPerformAction(ToolActions.PICKAXE_DIG);
            case LauryEntity.SLOT_SHOVEL -> s.canPerformAction(ToolActions.SHOVEL_DIG);
            case LauryEntity.SLOT_HOE -> s.canPerformAction(ToolActions.HOE_DIG);
            case LauryEntity.SLOT_BOW -> s.getItem() instanceof BowItem;
            case LauryEntity.SLOT_SHIELD -> s.canPerformAction(ToolActions.SHIELD_BLOCK);
            default -> false;
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return laury == null || (laury.isAlive() && !laury.isRemoved());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, END, true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = ARMOR_START; i < BAG_START && !moved; i++) {
                Slot s = slots.get(i);
                if (!s.hasItem() && s.mayPlace(stack)) moved = moveItemStackTo(stack, i, i + 1, false);
            }
            if (!moved && !moveItemStackTo(stack, BAG_START, PLAYER_START, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    public static class ToolSlot extends Slot {
        private final int type;
        public ToolSlot(Container c, int i, int x, int y) { super(c, i, x, y); type = i; }
        @Override public boolean mayPlace(ItemStack s) { return fitsTool(type, s); }
        @Override public int getMaxStackSize() { return 1; }
        public int getType() { return type; }
    }

    public static class ArmorSlot extends Slot {
        private final EquipmentSlot equip;
        public ArmorSlot(Container c, int i, int x, int y, EquipmentSlot equip) { super(c, i, x, y); this.equip = equip; }
        @Override public boolean mayPlace(ItemStack s) { return !s.isEmpty() && Mob.getEquipmentSlotForItem(s) == equip; }
        @Override public int getMaxStackSize() { return 1; }
    }
}
