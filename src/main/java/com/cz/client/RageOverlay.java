package com.cz.client;

import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ClientStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** La ira tiñe la vista de rojo: late durante la transformación y queda un borde rojo en las fases de bestia. */
public class RageOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partial, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        ZorroState s = ClientStates.get(mc.player.getId());
        if (!s.awakened) return;
        float t = mc.player.tickCount + partial;
        float a = 0;
        if (s.powering > 0) {
            float pr = s.progress();
            a = (0.12F + pr * 0.25F) * (0.6F + 0.4F * Mth.sin(t * 0.5F)) * (0.6F + s.target * 0.15F);   // latidos de ira
        }
        long since = ClientStates.sinceDone(mc.player.getId());
        if (since < 14) a = Math.max(a, (14 - since) / 14.0F * 0.55F);                                   // destello al terminar
        if (a > 0.01F) g.fill(0, 0, w, h, ((int) (a * 255) << 24) | 0x9A0808);
        // borde rojo en las fases de bestia
        Phase v = s.active();
        if (v.beast) {
            int edge = (int) (40 + 20 * Mth.sin(t * 0.2F));
            int c = (edge << 24) | 0x700000;
            int bw = w / 8;
            g.fillGradient(0, 0, w, bw / 2, c, 0);
            g.fillGradient(0, h - bw / 2, w, h, 0, c);
        }
    }
}
