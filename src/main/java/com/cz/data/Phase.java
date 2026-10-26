package com.cz.data;

/**
 * Mantos de chakra del zorro. Cada fase tiene más colas y más poder.
 * vida = vida extra (2 = un corazón) · daño = daño extra · drenaje = chakra por segundo.
 */
public enum Phase {
    NONE("Sin manto", 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0xFFFFFF, 0xFFFFFF, false, false, false, false, 1.0F),
    ONE("Manto de una cola", 1, 10, 4, 0.15F, 0.6F, 4, 0.2F, 0, 1, 20, 70, 0xFF7A1A, 0xFF5A10, false, false, false, false, 1.0F),
    TWO("Manto de dos colas", 2, 20, 8, 0.22F, 1.0F, 6, 0.3F, 1, 2, 35, 80, 0xFF6418, 0xFF4A10, false, false, false, false, 1.0F),
    THREE("Manto de tres colas", 3, 32, 13, 0.30F, 1.4F, 8, 0.45F, 1, 3, 50, 90, 0xFF4A14, 0xFF3010, true, false, false, false, 1.0F),
    FOUR("Manto de cuatro colas", 4, 50, 20, 0.38F, 1.8F, 12, 0.7F, 2, 5, 70, 110, 0xD81C14, 0xFF1A10, true, true, true, false, 1.25F),
    FIVE("Manto de cinco colas", 5, 70, 28, 0.45F, 2.2F, 16, 0.85F, 2, 7, 90, 130, 0xC01410, 0xFF1010, true, true, true, true, 1.4F);

    public final String display;
    public final int tails, health, damage, armor, jump, drain, minChakra, powerTicks, cloakColor, auraColor;
    public final float speed, attackSpeed, knockback, elongation;
    /** El manto quema (fase 3+), anda en cuatro patas (4+), piel roja de bestia (4+), esqueleto de zorro (5). */
    public final boolean burns, quadruped, beast, skeleton;

    Phase(String display, int tails, int health, int damage, float speed, float attackSpeed, int armor, float knockback, int jump,
          int drain, int minChakra, int powerTicks, int cloakColor, int auraColor, boolean burns, boolean quadruped, boolean beast,
          boolean skeleton, float elongation) {
        this.display = display;
        this.tails = tails;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.attackSpeed = attackSpeed;
        this.armor = armor;
        this.knockback = knockback;
        this.jump = jump;
        this.drain = drain;
        this.minChakra = minChakra;
        this.powerTicks = powerTicks;
        this.cloakColor = cloakColor;
        this.auraColor = auraColor;
        this.burns = burns;
        this.quadruped = quadruped;
        this.beast = beast;
        this.skeleton = skeleton;
        this.elongation = elongation;
    }

    public static Phase byId(int id) {
        Phase[] v = values();
        return id >= 0 && id < v.length ? v[id] : NONE;
    }

    public float r() { return ((cloakColor >> 16) & 255) / 255F; }
    public float g() { return ((cloakColor >> 8) & 255) / 255F; }
    public float b() { return (cloakColor & 255) / 255F; }
}
