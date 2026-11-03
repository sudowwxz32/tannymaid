package com.bdsz.client.kaori;

import com.bdsz.companion.KaoriHolo;
import com.bdsz.companion.KaoriMode;
import com.bdsz.network.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** Holograma azul de Kaori (tecla J): su mochila y equipo desde cualquier lugar, y teletransportes. */
public class KaoriHoloScreen extends Screen {
    private static final int W = 268, H = 214;
    private static final int CYAN = 0xFF6FE6FF, CYAN_D = 0xFF2A9FD0, GLASS = 0x5A0A3A5C, GLASS2 = 0x7A06243C, TXT = 0xFFBFF4FF;
    private static final String[] FORMS = {"Base", "Super Saiyajin", "Super Saiyajin 2", "Super Saiyajin 2", "Super Saiyajin Dios"};

    private final KaoriHolo.Data d;
    private final long opened = System.currentTimeMillis();

    public KaoriHoloScreen(KaoriHolo.Data d) {
        super(Component.literal("Holograma de Kaori"));
        this.d = d;
    }

    public static void show(KaoriHolo.Data d) {
        Minecraft.getInstance().setScreen(new KaoriHoloScreen(d));
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - H) / 2; }

    @Override
    protected void init() {
        if (!d.has) return;
        int y = top() + H - 28;
        addRenderableWidget(Button.builder(Component.literal("⇩ Traer a Kaori"), b -> send(KaoriHolo.TP_KAORI))
                .bounds(left() + 10, y, 120, 20)
                .tooltip(Tooltip.create(Component.literal("Kaori se teletransporta a donde estás tú (aunque esté lejos)"))).build());
        Button home = Button.builder(Component.literal("⌂ Ir a mi casa"), b -> send(KaoriHolo.TP_HOME))
                .bounds(left() + W - 130, y, 120, 20)
                .tooltip(Tooltip.create(Component.literal(d.home ? "Te lleva dentro de la " + d.hut + " de Kaori, junto a tu cama"
                        : "Duerme primero en la cama de una casa-cápsula de Kaori"))).build();
        home.active = d.home;
        addRenderableWidget(home);
    }

    private void send(int action) {
        Net.CHANNEL.sendToServer(new KaoriHolo.Request(action));
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        // sin el fondo oscuro: el holograma flota sobre el mundo
        int x = left(), y = top();
        float t = (System.currentTimeMillis() - opened) / 1000F;
        float appear = Mth.clamp(t * 4F, 0F, 1F);
        int h = (int) (H * appear);
        int cy = y + H / 2;
        int y0 = cy - h / 2, y1 = cy + h / 2;
        g.fill(x, y0, x + W, y1, GLASS);
        g.fillGradient(x + 1, y0 + 1, x + W - 1, y1 - 1, GLASS2, 0x3A0A4A70);
        // bordes y esquinas brillantes
        g.fill(x, y0, x + W, y0 + 1, CYAN);
        g.fill(x, y1 - 1, x + W, y1, CYAN);
        g.fill(x, y0, x + 1, y1, CYAN_D);
        g.fill(x + W - 1, y0, x + W, y1, CYAN_D);
        for (int[] c : new int[][]{{x, y0}, {x + W - 10, y0}, {x, y1 - 2}, {x + W - 10, y1 - 2}}) g.fill(c[0], c[1], c[0] + 10, c[1] + 2, 0xFFFFFFFF);
        // líneas de barrido del holograma
        int scan = (int) ((t * 40) % H);
        for (int sy = y0 + 2; sy < y1 - 2; sy += 3) g.fill(x + 1, sy, x + W - 1, sy + 1, 0x1080E8FF);
        if (y0 + scan < y1) g.fill(x + 1, y0 + scan, x + W - 1, y0 + scan + 2, 0x4080F0FF);
        if (appear < 1F) return;

        g.drawString(font, "◈ HOLOGRAMA · KAORI", x + 10, y + 7, 0xFFFFFFFF, true);
        float blink = 0.5F + 0.5F * Mth.sin(t * 4F);
        g.fill(x + W - 14, y + 9, x + W - 9, y + 14, d.loaded ? (0xFF000000 | (int) (blink * 255) << 8 | 0x40) : 0xFFFFB040);
        g.fill(x + 8, y + 18, x + W - 8, y + 19, 0x8080E8FF);
        if (!d.has) {
            g.drawCenteredString(font, "§bSin señal: todavía no tienes a Kaori.", x + W / 2, y + H / 2 - 4, TXT);
            g.drawCenteredString(font, "§7Craftea su cápsula para invocarla.", x + W / 2, y + H / 2 + 8, TXT);
            return;
        }
        // estado
        int sx = x + 10, sy = y + 24;
        g.drawString(font, "Ubicación: §f" + d.where, sx, sy, TXT, false);
        g.drawString(font, "Forma: §f" + FORMS[Math.max(0, Math.min(4, d.form))], sx, sy + 11, TXT, false);
        g.drawString(font, "Modo: §f" + KaoriMode.byId(d.mode).display + (d.weak ? " §7(descansando)" : ""), sx, sy + 22, TXT, false);
        // vida
        int bw = 110;
        float hp = d.max > 0 ? Mth.clamp(d.health / d.max, 0, 1) : 0;
        g.fill(sx, sy + 34, sx + bw, sy + 39, 0x6006243C);
        g.fill(sx, sy + 34, sx + (int) (bw * hp), sy + 39, 0xFF6FE6FF);
        g.drawString(font, String.format("♥ %.0f/%.0f", d.health, d.max), sx + bw + 4, sy + 33, TXT, false);
        // equipo
        g.drawString(font, "Equipo", sx, sy + 46, CYAN, false);
        String[] lbl = {"Casco", "Pechera", "Grebas", "Botas", "Arma", "Escudo"};
        ItemStack hover = ItemStack.EMPTY;
        for (int i = 0; i < 6 && i < d.equip.size(); i++) {
            int ex = sx + (i % 3) * 40, ey = sy + 57 + (i / 3) * 22;
            hover = slot(g, d.equip.get(i), ex, ey, mx, my, hover);
            if (d.equip.get(i).isEmpty()) g.drawString(font, lbl[i].substring(0, 1), ex + 6, ey + 4, 0x6080E8FF, false);
        }
        g.drawString(font, d.hut.isEmpty() ? "§7Casa: ninguna colocada" : "Casa: §f" + d.hut + (d.home ? " §a✔ tu cama" : ""), sx, sy + 104, TXT, false);
        // mochila 9×3
        int gx = x + W - 10 - 9 * 18 + 36, gy = y + 24;
        gx = x + 132;
        g.drawString(font, "Mochila", gx, gy, CYAN, false);
        for (int i = 0; i < 27 && i < d.bag.size(); i++) {
            int col = i % 7, row = i / 7;
            hover = slot(g, d.bag.get(i), gx + col * 18, gy + 11 + row * 18, mx, my, hover);
        }
        super.render(g, mx, my, partial);
        if (!hover.isEmpty()) g.renderTooltip(font, hover, mx, my);
    }

    private ItemStack slot(GuiGraphics g, ItemStack s, int x, int y, int mx, int my, ItemStack hover) {
        boolean in = mx >= x && mx < x + 17 && my >= y && my < y + 17;
        g.fill(x, y, x + 17, y + 17, in ? 0x6080E8FF : 0x3080E8FF);
        g.fill(x, y, x + 17, y + 1, 0x9080E8FF);
        if (!s.isEmpty()) {
            g.renderItem(s, x, y);
            g.renderItemDecorations(font, s, x, y);
            if (in) return s;
        }
        return hover;
    }
}
