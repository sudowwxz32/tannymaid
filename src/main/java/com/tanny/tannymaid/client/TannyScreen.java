package com.tanny.tannymaid.client;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.menu.TannyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Pantalla de Tanny: pestañas (Equipo · Poké Balls · PC), su modelo 3D, vida y modo,
 * armadura, herramientas, mochila y un panel lateral con todas sus órdenes.
 */
public class TannyScreen extends AbstractContainerScreen<TannyMenu> {
    // Paleta
    private static final int CREAM = 0xFFFBEFF2, CREAM_DK = 0xFFEBD3DB, PINK = 0xFFE58AAB, PINK_DK = 0xFFB0577A,
            PLUM = 0xFF3A2236, PLUM_DK = 0xFF24151F, TEXT = 0xFF5A1E35, TEXT_SOFT = 0xFF8C5A6E, GOLD = 0xFFF2C14E;

    private static final ItemStack[] GHOSTS = {
            new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.IRON_AXE),
            new ItemStack(Items.IRON_SWORD), new ItemStack(Items.SHIELD)
    };
    private static final String[] TOOL_NAMES = {"pico", "pala", "hacha", "espada", "escudo"};
    private static final String[] ARMOR_NAMES = {"casco", "pechera", "pantalones", "botas"};
    private static final String[] TABS = {"Equipo", "Poké Balls", "PC Pokémon"};
    private static final String[] ICONS = {"➤", "■", "⚔", "✿", "⌂", "✦", "☰", "✋", "◆", "♥", "☕", "●", "☂"};
    private static final String[] ORDER_HELP = {
            "Te sigue y te defiende", "Se queda donde está", "Defensa activa en un radio amplio",
            "Cosecha y vuelve a sembrar aquí", "Va a casa y se queda ahí", "Se teletransporta a tu lado",
            "Ordena cofres y barriles cercanos", "Activa/desactiva recoger objetos", "Saca o guarda a Lucario",
            "Saca o guarda a Gardevoir", "24 panes (cada 5 min)", "Bayas curativas de Cobblemon (cada 5 min)",
            "Te lleva a un lugar seguro"
    };

    private static final int SIDE_W = 86, SIDE_GAP = 4, BTN_H = 15, TAB_H = 16;
    private ItemStack ballGhost = ItemStack.EMPTY;

    public TannyScreen(TannyMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 254;
        this.inventoryLabelY = TannyMenu.PLAYER_Y - 11;
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos -= (SIDE_W + SIDE_GAP) / 2;  // centra el conjunto (panel + órdenes)
        this.topPos += TAB_H / 2;
        Item ball = ForgeRegistries.ITEMS.getValue(new ResourceLocation("cobblemon", "poke_ball"));
        ballGhost = ball != null && ball != Items.AIR ? new ItemStack(ball) : ItemStack.EMPTY;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderHints(g, mouseX, mouseY);
        renderTooltip(g, mouseX, mouseY);
    }

    // ------------------------------------------------------------------ fondo

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        boolean balls = menu.getPage() == TannyMenu.PAGE_BALLS;

        renderTabs(g, mouseX, mouseY);

        // Panel principal con borde redondeado
        panel(g, x, y, imageWidth, imageHeight);
        g.fillGradient(x + 3, y + 3, x + imageWidth - 3, y + 15, PINK, PINK_DK);
        for (int i = 0; i < 6; i++) heart(g, x + imageWidth - 14 - i * 9, y + 6, i % 2 == 0 ? 0x55FFFFFF : 0x33FFFFFF);

        // Marco del modelo
        int mx0 = x + 27, my0 = y + 17, mx1 = x + 77, my1 = y + 92;
        g.fill(mx0 - 1, my0 - 1, mx1 + 1, my1 + 1, PINK_DK);
        g.fillGradient(mx0, my0, mx1, my1, PLUM, PLUM_DK);
        for (int i = 0; i < 5; i++) heart(g, mx0 + 5 + (i * 11) % 44, my0 + 6 + (i * 17) % 62, 0x22FF9AC2);
        g.fill(mx0, my1 - 10, mx1, my1, 0x33000000);

        TannyEntity t = menu.getTanny();
        // Vida
        int barX = x + 82, barY = y + 30, barW = 86, barH = 7;
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, PINK_DK);
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF3A1A22);
        if (t != null) {
            float pct = Math.max(0.0F, Math.min(1.0F, t.getHealth() / t.getMaxHealth()));
            int color = t.isWeakened() ? 0xFF8A8A8A : pct > 0.5F ? 0xFFE0436B : pct > 0.25F ? 0xFFE89A3C : 0xFFD43030;
            g.fill(barX, barY, barX + (int) (barW * pct), barY + barH, color);
            g.fill(barX, barY, barX + (int) (barW * pct), barY + 2, 0x66FFFFFF);
        }

        // Ranuras
        for (Slot slot : menu.slots) {
            boolean special = slot instanceof TannyMenu.BallSlot || slot instanceof TannyMenu.ArmorSlot || slot instanceof TannyMenu.ToolSlot;
            slotBg(g, x + slot.x - 1, y + slot.y - 1, special);
        }

        if (balls) {
            // Recuadro informativo y botón del PC
            g.fill(x + 81, y + 44, x + 169, y + 92, CREAM_DK);
            int total = 0;
            for (Slot s : menu.slots) if (s instanceof TannyMenu.BallSlot && s.hasItem()) total += s.getItem().getCount();
            g.drawString(font, "Poké Balls: §l" + total, x + 85, y + 48, TEXT, false);
            button(g, x + 82, y + 66, 86, 22, "▣ Abrir mis cajas", isIn(mouseX, mouseY, x + 82, y + 66, 86, 22), true);
            // Fantasmas de Poké Ball en las ranuras vacías
            if (!ballGhost.isEmpty()) {
                for (Slot s : menu.slots) {
                    if (s instanceof TannyMenu.BallSlot && !s.hasItem()) ghost(g, ballGhost, x + s.x, y + s.y, 0xC8C9A3B1);
                }
            }
        } else {
            g.fill(x + TannyMenu.TOOL_X, y + TannyMenu.TOOL_Y - 1, x + TannyMenu.TOOL_X + 18 * 5, y + TannyMenu.TOOL_Y, PINK);
            for (int i = 0; i < TannyMenu.TOOL_COUNT; i++) {
                Slot slot = menu.slots.get(menu.toolStart() + i);
                if (!slot.hasItem()) ghost(g, GHOSTS[i], x + slot.x, y + slot.y, 0xB89B8B93);
            }
        }

        // Tanny en 3D
        if (t != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 52, y + 86, 30,
                    (float) (x + 52 - mouseX), (float) (y + 36 - mouseY), t);
        }

        renderOrders(g, mouseX, mouseY);
    }

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        int x = leftPos + 4, y = topPos - TAB_H + 2;
        int active = menu.getPage() == TannyMenu.PAGE_BALLS ? 1 : 0;
        for (int i = 0; i < TABS.length; i++) {
            int w = tabWidth(i);
            boolean on = i == active, hover = isIn(mouseX, mouseY, x, y, w, TAB_H);
            g.fill(x, y, x + w, y + TAB_H + (on ? 2 : 0), on ? PINK_DK : 0xFF7A4560);
            g.fill(x + 1, y + 1, x + w - 1, y + TAB_H + (on ? 2 : -1), on ? CREAM : hover ? 0xFFF3D5E0 : CREAM_DK);
            g.drawCenteredString(font, TABS[i], x + w / 2, y + 4, on ? 0xFFB0577A : 0xFF7A4560);
            x += w + 2;
        }
    }

    private int tabWidth(int i) {
        return font.width(TABS[i]) + 14;
    }

    private void renderOrders(GuiGraphics g, int mouseX, int mouseY) {
        int x = leftPos + imageWidth + SIDE_GAP, y = topPos;
        int h = 18 + TannyMenu.ORDERS.length * (BTN_H + 1) + 4;
        panel(g, x, y, SIDE_W, h);
        g.fillGradient(x + 3, y + 3, x + SIDE_W - 3, y + 15, PINK, PINK_DK);
        g.drawCenteredString(font, "Órdenes", x + SIDE_W / 2, y + 5, 0xFFFFFFFF);
        for (int i = 0; i < TannyMenu.ORDERS.length; i++) {
            int by = y + 18 + i * (BTN_H + 1);
            button(g, x + 4, by, SIDE_W - 8, BTN_H, ICONS[i] + " " + TannyMenu.ORDERS[i],
                    isIn(mouseX, mouseY, x + 4, by, SIDE_W - 8, BTN_H), false);
        }
    }

    // ------------------------------------------------------------------ dibujo

    private static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 2, y, x + w - 2, y + h, PINK_DK);
        g.fill(x, y + 2, x + w, y + h - 2, PINK_DK);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, PINK_DK);
        g.fillGradient(x + 2, y + 2, x + w - 2, y + h - 2, CREAM, CREAM_DK);
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, String text, boolean hover, boolean big) {
        g.fill(x, y, x + w, y + h, hover ? PINK_DK : 0xFFC98AA3);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, hover ? 0xFFFFB5CE : 0xFFFFF6F9, hover ? PINK : 0xFFF2D7E1);
        int tw = font.width(text);
        if (big || tw <= w - 6) {
            g.drawString(font, text, x + (w - tw) / 2, y + (h - 8) / 2, hover ? 0xFFFFFFFF : TEXT, false);
        } else {
            float s = (w - 6) / (float) tw;
            g.pose().pushPose();
            g.pose().translate(x + 3, y + (h - 8 * s) / 2, 0);
            g.pose().scale(s, s, 1);
            g.drawString(font, text, 0, 0, hover ? 0xFFFFFFFF : TEXT, false);
            g.pose().popPose();
        }
    }

    private static void slotBg(GuiGraphics g, int x, int y, boolean special) {
        g.fill(x, y, x + 18, y + 18, special ? 0xFFC9A3B1 : 0xFFB89AA6);
        g.fill(x, y, x + 17, y + 1, 0xFF6E4457);
        g.fill(x, y, x + 1, y + 17, 0xFF6E4457);
        g.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
    }

    private static void ghost(GuiGraphics g, ItemStack stack, int x, int y, int veil) {
        g.renderFakeItem(stack, x, y);
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        g.fill(x, y, x + 16, y + 16, veil);
        g.pose().popPose();
    }

    /** Corazón pixelado de 7x6. */
    private static void heart(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 1, y, x + 3, y + 1, color);
        g.fill(x + 4, y, x + 6, y + 1, color);
        g.fill(x, y + 1, x + 7, y + 3, color);
        g.fill(x + 1, y + 3, x + 6, y + 4, color);
        g.fill(x + 2, y + 4, x + 5, y + 5, color);
        g.fill(x + 3, y + 5, x + 4, y + 6, color);
    }

    private static boolean isIn(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ------------------------------------------------------------------ textos

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "♡ " + title.getString(), 7, titleLabelY, 0xFFFFFFFF, true);
        TannyEntity t = menu.getTanny();
        if (t != null) {
            g.drawString(font, String.format("♥ %.0f / %.0f", t.getHealth(), t.getMaxHealth()), 82, 20, 0xFF8B1E3F, false);
            String state = t.isWeakened() ? "Debilitada" : t.getMode().displayName();
            g.drawString(font, "Modo: ", 82, 41, TEXT_SOFT, false);
            g.drawString(font, state, 82 + font.width("Modo: "), 41, TEXT, false);
        }
        if (menu.getPage() == TannyMenu.PAGE_BALLS) {
            g.drawString(font, "Bolsa de Poké Balls", 8, TannyMenu.BAG_Y - 10, TEXT, false);
            g.drawString(font, "Solo Poké Balls", 85, 57, TEXT_SOFT, false);
        } else {
            g.drawString(font, "Herramientas", 82, TannyMenu.TOOL_Y - 10, TEXT, false);
            g.drawString(font, "Mochila", 8, TannyMenu.BAG_Y - 10, TEXT, false);
        }
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
    }

    private void renderHints(GuiGraphics g, int mouseX, int mouseY) {
        for (Slot slot : menu.slots) {
            if (slot.hasItem() || !isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) continue;
            String hint = null;
            if (slot instanceof TannyMenu.ToolSlot) hint = "Ranura de " + TOOL_NAMES[slot.getContainerSlot()];
            else if (slot instanceof TannyMenu.ArmorSlot) hint = "Armadura: " + ARMOR_NAMES[slot.getContainerSlot()];
            else if (slot instanceof TannyMenu.BallSlot) hint = "Solo Poké Balls";
            if (hint != null) g.renderTooltip(font, Component.literal(hint), mouseX, mouseY);
        }
        int x = leftPos + imageWidth + SIDE_GAP + 4, y = topPos + 18;
        for (int i = 0; i < TannyMenu.ORDERS.length; i++) {
            if (isIn(mouseX, mouseY, x, y + i * (BTN_H + 1), SIDE_W - 8, BTN_H)) {
                g.renderTooltip(font, Component.literal(ORDER_HELP[i]), mouseX, mouseY);
            }
        }
        if (menu.getPage() == TannyMenu.PAGE_BALLS && isIn(mouseX, mouseY, leftPos + 82, topPos + 66, 86, 22)) {
            g.renderTooltip(font, Component.literal("Abre el PC de Cobblemon: tus cajas de Pokémon"), mouseX, mouseY);
        }
    }

    // ------------------------------------------------------------------ clics

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int tx = leftPos + 4, ty = topPos - TAB_H + 2;
            for (int i = 0; i < TABS.length; i++) {
                int w = tabWidth(i);
                if (isIn(mx, my, tx, ty, w, TAB_H)) {
                    press(i == 0 ? TannyMenu.BTN_PAGE_GEAR : i == 1 ? TannyMenu.BTN_PAGE_BALLS : TannyMenu.BTN_PC);
                    return true;
                }
                tx += w + 2;
            }
            int ox = leftPos + imageWidth + SIDE_GAP + 4, oy = topPos + 18;
            for (int i = 0; i < TannyMenu.ORDERS.length; i++) {
                if (isIn(mx, my, ox, oy + i * (BTN_H + 1), SIDE_W - 8, BTN_H)) {
                    press(TannyMenu.BTN_ORDER + i);
                    return true;
                }
            }
            if (menu.getPage() == TannyMenu.PAGE_BALLS && isIn(mx, my, leftPos + 82, topPos + 66, 86, 22)) {
                press(TannyMenu.BTN_PC);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int left, int top, int button) {
        int sideX = leftPos + imageWidth + SIDE_GAP;
        if (isIn(mx, my, sideX, topPos, SIDE_W, imageHeight) || isIn(mx, my, leftPos, topPos - TAB_H, imageWidth, TAB_H)) return false;
        return super.hasClickedOutside(mx, my, left, top, button);
    }

    private void press(int id) {
        if (minecraft == null || minecraft.gameMode == null) return;
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }
}
