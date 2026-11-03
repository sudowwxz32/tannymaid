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
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Mochila de Kaori: armadura (4), arma y escudo, cinturón de herramientas (pico, hacha, pala, azada, arco y otra),
 * antorcha del cinturón y 27 ranuras de mochila.
 */
public class KaoriMenu extends AbstractContainerMenu {
    public static final int ARMOR_X = 8, ARMOR_Y = 20, HAND_X = 98, TOOL_X = 124, TOOL_Y = 20, BAG_X = 8, BAG_Y = 104, PLAYER_Y = 172, HOTBAR_Y = 230;
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final int EQUIP_SLOTS = KaoriEntity.EQUIP_SIZE;

    @Nullable public final KaoriEntity kaori;

    /** Cliente */
    public KaoriMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, new SimpleContainer(27), new SimpleContainer(KaoriEntity.EQUIP_SIZE), find(inv.player, buf.readVarInt()));
    }

    @Nullable
    private static KaoriEntity find(Player p, int id) {
        Entity e = p.level().getEntity(id);
        return e instanceof KaoriEntity k ? k : null;
    }

    private static Slot only(Container c, int i, int x, int y, Predicate<ItemStack> ok, int max) {
        return new Slot(c, i, x, y) {
            @Override public boolean mayPlace(ItemStack s) { return ok.test(s); }
            @Override public int getMaxStackSize() { return max; }
        };
    }

    /** Servidor */
    public KaoriMenu(int id, Inventory inv, Container bag, Container equip, @Nullable KaoriEntity kaori) {
        super(com.bdsz.registry.ModRegistry.KAORI_MENU.get(), id);
        this.kaori = kaori;
        for (int i = 0; i < 4; i++) {
            EquipmentSlot es = ARMOR[i];
            addSlot(only(equip, i, ARMOR_X, ARMOR_Y + i * 18, s -> s.getItem() instanceof Equipable eq && eq.getEquipmentSlot() == es, 1));
        }
        addSlot(only(equip, 4, HAND_X, TOOL_Y, s -> true, 64));                                                     // arma
        addSlot(only(equip, 5, HAND_X, TOOL_Y + 18, s -> true, 64));                                                // escudo / mano izquierda
        // cinturón de herramientas
        addSlot(only(equip, KaoriEntity.PICK, TOOL_X, TOOL_Y, s -> s.getItem() instanceof PickaxeItem, 1));
        addSlot(only(equip, KaoriEntity.AXE, TOOL_X + 18, TOOL_Y, s -> s.getItem() instanceof AxeItem, 1));
        addSlot(only(equip, KaoriEntity.SHOVEL, TOOL_X + 36, TOOL_Y, s -> s.getItem() instanceof ShovelItem, 1));
        addSlot(only(equip, KaoriEntity.HOE, TOOL_X, TOOL_Y + 18, s -> s.getItem() instanceof HoeItem, 1));
        addSlot(only(equip, KaoriEntity.BOW, TOOL_X + 18, TOOL_Y + 18, s -> s.getItem() instanceof ProjectileWeaponItem, 1));
        addSlot(only(equip, KaoriEntity.EXTRA, TOOL_X + 36, TOOL_Y + 18, s -> s.isDamageableItem() || s.getItem() instanceof TieredItem
                || s.canPerformAction(ToolActions.SHEARS_DIG), 1));
        addSlot(only(equip, KaoriEntity.TORCH, HAND_X, TOOL_Y + 46, s -> s.is(Items.TORCH) || s.is(Items.SOUL_TORCH), 64));   // antorcha del cinturón
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
        int kEnd = EQUIP_SLOTS + 27;
        if (index < kEnd) {
            if (!moveItemStackTo(st, kEnd, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            // primero a su ranura especial (armadura, herramienta, antorcha), si no a la mochila
            for (int i = 0; i < EQUIP_SLOTS && !moved; i++) {
                if (i == 4 || i == 5) continue;
                if (slots.get(i).mayPlace(st) && !slots.get(i).hasItem()) moved = moveItemStackTo(st, i, i + 1, false);
                else if (i == EQUIP_SLOTS - 1 && slots.get(i).mayPlace(st)) moved = moveItemStackTo(st, i, i + 1, false);
            }
            if (!moved && !moveItemStackTo(st, EQUIP_SLOTS, kEnd, false)) return ItemStack.EMPTY;
        }
        if (st.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
