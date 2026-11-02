package com.bdsz.companion;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/** Mochila de Kaori: armadura (4), arma y mano secundaria, y 27 ranuras. */
public class KaoriMenu extends AbstractContainerMenu {
    public static final int EQUIP_X = 8, EQUIP_Y = 20, BAG_X = 8, BAG_Y = 102, PLAYER_Y = 170, HOTBAR_Y = 228;
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @Nullable public final KaoriEntity kaori;
    private final Container bag, equip;

    /** Cliente */
    public KaoriMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, new SimpleContainer(27), new SimpleContainer(6), find(inv.player, buf.readVarInt()));
    }

    @Nullable
    private static KaoriEntity find(Player p, int id) {
        Entity e = p.level().getEntity(id);
        return e instanceof KaoriEntity k ? k : null;
    }

    /** Servidor */
    public KaoriMenu(int id, Inventory inv, Container bag, Container equip, @Nullable KaoriEntity kaori) {
        super(com.bdsz.registry.ModRegistry.KAORI_MENU.get(), id);
        this.kaori = kaori;
        this.bag = bag;
        this.equip = equip;
        for (int i = 0; i < 4; i++) {
            EquipmentSlot es = ARMOR[i];
            addSlot(new Slot(equip, i, EQUIP_X, EQUIP_Y + i * 18) {
                @Override public boolean mayPlace(ItemStack s) {
                    return s.getItem() instanceof net.minecraft.world.item.Equipable eq && eq.getEquipmentSlot() == es;
                }
                @Override public int getMaxStackSize() { return 1; }
            });
        }
        addSlot(new Slot(equip, 4, EQUIP_X + 22, EQUIP_Y + 27));
        addSlot(new Slot(equip, 5, EQUIP_X + 22, EQUIP_Y + 45));
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(bag, c + r * 9, BAG_X + c * 18, BAG_Y + r * 18));
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, BAG_X + c * 18, PLAYER_Y + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, BAG_X + c * 18, HOTBAR_Y));
    }

    @Override
    public boolean stillValid(Player p) {
        return kaori == null || (kaori.isAlive() && kaori.distanceToSqr(p) < 10 * 10);
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack st = slot.getItem(), copy = st.copy();
        int kEnd = 6 + 27;
        if (index < kEnd) {
            if (!moveItemStackTo(st, kEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = 0; i < 4 && !moved; i++) {
                if (slots.get(i).mayPlace(st) && !slots.get(i).hasItem()) moved = moveItemStackTo(st, i, i + 1, false);
            }
            if (!moved && !moveItemStackTo(st, 6, kEnd, false)) return ItemStack.EMPTY;
        }
        if (st.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
