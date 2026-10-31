package com.tanny.tannymaid.menu;

import com.tanny.tannymaid.cobblemon.CobblemonBridge;
import com.tanny.tannymaid.cobblemon.GardevoirCompanion;
import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.registry.ModMenus;
import com.tanny.tannymaid.util.TannySpeech;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;

/**
 * Menú de Tanny con pestañas:
 * <ul>
 *   <li>Equipo: armadura (4), herramientas (pico, pala, hacha, espada, escudo) y mochila de 27.</li>
 *   <li>Poké Balls: bolsa de 27 que solo acepta Poké Balls.</li>
 * </ul>
 * Los botones de la pantalla (pestañas, PC y órdenes) llegan como clickMenuButton.
 */
public class TannyMenu extends AbstractContainerMenu {
    public static final int PAGE_GEAR = 0;
    public static final int PAGE_BALLS = 1;

    public static final int TOOL_COUNT = 5;
    public static final int ARMOR_X = 8;
    public static final int ARMOR_Y = 18;
    public static final int TOOL_X = 79;
    public static final int TOOL_Y = 64;
    public static final int BAG_Y = 104;
    public static final int PLAYER_Y = 172;
    public static final int HOTBAR_Y = 230;

    // Botones (ids de clickMenuButton)
    public static final int BTN_PAGE_GEAR = 0, BTN_PAGE_BALLS = 1, BTN_PC = 2;
    public static final int BTN_ORDER = 10;
    public static final String[] ORDERS = {
            "Seguirme", "Quedarse", "Defender", "Granja", "Ir a casa", "Venir aquí",
            "Ordenar cofres", "Recoger cosas", "Lucario", "Gardevoir", "Pan", "Bayas", "Lugar seguro"
    };

    public static final ToolAction[] TOOL_ACTIONS = {
            ToolActions.PICKAXE_DIG, ToolActions.SHOVEL_DIG, ToolActions.AXE_DIG,
            ToolActions.SWORD_DIG, ToolActions.SHIELD_BLOCK
    };

    @Nullable private final TannyEntity tanny;
    private final int page;
    private final int playerStart;
    private final int end;
    private final int toolStart, armorStart, bagStart;

