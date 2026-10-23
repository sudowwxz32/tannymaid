package com.bdsz.client;

import com.bdsz.entity.DbzEnemy;
import com.bdsz.network.ClientStates;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

/** Sentir el ki (tecla J): un saiyajin ve a los enemigos cercanos a través de las paredes. */
public final class KiSense {
    public static final double RANGE = 48.0;
    private static boolean enabled = true;

    private KiSense() {}

    public static void toggle() {
        enabled = !enabled;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(enabled ? "§bSentir ki: activado §7(ves a los enemigos cercanos)" : "§7Sentir ki: desactivado"), true);
        }
    }

    public static boolean shouldGlow(Entity e) {
        if (!enabled) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || e == mc.player || !(e instanceof LivingEntity le) || !le.isAlive()) return false;
        if (!(e instanceof DbzEnemy) && !(e instanceof Enemy)) return false;
        if (!ClientStates.get(mc.player.getId()).saiyan) return false;
        return e.distanceToSqr(mc.player) < RANGE * RANGE;
    }
}
