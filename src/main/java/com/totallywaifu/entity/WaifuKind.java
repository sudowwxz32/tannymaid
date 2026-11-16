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
    KOZUE("Kozue", "Mesa de crafteo", Blocks.CRAFTING_TABLE, "§6", "Craftea CUALQUIER cosa que le pidas (fabrica también las piezas intermedias, usa su mochila y el cofre de Kura), tala, replanta y mina", 40, 0.32, 6, 4, 0xB08452, 0x54943F, 1.0F),
    HOMURA("Homura", "Horno", Blocks.FURNACE, "§c", "Cocina SOLO lo que le pidas (su horno o su pedido), mina menas y piedra para ti", 40, 0.32, 6, 4, 0x707074, 0xF08C28, 1.0F),
    KURA("Kura", "Cofre", Blocks.CHEST, "§e", "Cofre doble con piernas: recoge todo, vacía las mochilas de las demás, ordena y te da comida si tienes hambre", 40, 0.32, 6, 4, 0xA27034, 0xDEB03C, 0.97F),
    SHIORI("Shiori", "Mesa de encantamientos", Blocks.ENCHANTING_TABLE, "§d", "Encanta con poder máximo, magia contra monstruos, guarda experiencia, repara tu equipo y te da un escudo mágico", 40, 0.32, 6, 4, 0x241A34, 0x5CDCD6, 1.02F),
    // ---- animales (permanentes)
    OVEJA("Lana", "Oveja", null, "§f", "Lana (sola o con tijeras), te arropa: resistencia al frío y regeneración cuando descansas a su lado", 30, 0.3, 3, 2, 0xF2F0EA, 0xEEAABA, 0.95F),
    VACA("Bella", "Vaca", null, "§7", "Leche con un cubo, cuero, y te quita los efectos malos cuando estás cerca", 36, 0.28, 4, 3, 0xF4F4F0, 0x282428, 1.03F),
    CERDO("Rosita", "Cerdo", null, "§d", "Desentierra zanahorias, papas, remolachas y hongos; olfatea trufas (¡a veces esmeraldas!)", 30, 0.3, 3, 2, 0xF2A6B0, 0xD27888, 0.92F),
    GALLINA("Pía", "Gallina", null, "§e", "Huevos y plumas; sin daño por caída y te da caída lenta si te caes cerca de ella", 24, 0.32, 2, 1, 0xFAFAF6, 0xDC2828, 0.9F),
    PERRO("Luna", "Perro", null, "§b", "La mejor aliada: caza monstruos, caza por ti el animal que golpees, te da fuerza y te trae comida", 60, 0.4, 10, 6, 0xBEBEC4, 0xC82828, 1.0F),
    GATO("Mika", "Gato", null, "§6", "Creepers y phantoms le temen; regalos al amanecer y te da visión nocturna", 30, 0.36, 5, 2, 0xE6963C, 0xFAECD2, 0.93F),
    GOLEM("Ferra", "Gólem de hierro", null, "§7", "Gigante: lanza monstruos por los aires, te protege con resistencia; se cura con hierro", 120, 0.28, 16, 14, 0xD2D0C8, 0x468232, 1.4F),
    ALDEANA("Elena", "Aldeana", null, "§a", "Comercia contigo (conserva sus tratos) y te da descuentos y suerte", 30, 0.3, 3, 2, 0x785A40, 0x468C50, 1.0F),
    ZOMBIE("Zoe", "Zombi", null, "§2", "Pelea sin miedo, se regenera rápido y hace que los zombis la ignoren", 44, 0.3, 8, 4, 0xC4DEC8, 0x3C96A0, 0.98F),
    ESQUELETO("Nyra", "Esqueleto", null, "§f", "Arquera: lluvia de flechas a los monstruos; a veces te regala flechas y huesos", 34, 0.32, 4, 2, 0xECEAE2, 0x5A5A60, 1.04F),
    CREEPER("Cree", "Creeper", null, "§a", "Explosión controlada (solo daña monstruos) y te regala pólvora", 34, 0.3, 4, 2, 0x5AB046, 0x1E1E22, 0.94F),
    ARANA("Aracne", "Araña", null, "§4", "Trepa paredes, enreda monstruos (lentitud) y te da hilo; ve de noche", 34, 0.34, 6, 3, 0x24202A, 0xC8141E, 0.97F),
    BRUJA("Morgana", "Bruja", null, "§5", "Hechizos (veneno y debilidad), te cura y te da pociones de velocidad y fuerza", 34, 0.3, 4, 3, 0x602C78, 0x5AA03C, 1.03F),
    ENDERMAN("Endra", "Enderman", null, "§5", "Altísima: se teletransporta y golpea fuerte; te trae perlas de ender", 60, 0.34, 12, 4, 0x18141E, 0xBE50E6, 1.5F),
    // ---- 4.0
    CAMA("Yume", "Cama", null, "§9", "Clic izquierdo o derecho de noche: duermes en sus piernas; te protege y guarda tu punto de regreso", 40, 0.3, 4, 4, 0xB0CCF2, 0xF6B4D2, 0.98F),
    SLIME("Gela", "Slime", null, "§a", "Rebota a los monstruos, te quita el daño por caída y te da bolas de slime", 40, 0.32, 5, 3, 0x76CE60, 0xB0EE96, 0.9F),
    CONEJO("Coco", "Conejo", null, "§6", "Rapidísima, te da salto y velocidad; encuentra zanahorias y patas de conejo", 26, 0.42, 3, 1, 0x966842, 0xF4E8D2, 0.88F),
    PANDA("Mei", "Panda", null, "§f", "Fuerte y tierna: golpes que empujan, junta bambú y caña, y estornuda regalos", 60, 0.28, 9, 6, 0xF2F2F4, 0x242228, 1.05F),
    CABALLO("Brisa", "Caballo", null, "§6", "¡Puedes montarla! (clic derecho) Muy rápida; te da velocidad", 50, 0.36, 5, 4, 0x7C502E, 0xD8BE92, 1.1F),
    ABEJA("Miel", "Abeja", null, "§e", "Poliniza tus cultivos (crecen), te da miel y panales, y pica con veneno", 24, 0.36, 4, 1, 0xFAC832, 0x221E1E, 0.86F),
    PIGLIN("Aurea", "Piglin", null, "§6", "Trueque: dale oro y te da tesoros del Nether; espada de oro", 40, 0.32, 8, 5, 0xF0C43C, 0x784A2C, 1.0F),
    WITHER("Nyx", "Esqueleto del Nether", null, "§8", "Golpes con marchitamiento, inmune al fuego; te da carbón y huesos", 60, 0.33, 12, 6, 0x2C2C32, 0xE2DED2, 1.2F),
    BLAZE("Flama", "Blaze", null, "§6", "Dispara bolas de fuego, inmune al fuego, te da resistencia al fuego y varas de blaze", 40, 0.32, 6, 4, 0xFCCE3C, 0xF48C1E, 1.0F),
    GHAST("Nube", "Ghast", null, "§f", "Grandes bolas de fuego explosivas, flota, te da caída lenta, lágrimas y pólvora", 40, 0.3, 5, 2, 0xF8F8FA, 0xB0B0BA, 1.1F),
    DINA("Dina", "TNT (hermana mayor)", null, "§c", "Explosión enorme (más que Cree) y lanza explosivos; solo daña monstruos", 40, 0.32, 6, 3, 0xD62C26, 0xF6F0EC, 0.96F),
    MITA("Mita", "TNT (hermana menor)", null, "§c", "Lanza explosivos sin parar y explota a lo grande; solo daña monstruos", 36, 0.34, 5, 3, 0xE23C2C, 0x1E1C20, 0.94F),
    WARDEN("Umbra", "Warden", null, "§3", "Guardiana máxima: rayo sónico, oscuridad a tus enemigos, te reduce el daño y aparece si te atacan", 300, 0.33, 30, 20, 0x16282E, 0x3CE6E2, 1.55F),
    // ---- sorpresas (se desbloquean al completar la colección)
    ZORRO("Kitsu", "Zorro", null, "§6", "Sorpresa: rapidísima, salta sobre los monstruos y te trae bayas y objetos", 34, 0.4, 7, 2, 0xE67C2C, 0xFAF6F0, 0.93F),
    AJOLOTE("Ondina", "Ajolote", null, "§d", "Sorpresa: respiración acuática y regeneración en el agua, caza ahogados y guardianes", 34, 0.34, 6, 2, 0xF6B0CC, 0xDE5092, 0.9F),
    ALLAY("Lumi", "Allay", null, "§b", "Sorpresa: recoge objetos de muy lejos, vuela, y su música te cura", 26, 0.38, 2, 1, 0x78D2FA, 0xBEF0FF, 0.75F),
    RANA("Croa", "Rana", null, "§2", "Sorpresa: lengua que atrapa monstruos pequeños, te da salto y luces de rana", 30, 0.34, 6, 2, 0x6EB046, 0xF8D246, 0.9F);

    public final String display, role, color, job;
    @Nullable public final Block block;
    public final double health, speed, attack, armor;
    public final int eggA, eggB;
    public final float scale;

    WaifuKind(String display, String role, @Nullable Block block, String color, String job, double health, double speed, double attack, double armor,
              int eggA, int eggB, float scale) {
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
        this.scale = scale;
    }

    /** Orden en la colección: mesas y cama primero, luego granja, compañeras, monstruos, Nether, especiales y sorpresas. */
    public static final WaifuKind[] ORDER = {KOZUE, HOMURA, KURA, SHIORI, CAMA, OVEJA, VACA, CERDO, GALLINA, CONEJO, ABEJA, PANDA, CABALLO, PERRO, GATO, GOLEM,
            ALDEANA, SLIME, ZOMBIE, ESQUELETO, CREEPER, ARANA, BRUJA, ENDERMAN, PIGLIN, WITHER, BLAZE, GHAST, DINA, MITA, WARDEN, ZORRO, AJOLOTE, ALLAY, RANA};

    public String id() {
        return name().toLowerCase();
    }

    /** Las de las mesas y la cama: se pueden volver a convertir en su bloque. */
    public boolean station() {
        return block != null || this == CAMA;
    }

    /** Se desbloquean al completar la colección. */
    public boolean surprise() {
        return this == ZORRO || this == AJOLOTE || this == ALLAY || this == RANA;
    }

    /** Pelean cuerpo a cuerpo (cazan monstruos cuando te siguen o trabajan). */
    public boolean fighter() {
        return switch (this) {
            case PERRO, GOLEM, ZOMBIE, ENDERMAN, ARANA, PANDA, PIGLIN, WITHER, WARDEN, ZORRO, AJOLOTE, SLIME -> true;
            default -> false;
        };
    }

    /** Atacan desde lejos (no persiguen cuerpo a cuerpo). */
    public boolean ranged() {
        return this == SHIORI || this == ESQUELETO || this == BRUJA || this == BLAZE || this == GHAST || this == DINA || this == MITA;
    }

    public boolean fireproof() {
        return this == BLAZE || this == WITHER || this == GHAST || this == PIGLIN || this == HOMURA || this == WARDEN;
    }

    public boolean noFall() {
        return switch (this) {
            case GALLINA, GATO, ARANA, ENDERMAN, SLIME, CONEJO, BLAZE, GHAST, ALLAY, RANA, ZORRO, WARDEN, GOLEM -> true;
            default -> false;
        };
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
        if (b instanceof net.minecraft.world.level.block.BedBlock) return CAMA;
        if (b == Blocks.TNT) return DINA;
        return null;
    }

    @Nullable
    public static WaifuKind fromEntity(Entity e) {
        if (e instanceof net.minecraft.world.entity.monster.warden.Warden) return WARDEN;
        if (e instanceof WitherSkeleton) return WITHER;
        if (e instanceof Blaze) return BLAZE;
        if (e instanceof Ghast) return GHAST;
        if (e instanceof Slime) return SLIME;
        if (e instanceof Rabbit) return CONEJO;
        if (e instanceof Panda) return PANDA;
        if (e instanceof net.minecraft.world.entity.animal.horse.AbstractHorse) return CABALLO;
        if (e instanceof Bee) return ABEJA;
        if (e instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin) return PIGLIN;
        if (e instanceof Fox) return ZORRO;
        if (e instanceof net.minecraft.world.entity.animal.axolotl.Axolotl) return AJOLOTE;
        if (e instanceof net.minecraft.world.entity.animal.allay.Allay) return ALLAY;
        if (e instanceof net.minecraft.world.entity.animal.frog.Frog) return RANA;
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