    /** Cliente */
    public TannyMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, buf.readVarInt(), buf);
    }

    private TannyMenu(int id, Inventory playerInv, int entityId, FriendlyByteBuf buf) {
        this(id, playerInv, buf.readVarInt(), new SimpleContainer(27), new SimpleContainer(TOOL_COUNT),
                new SimpleContainer(4), new SimpleContainer(27), findTanny(playerInv, entityId));
    }

    /** Servidor */
    public TannyMenu(int id, Inventory playerInv, int page, Container bag, Container tools, Container armor,
                     Container balls, @Nullable TannyEntity tanny) {
        super(ModMenus.TANNY.get(), id);
        this.tanny = tanny;
        this.page = page;
        if (page == PAGE_BALLS) {
            toolStart = armorStart = -1;
            bagStart = 0;
            balls.startOpen(playerInv.player);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    addSlot(new BallSlot(balls, col + row * 9, 8 + col * 18, BAG_Y + row * 18));
                }
            }
        } else {
            bag.startOpen(playerInv.player);
            toolStart = 0;
            for (int i = 0; i < TOOL_COUNT; i++) {
                addSlot(new ToolSlot(tools, i, TOOL_X + i * 18 + 1, TOOL_Y + 1));
            }
            armorStart = slots.size();
            for (int i = 0; i < 4; i++) {
                addSlot(new ArmorSlot(armor, i, ARMOR_X, ARMOR_Y + i * 18));
            }
            bagStart = slots.size();
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    addSlot(new Slot(bag, col + row * 9, 8 + col * 18, BAG_Y + row * 18));
                }
            }
        }
        playerStart = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, HOTBAR_Y));
        }
        end = slots.size();
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

    public int getPage() {
        return page;
    }

    public int toolStart() { return toolStart; }
    public int armorStart() { return armorStart; }

    public static boolean fitsToolSlot(int index, ItemStack stack) {
        return !stack.isEmpty() && stack.canPerformAction(TOOL_ACTIONS[index]);
    }

    public static boolean fitsArmorSlot(int index, ItemStack stack) {
        return !stack.isEmpty() && Mob.getEquipmentSlotForItem(stack) == TannyEntity.ARMOR_SLOTS[index]
                && stack.getItem() != net.minecraft.world.item.Items.SHIELD;
    }

    @Override
    public boolean stillValid(Player player) {
        return tanny == null || (tanny.isAlive() && !tanny.isRemoved()
                && (tanny.isOwner(player) || tanny.distanceToSqr(player) < 8 * 8));
    }

    // ------------------------------------------------------------------ botones

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (tanny == null || !(player instanceof ServerPlayer sp) || !tanny.isOwner(player)) return false;
        switch (id) {
            case BTN_PAGE_GEAR, BTN_PAGE_BALLS -> {
                if (id != page) tanny.openInventory(sp, id);
                return true;
            }
            case BTN_PC -> {
                if (!CobblemonBridge.isLoaded()) {
                    TannySpeech.say(tanny, sp, "Amo, sin Cobblemon no tengo cajas de Pokémon que mostrarte.");
                    return true;
                }
                sp.closeContainer();
                if (!CobblemonBridge.openPC(sp)) TannySpeech.say(tanny, sp, "Perdón, Amo... no pude abrir tus cajas esta vez.");
                return true;
            }
            default -> { }
        }
        int order = id - BTN_ORDER;
        if (order < 0 || order >= ORDERS.length) return false;
        switch (order) {
            case 0 -> tanny.commandFollow(sp);
            case 1 -> tanny.commandStay(sp);
            case 2 -> tanny.commandDefend(sp);
            case 3 -> tanny.commandFarm(sp);
            case 4 -> tanny.commandHome(sp);
            case 5 -> tanny.commandTp(sp);
            case 6 -> tanny.commandOrganize(sp);
            case 7 -> tanny.commandCollect(sp);
            case 8 -> tanny.getLucario().toggleManual(sp);
            case 9 -> GardevoirCompanion.toggle(sp);
            case 10 -> tanny.commandBread(sp);
            case 11 -> tanny.commandBerries(sp);
            case 12 -> {
                sp.closeContainer();
                tanny.commandSafe(sp);
            }
            default -> { return false; }
        }
        return true;
    }

    // ------------------------------------------------------------------ shift-clic

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < playerStart) {
            if (!moveItemStackTo(stack, playerStart, end, true)) return ItemStack.EMPTY;
        } else if (page == PAGE_BALLS) {
            if (!CobblemonBridge.isPokeBall(stack) || !moveItemStackTo(stack, 0, playerStart, false)) return ItemStack.EMPTY;
        } else {
            boolean moved = false;
            for (int i = 0; i < 4 && !moved; i++) {
                if (fitsArmorSlot(i, stack) && !this.slots.get(armorStart + i).hasItem()) {
                    moved = moveItemStackTo(stack, armorStart + i, armorStart + i + 1, false);
                }
            }
            for (int i = 0; i < TOOL_COUNT && !moved; i++) {
                if (fitsToolSlot(i, stack) && !this.slots.get(toolStart + i).hasItem()) {
                    moved = moveItemStackTo(stack, toolStart + i, toolStart + i + 1, false);
                }
            }
            if (!moved && !moveItemStackTo(stack, bagStart, playerStart, false)) return ItemStack.EMPTY;
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

    /** Ranura de armadura (casco, pechera, pantalones o botas). */
    public static class ArmorSlot extends Slot {
        private final int type;

        public ArmorSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
            this.type = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return fitsArmorSlot(type, stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        public EquipmentSlot slotType() {
            return TannyEntity.ARMOR_SLOTS[type];
        }

        private static final net.minecraft.resources.ResourceLocation[] ICONS = {
                net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
                net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
                net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
                net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
        };

        @Override
        public com.mojang.datafixers.util.Pair<net.minecraft.resources.ResourceLocation, net.minecraft.resources.ResourceLocation> getNoItemIcon() {
            return com.mojang.datafixers.util.Pair.of(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS, ICONS[type]);
        }
    }

    /** Ranura de la bolsa de Poké Balls. */
    public static class BallSlot extends Slot {
        public BallSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return CobblemonBridge.isPokeBall(stack);
        }
    }
}
