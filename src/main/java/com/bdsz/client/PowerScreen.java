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
 * Menú de transformaciones (tecla K): compacto y con colores.
 *  - Clic en una fase para elegirla · clic en una ficha de Kaioken · botones de abajo para transformarte, aura, vuelo y Ozaru.
 *  - Teclado: ↑ ↓ fase · ← → Kaioken · Enter transformarse · K o Esc para cerrar.
 */
public class PowerScreen extends Screen {
    private static final int W = 300, ROW = 12;
    /** Columnas de fases: izquierda bloques 1-3, derecha bloques 4 en adelante. */
    private static final Form[][] COLS = {
            {Form.BASE, Form.FALSE_SSJ, Form.SSJ1, Form.SSJ2, Form.SSJ3, Form.GOD, Form.BLUE, Form.BLUE_EVO, Form.ULTRA},
            {Form.SSJ4, Form.ULTRA_EGO, Form.BEAST, Form.ROSE, Form.LEGENDARY, Form.LEGEND1, Form.LEGEND2, Form.LEGEND3}};
    private int form, kaioken;
    private int left, top, h;
    private String hint = "";

    public PowerScreen() {
        super(Component.literal("Transformaciones"));
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
        h = 24 + 9 * ROW + 4 * 9 + 8 + 20 + 26 + 16 + 14;
        left = (width - W) / 2;
        top = Math.max(4, (height - h) / 2);
    }

    // ------------------------------------------------------------ dibujo

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        SaiyanState s = state();
        Form sel = Form.byId(form);
        int accent = 0xFF000000 | (sel == Form.BASE ? 0x7FD8FF : sel == Form.SSJ4 ? 0xE8442A : sel == Form.LEGEND1 ? 0x3CFF4A : sel.color);
        // panel
        g.fill(left - 1, top - 1, left + W + 1, top + h + 1, SaiyanHud.withAlpha(accent, 200));
        g.fill(left, top, left + W, top + h, 0xF0101216);
        g.fill(left, top, left + W, top + 18, 0xFF1C2028);
        g.fill(left, top + 17, left + W, top + 18, accent);
        g.drawString(font, "§lTransformaciones", left + 8, top + 5, 0xFFFFFFFF, true);
        String status = s.ozaru ? "§6Ozaru" : s.transformed ? "§aTransformado" : s.powering > 0 ? "§eCargando..." : "§7Estado base";
        g.drawString(font, status, left + W - 8 - font.width(status), top + 5, 0xFFFFFFFF, true);

        hint = "";
        // ---- columnas de fases
        int cy = top + 24;
        for (int c = 0; c < 2; c++) {
            int x = left + 8 + c * 146, y = cy;
            int lastTier = -1;
            for (Form f : COLS[c]) {
                if (f.tier() != lastTier) {
                    lastTier = f.tier();
                    String head = Form.blockName(f.tier()).toUpperCase(java.util.Locale.ROOT);
                    small(g, head, x + 1, y + 1, f.tier() >= 6 ? 0xFFE070FF : 0xFF8A93A6);
                    y += 9;
                }
                drawForm(g, f, x, y, 138, mx, my, s);
                y += ROW;
            }
        }

        // ---- Kaioken
        int ky = top + 24 + 9 * ROW + 4 * 9 + 8;
        g.drawString(font, "§c§lKaioken", left + 8, ky + 3, 0xFFFFFFFF, true);
        for (int i = 0; i <= 10; i++) {
            int kx = left + 62 + i * 21, lvl = i * 10;
            boolean on = lvl == kaioken, hover = in(mx, my, kx, ky, 19, 14);
            int col = i == 0 ? 0xFF4A4F5A : lerpColor(0xFFFFB040, 0xFFE01010, (i - 1) / 9.0F);
            g.fill(kx, ky, kx + 19, ky + 14, on ? col : SaiyanHud.withAlpha(col, hover ? 150 : 80));
            if (on) outline(g, kx, ky, 19, 14, 0xFFFFFFFF);
            String t = i == 0 ? "—" : String.valueOf(lvl);
            small(g, t, kx + 10 - (int) (font.width(t) * 0.72F / 2), ky + 4, on ? 0xFF000000 : 0xFFFFFFFF);
            if (hover) hint = i == 0 ? "Sin Kaioken" : "Kaioken x" + lvl + ": más fuerza, menos tiempo";
        }

