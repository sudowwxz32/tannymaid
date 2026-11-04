package com.linterna.data;

import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

/** Silueta verde de resalte (el contorno brillante que se ve incluso detrás de las paredes). */
public final class Glow {
    private Glow() {}

    private static final String TEAM = "linterna_verde";

    private static PlayerTeam team(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        PlayerTeam t = sb.getPlayerTeam(TEAM);
        if (t == null) {
            t = sb.addPlayerTeam(TEAM);
            t.setColor(ChatFormatting.GREEN);
            t.setCollisionRule(PlayerTeam.CollisionRule.ALWAYS);
        }
        return t;
    }

    public static void on(Entity e) {
        if (e.getServer() == null) return;
        Scoreboard sb = e.getServer().getScoreboard();
        // si ya pertenece a otro equipo no se lo cambiamos (brilla con el color de su equipo)
        PlayerTeam cur = sb.getPlayersTeam(e.getScoreboardName());
        if (cur == null) sb.addPlayerToTeam(e.getScoreboardName(), team(e.getServer()));
        e.setGlowingTag(true);
    }

    public static void off(Entity e) {
        if (e.getServer() == null) return;
        Scoreboard sb = e.getServer().getScoreboard();
        PlayerTeam cur = sb.getPlayersTeam(e.getScoreboardName());
        if (cur != null && TEAM.equals(cur.getName())) sb.removePlayerFromTeam(e.getScoreboardName(), cur);
        e.setGlowingTag(false);
    }
}
