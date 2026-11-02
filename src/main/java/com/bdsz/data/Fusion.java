package com.bdsz.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.warden.Warden;

/** Con quién te puedes fusionar con los Aretes de Fusión, cuánto dura y qué habilidad te da. */
public enum Fusion {
    NONE("", 0, 0xFFFFFF, ""),
    ZOMBIE("Zombi", 12 * 60, 0x6FBF4A,
            "Fuerza de no-muerto, regeneración, no necesitas comer, los zombis no te atacan, tus golpes dan hambre. §eX§7: Horda — 3 zombis aliados"),
    SKELETON("Esqueleto", 10 * 60, 0xE8E4D8,
            "Puntería y velocidad, flechas sin gastar, los esqueletos no te atacan. §eX§7: Arco de energía (flechas de ki amarillas)"),
    ENDERMAN("Enderman", 9 * 60, 0xC070FF,
            "Brazos largos, los endermans no te atacan. §eX§7: Teletransporte a donde miras"),
    CREEPER("Creeper", 8 * 60, 0x58D04A,
            "Inmune a explosiones, los creepers no te atacan. §eX§7: Explotas como un creeper (siseo y ¡boom!, no te daña)"),
    GOLEM("Gólem de hierro", 7 * 60, 0xD8D8D8,
            "Fuerza y resistencia enormes, al golpear tus brazos se cubren de ki con forma de brazos de gólem y lanzan por los aires. §eX§7: Pisotón sísmico"),
    WARDEN("Warden", 5 * 60, 0x2FD4C8,
            "Poder abismal: mucha vida, fuerza y resistencia, sientes las vibraciones (ves a todos brillar), oscuridad a tus enemigos. §eX§7: Rugido sónico"),
    DRAGON("Dragón del End", 15 * 60, 0xB050FF,
            "§5LA FUSIÓN MÁS PODEROSA§7: poder x80, más alto y fornido, alas, cuernos y cola, inmune al fuego, regeneración y "
                    + "resistencia enormes. §eX§7: Aliento de dragón. §c¡Puede quedarse para siempre!");

    /** El dragón no puede "recoger" el arete: basta con lanzarlo cerca de él. */
    public static boolean isDragon(Entity e) {
        return e instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon;
    }

    public final String display;
    public final int seconds;
    public final int color;
    public final String ability;

    Fusion(String display, int seconds, int color, String ability) {
        this.display = display;
        this.seconds = seconds;
        this.color = color;
        this.ability = ability;
    }

    /** Transformaciones con más afinidad con cada fusión. */
    public boolean affinity(Form f) {
        return switch (this) {
            case ZOMBIE -> f == Form.SSJ4 || f == Form.LEGENDARY || f == Form.LEGEND1 || f == Form.BEAST;
            case SKELETON -> f == Form.ULTRA || f == Form.GOD || f == Form.BLUE;
            case ENDERMAN -> f == Form.ULTRA_EGO || f == Form.ROSE || f == Form.GOD;
            case CREEPER -> f == Form.SSJ2 || f == Form.SSJ3 || f == Form.BEAST;
            case GOLEM -> f == Form.BLUE || f == Form.BLUE_EVO || f == Form.SSJ1;
            case WARDEN -> f == Form.LEGEND2 || f == Form.LEGEND3 || f == Form.ULTRA_EGO;
            case DRAGON -> f == Form.UI_SIGN || f == Form.UI_PERFECT || f == Form.UI_SELF || f == Form.LEGEND3 || f == Form.ULTRA_EGO;
            default -> false;
        };
    }

    public String affinityText() {
        return switch (this) {
            case ZOMBIE -> "Fase 4, Legendario Z, Legendario Fase 1, Bestia";
            case SKELETON -> "Ultra Instinto, Dios, Blue";
            case ENDERMAN -> "Ultra Ego, Rosé, Dios";
            case CREEPER -> "Fase 2, Fase 3, Bestia";
            case GOLEM -> "Blue, Blue Evolución, Fase 1";
            case WARDEN -> "Legendario Fase 2 y 3, Ultra Ego";
            case DRAGON -> "los tres Ultra Instintos, Legendario Fase 3, Ultra Ego";
            default -> "";
        };
    }

    public static Fusion byId(int id) {
        Fusion[] v = values();
        return id >= 0 && id < v.length ? v[id] : NONE;
    }

    /** ¿Con qué fusión corresponde este mob? */
    public static Fusion of(Entity e) {
        if (isDragon(e)) return DRAGON;
        if (e instanceof Warden) return WARDEN;
        if (e instanceof IronGolem) return GOLEM;
        if (e instanceof Creeper) return CREEPER;
        if (e instanceof EnderMan) return ENDERMAN;
        if (e instanceof AbstractSkeleton) return SKELETON;
        if (e instanceof Zombie) return ZOMBIE;
        return NONE;
    }

    public float r() { return ((color >> 16) & 255) / 255F; }
    public float g() { return ((color >> 8) & 255) / 255F; }
    public float b() { return (color & 255) / 255F; }
}
