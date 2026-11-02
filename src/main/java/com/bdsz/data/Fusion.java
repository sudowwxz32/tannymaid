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
            "Fuerza de no-muerto, regeneración, los zombis no te atacan, tus golpes dan hambre. §eX§7: Horda — levanta a 3 zombis aliados"),
    SKELETON("Esqueleto", 10 * 60, 0xE8E4D8,
            "Puntería y velocidad, flechas sin gastar, los esqueletos no te atacan. §eX§7: Lluvia de flechas"),
    ENDERMAN("Enderman", 9 * 60, 0xC070FF,
            "Brazos largos, los endermans no te atacan. §eX§7: Teletransporte a donde miras"),
    CREEPER("Creeper", 8 * 60, 0x58D04A,
            "Inmune a explosiones, sigiloso, los creepers no te atacan. §eX§7: Explosión (no te daña)"),
    GOLEM("Gólem de hierro", 7 * 60, 0xD8D8D8,
            "Fuerza y resistencia enormes, golpes que lanzan por los aires, no te empujan. §eX§7: Pisotón sísmico"),
    WARDEN("Warden", 5 * 60, 0x2FD4C8,
            "Poder abismal: mucha vida, fuerza y resistencia, oscuridad a tus enemigos. §eX§7: Rugido sónico");

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

    public static Fusion byId(int id) {
        Fusion[] v = values();
        return id >= 0 && id < v.length ? v[id] : NONE;
    }

    /** ¿Con qué fusión corresponde este mob? */
    public static Fusion of(Entity e) {
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
