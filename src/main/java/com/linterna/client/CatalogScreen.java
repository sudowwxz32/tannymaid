package com.linterna.client;

import com.linterna.data.Constructs;
import com.linterna.data.LanternState;
import com.linterna.network.ActionPacket;
import com.linterna.network.ClientStates;
import com.linterna.network.Net;
import com.linterna.registry.ModRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Catálogo del anillo (tecla M): todo en holograma verde. Pestañas: Constructos y Armas. */
public class CatalogScreen extends Screen {
    private static final int W = 312, H = 196, CW = 70, CH = 64;
    private static final int G1 = 0xFF4CFF6A, G2 = 0xFF2AA040, G_TXT = 0xFFC8FFD2, G_DIM = 0xFF5E9A6A, G_HI = 0xFFE8FFEC;
    private static int tab;
    private final long opened = System.currentTimeMillis();
    private ItemStack[] icons;

    public CatalogScreen() {
        super(Component.literal("Catálogo del anillo"));
    }

    @Override
    protected void init() {
        icons = new ItemStack[]{new ItemStack(Items.LIME_STAINED_GLASS), new ItemStack(Items.LIME_STAINED_GLASS_PANE), new ItemStack(Items.GLASS),
                new ItemStack(Items.LIME_CARPET), new ItemStack(Items.IRON_BARS), new ItemStack(ModRegistry.ENERGY_BOMB.get()),
                new ItemStack(ModRegistry.ENERGY_SWORD.get()), new ItemStack(Items.EMERALD),
                new ItemStack(ModRegistry.ENERGY_SWORD.get()), new ItemStack(ModRegistry.ENERGY_AXE.get()), new ItemStack(ModRegistry.ENERGY_BOW.get()),
                new ItemStack(ModRegistry.ENERGY_SHIELD.get()), new ItemStack(ModRegistry.ENERGY_LANCE.get()), new ItemStack(ModRegistry.ENERGY_MACE.get()),
                new ItemStack(ModRegistry.ENERGY_CANNON.get()),
                new ItemStack(ModRegistry.ENERGY_SCYTHE.get())};
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - H) / 2; }
    private int first() { return tab == 0 ? 0 : Constructs.CONSTRUCTS; }
    private int count() { return tab == 0 ? Constructs.CONSTRUCTS : Constructs.NAMES.length - Constructs.CONSTRUCTS; }
    private int cardX(int i) { return left() + 10 + (i % 4) * (CW + 6); }
    private int cardY(int i) { return top() + 44 + (i / 4) * (CH + 6); }
    private int tabX(int t) { return left() + 10 + t * 100; }

    private int ringX() { return left() + 210; }
    private boolean onRing(double mx, double my) { return mx >= ringX() && mx < ringX() + 92 && my >= top() + 22 && my < top() + 37; }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (onRing(mx, my)) {
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.OPEN_RING, 0));
            return true;
        }
        for (int t = 0; t < 2; t++) {
            if (mx >= tabX(t) && mx < tabX(t) + 96 && my >= top() + 22 && my < top() + 37) {
                tab = t;
                return true;
            }
        }
        for (int i = 0; i < count(); i++) {
            if (mx >= cardX(i) && mx < cardX(i) + CW && my >= cardY(i) && my < cardY(i) + CH) {
                Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.CATALOG, first() + i));
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void frame(GuiGraphics g, int x, int y, int w, int h, int fill, int edge) {
        g.fill(x, y, x + w, y + h, fill);
        g.fill(x, y, x + w, y + 1, edge);
        g.fill(x, y + h - 1, x + w, y + h, edge);
        g.fill(x, y, x + 1, y + h, edge);
        g.fill(x + w - 1, y, x + w, y + h, edge);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        g.fillGradient(0, 0, width, height, 0x40021A08, 0x60010C04);     // velo verde sobre el mundo
        float t = (System.currentTimeMillis() - opened) / 1000F;
        float appear = Mth.clamp(t * 4F, 0F, 1F);
        int x = left(), y = top(), h = Math.max(2, (int) (H * appear)), y0 = y + (H - h) / 2;
        g.fillGradient(x, y0, x + W, y0 + h, 0x900A4A1C, 0x70031E0A);
        frame(g, x, y0, W, h, 0, G1);
        for (int[] c : new int[][]{{x, y0}, {x + W - 12, y0}, {x, y0 + h - 2}, {x + W - 12, y0 + h - 2}}) g.fill(c[0], c[1], c[0] + 12, c[1] + 2, G_HI);
        for (int sy = y0 + 2; sy < y0 + h - 2; sy += 3) g.fill(x + 1, sy, x + W - 1, sy + 1, 0x0E6AFF80);
        int scan = (int) ((t * 50) % H);
        if (y + scan < y0 + h - 2) g.fill(x + 1, y + scan, x + W - 1, y + scan + 2, 0x356AFF80);
        if (appear < 1F) return;

        LanternState s = Minecraft.getInstance().player == null ? new LanternState() : ClientStates.get(Minecraft.getInstance().player.getId());
        g.drawString(font, "◈ CATÁLOGO DEL ANILLO", x + 10, y + 8, G_HI, true);
        int pct = (int) Math.ceil(s.charge * 100.0 / LanternState.MAX_CHARGE);
        String e = "Energía " + pct + "%";
        g.drawString(font, e, x + W - 70 - font.width(e) / 2 + 20, y + 8, G1, false);
        g.fill(x + W - 70, y + 18, x + W - 10, y + 20, 0x6006240C);
        g.fill(x + W - 70, y + 18, x + W - 70 + 60 * pct / 100, y + 20, G1);
        // pestañas
        String[] tabs = {"Constructos", "Armas"};
        for (int k = 0; k < 2; k++) {
            boolean sel = tab == k, hov = mx >= tabX(k) && mx < tabX(k) + 96 && my >= y + 22 && my < y + 37;
            frame(g, tabX(k), y + 22, 96, 15, sel ? 0x704CFF6A : hov ? 0x404CFF6A : 0x2032C050, sel ? G_HI : G2);
            g.drawCenteredString(font, tabs[k], tabX(k) + 48, y + 26, sel ? G_HI : G_TXT);
        }
        boolean rh = onRing(mx, my);
        frame(g, ringX(), y + 22, 92, 15, rh ? 0x704CFF6A : 0x3032C050, rh ? G_HI : G1);
        g.drawCenteredString(font, (s.worn ? "◉ " : "◎ ") + "Ranura del anillo", ringX() + 46, y + 26, s.worn ? G_HI : G_TXT);
        String tip = rh ? "Ponte el anillo en su ranura: funciona sin tenerlo en la mano y nunca lo pierdes al morir" : "";
        for (int i = 0; i < count(); i++) {
            int idx = first() + i, cx = cardX(i), cy = cardY(i);
            boolean hover = mx >= cx && mx < cx + CW && my >= cy && my < cy + CH;
            boolean can = s.charge >= Constructs.COST[idx];
            frame(g, cx, cy, CW, CH, hover ? 0x704CFF6A : 0x3032C050, hover ? G_HI : G2);
            // icono en holograma verde
            float bob = Mth.sin(t * 2 + i) * 1.5F;
            RenderSystem.setShaderColor(0.35F, 1.0F, 0.45F, 1.0F);
            g.pose().pushPose();
            g.pose().translate(cx + CW / 2F - 12, cy + 6 + bob, 0);
            g.pose().scale(1.5F, 1.5F, 1);
            g.renderItem(icons[idx], 0, 0);
            g.pose().popPose();
            g.flush();
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            g.fill(cx + 4, cy + 30, cx + CW - 4, cy + 31, 0x604CFF6A);
            String n = Constructs.NAMES[idx];
            g.drawCenteredString(font, n.length() > 12 ? n.substring(0, 11) + "." : n, cx + CW / 2, cy + 36, can ? G_HI : G_DIM);
            g.drawCenteredString(font, (Constructs.COST[idx] * 100 / LanternState.MAX_CHARGE + 1) + "%", cx + CW / 2, cy + 49, can ? G1 : 0xFF3A6A44);
            if (hover) tip = n + ": " + Constructs.TIPS[idx];
        }
        g.drawString(font, "§2Clic para crear · los constructos se desvanecen solos", x + 10, y + H - 12, G_DIM, false);
        super.render(g, mx, my, partial);
        if (!tip.isEmpty()) g.renderTooltip(font, font.split(Component.literal("§a" + tip), 200), mx, my);
    }
}
