package com.totallywaifu.menu;

import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

/**
 * La mesa de encantamientos de Shiori: siempre tiene el poder máximo (como 15 estanterías) y te presta su lapislázuli.
 * Calcula los mismos encantamientos que la mesa normal (misma semilla), así lo que ves es lo que obtienes.
 */
public class WaifuEnchantMenu extends EnchantmentMenu {
    private static final int POWER = 15;
    private final WaifuEntity w;
    private final Player player;

    public WaifuEnchantMenu(int id, Inventory inv, ContainerLevelAccess access, WaifuEntity w) {
        super(id, inv, access);
        this.w = w;
        this.player = inv.player;
        // pone su lapislázuli en la mesa (lo que sobre vuelve a tu inventario al cerrar)
        if (!player.level().isClientSide) {
            for (int i = 0; i < w.bag.getContainerSize() && getSlot(1).getItem().getCount() < 64; i++) {
                ItemStack s = w.bag.getItem(i);
                if (!s.is(Items.LAPIS_LAZULI)) continue;
                ItemStack cur = getSlot(1).getItem();
                int take = Math.min(s.getCount(), 64 - cur.getCount());
                if (cur.isEmpty()) getSlot(1).set(s.split(take));
                else { cur.grow(take); s.shrink(take); }
            }
            w.bag.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player p) {
        return w.isAlive() && w.distanceToSqr(p) < 8 * 8;
    }

    @Override
    public void slotsChanged(Container c) {
        super.slotsChanged(c);
        if (player.level().isClientSide) return;
        ItemStack st = getSlot(0).getItem();
        if (st.isEmpty() || !st.isEnchantable()) return;
        RandomSource r = RandomSource.create();
        r.setSeed(getEnchantmentSeed());
        for (int k = 0; k < 3; k++) {
            costs[k] = EnchantmentHelper.getEnchantmentCost(r, k, POWER, st);
            enchantClue[k] = -1;
            levelClue[k] = -1;
            if (costs[k] < k + 1) costs[k] = 0;
            costs[k] = ForgeEventFactory.onEnchantmentLevelSet(player.level(), w.blockPosition(), k, POWER, st, costs[k]);
        }
        for (int k = 0; k < 3; k++) {
            if (costs[k] <= 0) continue;
            r.setSeed(getEnchantmentSeed() + k);
            List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(r, st, costs[k], false);
            if (st.is(Items.BOOK) && list.size() > 1) list.remove(r.nextInt(list.size()));
            if (!list.isEmpty()) {
                EnchantmentInstance e = list.get(r.nextInt(list.size()));
                enchantClue[k] = BuiltInRegistries.ENCHANTMENT.getId(e.enchantment);
                levelClue[k] = e.level;
            }
        }
        broadcastChanges();
    }
}
