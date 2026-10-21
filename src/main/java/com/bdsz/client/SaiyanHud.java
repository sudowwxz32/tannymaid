package com.bdsz.client;

import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Interfaz saiyajin: panel con barras de vida, ki y tiempo de transformación. */
public class SaiyanHud implements IGuiOverlay {
    private float shownHp = -1, shownKi = -1;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partial, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.options.hideGui || p.isSpectator()) return;
        SaiyanState s = ClientStates.get(p.getId());
        if (!s.saiyan) return;
        Font font = mc.font;
        Form f = s.activeForm();
        int accent = f == Form.BASE ? (s.activeKaioken() > 0 ? 0xFFFF3A2A : 0xFF7FD8FF) : 0xFF000000 | f.color;

        int x = 6, y = 6, w = 168;
        int h = 66;
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x90000000);
        g.fill(x - 2, y - 2, x + 3, y + h + 2, accent);                                  // franja de color de la fase
        String title = (f == Form.BASE ? "§fSaiyajin" : "§l" + f.display) + (s.activeKaioken() > 0 ? " §c+ Kaioken x" + s.activeKaioken() : "");
        g.drawString(font, title, x + 7, y + 1, accent, true);

        // vida
        float hp = p.getHealth(), maxHp = p.getMaxHealth();
        shownHp = shownHp < 0 ? hp : shownHp + (hp - shownHp) * 0.2F;
        bar(g, font, x + 7, y + 14, w - 9, 0xFFB01828, 0xFFFF4A5A, shownHp / maxHp, String.format("❤ %.0f / %.0f", hp, maxHp));

        // ki
        int maxKi = Math.max(1, s.maxKi());
        shownKi = shownKi < 0 ? s.ki : shownKi + (s.ki - shownKi) * 0.25F;
        int kiDark = f == Form.BASE ? 0xFF1A6FB0 : darken(accent);
        bar(g, font, x + 7, y + 30, w - 9, kiDark, accent, shownKi / maxKi, "Ki " + s.ki + " / " + maxKi);

        // tiempo de transformación
        String timeText;
        float pct;
        if (s.transformed && s.timerMax > 0) {
            int sec = s.timer / 20;
            timeText = String.format("Transformación %d:%02d", sec / 60, sec % 60);
            pct = (float) s.timer / s.timerMax;
        } else if (s.cooldown > 0) {
            timeText = "Agotado " + (s.cooldown / 20) + " s";
            pct = 0;
        } else {
            timeText = "Listo para transformarte (P)";
            pct = 1;
        }
        bar(g, font, x + 7, y + 46, w - 9, 0xFFA0761A, 0xFFFFD34A, pct, timeText);

        if (s.flight) {
            String fl = p.getAbilities().flying ? "§b✈ Volando" : "§7✈ Vuelo listo";
            g.drawString(font, fl, x + w - font.width(fl.substring(2)) - 2, y + 1, 0xFFFFFF, true);
        }
    }

    private static void bar(GuiGraphics g, Font font, int x, int y, int w, int dark, int light, float pct, String text) {
        pct = Math.max(0, Math.min(1, pct));
        int h = 12;
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.fill(x, y, x + w, y + h, 0xFF2A2A2A);
        int fw = (int) (w * pct);
        g.fill(x, y, x + fw, y + h, dark);
        g.fill(x, y, x + fw, y + h / 2, light);
        g.fill(x, y, x + fw, y + 1, 0x60FFFFFF);
        g.drawCenteredString(font, text, x + w / 2, y + 2, 0xFFFFFF);
    }

    private static int darken(int c) {
        int r = ((c >> 16) & 255) * 6 / 10, gg = ((c >> 8) & 255) * 6 / 10, b = (c & 255) * 6 / 10;
        return 0xFF000000 | (r << 16) | (gg << 8) | b;
    }
}
