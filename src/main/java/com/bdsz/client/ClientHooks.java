package com.bdsz.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Llamadas que solo existen en el cliente. */
public final class ClientHooks {
    private ClientHooks() {}

    /** Recalcula el tamaño del jugador (al entrar o salir del Modo Ozaru). */
    public static void refreshSize(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(id);
        if (e != null) e.refreshDimensions();
    }
}
