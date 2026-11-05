package com.linterna.client;

import com.linterna.data.RingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Ranura del anillo: holograma verde con tu personaje, la ranura del anillo y tu inventario. */
public class RingScreen extends AbstractContainerScreen<RingMenu> {
    private static final int G1 = 0xFF4CFF6A, G2 = 0xFF2AA040, G_TXT = 0xFFC8FFD2, G_HI = 0xFFE8FFEC, G_DIM = 0xFF6FA87A;
    private final long opened = System.currentTimeMillis();

    public RingScreen(RingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 73;
    }

    private void frame(GuiGraphics g, int x, int y, int w, int h, int fill, int edge) {
        g.fill(x, y, x + w, y + h, fill);
        g.fill(x, y, x + w, y + 1, edge);
        g.fill(x, y + h - 1, x + w, y + h, edge);
        g.fill(x, y, x + 1, y + h, edge);
        g.fill(x + w - 1, y, x + w, y + h, edge);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int x = leftPos, y = topPos;
        float t = (System.currentTimeMillis() - opened) / 1000F;
        g.fillGradient(x, y, x + imageWidth, y + imageHeight, 0xE00A4A1C, 0xE0031E0A);
        frame(g, x, y, imageWidth, imageHeight, 0, G1);
        for (int[] c : new int[][]{{x, y}, {x + imageWidth - 12, y}, {x, y + imageHeight - 2}, {x + imageWidth - 12, y + imageHeight - 2}})
            g.fill(c[0], c[1], c[0] + 12, c[1] + 2, G_HI);
        for (int sy = y + 2; sy < y + imageHeight - 2; sy += 3) g.fill(x + 1, sy, x + imageWidth - 1, sy + 1, 0x0E6AFF80);
        int scan = (int) ((t * 40) % imageHeight);
        g.fill(x + 1, y + scan, x + imageWidth - 1, y + Math.min(imageHeight - 1, scan + 2), 0x306AFF80);
        // tu personaje (se ve el anillo en el dedo)
        frame(g, x + 8, y + 16, 52, 56, 0x40021A08, G2);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 34, y + 68, 24, (float) (x + 34 - mx), (float) (y + 30 - my), minecraft.player);
        // ranura del anillo con un halo que late
        int sx = x + RingMenu.SLOT_X, sy = y + RingMenu.SLOT_Y;
        float pulse = 0.5F + 0.5F * Mth.sin(t * 3F);
        for (int i = 0; i < 40; i++) {
            double a = i * Math.PI * 2 / 40 + t * 0.8;
            int px = sx + 8 + (int) (Math.cos(a) * (15 + pulse * 2)), py = sy + 8 + (int) (Math.sin(a) * (15 + pulse * 2));
            g.fill(px, py, px + 1, py + 1, i % 2 == 0 ? G1 : 0x904CFF6A);
        }
        frame(g, sx - 2, sy - 2, 20, 20, 0x804CFF6A, G_HI);
        g.drawCenteredString(font, "Anillo", sx + 8, sy + 24, G_TXT);
        g.drawString(font, "§2Funciona", x + 66, y + 22, G_DIM, false);
        g.drawString(font, "§2sin tenerlo", x + 66, y + 32, G_DIM, false);
        g.drawString(font, "§2en la mano", x + 66, y + 42, G_DIM, false);
        g.drawString(font, "§2y no se pierde", x + 66, y + 52, G_DIM, false);
        g.drawString(font, "§2al morir.", x + 66, y + 62, G_DIM, false);
        // ranuras del inventario
        for (int i = 1; i < menu.slots.size(); i++) {
            Slot s = menu.slots.get(i);
            frame(g, x + s.x - 1, y + s.y - 1, 18, 18, 0x50021A08, 0x804CFF6A);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, "◈ " + title.getString(), 8, 5, G_HI, true);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, G_TXT, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
    }
}
