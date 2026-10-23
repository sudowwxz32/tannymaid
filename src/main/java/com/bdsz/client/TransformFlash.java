package com.bdsz.client;

import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Destello de luz en la pantalla cuando alguien cercano termina de transformarse. */
public class TransformFlash implements IGuiOverlay {
    private static final float TICKS = 14.0F;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partial, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        float best = 0;
        int color = 0xFFFFFF;
        for (Player p : mc.level.players()) {
            double d = p.distanceTo(mc.player);
            if (d > 40) continue;
            long since = ClientStates.ticksSinceTransform(p.getId());
            if (since > TICKS) continue;
            SaiyanState s = ClientStates.get(p.getId());
            Form f = Form.byId(s.selected);
            float strength = f.calm() ? 0.18F : 0.35F + f.drama() * 0.4F;
            float fade = 1.0F - (since + partial) / TICKS;
            float a = Math.max(0, fade * fade * strength * (float) (1.0 - d / 40.0));
            if (a > best) {
                best = a;
                int c = f == Form.BASE ? 0xFF5040 : f == Form.SSJ4 ? 0xFF6A40 : f.auraColor;
                // mezcla con blanco: el centro del destello es casi blanco
                int r = ((c >> 16) & 255) / 2 + 128, gg = ((c >> 8) & 255) / 2 + 128, b = (c & 255) / 2 + 128;
                color = (r << 16) | (gg << 8) | b;
            }
        }
        if (best <= 0.01F) return;
        g.fill(0, 0, width, height, ((int) (best * 255) << 24) | color);
    }
}
