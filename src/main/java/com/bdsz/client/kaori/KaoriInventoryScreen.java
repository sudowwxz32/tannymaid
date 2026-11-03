package com.bdsz.client.kaori;

import com.bdsz.companion.KaoriEntity;
import com.bdsz.companion.KaoriMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Mochila de Kaori: armadura, arma, escudo, cinturón de herramientas, antorcha, 27 ranuras y tu inventario. */
public class KaoriInventoryScreen extends AbstractContainerScreen<KaoriMenu> {
    private static final int EDGE = 0xFFC8383A, EDGE_DK = 0xFF5E1418, BG_TOP = 0xFF1E1A22, BG_BOT = 0xFF141117,
            SLOT = 0xFF0E0C10, SLOT_HI = 0xFF3A3040, GOLD = 0xFFF2C14E, TEXT = 0xFFF4E6D0, MUTED = 0xFF8C8088;
    /** Iconos "fantasma" de las ranuras vacías del equipo. */
    private static final ItemStack[] GHOST = {new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE),
            new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS), new ItemStack(Items.IRON_SWORD), new ItemStack(Items.SHIELD),
            new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_AXE), new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.IRON_HOE),
            new ItemStack(Items.BOW), new ItemStack(Items.SHEARS), new ItemStack(Items.TORCH)};

    public KaoriInventoryScreen(KaoriMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 182;
        imageHeight = 254;
        inventoryLabelY = KaoriMenu.PLAYER_Y - 11;
        titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int x = leftPos, y = topPos;
        g.fill(x - 1, y - 1, x + imageWidth + 1, y + imageHeight + 1, EDGE_DK);
        g.fill(x, y, x + imageWidth, y + imageHeight, EDGE);
        g.fillGradient(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, BG_TOP, BG_BOT);
        g.fillGradient(x + 1, y + 1, x + imageWidth - 1, y + 17, 0xFFB52B30, 0xFF6E1519);
        g.fill(x + 1, y + 17, x + imageWidth - 1, y + 18, GOLD);
        // retrato (aquí sí se ve la armadura que lleva puesta)
        g.fill(x + 28, y + 20, x + 94, y + 92, 0xFF26212B);
        if (menu.kaori != null) {
            KaoriRenderer.PREVIEW = true;
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 61, y + 88, 32, (float) (x + 61 - mx), (float) (y + 40 - my), menu.kaori);
            KaoriRenderer.PREVIEW = false;
        }
        // ranuras
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot s = menu.slots.get(i);
            boolean equip = i < KaoriMenu.EQUIP_SLOTS;
            g.fill(x + s.x - 1, y + s.y - 1, x + s.x + 17, y + s.y + 17, equip ? 0xFF6E4A2A : SLOT_HI);
            g.fill(x + s.x, y + s.y, x + s.x + 16, y + s.y + 16, SLOT);
            if (equip && !s.hasItem() && i < GHOST.length) {
                g.renderFakeItem(GHOST[i], x + s.x, y + s.y);
                g.fill(x + s.x, y + s.y, x + s.x + 16, y + s.y + 16, 0xB00E0C10);   // icono apagado
            }
        }
        g.drawString(font, "Antorcha", x + KaoriMenu.HAND_X + 19, y + KaoriMenu.TOOL_Y + 50, MUTED, false);
        g.drawString(font, "Mochila", x + 8, y + KaoriMenu.BAG_Y - 10, GOLD, false);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, GOLD, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        super.render(g, mx, my, partial);
        g.drawString(font, "⚙ " + title.getString(), leftPos + 8, topPos + 5, 0xFFFFFFFF, true);
        renderTooltip(g, mx, my);
    }
}
