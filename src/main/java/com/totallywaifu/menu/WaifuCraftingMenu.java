package com.totallywaifu.menu;

import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

/** La mesa de crafteo de Kozue: funciona mientras estés cerca de ella. */
public class WaifuCraftingMenu extends CraftingMenu {
    private final WaifuEntity w;

    public WaifuCraftingMenu(int id, Inventory inv, ContainerLevelAccess access, WaifuEntity w) {
        super(id, inv, access);
        this.w = w;
    }

    @Override
    public boolean stillValid(Player p) {
        return w.isAlive() && w.distanceToSqr(p) < 8 * 8;
    }
}
