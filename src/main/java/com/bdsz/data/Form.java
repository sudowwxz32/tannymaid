package com.bdsz.data;

/**
 * Fases de transformación. Todos los efectos crecen con la fase; Ultra Instinto es la cima
 * (su puñetazo supera los 500 de vida de un Warden).
 *
 * damage = multiplicador de daño · punch = daño extra de cada puñetazo · resist = reducción de daño recibido
 * hearts = vida extra (2 = un corazón) · speed = velocidad · agility = velocidad de ataque extra · jump = nivel de salto
 * maxKi = ki máximo · minutes = tiempo límite · fly = velocidad de vuelo
 */
public enum Form {
    BASE("Base", 0xFFFFFF, 1.0F, 0, 0.00F, 0, 0.00F, 0.0F, 0, 100, 0.0F, 0.06F, 0),
    FALSE_SSJ("Super Saiyajin Falso", 0xE8C24A, 1.5F, 2, 0.10F, 6, 0.08F, 0.5F, 1, 150, 35.0F, 0.07F, 1),
    SSJ1("Super Saiyajin Fase 1", 0xFFD83A, 2.0F, 5, 0.20F, 12, 0.15F, 1.0F, 1, 200, 30.0F, 0.08F, 1),
    SSJ2("Super Saiyajin Fase 2", 0xFFE45C, 3.0F, 10, 0.30F, 20, 0.22F, 1.5F, 2, 300, 20.0F, 0.09F, 4),
    SSJ3("Super Saiyajin Fase 3", 0xFFCF2E, 4.0F, 18, 0.40F, 30, 0.30F, 2.0F, 2, 400, 12.0F, 0.10F, 2),
    GOD("Super Saiyajin Dios", 0xE0304C, 6.0F, 35, 0.55F, 50, 0.40F, 3.0F, 3, 600, 8.0F, 0.12F, 3),
    BLUE("Super Saiyajin Blue", 0x3FC8FF, 8.0F, 70, 0.65F, 70, 0.50F, 4.0F, 3, 800, 5.0F, 0.14F, 1),
    ULTRA("Ultra Instinto", 0xDDE3F2, 12.0F, 600, 0.80F, 100, 0.70F, 6.0F, 4, 1200, 3.0F, 0.18F, 3);

    public final String display;
    public final int color;
    public final float damage, resist, speed, agility, minutes, fly;
    public final int punch, hearts, jump, maxKi;
    /** 0 sin cabello, 1 puntas, 2 largo (fase 3), 3 Dios/Ultra, 4 puntas + mechón (fase 2). */
    public final int hairStyle;

    Form(String display, int color, float damage, int punch, float resist, int hearts, float speed, float agility,
         int jump, int maxKi, float minutes, float fly, int hairStyle) {
        this.display = display;
        this.color = color;
        this.damage = damage;
        this.punch = punch;
        this.resist = resist;
        this.hearts = hearts;
        this.speed = speed;
        this.agility = agility;
        this.jump = jump;
        this.maxKi = maxKi;
        this.minutes = minutes;
        this.fly = fly;
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
