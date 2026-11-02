package com.bdsz.client.kaori;

import com.bdsz.companion.KaoriMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Mochila de Kaori: armadura, arma, mano secundaria, 27 ranuras y tu inventario. */
public class KaoriInventoryScreen extends AbstractContainerScreen<KaoriMenu> {
    private static final int EDGE = 0xFFE8783C, BG = 0xFF2B2F25, SLOT = 0xFF171A14, SLOT_HI = 0xFF4A503E, TEXT = 0xFFF2C38B;

    public KaoriInventoryScreen(KaoriMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 252;
        inventoryLabelY = KaoriMenu.PLAYER_Y - 11;
        titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int x = leftPos, y = topPos;
        g.fill(x - 1, y - 1, x + imageWidth + 1, y + imageHeight + 1, 0xFF8A3F1A);
        g.fill(x, y, x + imageWidth, y + imageHeight, EDGE);
        g.fillGradient(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, BG, 0xFF1D201A);
        for (Slot s : menu.slots) {
            g.fill(x + s.x - 1, y + s.y - 1, x + s.x + 17, y + s.y + 17, SLOT_HI);
            g.fill(x + s.x, y + s.y, x + s.x + 16, y + s.y + 16, SLOT);
        }
        g.drawString(font, "Arma", x + 54, y + KaoriMenu.EQUIP_Y + 31, 0xFFB9B39C, false);
        g.drawString(font, "Escudo", x + 54, y + KaoriMenu.EQUIP_Y + 49, 0xFFB9B39C, false);
        g.drawString(font, "Mochila", x + 8, y + KaoriMenu.BAG_Y - 10, TEXT, false);
        if (menu.kaori != null) {
            g.fill(x + 100, y + 16, x + 168, y + 92, 0xFF34392C);
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 134, y + 88, 32, (float) (x + 134 - mx), (float) (y + 40 - my), menu.kaori);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, title, titleLabelX, titleLabelY, TEXT, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        super.render(g, mx, my, partial);
        renderTooltip(g, mx, my);
    }
}
