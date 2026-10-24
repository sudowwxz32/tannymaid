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
    BASE("Base", 0xFFFFFF, 1.0F, 0, 0.00F, 0, 0.00F, 0.0F, 0, 100, 0.0F, 0.06F, 0, 0xBFE6FF, 1.0F),
    FALSE_SSJ("Super Saiyajin Falso", 0x26262C, 1.5F, 2, 0.10F, 6, 0.08F, 0.5F, 1, 150, 35.0F, 0.07F, 1, 0xD9B44A, 1.15F),
    SSJ1("Super Saiyajin Fase 1", 0xFFD83A, 2.0F, 5, 0.20F, 12, 0.15F, 1.0F, 1, 200, 30.0F, 0.08F, 1, 0xC8962E, 1.6F),
    SSJ2("Super Saiyajin Fase 2", 0xFFE45C, 3.0F, 10, 0.30F, 20, 0.22F, 1.5F, 2, 300, 20.0F, 0.09F, 4, 0xFFD94A, 1.35F),
    SSJ3("Super Saiyajin Fase 3", 0xFFCF2E, 4.0F, 18, 0.40F, 30, 0.30F, 2.0F, 2, 400, 12.0F, 0.10F, 2, 0xFFC21E, 1.5F),
    GOD("Super Saiyajin Dios", 0xE0304C, 6.0F, 35, 0.55F, 50, 0.40F, 3.0F, 3, 600, 8.0F, 0.12F, 3, 0xFF4A3A, 1.2F),
    BLUE("Super Saiyajin Blue", 0x3FC8FF, 8.0F, 70, 0.65F, 70, 0.50F, 4.0F, 3, 800, 5.0F, 0.14F, 1, 0x3FC8FF, 1.35F),
    ULTRA("Ultra Instinto", 0xDDE3F2, 12.0F, 600, 0.80F, 100, 0.70F, 6.0F, 4, 1200, 3.0F, 0.18F, 3, 0xC9D6FF, 1.1F),
    SSJ4("Super Saiyajin Fase 4", 0x1C1C22, 14.0F, 800, 0.82F, 120, 0.60F, 6.0F, 4, 1500, 3.0F, 0.18F, 5, 0xE8442A, 1.45F),
    ULTRA_EGO("Ultra Ego", 0x9040FF, 15.0F, 900, 0.80F, 120, 0.65F, 6.5F, 4, 1600, 2.5F, 0.19F, 6, 0x9B3CFF, 1.5F),
    // ---- se desbloquean con el Dominio Total
    BEAST("Gohan Bestia", 0xD4D4DC, 17.0F, 1100, 0.84F, 140, 0.72F, 7.0F, 5, 1800, 2.5F, 0.20F, 7, 0x9C9CA8, 1.6F),
    LEGENDARY("Super Saiyajin Legendario", 0x5CFF5A, 19.0F, 1300, 0.88F, 190, 0.85F, 7.5F, 5, 2200, 2.0F, 0.21F, 8, 0x6CFF4A, 1.8F),
    ROSE("Super Saiyajin Rosé", 0xFF6EC4, 17.0F, 1100, 0.84F, 140, 0.74F, 7.0F, 5, 1800, 2.5F, 0.20F, 9, 0xFF5CB8, 1.55F),
    // ---- fases del Super Saiyajin Legendario
    LEGEND1("Legendario Fase 1", 0x1A1A22, 21.0F, 1500, 0.88F, 210, 0.82F, 7.5F, 5, 2400, 3.0F, 0.21F, 8, 0x3CFF4A, 1.75F),
    LEGEND2("Legendario Fase 2", 0xFFE040, 25.0F, 1800, 0.91F, 250, 0.92F, 8.0F, 5, 2800, 2.5F, 0.22F, 8, 0xFFE84A, 1.9F),
    LEGEND3("Legendario Fase 3", 0x4CFF4C, 34.0F, 2600, 0.95F, 320, 1.05F, 9.0F, 5, 3400, 0.0F, 0.25F, 10, 0x40FF40, 2.15F);

    public final String display;
    public final int color;
    public final float damage, resist, speed, agility, minutes, fly;
    public final int punch, hearts, jump, maxKi;
    /** 0 sin cabello, 1 puntas, 2 largo (fase 3), 3 Dios/Ultra, 4 puntas + mechón (fase 2). */
    public final int hairStyle;
    /** Color y tamaño del aura (la fase 1 tiene un aura grande café amarillenta). */
    public final int auraColor;
    public final float auraSize;

    Form(String display, int color, float damage, int punch, float resist, int hearts, float speed, float agility,
         int jump, int maxKi, float minutes, float fly, int hairStyle, int auraColor, float auraSize) {
        this.auraColor = auraColor;
        this.auraSize = auraSize;
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
    public float ar() { return ((auraColor >> 16) & 255) / 255.0F; }
    public float ag() { return ((auraColor >> 8) & 255) / 255.0F; }
    public float ab() { return (auraColor & 255) / 255.0F; }
    /** Duración (ticks) de la carga antes de transformarse: más larga y dramática en fases altas. */
    public int powerUpTicks() { return 24 + ordinal() * 8; }
    /** Bloque de entrenamiento que desbloquea esta fase (1 = solo Kaioken). */
    public int tier() {
        return switch (this) {
            case BASE -> 1;
            case FALSE_SSJ, SSJ1 -> 2;
            case SSJ2, SSJ3, GOD -> 3;
            case BLUE, ULTRA -> 4;
            case SSJ4, ULTRA_EGO -> 5;
            case BEAST, LEGENDARY, ROSE, LEGEND1, LEGEND2, LEGEND3 -> 6;     // 6 = requiere el Dominio Total
        };
    }

    /** Musculatura: cuánto se ensancha el cuerpo en esta fase. */
    public float muscle() {
        return switch (this) {
            case BASE -> 1.0F;
            case FALSE_SSJ -> 1.03F;
            case SSJ1 -> 1.05F;
            case SSJ2 -> 1.07F;
            case SSJ3 -> 1.08F;
            case GOD -> 0.97F;                    // Dios: cuerpo delgado, sin exagerar
            case ULTRA -> 1.04F;
            case BLUE -> 1.06F;
            case SSJ4 -> 1.16F;
            case ULTRA_EGO -> 1.14F;
            case BEAST -> 1.12F;
            case LEGENDARY -> 1.34F;              // Legendario: musculatura enorme
            case ROSE -> 1.05F;
            case LEGEND1 -> 1.22F;                // más músculo
            case LEGEND2 -> 1.3F;
            case LEGEND3 -> 1.48F;
        };
    }

    /** Escala de altura (la Fase 4 es más alta y robusta). */
    public float height() {
        return switch (this) {
            case SSJ4 -> 1.12F;
            case ULTRA_EGO -> 1.05F;
            case BEAST -> 1.05F;
            case LEGENDARY -> 1.18F;
            case LEGEND1 -> 1.06F;
            case LEGEND2 -> 1.14F;                // más alto
            case LEGEND3 -> 1.3F;                 // todavía más alto
            default -> 1.0F;
        };
    }

    /** Color de los rayos del aura. */
    public int lightningColor() {
        return switch (this) {
            case ULTRA_EGO -> 0xB066FF;
            case BEAST -> 0xFF2626;
            case ROSE -> 0xFF7AD6;
            case LEGEND1, LEGEND3 -> 0x7CFF6A;
            case LEGEND2 -> 0xE8FF60;
            case LEGENDARY -> 0xA8FF70;
            case BLUE -> 0x9CE8FF;
            default -> 0xC0E0FF;
        };
    }

    /** Fases con rayos eléctricos en el aura. */
    public boolean lightningAura() {
        return this == SSJ2 || this == SSJ3 || this == ULTRA_EGO || this == BEAST || this == ROSE || this == LEGENDARY
                || this == LEGEND1 || this == LEGEND2 || this == LEGEND3;
    }

    /** Color de la capa exterior del aura (el Rosé tiene una segunda aura rosa oscura). */
    public int outerColor() {
        return switch (this) {
            case ROSE -> 0x9C0E5C;
            case LEGEND1 -> 0x1E9A2A;
            case LEGEND2 -> 0x3CFF4A;             // aura amarilla combinada con verde
            case LEGEND3 -> 0x0E8A1E;
            case BEAST -> 0x5E5E68;
            case LEGENDARY -> 0x1E9A2A;
            case GOD -> 0xFF8A2A;
            case ULTRA_EGO -> 0x5A1AA8;
            default -> auraColor;
        };
    }

    /** Color de las ondas del aura (la Bestia tiene ondas rojas). */
    public int waveColor() {
        return this == BEAST ? 0xFF2A2A : auraColor;
    }

    /** Ultra Instinto es la única transformación tranquila y relajada. */
    public boolean calm() { return this == ULTRA; }

    /** Qué tan dramática es la transformación (0..1): aura de fuego, escombros, temblor. */
    public float drama() {
        return switch (this) {
            case BASE, ULTRA -> 0.0F;
            case FALSE_SSJ -> 0.35F;
            case SSJ1 -> 0.55F;
            case SSJ2, GOD -> 0.65F;
            case SSJ3, BLUE -> 0.75F;
            case SSJ4, ULTRA_EGO -> 0.85F;
            case BEAST, ROSE, LEGEND1 -> 0.95F;
            case LEGEND2, LEGEND3 -> 1.0F;
            case LEGENDARY -> 1.0F;
        };
    }

    /** Sin límite de ki: la energía del Legendario nunca se acaba. */
    public boolean infiniteKi() { return this == LEGENDARY || this == LEGEND3; }

    /** Legendario Fase 3: no pierde vida, no pierde ki y no tiene tiempo límite. */
    public boolean legendary3() { return this == LEGEND3; }

    /** Cuerpo delgado (modelo de brazos finos, como Alex). */
    public boolean slimBody() { return this == GOD; }
}
