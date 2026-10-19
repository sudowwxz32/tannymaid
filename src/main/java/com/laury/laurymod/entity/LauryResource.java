package com.laury.laurymod.entity;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.Locale;

/** Lo que Laury puede recolectar (máx. 64 por pedido). */
public enum LauryResource {
    MADERA("madera", "madera", LauryEntity.SLOT_AXE, false),
    PIEDRA("piedra", "piedra", LauryEntity.SLOT_PICK, true),
    CARBON("carbon", "carbón", LauryEntity.SLOT_PICK, true),
    HIERRO("hierro", "hierro", LauryEntity.SLOT_PICK, true),
    TIERRA("tierra", "tierra", LauryEntity.SLOT_SHOVEL, false),
    ARENA("arena", "arena", LauryEntity.SLOT_SHOVEL, false),
    CARNE("carne", "carne", LauryEntity.SLOT_SWORD, false);

    public static final String[] KEYS = {"madera", "piedra", "carbon", "hierro", "tierra", "arena", "carne"};

    public final String key, display;
    public final int toolSlot;
    public final boolean needsTool;

    LauryResource(String key, String display, int toolSlot, boolean needsTool) {
        this.key = key;
        this.display = display;
        this.toolSlot = toolSlot;
        this.needsTool = needsTool;
    }

    public boolean isBlock() {
        return this != CARNE;
    }

    public boolean matches(BlockState s) {
        return switch (this) {
            case MADERA -> s.is(BlockTags.LOGS);
            case PIEDRA -> s.is(Blocks.STONE) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.DEEPSLATE) || s.is(Blocks.COBBLED_DEEPSLATE)
                    || s.is(Blocks.ANDESITE) || s.is(Blocks.DIORITE) || s.is(Blocks.GRANITE) || s.is(Blocks.TUFF);
            case CARBON -> s.is(BlockTags.COAL_ORES);
            case HIERRO -> s.is(BlockTags.IRON_ORES);
            case TIERRA -> s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.ROOTED_DIRT)
                    || s.is(Blocks.PODZOL) || s.is(Blocks.MYCELIUM);
            case ARENA -> s.is(BlockTags.SAND);
            case CARNE -> false;
        };
    }

    public String toolName() {
        return switch (toolSlot) {
            case LauryEntity.SLOT_PICK -> "un pico";
            case LauryEntity.SLOT_SHOVEL -> "una pala";
            case LauryEntity.SLOT_SWORD -> "una espada";
            default -> "un hacha";
        };
    }

    @Nullable
    public static LauryResource byName(String name) {
        String n = Normalizer.normalize(name.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        for (LauryResource r : values()) if (n.startsWith(r.key)) return r;
        if (n.startsWith("tronco") || n.startsWith("arbol") || n.startsWith("wood") || n.startsWith("log")) return MADERA;
        if (n.startsWith("roca") || n.startsWith("stone") || n.startsWith("cobble")) return PIEDRA;
        if (n.startsWith("coal")) return CARBON;
        if (n.startsWith("iron") || n.startsWith("fierro")) return HIERRO;
        if (n.startsWith("dirt") || n.startsWith("pasto")) return TIERRA;
        if (n.startsWith("sand")) return ARENA;
        if (n.startsWith("meat") || n.startsWith("comida") || n.startsWith("caza")) return CARNE;
        return null;
    }
}
