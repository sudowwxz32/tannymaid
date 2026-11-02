package com.bdsz.companion;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Lo que Kaori puede farmear (recolectar) por ti. */
public enum KaoriResource {
    MADERA("madera", false, "un hacha"),
    PIEDRA("piedra", true, "un pico"),
    CARBON("carbón", true, "un pico"),
    HIERRO("hierro", true, "un pico de piedra o mejor"),
    TIERRA("tierra", false, "una pala"),
    ARENA("arena", false, "una pala");

    public final String display;
    public final boolean needsTool;
    private final String tool;

    KaoriResource(String display, boolean needsTool, String tool) {
        this.display = display;
        this.needsTool = needsTool;
        this.tool = tool;
    }

    public String toolName() {
        return tool;
    }

    public boolean matches(BlockState s) {
        return switch (this) {
            case MADERA -> s.is(BlockTags.LOGS);
            case PIEDRA -> s.is(Blocks.STONE) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.DEEPSLATE) || s.is(Blocks.ANDESITE)
                    || s.is(Blocks.DIORITE) || s.is(Blocks.GRANITE) || s.is(Blocks.TUFF) || s.is(Blocks.COBBLED_DEEPSLATE);
            case CARBON -> s.is(BlockTags.COAL_ORES);
            case HIERRO -> s.is(BlockTags.IRON_ORES);
            case TIERRA -> s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL);
            case ARENA -> s.is(BlockTags.SAND);
        };
    }

    public static KaoriResource byId(int id) {
        KaoriResource[] v = values();
        return id >= 0 && id < v.length ? v[id] : MADERA;
    }
}
