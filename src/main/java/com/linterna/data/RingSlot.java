package com.linterna.data;

import com.linterna.registry.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

/** La ranura del anillo: se guarda en los datos persistentes del jugador, así que no se pierde al morir. */
public final class RingSlot {
    private RingSlot() {}

    private static final String KEY = "LinternaAnillo";

    public static ItemStack get(Player p) {
        return ItemStack.of(LanternData.root(p).getCompound(KEY));
    }

    public static void set(Player p, ItemStack st) {
        LanternData.root(p).put(KEY, st.save(new net.minecraft.nbt.CompoundTag()));
    }

    public static boolean worn(Player p) {
        return get(p).is(ModRegistry.RING.get());
    }

    public static void open(ServerPlayer p) {
        if (!LanternData.get(p).bearer()) return;
        NetworkHooks.openScreen(p, new SimpleMenuProvider((id, inv, pl) -> new RingMenu(id, inv, new Store(pl)),
                Component.literal("Ranura del anillo")));
    }

    /** Contenedor de una ranura que guarda en el jugador cada vez que cambia. */
    public static class Store extends SimpleContainer {
        private final Player p;

        public Store(Player p) {
            super(1);
            this.p = p;
            super.setItem(0, get(p));
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (p != null) set(p, getItem(0));
        }
    }
}