        // ---- estadísticas de la selección
        int sy = ky + 20;
        SaiyanState pv = new SaiyanState();
        pv.saiyan = true;
        pv.selected = form;
        pv.kaioken = kaioken;
        pv.transformed = form != 0 || kaioken > 0;
        int limit = SaiyanState.timeLimit(form, kaioken, s.mastery);
        int secs = Math.max(0, limit) / 20;
        String time = limit < 0 ? "∞" : String.format("%d:%02d", secs / 60, secs % 60);
        String ki = sel.infiniteKi() ? "∞" : String.valueOf(pv.maxKi());
        g.fill(left + 8, sy, left + W - 8, sy + 20, 0xFF1A1E26);
        int sx = left + 14;
        sx = stat(g, sx, sy + 6, "⚔", String.format("x%.1f", pv.damageMultiplier()), 0xFFFF8A5A);
        sx = stat(g, sx, sy + 6, "◈", Math.round(pv.resistance() * 100) + "%", 0xFF9AD0FF);
        sx = stat(g, sx, sy + 6, "❤", "+" + pv.extraHealth() / 2, 0xFFFF5A6A);
        sx = stat(g, sx, sy + 6, "»", "+" + Math.round(pv.speedBonus() * 100) + "%", 0xFF8AFFB0);
        sx = stat(g, sx, sy + 6, "⚡", ki, 0xFF7FE0FF);
        stat(g, sx, sy + 6, "⌛", time, 0xFFFFD34A);

        // ---- botones
        int bty = sy + 26;
        button(g, left + 8, bty, 92, s.transformed ? "Destransformar" : "Transformar", "P", accent, mx, my);
        button(g, left + 104, bty, 62, "Aura " + (s.aura ? "sí" : "no"), "U", s.aura ? 0xFF7FE0FF : 0xFF5A6070, mx, my);
        button(g, left + 170, bty, 58, "Vuelo " + (s.flight ? "sí" : "no"), "I", s.flight ? 0xFF7FE0FF : 0xFF5A6070, mx, my);
        button(g, left + 232, bty, 60, "Ozaru", "O", s.ozaruUnlocked() ? 0xFFC8863C : 0xFF3A3E48, mx, my);
        if (in(mx, my, left + 232, bty, 60, 16) && !s.ozaruUnlocked()) hint = "Ozaru: se desbloquea con el Dominio Total";

