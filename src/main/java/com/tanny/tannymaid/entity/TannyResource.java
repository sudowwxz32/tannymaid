package com.tanny.tannymaid.entity;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.Locale;

/** Recursos que Tanny puede recolectar con /tanny recolectar. */
public enum TannyResource {
    MADERA("madera", "madera", 2, false),
    PIEDRA("piedra", "piedra", 0, true),
    CARBON("carbon", "carbón", 0, true),
    HIERRO("hierro", "hierro", 0, true),
    TIERRA("tierra", "tierra", 1, false);

    public static final String[] KEYS = {"madera", "piedra", "carbon", "hierro", "tierra"};

    public final String key;
    public final String display;
    /** Ranura de herramienta: 0 pico, 1 pala, 2 hacha. */
    public final int toolSlot;
    public final boolean needsTool;

    TannyResource(String key, String display, int toolSlot, boolean needsTool) {
        this.key = key;
        this.display = display;
        this.toolSlot = toolSlot;
        this.needsTool = needsTool;
    }

    public boolean matches(BlockState s) {
        return switch (this) {
            case MADERA -> s.is(BlockTags.LOGS);
            case PIEDRA -> s.is(Blocks.STONE) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.DEEPSLATE)
                    || s.is(Blocks.COBBLED_DEEPSLATE) || s.is(Blocks.ANDESITE) || s.is(Blocks.DIORITE)
                    || s.is(Blocks.GRANITE) || s.is(Blocks.TUFF);
            case CARBON -> s.is(BlockTags.COAL_ORES);
            case HIERRO -> s.is(BlockTags.IRON_ORES);
            case TIERRA -> s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.COARSE_DIRT)
                    || s.is(Blocks.ROOTED_DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.MYCELIUM);
        };
    }

    public String toolName() {
        return switch (toolSlot) {
            case 0 -> "un pico";
            case 1 -> "una pala";
            default -> "un hacha";
        };
    }

    @Nullable
    public static TannyResource byName(String name) {
        String n = Normalizer.normalize(name.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        for (TannyResource r : values()) {
            if (n.startsWith(r.key)) return r;
        }
        if (n.startsWith("tronco") || n.startsWith("arbol") || n.startsWith("wood") || n.startsWith("log")) return MADERA;
        if (n.startsWith("roca") || n.startsWith("stone") || n.startsWith("cobble")) return PIEDRA;
        if (n.startsWith("coal")) return CARBON;
        if (n.startsWith("iron") || n.startsWith("fierro")) return HIERRO;
        if (n.startsWith("dirt") || n.startsWith("pasto") || n.startsWith("cesped")) return TIERRA;
        return null;
    }
}
