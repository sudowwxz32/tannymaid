package com.tanny.tannymaid.client;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.menu.TannyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Pantalla del inventario de Tanny: su modelo, barra de vida, herramientas y mochila. */
public class TannyScreen extends AbstractContainerScreen<TannyMenu> {
    private static final ItemStack[] GHOSTS = {
            new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.IRON_AXE),
            new ItemStack(Items.IRON_SWORD), new ItemStack(Items.SHIELD)
    };
    private static final String[] TOOL_NAMES = {"Pico", "Pala", "Hacha", "Espada", "Escudo"};

    public TannyScreen(TannyMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 234;
        this.inventoryLabelY = TannyMenu.PLAYER_Y - 11;
        this.titleLabelY = 6;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderToolHints(g, mouseX, mouseY);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        // Panel
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF000000);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFFFFFFF);
        g.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFFC6C6C6);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + 15, 0xFFE7B6C8); // franja rosa

        // Marco del modelo
        g.fill(x + 7, y + 17, x + 57, y + 72, 0xFF373737);
        g.fill(x + 8, y + 18, x + 57, y + 72, 0xFFFFFFFF);
        g.fill(x + 8, y + 18, x + 56, y + 71, 0xFF1E1B24);

        // Barra de vida
        TannyEntity t = menu.getTanny();
        int barX = x + 62, barY = y + 29, barW = 106, barH = 8;
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF000000);
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF3A1A22);
        if (t != null) {
            float pct = Math.max(0.0F, Math.min(1.0F, t.getHealth() / t.getMaxHealth()));
            int color = t.isWeakened() ? 0xFF8A8A8A : pct > 0.5F ? 0xFFE0436B : pct > 0.25F ? 0xFFE89A3C : 0xFFD43030;
            g.fill(barX, barY, barX + (int) (barW * pct), barY + barH, color);
            g.fill(barX, barY, barX + (int) (barW * pct), barY + 2, 0x55FFFFFF);
        }

        // Ranuras
        for (Slot slot : menu.slots) {
            slotBg(g, x + slot.x - 1, y + slot.y - 1);
        }
        // Barra de herramientas resaltada
        g.fill(x + TannyMenu.TOOL_X - 1, y + TannyMenu.TOOL_Y - 1, x + TannyMenu.TOOL_X + 18 * 5 + 1, y + TannyMenu.TOOL_Y, 0xFFB0577A);

        // Iconos guía en ranuras de herramienta vacías
        for (int i = 0; i < TannyMenu.TOOL_COUNT; i++) {
            Slot slot = menu.slots.get(i);
            if (!slot.hasItem()) {
                int sx = x + slot.x, sy = y + slot.y;
                g.renderFakeItem(GHOSTS[i], sx, sy);
                g.pose().pushPose();
                g.pose().translate(0, 0, 300);
                g.fill(sx, sy, sx + 16, sy + 16, 0xB08B8B8B);
                g.pose().popPose();
            }
        }

        // Tanny en 3D
        if (t != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 32, y + 68, 25,
                    (float) (x + 32 - mouseX), (float) (y + 26 - mouseY), t);
        }
    }

    private static void slotBg(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        g.fill(x, y, x + 17, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + 17, 0xFF373737);
        g.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0x5A1E35, false);
        TannyEntity t = menu.getTanny();
        if (t != null) {
            String hp = String.format("♥ %.0f / %.0f", t.getHealth(), t.getMaxHealth());
            g.drawString(font, hp, 62, 18, 0x8B1E3F, false);
            String state = t.isWeakened() ? "Debilitada" : t.getMode().displayName();
            g.drawString(font, state, 168 - font.width(state), 18, 0x404040, false);
        }
        g.drawString(font, "Herramientas", 62, 43, 0x404040, false);
        g.drawString(font, "Mochila", 8, TannyMenu.BAG_Y - 10, 0x404040, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private void renderToolHints(GuiGraphics g, int mouseX, int mouseY) {
        for (int i = 0; i < TannyMenu.TOOL_COUNT; i++) {
            Slot slot = menu.slots.get(i);
            if (!slot.hasItem() && isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                g.renderTooltip(font, Component.literal("Ranura de " + TOOL_NAMES[i].toLowerCase()), mouseX, mouseY);
            }
        }
    }
}
