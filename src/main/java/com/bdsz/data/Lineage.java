package com.bdsz.data;

import java.util.EnumSet;

/**
 * Bloques de linaje (solo en partidas online). Cada uno desbloquea al instante un grupo de fases; las demás
 * nunca están disponibles. El Kaioken siempre está disponible.
 */
public enum Lineage {
    NONE("", 0xFFFFFF, EnumSet.noneOf(Form.class), ""),
    G("Bloque G", 0xFF9A2A, EnumSet.of(Form.FALSE_SSJ, Form.SSJ1, Form.SSJ2, Form.SSJ3, Form.GOD, Form.BLUE, Form.ULTRA),
            "Del Super Saiyajin Falso al Ultra Instinto"),
    V("Bloque V", 0x3A7BFF, EnumSet.of(Form.FALSE_SSJ, Form.SSJ1, Form.SSJ2, Form.SSJ3, Form.GOD, Form.BLUE, Form.BLUE_EVO, Form.SSJ4, Form.ULTRA_EGO),
            "Del Falso al Blue, Blue Evolución, Fase 4 y Ultra Ego"),
    A("Bloque A", 0xD9B44A, EnumSet.of(Form.FALSE_SSJ, Form.SSJ1, Form.SSJ2, Form.SSJ3, Form.GOD, Form.SSJ4, Form.BEAST),
            "Falso, Fases 1 a 4, Dios y Gohan Bestia"),
    B("Bloque B", 0x5CFF5A, EnumSet.of(Form.LEGENDARY, Form.LEGEND1, Form.LEGEND2, Form.LEGEND3),
            "Solo el Legendario Z y sus 3 fases"),
    R("Bloque R", 0xFF6EC4, EnumSet.of(Form.SSJ1, Form.SSJ2, Form.SSJ3, Form.ROSE),
            "Fases 1, 2 y 3 y el Super Saiyajin Rosé"),
    NORMAL("Normal", 0xBFE6FF, EnumSet.of(Form.FALSE_SSJ, Form.SSJ1, Form.SSJ2, Form.SSJ3, Form.GOD, Form.BLUE, Form.ULTRA),
            "Del Super Saiyajin Falso al Ultra Instinto");

    /** Elección "aleatorio": se sortea entre estos. */
    public static final Lineage[] RANDOM_POOL = {G, V, A, B, R};
    /** Valor que manda el cliente para pedir un bloque aleatorio. */
    public static final int RANDOM = 99;

    public final String display;
    public final int color;
    public final EnumSet<Form> forms;
    public final String description;

    Lineage(String display, int color, EnumSet<Form> forms, String description) {
        this.display = display;
        this.color = color;
        this.forms = forms;
        this.description = description;
    }

    public boolean allows(Form f) {
        return f == Form.BASE || forms.contains(f);
    }

    public static Lineage byId(int id) {
        Lineage[] v = values();
        return id >= 0 && id < v.length ? v[id] : NONE;
    }
}
