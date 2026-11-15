package com.totallywaifu.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Las cuatro waifus: de qué bloque nacen, su nombre, su papel y su color de mensaje. */
public enum WaifuKind {
    KOZUE("Kozue", "Mesa de crafteo", Blocks.CRAFTING_TABLE, "§6",
            "Craftea contigo y por ti (pedidos), tala árboles y mina menas"),
    HOMURA("Homura", "Horno", Blocks.FURNACE, "§c",
            "Su horno cocina siempre (el doble de rápido), busca carbón y caza animales para cocinarlos"),
    KURA("Kura", "Cofre", Blocks.CHEST, "§e",
            "Cofre doble con piernas: recoge todo lo tirado, vacía las mochilas de las demás y ordena"),
    SHIORI("Shiori", "Mesa de encantamientos", Blocks.ENCHANTING_TABLE, "§d",
            "Encanta con poder máximo, ataca monstruos con magia, guarda su experiencia y repara tu equipo");

    public final String display, role, color, job;
    public final Block block;

    WaifuKind(String display, String role, Block block, String color, String job) {
        this.display = display;
        this.role = role;
        this.block = block;
        this.color = color;
        this.job = job;
    }

    public String id() {
        return name().toLowerCase();
    }

    public static WaifuKind byId(int i) {
        WaifuKind[] v = values();
        return i >= 0 && i < v.length ? v[i] : KOZUE;
    }

    public static WaifuKind fromBlock(Block b) {
        if (b == Blocks.CRAFTING_TABLE) return KOZUE;
        if (b == Blocks.FURNACE) return HOMURA;
        if (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST) return KURA;
        if (b == Blocks.ENCHANTING_TABLE) return SHIORI;
        return null;
    }
}
