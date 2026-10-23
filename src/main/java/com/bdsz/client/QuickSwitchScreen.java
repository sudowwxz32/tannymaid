package com.bdsz.client;

import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ActionPacket;
import com.bdsz.network.ClientStates;
import com.bdsz.network.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

/**
 * Cambio rápido de transformación (Alt + V).
 * Catálogo de fases en orden y el Kaioken abajo.
 *  - Rueda del ratón: cambia de fase · Shift + rueda (o el ratón sobre el Kaioken): cambia el Kaioken
 *  - Clic derecho / Tab: pasar entre fases y Kaioken
 *  - Clic izquierdo, Enter o V: aplicar (te transforma al instante) · Esc: cancelar
 */
public class QuickSwitchScreen extends Screen {
    private static final int W = 156, ROW = 11;
    private int form, kaioken;
    private boolean kaiFocus;
    private int left, top, listH;

    public QuickSwitchScreen() {
        super(Component.literal("Cambio rápido"));
    }

    private SaiyanState state() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? new SaiyanState() : ClientStates.get(mc.player.getId());
    }

    @Override
    protected void init() {
        SaiyanState s = state();
        form = s.selected;
        kaioken = s.kaioken;
        listH = Form.values().length * ROW;
        int h = 16 + listH + 30 + 12;
        left = width / 2 + 40;
        if (left + W > width - 4) left = width - W - 4;
        top = Math.max(4, (height - h) / 2);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        g.fillGradient(0, 0, width, height, 0x40000000, 0x60000000);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        SaiyanState s = state();
        Form sel = Form.byId(form);
        int accent = 0xFF000000 | (sel == Form.BASE ? 0x7FD8FF : sel == Form.SSJ4 ? 0xE8442A : sel.color);
        int h = 16 + listH + 30 + 12;
        g.fill(left - 1, top - 1, left + W + 1, top + h + 1, SaiyanHud.withAlpha(accent, 220));
        g.fill(left, top, left + W, top + h, 0xEE101216);
        g.drawString(font, "§lCambio rápido", left + 6, top + 4, 0xFFFFFFFF, true);
        small(g, "Alt+V", left + W - 6 - (int) (font.width("Alt+V") * 0.72F), top + 5, 0xFF8A93A6);

        // ---- catálogo de fases en orden
        int y = top + 16;
        for (Form f : Form.values()) {
            boolean open = s.unlocked(f), on = f.ordinal() == form;
            int col = 0xFF000000 | (f == Form.BASE ? 0xBFE6FF : f == Form.SSJ4 ? 0xE8442A : f.color);
            if (on) {
                g.fill(left + 3, y, left + W - 3, y + ROW - 1, SaiyanHud.withAlpha(col, kaiFocus ? 50 : 110));
                g.fill(left + 3, y, left + 5, y + ROW - 1, col);
            } else if (open && in(mx, my, left + 3, y, W - 6, ROW - 1)) {
                g.fill(left + 3, y, left + W - 3, y + ROW - 1, 0x28FFFFFF);
            }
            g.fill(left + 8, y + 2, left + 14, y + 8, open ? col : 0xFF3A3E48);
            String name = f == Form.BASE ? "Base (solo Kaioken)" : f.display.replace("Super Saiyajin", "SSJ");
            small(g, name, left + 18, y + 2, open ? (on ? 0xFFFFFFFF : 0xFFC8CDD6) : 0xFF4E5462);
            if (s.transformed && s.selected == f.ordinal()) g.fill(left + W - 9, y + 3, left + W - 5, y + 7, 0xFF5AFF7A);
            y += ROW;
        }

        // ---- Kaioken
        int ky = top + 16 + listH + 4;
        small(g, "Kaioken", left + 6, ky, kaiFocus ? 0xFFFF6A50 : 0xFFAA5A50);
        int cy = ky + 9;
        for (int i = 0; i <= 10; i++) {
            int kx = left + 5 + i * 13, lvl = i * 10;
            boolean on = lvl == kaioken;
            int col = i == 0 ? 0xFF4A4F5A : lerp(0xFFFFB040, 0xFFE01010, (i - 1) / 9.0F);
            g.fill(kx, cy, kx + 12, cy + 12, on ? col : SaiyanHud.withAlpha(col, 70));
            if (on) outline(g, kx, cy, 12, 12, kaiFocus ? 0xFFFFFFFF : 0xFFB0B0B0);
            String t = i == 0 ? "-" : String.valueOf(i);
            small(g, t, kx + 6 - (int) (font.width(t) * 0.72F / 2), cy + 3, on ? 0xFF000000 : 0xFFDDDDDD);
        }
        String hint = kaiFocus ? "Rueda: Kaioken · Tab: fases · Clic: aplicar" : "Rueda: fase · Shift+rueda: Kaioken · Clic: aplicar";
        small(g, hint, left + W / 2 - (int) (font.width(hint) * 0.6F / 2), top + h - 9, 0xFF6A7386, 0.6F);
    }

    // ------------------------------------------------------------ entrada

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int dir = delta > 0 ? -1 : 1;
        boolean kai = kaiFocus || hasShiftDown() || my > top + 16 + listH;
        if (kai) {
            kaioken = Math.max(0, Math.min(100, kaioken + dir * 10));     // rueda abajo = más Kaioken
        } else {
            moveForm(dir);
        }
        tick();
        return true;
    }

    private void moveForm(int dir) {
        SaiyanState s = state();
        int n = Form.values().length;
        for (int i = 1; i < n; i++) {
            int cand = form + dir * i;
            if (cand < 0 || cand >= n) break;                 // en orden, sin dar la vuelta
            if (s.unlocked(Form.byId(cand))) { form = cand; return; }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) { kaiFocus = !kaiFocus; tick(); return true; }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;
        SaiyanState s = state();
        int y = top + 16;
        for (Form f : Form.values()) {
            if (in(mx, my, left + 3, y, W - 6, ROW - 1)) {
                if (s.unlocked(f)) { form = f.ordinal(); apply(); }
                return true;
            }
            y += ROW;
        }
        int cy = top + 16 + listH + 13;
        for (int i = 0; i <= 10; i++) {
            if (in(mx, my, left + 5 + i * 13, cy, 12, 12)) { kaioken = i * 10; tick(); return true; }
        }
        apply();
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        if (key == GLFW.GLFW_KEY_TAB) { kaiFocus = !kaiFocus; tick(); return true; }
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
            if (kaiFocus) kaioken = Math.max(0, Math.min(100, kaioken + (key == GLFW.GLFW_KEY_UP ? 10 : -10)));
            else moveForm(key == GLFW.GLFW_KEY_UP ? -1 : 1);
            tick();
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT) {
            kaioken = Math.max(0, Math.min(100, kaioken + (key == GLFW.GLFW_KEY_LEFT ? -10 : 10)));
            tick();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_SPACE
                || key == ClientModEvents.QUICK.getKey().getValue()) {
            apply();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    /** Elige la fase y el Kaioken y, si no estabas transformado, te transforma. */
    private void apply() {
        SaiyanState s = state();
        Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.SELECT, form, kaioken));
        if (!s.transformed && s.powering <= 0 && (form != 0 || kaioken > 0)) {
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.TRANSFORM, 0, 0));
        }
        onClose();
    }

    private static void tick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.6F, 0.25F));
    }

    // ------------------------------------------------------------ utilidades

    private void small(GuiGraphics g, String text, int x, int y, int color) {
        small(g, text, x, y, color, 0.72F);
    }

    private void small(GuiGraphics g, String text, int x, int y, int color, float sc) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(sc, sc, 1.0F);
        g.drawString(font, text, 0, 0, color, true);
        g.pose().popPose();
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int gg = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bb = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return 0xFF000000 | (r << 16) | (gg << 8) | bb;
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
