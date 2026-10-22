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

/**
 * Panel saiyajin compacto (esquina superior izquierda): nombre de la fase, vida, ki y tiempo en barras finas de color.
 * Cuando no pasa nada (sin transformar, ki lleno) se vuelve más pequeño y transparente para no estorbar.
 */
public class SaiyanHud implements IGuiOverlay {
    private static final float TXT = 0.72F;
    private float shownHp = -1, shownKi = -1, fade = 1.0F;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partial, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.options.hideGui || p.isSpectator() || mc.screen instanceof PowerScreen) return;
        SaiyanState s = ClientStates.get(p.getId());
        if (!s.saiyan) return;
        Font font = mc.font;
        Form f = s.activeForm();
        int accent = s.ozaru ? 0xFFC8863C : f == Form.BASE ? (s.activeKaioken() > 0 ? 0xFFFF3A2A : 0xFF7FD8FF) : 0xFF000000 | f.color;
        if (f == Form.SSJ4 && !s.ozaru) accent = 0xFFE8442A;     // cabello negro: se usa el rojo del pelaje

        boolean busy = s.transformed || s.ozaru || s.powering > 0 || s.charging || s.cooldown > 0 || s.dragonFist > 0
                || s.ki < s.maxKi() || p.getHealth() < p.getMaxHealth();
        fade += ((busy ? 1.0F : 0.55F) - fade) * 0.08F;
        int bgA = (int) (0x70 * fade);

        int x = 4, y = 4, w = 112;
        boolean showTime = s.transformed || s.powering > 0 || s.cooldown > 0;
        int rows = 2 + (showTime ? 1 : 0);
        int h = 12 + rows * 8 + 2;
        g.fill(x, y, x + w, y + h, bgA << 24);
        g.fill(x, y, x + 2, y + h, withAlpha(accent, (int) (255 * fade)));            // franja con el color de la fase

        // título: nombre de la fase + Kaioken
        String title = s.ozaru ? "Ozaru" : f == Form.BASE ? "Saiyajin" : f.display;
        small(g, font, title, x + 5, y + 3, withAlpha(accent, (int) (255 * Math.max(0.6F, fade))));
        if (s.activeKaioken() > 0) {
            String kk = "x" + s.activeKaioken();
            small(g, font, kk, x + w - 3 - (int) (font.width(kk) * TXT), y + 3, 0xFFFF5040);
        } else if (s.flight) {
            String fl = p.getAbilities().flying ? "vuelo" : "vuelo listo";
            small(g, font, fl, x + w - 3 - (int) (font.width(fl) * TXT), y + 3, p.getAbilities().flying ? 0xFF7FE0FF : 0xFF8A8A8A);
        }

        int by = y + 12;
        // vida
        float hp = p.getHealth(), maxHp = p.getMaxHealth();
        shownHp = shownHp < 0 ? hp : shownHp + (hp - shownHp) * 0.2F;
        bar(g, font, x + 5, by, w - 8, 0xFF8E1220, 0xFFFF4A5A, shownHp / maxHp, "❤", String.format("%.0f", hp), fade);
        by += 8;
        // ki
        int maxKi = Math.max(1, s.maxKi());
        shownKi = shownKi < 0 ? s.ki : shownKi + (s.ki - shownKi) * 0.25F;
        String kiTxt = s.infiniteKi() ? "∞" : String.valueOf(s.ki);
        bar(g, font, x + 5, by, w - 8, darken(accent), accent, s.infiniteKi() ? 1.0F : shownKi / maxKi, "⚡", kiTxt, fade);
        by += 8;
        // tiempo de transformación
        if (showTime) {
            String txt;
            float pct;
            int dark = 0xFF8A6012, light = 0xFFFFD34A;
            if (s.powering > 0) {
                pct = 1.0F - (float) s.powering / Math.max(1, s.poweringTotal);
                txt = Math.round(pct * 100) + "%";
                dark = darken(accent);
                light = accent;
            } else if (s.transformed && s.timerMax < 0) {
                txt = "∞";
                pct = 1.0F;
            } else if (s.transformed && s.timerMax > 0) {
                int sec = s.timer / 20;
                txt = String.format("%d:%02d", sec / 60, sec % 60);
                pct = (float) s.timer / s.timerMax;
            } else {
                txt = (s.cooldown / 20) + "s";
                pct = 0;
                dark = 0xFF5A5A5A;
                light = 0xFF8A8A8A;
            }
            bar(g, font, x + 5, by, w - 8, dark, light, pct, s.cooldown > 0 && !s.transformed ? "✖" : "⌛", txt, fade);
        }

        // avisos pequeños debajo del panel (solo cuando importan)
        int ny = y + h + 2;
        if (s.powering > 0) {
            small(g, font, "¡Transformándose!", x + 2, ny, accent);
            ny += 8;
        }
        if (s.charging) {
            small(g, font, "Recargando ki (H)", x + 2, ny, 0xFF7FE0FF);
            ny += 8;
        }
        if (s.dragonFist > 0) {
            small(g, font, "Puño del Dragón: ¡golpea! " + s.dragonFist / 20 + "s", x + 2, ny, 0xFFFFB830);
            ny += 8;
        } else if (s.dragonCooldown > 0) {
            int sec = s.dragonCooldown / 20;
            small(g, font, String.format("Puño del Dragón %d:%02d", sec / 60, sec % 60), x + 2, ny, 0xFF8A8A8A);
        }
    }

    private static void small(GuiGraphics g, Font font, String text, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(TXT, TXT, 1.0F);
        g.drawString(font, text, 0, 0, color, true);
        g.pose().popPose();
    }

    private static void bar(GuiGraphics g, Font font, int x, int y, int w, int dark, int light, float pct, String icon, String value, float fade) {
        pct = Math.max(0, Math.min(1, pct));
        int iconW = 8, valW = 22;
        small(g, font, icon, x, y, withAlpha(light, (int) (255 * Math.max(0.7F, fade))));
        int bx = x + iconW, bw = w - iconW - valW;
        g.fill(bx - 1, y, bx + bw + 1, y + 6, 0xC0000000);
        int fw = (int) (bw * pct);
        g.fill(bx, y + 1, bx + fw, y + 5, dark);
        g.fill(bx, y + 1, bx + fw, y + 3, light);
        if (fw > 0) g.fill(bx + fw - 1, y + 1, bx + fw, y + 5, 0xFFFFFFFF);          // brillo en el borde
        small(g, font, value, bx + bw + 3, y, 0xFFEFEFEF);
    }

    static int darken(int c) {
        int r = ((c >> 16) & 255) * 55 / 100, gg = ((c >> 8) & 255) * 55 / 100, b = (c & 255) * 55 / 100;
        return 0xFF000000 | (r << 16) | (gg << 8) | b;
    }

    static int withAlpha(int c, int a) {
        return (Math.max(0, Math.min(255, a)) << 24) | (c & 0xFFFFFF);
    }
}
