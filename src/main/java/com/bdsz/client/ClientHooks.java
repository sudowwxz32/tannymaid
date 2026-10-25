package com.bdsz.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Llamadas que solo existen en el cliente. */
public final class ClientHooks {
    private ClientHooks() {}

    /** Abre la pantalla de apariencia (género, peinado y cola). */
    public static void openAppearance(boolean online) {
        Minecraft.getInstance().setScreen(new AppearanceScreen(online));
    }

    /** Recalcula el tamaño del jugador (al entrar o salir del Modo Ozaru). */
    public static void refreshSize(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(id);
        if (e != null) e.refreshDimensions();
    }
}
