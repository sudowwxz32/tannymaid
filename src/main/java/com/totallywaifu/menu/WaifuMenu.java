package com.totallywaifu.menu;

import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.entity.WaifuWork;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/** Panel de la waifu (Shift + clic derecho): su mochila, los modos y sus botones especiales. */
public class WaifuMenu extends AbstractContainerMenu {
    public static final int BAG_Y = 92, INV_Y = 160, HOTBAR_Y = 218, ORDER_X = 154, ORDER_Y = 70;
    public static final int B_FOLLOW = 0, B_STAY = 1, B_WORK = 2, B_SORT = 3, B_XP = 4, B_HEART = 5;

    @Nullable public final WaifuEntity waifu;
    public final WaifuKind kind;
    private final DataSlot mode = DataSlot.standalone(), xp = DataSlot.standalone();

    public static WaifuMenu fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        Entity e = inv.player.level().getEntity(buf.readVarInt());
        WaifuKind k = WaifuKind.byId(buf.readVarInt());
        return new WaifuMenu(id, inv, e instanceof WaifuEntity w ? w : null, k, new SimpleContainer(27), new SimpleContainer(1));
    }

    public WaifuMenu(int id, Inventory inv, WaifuEntity w) {
        this(id, inv, w, w.kind, w.bag, w.order);
    }

    private WaifuMenu(int id, Inventory inv, @Nullable WaifuEntity w, WaifuKind kind, Container bag, Container order) {
        super(ModRegistry.PANEL.get(), id);
        this.waifu = w;
        this.kind = kind;
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(bag, c + r * 9, 8 + c * 18, BAG_Y + r * 18));
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, INV_Y + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, HOTBAR_Y));
        if (kind == WaifuKind.KOZUE || kind.cooker()) addSlot(new Slot(order, 0, ORDER_X, ORDER_Y) {
            @Override public int getMaxStackSize() { return 1; }
        });
        addDataSlot(mode);
        addDataSlot(xp);
        if (w != null) {
            mode.set(w.getMode());
            xp.set(w.storedXp);
        }
    }

    public int mode() { return mode.get(); }
    public int xp() { return xp.get(); }

    @Override
    public void broadcastChanges() {
        if (waifu != null) {
            mode.set(waifu.getMode());
            xp.set(waifu.storedXp);
        }
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player p, int id) {
        if (waifu == null || !waifu.isOwner(p)) return false;
        switch (id) {
            case B_FOLLOW -> { waifu.setMode(WaifuEntity.FOLLOW); waifu.say(p, "¡Voy contigo!"); }
            case B_STAY -> { waifu.setMode(WaifuEntity.STAY); waifu.say(p, "Me quedo aquí esperándote."); }
            case B_WORK -> { waifu.setMode(WaifuEntity.WORK); waifu.say(p, "¡A trabajar! Me quedo por esta zona."); }
            case B_SORT -> { if (kind == WaifuKind.KURA) { WaifuWork.sortChest(waifu); waifu.say(p, "¡Cofre ordenado!"); } }
            case B_XP -> {
                if (kind == WaifuKind.SHIORI && waifu.storedXp > 0) {
                    p.giveExperiencePoints(waifu.storedXp);
                    waifu.say(p, "Toma, " + waifu.storedXp + " puntos de experiencia ✦");
                    waifu.storedXp = 0;
                }
            }
            case B_HEART -> {
                p.closeContainer();
                if (p instanceof net.minecraft.server.level.ServerPlayer sp) com.totallywaifu.data.Collection.storeEntity(sp, waifu);
                return true;
            }
            default -> { return false; }
        }
        waifu.cheer(40);
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player p) {
        return waifu == null || (waifu.isAlive() && waifu.distanceToSqr(p) < 8 * 8);
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack st = slot.getItem(), copy = st.copy();
        if (index < 27) {
            if (!moveItemStackTo(st, 27, 63, true)) return ItemStack.EMPTY;
        } else if (index < 63) {
            if (!moveItemStackTo(st, 0, 27, false)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(st, 27, 63, true)) return ItemStack.EMPTY;
        }
        if (st.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
}
