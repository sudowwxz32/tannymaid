package com.laury.laurymod.client;

import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.menu.LauryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Inventario de Laury: modelo 3D, barra de vida, armadura, herramientas y mochila. */
public class LauryScreen extends AbstractContainerScreen<LauryMenu> {
    private static final ItemStack[] ARMOR_GHOSTS = {new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE),
            new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS)};
    private static final ItemStack[] TOOL_GHOSTS = {new ItemStack(Items.IRON_SWORD), new ItemStack(Items.IRON_AXE),
            new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.IRON_HOE),
            new ItemStack(Items.BOW), new ItemStack(Items.SHIELD)};
    private static final String[] ARMOR_NAMES = {"Casco", "Peto", "Pantalones", "Botas"};
    private static final String[] TOOL_NAMES = {"Espada / arma", "Hacha", "Pico", "Pala", "Azada", "Arco", "Escudo"};

    public LauryScreen(LauryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 254;
        inventoryLabelY = LauryMenu.PLAYER_Y - 11;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        for (int i = 0; i < 4 + LauryMenu.TOOL_COUNT; i++) {
            Slot s = menu.slots.get(i);
            if (!s.hasItem() && isHovering(s.x, s.y, 16, 16, mx, my)) {
                g.renderTooltip(font, Component.literal(i < 4 ? ARMOR_NAMES[i] : TOOL_NAMES[i - 4]), mx, my);
            }
        }
        renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF000000);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFFFFFFF);
        g.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFFC6C6C6);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + 15, 0xFF5B7BD5);   // franja azul dragón

        // marco del modelo
        g.fill(x + 26, y + 17, x + 78, y + 91, 0xFF373737);
        g.fill(x + 27, y + 18, x + 78, y + 91, 0xFFFFFFFF);
        g.fill(x + 27, y + 18, x + 77, y + 90, 0xFF16213F);

        // barra de vida
        LauryEntity l = menu.getLaury();
        int bx = x + 82, by = y + 29, bw = 86, bh = 8;
        g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF000000);
        g.fill(bx, by, bx + bw, by + bh, 0xFF2A1A22);
        if (l != null) {
            float pct = Math.max(0, Math.min(1, l.getHealth() / l.getMaxHealth()));
            int c = l.isWeakened() ? 0xFF8A8A8A : pct > 0.5F ? 0xFF3FBF5A : pct > 0.25F ? 0xFFE0A030 : 0xFFD43030;
            g.fill(bx, by, bx + (int) (bw * pct), by + bh, c);
            g.fill(bx, by, bx + (int) (bw * pct), by + 2, 0x55FFFFFF);
        }

        for (Slot s : menu.slots) slot(g, x + s.x - 1, y + s.y - 1);
        for (int i = 0; i < 4 + LauryMenu.TOOL_COUNT; i++) {
            Slot s = menu.slots.get(i);
            if (s.hasItem()) continue;
            int sx = x + s.x, sy = y + s.y;
            g.renderFakeItem(i < 4 ? ARMOR_GHOSTS[i] : TOOL_GHOSTS[i - 4], sx, sy);
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            g.fill(sx, sy, sx + 16, sy + 16, 0xB08B8B8B);
            g.pose().popPose();
        }

        if (l != null) {
            int size = l.isDragon() ? 9 : 26;
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 52, y + (l.isDragon() ? 78 : 86), size,
                    (float) (x + 52 - mx), (float) (y + 40 - my), l);
        }
    }

    private static void slot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        g.fill(x, y, x + 17, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + 17, 0xFF373737);
        g.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, title, 8, 5, 0xFFFFFF, false);
        LauryEntity l = menu.getLaury();
        if (l != null) {
            g.drawString(font, String.format("❤ %.0f / %.0f", l.getHealth(), l.getMaxHealth()), 82, 19, 0x1E5A2A, false);
            String st = l.isWeakened() ? "Debilitada" : l.isDragon() ? "Dragón" : "Humana";
            g.drawString(font, st, 168 - font.width(st), 5, 0xE8EEFF, false);
        }
        g.drawString(font, "Herramientas", 82, 42, 0x404040, false);
        g.drawString(font, "Mochila", 8, LauryMenu.BAG_Y - 10, 0x404040, false);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
    }
}
