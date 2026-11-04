package com.linterna.client;

import com.linterna.data.LanternState;
import com.linterna.network.ClientStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Barra de energía del anillo y poderes activos (abajo a la izquierda). */
public class LanternHud implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partial, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        LanternState s = ClientStates.get(mc.player.getId());
        if (!s.bearer()) return;
        int x = 8, y = h - 30, bw = 90;
        float k = s.charge / (float) LanternState.MAX_CHARGE;
        g.fill(x - 1, y - 1, x + bw + 1, y + 7, 0xC0061A0A);
        int col = k > 0.25F ? 0xFF4CFF6A : k > 0.1F ? 0xFFE8E040 : 0xFFFF5040;
        g.fill(x, y, x + (int) (bw * k), y + 6, col);
        g.drawString(mc.font, "◎ Anillo " + (int) Math.ceil(k * 100) + "%" + (s.recharge > 0 ? " §a(recargando)" : ""), x, y - 11, 0xFFB8FFC4, true);
        StringBuilder on = new StringBuilder();
        if (s.on(LanternState.FLY)) on.append("Vuelo ");
        if (s.on(LanternState.ARMOR)) on.append("Armadura ");
        if (s.on(LanternState.PICK)) on.append("Pico ");
        if (s.on(LanternState.GHOST)) on.append("Intangible ");
        if (s.on(LanternState.GUNS)) on.append("Armas ");
        if (s.on(LanternState.SUIT)) on.append("Traje ");
        if (on.length() > 0) g.drawString(mc.font, "§2" + on.toString().trim(), x, y + 9, 0xFFFFFFFF, true);
    }
}
