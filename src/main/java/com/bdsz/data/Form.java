package com.bdsz.data;

/**
 * Fases de transformación. damage = multiplicador de daño, resist = reducción de daño recibido,
 * speed = velocidad extra, drain = cansancio (hambre) por segundo.
 */
public enum Form {
    BASE("Base", 0xFFFFFF, 1.0F, 0.00F, 0.00F, 0.00F, 0),
    FALSE_SSJ("Super Saiyajin Falso", 0xE8C24A, 1.5F, 0.10F, 0.05F, 0.05F, 0),
    SSJ1("Super Saiyajin Fase 1", 0xFFD83A, 2.0F, 0.20F, 0.10F, 0.06F, 1),
    SSJ2("Super Saiyajin Fase 2", 0xFFE45C, 3.0F, 0.30F, 0.15F, 0.09F, 1),
    SSJ3("Super Saiyajin Fase 3", 0xFFCF2E, 4.0F, 0.40F, 0.18F, 0.16F, 2),
    GOD("Super Saiyajin Dios", 0xE0304C, 6.0F, 0.50F, 0.22F, 0.10F, 3),
    BLUE("Super Saiyajin Blue", 0x3FC8FF, 8.0F, 0.60F, 0.26F, 0.14F, 1),
    ULTRA("Ultra Instinto", 0xDDE3F2, 10.0F, 0.70F, 0.32F, 0.18F, 3);

    public final String display;
    public final int color;
    public final float damage, resist, speed, drain;
    /** 0 sin cabello, 1 puntas, 2 cabello largo (fase 3), 3 cabello de dios. */
    public final int hairStyle;

    Form(String display, int color, float damage, float resist, float speed, float drain, int hairStyle) {
        this.display = display;
        this.color = color;
        this.damage = damage;
        this.resist = resist;
        this.speed = speed;
        this.drain = drain;
        this.hairStyle = hairStyle;
    }

    public static Form byId(int id) {
        Form[] v = values();
        return id >= 0 && id < v.length ? v[id] : BASE;
    }

    public float r() { return ((color >> 16) & 255) / 255.0F; }
    public float g() { return ((color >> 8) & 255) / 255.0F; }
    public float b() { return (color & 255) / 255.0F; }
}
