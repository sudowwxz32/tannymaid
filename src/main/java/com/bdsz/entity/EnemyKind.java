package com.bdsz.entity;

import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;

/**
 * Enemigos originales del mod. Todos tienen más vida que un Warden (500) y que el Dragón del End (200).
 * La vida de los jefes supera el límite de Minecraft (1024): se guarda como 1000 y el daño recibido se divide
 * en proporción (hpScale), así que en la práctica aguantan toda su vida.
 */
public enum EnemyKind {
    //            id                     ancho alto  escala vida  armad ataque vel.  color ki  ráfaga espera jefe   vuela  telep. pisotón regen invoca           esquiva xp   barra
    ANDROIDE("androide",                 0.6F, 1.95F, 1.0F,  650,  10, 18, 0.32, 0x6FE8FF, 18, 50, false, false, true,  false, 0,  null,             0.15F, 60,  BossEvent.BossBarColor.BLUE),
    ROBOT("robot_guardian",              1.3F, 2.9F,  1.45F, 900,  20, 28, 0.23, 0xFF3A30, 24, 60, false, false, false, true,  0,  null,             0.0F,  80,  BossEvent.BossBarColor.RED),
    SOLDADO("soldado_imperial",          0.6F, 1.95F, 1.0F,  560,  8,  14, 0.30, 0xB060FF, 14, 40, false, false, false, false, 0,  null,             0.05F, 50,  BossEvent.BossBarColor.PURPLE),
    ENGENDRO("engendro_bio",             0.55F, 1.5F, 0.8F,  600,  6,  16, 0.38, 0xFF8A2A, 14, 70, false, false, true,  false, 0,  null,             0.2F,  55,  BossEvent.BossBarColor.YELLOW),
    BIO_TITAN("bio_titan",               0.8F, 2.5F,  1.3F,  3000, 16, 40, 0.30, 0xFF7A1A, 30, 45, true,  false, true,  true,  6,  "engendro_bio",   0.1F,  500, BossEvent.BossBarColor.YELLOW),
    DEMONIO("demonio_arcano",            0.75F, 2.4F, 1.25F, 2800, 12, 34, 0.30, 0xC040FF, 28, 40, true,  true,  true,  false, 14, null,             0.15F, 500, BossEvent.BossBarColor.PURPLE),
    EMPERADOR("emperador_galactico",     0.75F, 2.3F, 1.2F,  4000, 18, 44, 0.32, 0xFF2A4A, 34, 35, true,  true,  true,  false, 0,  "soldado_imperial", 0.2F, 800, BossEvent.BossBarColor.RED);

    public final String id;
    public final float width, height, scale;
    public final int hp, armor, attack;
    public final double speed;
    public final int kiColor, blastDamage, blastCooldown;
    public final boolean boss, flies, teleport, stomp;
    public final int regen;
    public final String summon;
    public final float dodge;
    public final int xp;
    public final BossEvent.BossBarColor barColor;

    EnemyKind(String id, float width, float height, float scale, int hp, int armor, int attack, double speed, int kiColor,
              int blastDamage, int blastCooldown, boolean boss, boolean flies, boolean teleport, boolean stomp, int regen,
              String summon, float dodge, int xp, BossEvent.BossBarColor barColor) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.hp = hp;
        this.armor = armor;
        this.attack = attack;
        this.speed = speed;
        this.kiColor = kiColor;
        this.blastDamage = blastDamage;
        this.blastCooldown = blastCooldown;
        this.boss = boss;
        this.flies = flies;
        this.teleport = teleport;
        this.stomp = stomp;
        this.regen = regen;
        this.summon = summon;
        this.dodge = dodge;
        this.xp = xp;
        this.barColor = barColor;
    }

    /** Vida real del atributo (Minecraft no permite más de 1024). */
    public double attributeHealth() { return Math.min(hp, 1000); }

    /** Cuánto se divide el daño recibido para que aguante toda su vida. */
    public float hpScale() { return (float) (hp / attributeHealth()); }

    public static EnemyKind byId(String id) {
        for (EnemyKind k : values()) if (k.id.equals(id)) return k;
        return null;
    }

    public static EnemyKind of(EntityType<?> type) {
        for (EnemyKind k : values()) {
            if (com.bdsz.registry.ModRegistry.ENEMY.get(k).get() == type) return k;
        }
        return ANDROIDE;
    }
}