        // ---- ayuda
        String help = hint.isEmpty() ? "↑↓ fase   ←→ Kaioken   Enter transformarse   K cerrar" : hint;
        small(g, help, left + W / 2 - (int) (font.width(help) * 0.72F / 2), top + h - 9, hint.isEmpty() ? 0xFF6A7386 : 0xFFFFE08A);
    }

    private void drawForm(GuiGraphics g, Form f, int x, int y, int w, int mx, int my, SaiyanState s) {
        boolean open = s.unlocked(f), on = f.ordinal() == form, hover = in(mx, my, x, y, w, ROW - 1);
        int col = 0xFF000000 | (f == Form.BASE ? 0xBFE6FF : f == Form.SSJ4 ? 0xE8442A : f == Form.LEGEND1 ? 0x3CFF4A : f.color);
        if (on) {
            g.fill(x, y, x + w, y + ROW - 1, SaiyanHud.withAlpha(col, 90));
            g.fill(x, y, x + 2, y + ROW - 1, col);
        } else if (hover && open) {
            g.fill(x, y, x + w, y + ROW - 1, 0x30FFFFFF);
        }
        // muestra de color
        g.fill(x + 5, y + 2, x + 12, y + 9, open ? col : 0xFF3A3E48);
        outline(g, x + 5, y + 2, 7, 7, 0x80000000);
        String name = f == Form.BASE ? "Solo Kaioken" : f.display.replace("Super Saiyajin", "SSJ");
        g.drawString(font, name, x + 16, y + 2, open ? (on ? 0xFFFFFFFF : 0xFFD8DCE4) : 0xFF5A6070, on);
        if (!open) {
            lock(g, x + w - 10, y + 2);
            if (hover) hint = "Bloqueada: " + unlockText(f);
        } else if (s.transformed && s.selected == f.ordinal()) {
            g.fill(x + w - 8, y + 4, x + w - 4, y + 8, 0xFF5AFF7A);      // activa ahora
        }
    }

    private static String unlockText(Form f) {
        return switch (f.tier()) {
            case 2 -> "Lista de Entrenamiento I";
            case 3 -> "Lista de Entrenamiento II";
            case 4 -> "Lista de Entrenamiento III";
            case 5 -> "Lista de Entrenamiento IV (Bloque Especial)";
            case 6 -> "Lista de Entrenamiento V (Bloque Especial 2)";
            case 7 -> "Lista de Entrenamiento VI (Bloque Especial 3) + Dominio Total";
            default -> "";
        };
    }

    private int stat(GuiGraphics g, int x, int y, String icon, String value, int color) {
        g.drawString(font, icon, x, y, color, true);
        int vx = x + font.width(icon) + 2;
        small(g, value, vx, y + 1, 0xFFEFEFEF);
        return vx + (int) (font.width(value) * 0.72F) + 9;
    }

    private void button(GuiGraphics g, int x, int y, int w, String text, String key, int color, int mx, int my) {
        boolean hover = in(mx, my, x, y, w, 16);
        g.fill(x, y, x + w, y + 16, SaiyanHud.withAlpha(color, hover ? 210 : 140));
        outline(g, x, y, w, 16, hover ? 0xFFFFFFFF : SaiyanHud.withAlpha(color, 255));
        g.fill(x + 3, y + 3, x + 13, y + 13, 0x90000000);
        g.drawString(font, key, x + 8 - font.width(key) / 2, y + 4, 0xFFFFE08A, false);
        small(g, text, x + 17, y + 5, 0xFFFFFFFF);
    }

    private void lock(GuiGraphics g, int x, int y) {
        g.fill(x + 1, y, x + 5, y + 1, 0xFF8A8A8A);
        g.fill(x, y + 1, x + 1, y + 3, 0xFF8A8A8A);
        g.fill(x + 5, y + 1, x + 6, y + 3, 0xFF8A8A8A);
        g.fill(x - 1, y + 3, x + 7, y + 8, 0xFF8A8A8A);
        g.fill(x + 2, y + 4, x + 4, y + 6, 0xFF2A2A2A);
    }

    private void small(GuiGraphics g, String text, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.72F, 0.72F, 1.0F);
        g.drawString(font, text, 0, 0, color, true);
        g.pose().popPose();
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static int lerpColor(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int gg = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bb = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return 0xFF000000 | (r << 16) | (gg << 8) | bb;
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ------------------------------------------------------------ interacción

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        SaiyanState s = state();
        int cy = top + 24;
        for (int c = 0; c < 2; c++) {
            int x = left + 8 + c * 146, y = cy, lastTier = -1;
            for (Form f : COLS[c]) {
                if (f.tier() != lastTier) { lastTier = f.tier(); y += 9; }
                if (in(mx, my, x, y, 138, ROW - 1)) {
                    if (s.unlocked(f)) { form = f.ordinal(); send(); click(); }
                    return true;
                }
                y += ROW;
            }
        }
        int ky = top + 24 + 9 * ROW + 4 * 9 + 8;
        for (int i = 0; i <= 10; i++) {
            if (in(mx, my, left + 62 + i * 21, ky, 19, 14)) { kaioken = i * 10; send(); click(); return true; }
        }
        int bty = ky + 20 + 26;
        if (in(mx, my, left + 8, bty, 92, 16)) { action(ActionPacket.TRANSFORM); onClose(); return true; }
        if (in(mx, my, left + 104, bty, 62, 16)) { action(ActionPacket.AURA); click(); return true; }
        if (in(mx, my, left + 170, bty, 58, 16)) { action(ActionPacket.FLY); click(); return true; }
        if (in(mx, my, left + 232, bty, 60, 16)) { if (s.ozaruUnlocked()) { action(ActionPacket.OZARU); onClose(); } return true; }
        return super.mouseClicked(mx, my, button);
    }

    /** Rueda del ratón: cambia de fase (con Shift, el Kaioken). */
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (hasShiftDown()) return keyPressed(delta > 0 ? GLFW.GLFW_KEY_LEFT : GLFW.GLFW_KEY_RIGHT, 0, 0);
        return keyPressed(delta > 0 ? GLFW.GLFW_KEY_UP : GLFW.GLFW_KEY_DOWN, 0, 0);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (ClientModEvents.MENU.matches(key, scan)) { onClose(); return true; }
        SaiyanState s = state();
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
            int dir = key == GLFW.GLFW_KEY_UP ? -1 : 1;
            int n = Form.ORDERED.length, cur = Form.byId(form).order();
            for (int i = 1; i < n; i++) {
                Form cand = Form.ORDERED[Math.floorMod(cur + dir * i, n)];
                if (s.unlocked(cand)) { form = cand.ordinal(); send(); click(); break; }
            }
            return true;
        }
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT) {
            kaioken = Math.max(0, Math.min(100, kaioken + (key == GLFW.GLFW_KEY_LEFT ? -10 : 10)));
            send();
            click();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || ClientModEvents.TRANSFORM.matches(key, scan)) {
            action(ActionPacket.TRANSFORM);
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    private void send() {
        Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.SELECT, form, kaioken));
    }

    private static void action(int a) {
        Net.CHANNEL.sendToServer(new ActionPacket(a, 0, 0));
    }

    private static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.6F));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
