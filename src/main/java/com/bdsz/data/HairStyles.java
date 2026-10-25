package com.bdsz.data;

/** Peinados base que se eligen con /raza saiyajin (diseños originales con nombres descriptivos). */
public final class HairStyles {
    private HairStyles() {}

    public static final int BLACK = 0x23232B;
    public static final int DARK_SKY = 0x5A7CB8;

    public record Style(String group, String name, int color, boolean female, boolean smooth) {}

    public static final Style[] ALL = {
            // masculinos
            new Style("m1_", "Puntas salvajes", BLACK, false, false),
            new Style("m2_", "Puntas con mechón al frente", BLACK, false, false),
            new Style("m3_", "Llama alta con pico", BLACK, false, false),
            new Style("m4_", "Melena tranquila", BLACK, false, false),
            new Style("m5_", "Liso con raya al medio", DARK_SKY, false, true),
            new Style("m6_", "Desordenado con picos", BLACK, false, false),
            // femeninos
            new Style("w1_", "Corte corto liso", DARK_SKY, true, true),
            new Style("w2_", "Coletas", BLACK, true, true),
            new Style("w3_", "Puntas rebeldes", BLACK, true, false),
            new Style("w4_", "Melena larga con flequillo", BLACK, true, true),
            new Style("w5_", "Cola de caballo alta", BLACK, true, true),
            new Style("w6_", "Melena larga ondulada", BLACK, true, true),
            new Style("w7_", "Moño con flequillo", BLACK, true, true),
    };

    public static Style get(int id) {
        return id >= 0 && id < ALL.length ? ALL[id] : null;
    }
}
