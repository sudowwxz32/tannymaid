package com.totallywaifu.menu;

import com.totallywaifu.entity.WaifuEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.state.BlockState;

/** Los menús vanilla de las mesas nuevas: funcionan mientras estés cerca de la chica (no hace falta el bloque). */
public final class StationMenus {
    private StationMenus() {}

    static boolean near(WaifuEntity w, Player p) {
        return w.isAlive() && w.distanceToSqr(p) < 8 * 8;
    }

    public static class Anvil extends AnvilMenu {
        private final WaifuEntity w;
        public Anvil(int id, Inventory inv, ContainerLevelAccess a, WaifuEntity w) { super(id, inv, a); this.w = w; }
        @Override protected boolean isValidBlock(BlockState s) { return true; }
        @Override public boolean stillValid(Player p) { return near(w, p); }
    }

    public static class Grind extends GrindstoneMenu {
        private final WaifuEntity w;
        public Grind(int id, Inventory inv, ContainerLevelAccess a, WaifuEntity w) { super(id, inv, a); this.w = w; }
        @Override public boolean stillValid(Player p) { return near(w, p); }
    }

    public static class Loom extends LoomMenu {
        private final WaifuEntity w;
        public Loom(int id, Inventory inv, ContainerLevelAccess a, WaifuEntity w) { super(id, inv, a); this.w = w; }
        @Override public boolean stillValid(Player p) { return near(w, p); }
    }

    public static class Cutter extends StonecutterMenu {
        private final WaifuEntity w;
        public Cutter(int id, Inventory inv, ContainerLevelAccess a, WaifuEntity w) { super(id, inv, a); this.w = w; }
        @Override public boolean stillValid(Player p) { return near(w, p); }
    }

    public static class Smith extends SmithingMenu {
        private final WaifuEntity w;
        public Smith(int id, Inventory inv, ContainerLevelAccess a, WaifuEntity w) { super(id, inv, a); this.w = w; }
        @Override protected boolean isValidBlock(BlockState s) { return true; }
        @Override public boolean stillValid(Player p) { return near(w, p); }
    }

    public static class Carto extends CartographyTableMenu {
        private final WaifuEntity w;
        public Carto(int id, Inventory inv, ContainerLevelAccess a, WaifuEntity w) { super(id, inv, a); this.w = w; }
        @Override public boolean stillValid(Player p) { return near(w, p); }
    }
}
