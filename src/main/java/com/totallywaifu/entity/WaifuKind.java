package com.totallywaifu.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;

/**
 * Las chicas de Totally Waifu?. Las 4 de las mesas se transforman (y se destransforman) con la Vara de Transformación;
 * las de animales y monstruos se transforman para siempre.
 */
public enum WaifuKind {
    // ---- mesas (reversibles)
    KOZUE("Kozue", "Mesa de crafteo", Blocks.CRAFTING_TABLE, "§6", "Craftea contigo y por ti (pedidos), tala árboles y mina menas", 40, 0.32, 6, 4, 0xB08452, 0x54943F),
    HOMURA("Homura", "Horno", Blocks.FURNACE, "§c", "Su horno cocina siempre (el doble de rápido), busca carbón y caza para cocinar", 40, 0.32, 6, 4, 0x707074, 0xF08C28),
    KURA("Kura", "Cofre", Blocks.CHEST, "§e", "Cofre doble con piernas: recoge todo lo tirado, vacía las mochilas de las demás y ordena", 40, 0.32, 6, 4, 0xA27034, 0xDEB03C),
    SHIORI("Shiori", "Mesa de encantamientos", Blocks.ENCHANTING_TABLE, "§d", "Encanta con poder máximo, magia contra monstruos, guarda experiencia y repara tu equipo", 40, 0.32, 6, 4, 0x241A34, 0x5CDCD6),
    // ---- animales (permanentes)
    OVEJA("Lana", "Oveja", null, "§f", "Te da lana (tijeras o sola cada rato); muy suave y tranquila", 30, 0.3, 3, 2, 0xF2F0EA, 0xEEAABA),
    VACA("Bella", "Vaca", null, "§7", "Leche con un cubo y cuero de vez en cuando", 36, 0.28, 4, 3, 0xF4F4F0, 0x282428),
    CERDO("Rosita", "Cerdo", null, "§d", "Desentierra zanahorias, papas y remolachas para ti", 30, 0.3, 3, 2, 0xF2A6B0, 0xD27888),
    GALLINA("Pía", "Gallina", null, "§e", "Pone huevos y suelta plumas; nunca sufre daño por caída", 24, 0.32, 2, 1, 0xFAFAF6, 0xDC2828),
    PERRO("Luna", "Perro", null, "§b", "Rápida y feroz: caza monstruos y te defiende con uñas y dientes", 40, 0.38, 9, 4, 0xBEBEC4, 0xC82828),
    GATO("Mika", "Gato", null, "§6", "Los creepers y phantoms le temen; te trae regalos al amanecer", 30, 0.36, 5, 2, 0xE6963C, 0xFAECD2),
    GOLEM("Ferra", "Gólem de hierro", null, "§7", "Muy resistente: lanza a los monstruos por los aires; se cura con lingotes de hierro", 100, 0.28, 14, 12, 0xD2D0C8, 0x468232),
    ALDEANA("Elena", "Aldeana", null, "§a", "Comercia contigo (conserva sus tratos de aldeana)", 30, 0.3, 3, 2, 0x785A40, 0x468C50),
    ZOMBIE("Zoe", "Zombi", null, "§2", "Pelea cuerpo a cuerpo sin miedo y se regenera rápido", 44, 0.3, 8, 4, 0xC4DEC8, 0x3C96A0),
    ESQUELETO("Nyra", "Esqueleto", null, "§f", "Arquera: dispara flechas a los monstruos desde lejos", 34, 0.32, 4, 2, 0xECEAE2, 0x5A5A60),
    CREEPER("Cree", "Creeper", null, "§a", "Explosión controlada que daña a los monstruos sin romper bloques", 34, 0.3, 4, 2, 0x5AB046, 0x1E1E22),
    ARANA("Aracne", "Araña", null, "§4", "Trepa paredes y enreda a los monstruos con telarañas (los ralentiza)", 34, 0.34, 6, 3, 0x24202A, 0xC8141E),
    BRUJA("Morgana", "Bruja", null, "§5", "Hechizos contra monstruos y te cura cuando estás herido", 34, 0.3, 4, 3, 0x602C78, 0x5AA03C),
    ENDERMAN("Endra", "Enderman", null, "§5", "Se teletransporta junto a los monstruos y los golpea muy fuerte", 50, 0.34, 10, 4, 0x18141E, 0xBE50E6);

    public final String display, role, color, job;
    @Nullable public final Block block;
    public final double health, speed, attack, armor;
    public final int eggA, eggB;

    WaifuKind(String display, String role, @Nullable Block block, String color, String job, double health, double speed, double attack, double armor,
              int eggA, int eggB) {
        this.display = display;
        this.role = role;
        this.block = block;
        this.color = color;
        this.job = job;
        this.health = health;
        this.speed = speed;
        this.attack = attack;
        this.armor = armor;
        this.eggA = eggA;
        this.eggB = eggB;
    }

    public String id() {
        return name().toLowerCase();
    }

    /** Las 4 de las mesas: se pueden volver a convertir en su bloque. */
    public boolean station() {
        return block != null;
    }

    /** Pelean cuerpo a cuerpo (cazan monstruos cuando te siguen o trabajan). */
    public boolean fighter() {
        return this == PERRO || this == GOLEM || this == ZOMBIE || this == ENDERMAN || this == ARANA;
    }

    public static WaifuKind byId(int i) {
        WaifuKind[] v = values();
        return i >= 0 && i < v.length ? v[i] : KOZUE;
    }

    @Nullable
    public static WaifuKind fromBlock(Block b) {
        if (b == Blocks.CRAFTING_TABLE) return KOZUE;
        if (b == Blocks.FURNACE) return HOMURA;
        if (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST) return KURA;
        if (b == Blocks.ENCHANTING_TABLE) return SHIORI;
        return null;
    }

    @Nullable
    public static WaifuKind fromEntity(Entity e) {
        if (e instanceof Sheep) return OVEJA;
        if (e instanceof MushroomCow || e instanceof Cow) return VACA;
        if (e instanceof Pig) return CERDO;
        if (e instanceof Chicken) return GALLINA;
        if (e instanceof Wolf) return PERRO;
        if (e instanceof Cat || e instanceof Ocelot) return GATO;
        if (e instanceof IronGolem) return GOLEM;
        if (e instanceof Villager) return ALDEANA;
        if (e instanceof Zombie && !(e instanceof ZombifiedPiglin)) return ZOMBIE;
        if (e instanceof AbstractSkeleton) return ESQUELETO;
        if (e instanceof Creeper) return CREEPER;
        if (e instanceof Spider) return ARANA;
        if (e instanceof Witch) return BRUJA;
        if (e instanceof EnderMan) return ENDERMAN;
        return null;
    }
}
