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
import net.minecraft.world.item.Items;

/**
 * Holograma azul de Kaori (tecla J): su mochila, equipo y cinturón desde cualquier lugar. Clic en un objeto para
 * tomarlo (clic derecho: solo uno). Botones para traer a Kaori o ir a tu casa-cápsula.
 */
public class KaoriHoloScreen extends Screen {
    private static final int W = 304, H = 222;
    private static final int CYAN = 0xFF6FE6FF, CYAN_D = 0xFF2A9FD0, GLASS = 0x6A0A3A5C, GLASS2 = 0x8A06243C, TXT = 0xFFBFF4FF, DIM = 0xFF6FA8C0;
    private static final String[] FORMS = {"Base", "Super Saiyajin", "Super Saiyajin 2", "Super Saiyajin 2", "Super Saiyajin Dios"};
    private static final ItemStack[] GHOST = {new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE),
            new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS), new ItemStack(Items.IRON_SWORD), new ItemStack(Items.SHIELD),
            new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_AXE), new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.IRON_HOE),
            new ItemStack(Items.BOW), new ItemStack(Items.SHEARS), new ItemStack(Items.TORCH)};

    private final KaoriHolo.Data d;
    private final long opened = System.currentTimeMillis();

    public KaoriHoloScreen(KaoriHolo.Data d) {
        super(Component.literal("Holograma de Kaori"));
        this.d = d;
    }

    public static void show(KaoriHolo.Data d) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof KaoriHoloScreen old) {
            KaoriHoloScreen n = new KaoriHoloScreen(d);
            mc.setScreen(n);
            return;
        }
        mc.setScreen(new KaoriHoloScreen(d));
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - H) / 2; }

    // posiciones de las ranuras
    private int equipX(int i) {
        int col = i < 6 ? i : i < 12 ? i - 6 : 0;
        return left() + 10 + col * 18;
    }

    private int equipY(int i) { return top() + 98 + (i < 6 ? 0 : i < 12 ? 20 : 40); }
    private int bagX(int i) { return left() + W - 10 - 9 * 18 + (i % 9) * 18; }
    private int bagY(int i) { return top() + 36 + (i / 9) * 18; }

    @Override
    protected void init() {
        if (!d.has) return;
        int y = top() + H - 28;
        addRenderableWidget(Button.builder(Component.literal("⇩ Traer a Kaori"), b -> send(KaoriHolo.TP_KAORI, true))
                .bounds(left() + 10, y, 136, 20)
                .tooltip(Tooltip.create(Component.literal("Kaori se teletransporta a donde estás tú (aunque esté lejos)"))).build());
        Button home = Button.builder(Component.literal("⌂ Ir a mi casa"), b -> send(KaoriHolo.TP_HOME, true))
                .bounds(left() + W - 146, y, 136, 20)
                .tooltip(Tooltip.create(Component.literal(d.home ? "Te lleva dentro de la " + d.hut + " de Kaori, junto a tu cama"
                        : "Duerme primero en la cama de una casa-cápsula de Kaori"))).build();
        home.active = d.home;
        addRenderableWidget(home);
    }

    private void send(int action, boolean close) {
        Net.CHANNEL.sendToServer(new KaoriHolo.Request(action));
        if (close) onClose();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (d.has && (button == 0 || button == 1)) {
            for (int i = 0; i < d.bag.size(); i++) {
                if (in(mx, my, bagX(i), bagY(i)) && !d.bag.get(i).isEmpty()) {
                    send(KaoriHolo.take(i, button == 1), false);
                    return true;
                }
            }
            for (int i = 0; i < d.equip.size(); i++) {
                if (in(mx, my, equipX(i), equipY(i)) && !d.equip.get(i).isEmpty()) {
                    send(KaoriHolo.take(27 + i, button == 1), false);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private static boolean in(double mx, double my, int x, int y) {
        return mx >= x && mx < x + 17 && my >= y && my < y + 17;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        int x = left(), y = top();
        float t = (System.currentTimeMillis() - opened) / 1000F;
        float appear = Mth.clamp(t * 4F, 0F, 1F);
        int h = Math.max(2, (int) (H * appear));
        int cy = y + H / 2, y0 = cy - h / 2, y1 = cy + h / 2;
        g.fillGradient(x, y0, x + W, y1, GLASS2, GLASS);
        // bordes y esquinas brillantes
        g.fill(x, y0, x + W, y0 + 1, CYAN);
        g.fill(x, y1 - 1, x + W, y1, CYAN);
        g.fill(x, y0, x + 1, y1, CYAN_D);
        g.fill(x + W - 1, y0, x + W, y1, CYAN_D);
        for (int[] c : new int[][]{{x, y0}, {x + W - 12, y0}, {x, y1 - 2}, {x + W - 12, y1 - 2}}) g.fill(c[0], c[1], c[0] + 12, c[1] + 2, 0xFFFFFFFF);
        // líneas de barrido
        for (int sy = y0 + 2; sy < y1 - 2; sy += 3) g.fill(x + 1, sy, x + W - 1, sy + 1, 0x0C80E8FF);
        int scan = (int) ((t * 45) % H);
        if (y0 + scan < y1 - 2) g.fill(x + 1, y0 + scan, x + W - 1, y0 + scan + 2, 0x3080F0FF);
        if (appear < 1F) return;

        g.drawString(font, "◈ HOLOGRAMA · KAORI", x + 10, y + 7, 0xFFFFFFFF, true);
        float blink = 0.5F + 0.5F * Mth.sin(t * 4F);
        String sig = d.loaded ? "SEÑAL" : "LEJOS";
        g.drawString(font, sig, x + W - 20 - font.width(sig), y + 7, d.loaded ? 0xFF7CFFB0 : 0xFFFFC060, false);
        g.fill(x + W - 15, y + 8, x + W - 9, y + 14, d.loaded ? (0xFF000000 | ((int) (100 + blink * 155) << 8) | 0x60) : 0xFFFFB040);
        g.fill(x + 8, y + 19, x + W - 8, y + 20, 0x8080E8FF);
        if (!d.has) {
            g.drawCenteredString(font, "§bSin señal: todavía no tienes a Kaori.", x + W / 2, y + H / 2 - 4, TXT);
            g.drawCenteredString(font, "§7Craftea su cápsula para invocarla.", x + W / 2, y + H / 2 + 8, TXT);
            return;
        }
        // ---- estado
        int sx = x + 10, sy = y + 25;
        head(g, "Estado", sx, sy);
        g.drawString(font, "Dónde: §f" + d.where, sx, sy + 12, TXT, false);
        g.drawString(font, "Forma: §f" + FORMS[Math.max(0, Math.min(4, d.form))], sx, sy + 23, TXT, false);
        g.drawString(font, "Modo: §f" + KaoriMode.byId(d.mode).display + (d.weak ? " §7(descansa)" : ""), sx, sy + 34, TXT, false);
        float hp = d.max > 0 ? Mth.clamp(d.health / d.max, 0, 1) : 0;
        g.fill(sx, sy + 47, sx + 70, sy + 52, 0x8006243C);
        g.fill(sx, sy + 47, sx + (int) (70 * hp), sy + 52, CYAN);
        g.drawString(font, String.format("♥ %.0f/%.0f", d.health, d.max), sx + 75, sy + 46, TXT, false);
        // ---- equipo y cinturón
        head(g, "Equipo y cinturón", sx, y + 86);
        ItemStack hover = ItemStack.EMPTY;
        for (int i = 0; i < d.equip.size(); i++) hover = slot(g, d.equip.get(i), equipX(i), equipY(i), mx, my, hover, i < GHOST.length ? GHOST[i] : null);
        g.drawString(font, "← antorcha", equipX(12) + 21, equipY(12) + 5, DIM, false);
        // ---- mochila
        int bx = bagX(0);
        head(g, "Mochila", bx, y + 25);
        for (int i = 0; i < d.bag.size(); i++) hover = slot(g, d.bag.get(i), bagX(i), bagY(i), mx, my, hover, null);
        g.drawString(font, "§7Clic: tomar · Clic derecho: tomar 1", bx, y + 94, TXT, false);
        head(g, "Casa-cápsula", bx, y + 112);
        g.drawString(font, d.hut.isEmpty() ? "§7Ninguna colocada" : "§f" + d.hut, bx, y + 124, TXT, false);
        g.drawString(font, d.home ? "§a✔ Dormiste en su cama" : "§7Duerme en su cama para ir con ⌂", bx, y + 136, TXT, false);
        super.render(g, mx, my, partial);
        if (!hover.isEmpty()) g.renderTooltip(font, hover, mx, my);
    }

    private void head(GuiGraphics g, String text, int x, int y) {
        g.fill(x - 3, y, x - 1, y + 8, CYAN);
        g.drawString(font, text, x + 1, y, CYAN, false);
    }

    private ItemStack slot(GuiGraphics g, ItemStack s, int x, int y, int mx, int my, ItemStack hover, ItemStack ghost) {
        boolean in = in(mx, my, x, y);
        g.fill(x, y, x + 17, y + 17, in ? 0x7080E8FF : 0x3080E8FF);
        g.fill(x, y, x + 17, y + 1, 0xA080E8FF);
        if (s.isEmpty()) {
            if (ghost != null) {
                g.renderFakeItem(ghost, x, y);
                g.fill(x, y + 1, x + 17, y + 17, 0xC00A2A40);
            }
            return hover;
        }
        g.renderItem(s, x, y);
        g.renderItemDecorations(font, s, x, y);
        return in ? s : hover;
    }
}
