package com.tanny.tannymaid.client;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.entity.TannyMode;
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
 * Pantalla de Tanny, estilo elegante (carbón y dorado): pestañas (Equipo · Poké Balls · PC),
 * su modelo 3D, vida, armadura y modo, armadura, armas/herramientas, mochila y un panel de órdenes
 * agrupado (Modo · Acciones · Pokémon) que marca el modo activo.
 */
public class TannyScreen extends AbstractContainerScreen<TannyMenu> {
    // Paleta elegante
    private static final int BG_TOP = 0xFF26222E, BG_BOT = 0xFF17151C, GOLD = 0xFFC9A45C, GOLD_DK = 0xFF8A6E36,
            GOLD_HI = 0xFFE8CB8A, EDGE = 0xFF0C0B10, TEXT = 0xFFEDE3CF, TEXT_SOFT = 0xFFA79D8A, PANEL = 0xFF1F1C26,
            SLOT = 0xFF2F2A37, SLOT_SPECIAL = 0xFF3A3144, SLOT_SH = 0xFF121016, SLOT_HI = 0xFF4C4558;

    private static final ItemStack[] GHOSTS = {
            new ItemStack(Items.IRON_PICKAXE), new ItemStack(Items.IRON_SHOVEL), new ItemStack(Items.IRON_AXE),
            new ItemStack(Items.IRON_SWORD), new ItemStack(Items.SHIELD), new ItemStack(Items.BOW)
    };
    private static final String[] TOOL_NAMES = {"pico", "pala", "hacha", "espada", "escudo", "arco (usa flechas de la mochila)"};
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
    private static final TannyMode[] ORDER_MODE = {TannyMode.FOLLOW, TannyMode.STAY, TannyMode.DEFEND, TannyMode.FARM, TannyMode.HOME};
    /** Grupos del panel de órdenes: título y los índices de TannyMenu.ORDERS. */
    private static final String[] GROUPS = {"Modo", "Acciones", "Pokémon"};
    private static final int[][] GROUP_ITEMS = {{0, 1, 2, 3, 4}, {5, 6, 7, 10, 11, 12}, {8, 9}};

    private static final int SIDE_W = 92, SIDE_GAP = 4, BTN_H = 14, TAB_H = 16, HEAD_H = 11;
    private ItemStack ballGhost = ItemStack.EMPTY;

    public TannyScreen(TannyMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 194;
        this.imageHeight = 254;
        this.inventoryLabelX = TannyMenu.INV_X;
        this.inventoryLabelY = TannyMenu.PLAYER_Y - 11;
        this.titleLabelY = 5;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos -= (SIDE_W + SIDE_GAP) / 2;
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

        panel(g, x, y, imageWidth, imageHeight);
        header(g, x, y, imageWidth);

        // Marco del modelo con filete dorado
        int mx0 = x + 27, my0 = y + 17, mx1 = x + 77, my1 = y + 92;
        g.fill(mx0 - 1, my0 - 1, mx1 + 1, my1 + 1, GOLD_DK);
        g.fillGradient(mx0, my0, mx1, my1, 0xFF2E2838, 0xFF121016);
        g.fill(mx0, my1 - 9, mx1, my1, 0x40000000);
        g.fill(mx0 + 6, my1 - 6, mx1 - 6, my1 - 5, 0x55C9A45C);     // "suelo"

        TannyEntity t = menu.getTanny();
        int barX = x + 82, barY = y + 30, barW = 104, barH = 6;
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, GOLD_DK);
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF2A1218);
        if (t != null) {
            float pct = Math.max(0.0F, Math.min(1.0F, t.getHealth() / t.getMaxHealth()));
            int color = t.isWeakened() ? 0xFF7A7A7A : pct > 0.5F ? 0xFFB8323F : pct > 0.25F ? 0xFFC9822E : 0xFFD43030;
            g.fill(barX, barY, barX + (int) (barW * pct), barY + barH, color);
            g.fill(barX, barY, barX + (int) (barW * pct), barY + 2, 0x44FFFFFF);
        }

        for (Slot slot : menu.slots) {
            boolean special = slot instanceof TannyMenu.BallSlot || slot instanceof TannyMenu.ArmorSlot || slot instanceof TannyMenu.ToolSlot;
            slotBg(g, x + slot.x - 1, y + slot.y - 1, special);
        }

        if (balls) {
            g.fill(x + 81, y + 44, x + 187, y + 92, PANEL);
            g.fill(x + 81, y + 44, x + 187, y + 45, GOLD_DK);
            int total = 0;
            for (Slot s : menu.slots) if (s instanceof TannyMenu.BallSlot && s.hasItem()) total += s.getItem().getCount();
            g.drawString(font, "Poké Balls: " + total, x + 86, y + 49, TEXT, false);
            button(g, x + 84, y + 66, 100, 22, "▣ Abrir mis cajas", isIn(mouseX, mouseY, x + 84, y + 66, 100, 22), false);
            if (!ballGhost.isEmpty()) {
                for (Slot s : menu.slots) {
                    if (s instanceof TannyMenu.BallSlot && !s.hasItem()) ghost(g, ballGhost, x + s.x, y + s.y);
                }
            }
        } else {
            g.fill(x + TannyMenu.TOOL_X, y + TannyMenu.TOOL_Y - 1, x + TannyMenu.TOOL_X + 18 * TannyMenu.TOOL_COUNT, y + TannyMenu.TOOL_Y, GOLD_DK);
            for (int i = 0; i < TannyMenu.TOOL_COUNT; i++) {
                Slot slot = menu.slots.get(menu.toolStart() + i);
                if (!slot.hasItem()) ghost(g, GHOSTS[i], x + slot.x, y + slot.y);
            }
        }

        if (t != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 52, y + 86, 30,
                    (float) (x + 52 - mouseX), (float) (y + 36 - mouseY), t);
        }
        renderOrders(g, mouseX, mouseY, t);
    }

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        int x = leftPos + 6, y = topPos - TAB_H + 2;
        int active = menu.getPage() == TannyMenu.PAGE_BALLS ? 1 : 0;
        for (int i = 0; i < TABS.length; i++) {
            int w = tabWidth(i);
            boolean on = i == active, hover = isIn(mouseX, mouseY, x, y, w, TAB_H);
            g.fill(x, y, x + w, y + TAB_H + (on ? 2 : 0), on ? GOLD : EDGE);
            g.fillGradient(x + 1, y + 1, x + w - 1, y + TAB_H + (on ? 2 : -1), on ? BG_TOP : hover ? 0xFF2C2834 : 0xFF1A1820,
                    on ? BG_TOP : 0xFF141218);
            g.drawCenteredString(font, TABS[i], x + w / 2, y + 4, on ? GOLD_HI : hover ? TEXT : TEXT_SOFT);
            x += w + 2;
        }
    }

    private int tabWidth(int i) {
        return font.width(TABS[i]) + 16;
    }

    private int sideX() {
        return leftPos + imageWidth + SIDE_GAP;
    }

    /** Recorre los botones de órdenes: devuelve el índice en el que está el ratón, o -1. Si g != null los dibuja. */
    private int walkOrders(GuiGraphics g, double mouseX, double mouseY, TannyEntity t) {
        int x = sideX() + 5, y = topPos + 18, hit = -1;
        for (int gi = 0; gi < GROUPS.length; gi++) {
            if (g != null) {
                g.drawString(font, GROUPS[gi], x + 1, y + 2, GOLD, false);
                g.fill(x + font.width(GROUPS[gi]) + 4, y + 6, x + SIDE_W - 10, y + 7, 0x55C9A45C);
            }
            y += HEAD_H;
            for (int idx : GROUP_ITEMS[gi]) {
                boolean hover = isIn(mouseX, mouseY, x, y, SIDE_W - 10, BTN_H);
                if (hover) hit = idx;
                if (g != null) {
                    boolean active = t != null && idx < ORDER_MODE.length && t.getMode() == ORDER_MODE[idx] && !t.isWeakened();
                    button(g, x, y, SIDE_W - 10, BTN_H, ICONS[idx] + " " + TannyMenu.ORDERS[idx], hover, active);
                }
                y += BTN_H + 1;
            }
            y += 3;
        }
        return hit;
    }

    private int ordersHeight() {
        int h = 18;
        for (int[] items : GROUP_ITEMS) h += HEAD_H + items.length * (BTN_H + 1) + 3;
        return h + 4;
    }

    private void renderOrders(GuiGraphics g, int mouseX, int mouseY, TannyEntity t) {
        int x = sideX(), y = topPos;
        panel(g, x, y, SIDE_W, ordersHeight());
        header(g, x, y, SIDE_W);
        g.drawCenteredString(font, "Órdenes", x + SIDE_W / 2, y + 5, GOLD_HI);
        walkOrders(g, mouseX, mouseY, t);
    }

    // ------------------------------------------------------------------ dibujo

    private static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 1, y, x + w - 1, y + h, EDGE);
        g.fill(x, y + 1, x + w, y + h - 1, EDGE);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, GOLD_DK);
        g.fillGradient(x + 2, y + 2, x + w - 2, y + h - 2, BG_TOP, BG_BOT);
        // esquinas doradas
        for (int[] c : new int[][] {{x + 3, y + 3}, {x + w - 6, y + 3}, {x + 3, y + h - 6}, {x + w - 6, y + h - 6}}) {
            g.fill(c[0], c[1], c[0] + 3, c[1] + 3, GOLD);
        }
    }

    private static void header(GuiGraphics g, int x, int y, int w) {
        g.fillGradient(x + 2, y + 2, x + w - 2, y + 15, 0xFF3A3046, 0xFF241E2C);
        g.fill(x + 2, y + 15, x + w - 2, y + 16, GOLD_DK);
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, String text, boolean hover, boolean active) {
        int border = active ? GOLD_HI : hover ? GOLD : 0xFF3E3848;
        g.fill(x, y, x + w, y + h, border);
        int top = active ? 0xFF8C7340 : hover ? 0xFF3A3346 : 0xFF2A2633;
        int bot = active ? 0xFF5E4C28 : hover ? 0xFF2A2433 : 0xFF1C1A22;
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, top, bot);
        int color = active ? 0xFFFFF4DA : hover ? GOLD_HI : TEXT;
        int tw = font.width(text);
        if (tw <= w - 6) {
            g.drawString(font, text, x + 4, y + (h - 8) / 2, color, false);
        } else {
            float s = (w - 6) / (float) tw;
            g.pose().pushPose();
            g.pose().translate(x + 3, y + (h - 8 * s) / 2, 0);
            g.pose().scale(s, s, 1);
            g.drawString(font, text, 0, 0, color, false);
            g.pose().popPose();
        }
    }

    private static void slotBg(GuiGraphics g, int x, int y, boolean special) {
        g.fill(x, y, x + 18, y + 18, special ? SLOT_SPECIAL : SLOT);
        g.fill(x, y, x + 17, y + 1, SLOT_SH);
        g.fill(x, y, x + 1, y + 17, SLOT_SH);
        g.fill(x + 1, y + 17, x + 18, y + 18, SLOT_HI);
        g.fill(x + 17, y + 1, x + 18, y + 18, SLOT_HI);
    }

    private static void ghost(GuiGraphics g, ItemStack stack, int x, int y) {
        g.renderFakeItem(stack, x, y);
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        g.fill(x, y, x + 16, y + 16, 0xC0302A38);
        g.pose().popPose();
    }

    private static boolean isIn(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ------------------------------------------------------------------ textos

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "✦ " + title.getString(), 7, titleLabelY, GOLD_HI, false);
        TannyEntity t = menu.getTanny();
        if (t != null) {
            g.drawString(font, String.format("♥ %.0f / %.0f", t.getHealth(), t.getMaxHealth()), 82, 20, TEXT, false);
            String armor = "◈ " + t.getArmorValue();
            g.drawString(font, armor, 186 - font.width(armor), 20, TEXT_SOFT, false);
            String state = t.isWeakened() ? "Debilitada" : t.getMode().displayName();
            g.drawString(font, "Modo:", 82, 41, TEXT_SOFT, false);
            g.drawString(font, state, 82 + font.width("Modo: "), 41, GOLD_HI, false);
        }
        if (menu.getPage() == TannyMenu.PAGE_BALLS) {
            g.drawString(font, "Bolsa de Poké Balls", TannyMenu.INV_X, TannyMenu.BAG_Y - 10, TEXT, false);
            g.drawString(font, "Solo Poké Balls", 86, 57, TEXT_SOFT, false);
        } else {
            g.drawString(font, "Armas y herramientas", 82, TannyMenu.TOOL_Y - 10, TEXT, false);
            g.drawString(font, "Mochila", TannyMenu.INV_X, TannyMenu.BAG_Y - 10, TEXT, false);
        }
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_SOFT, false);
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
        int hit = walkOrders(null, mouseX, mouseY, null);
        if (hit >= 0) g.renderTooltip(font, Component.literal(ORDER_HELP[hit]), mouseX, mouseY);
        if (menu.getPage() == TannyMenu.PAGE_BALLS && isIn(mouseX, mouseY, leftPos + 84, topPos + 66, 100, 22)) {
            g.renderTooltip(font, Component.literal("Abre el PC de Cobblemon: tus cajas de Pokémon"), mouseX, mouseY);
        }
    }

    // ------------------------------------------------------------------ clics

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            int tx = leftPos + 6, ty = topPos - TAB_H + 2;
            for (int i = 0; i < TABS.length; i++) {
                int w = tabWidth(i);
                if (isIn(mx, my, tx, ty, w, TAB_H)) {
                    press(i == 0 ? TannyMenu.BTN_PAGE_GEAR : i == 1 ? TannyMenu.BTN_PAGE_BALLS : TannyMenu.BTN_PC);
                    return true;
                }
                tx += w + 2;
            }
            int hit = walkOrders(null, mx, my, null);
            if (hit >= 0) {
                press(TannyMenu.BTN_ORDER + hit);
                return true;
            }
            if (menu.getPage() == TannyMenu.PAGE_BALLS && isIn(mx, my, leftPos + 84, topPos + 66, 100, 22)) {
                press(TannyMenu.BTN_PC);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int left, int top, int button) {
        if (isIn(mx, my, sideX(), topPos, SIDE_W, ordersHeight()) || isIn(mx, my, leftPos, topPos - TAB_H, imageWidth, TAB_H)) return false;
        return super.hasClickedOutside(mx, my, left, top, button);
    }

    private void press(int id) {
        if (minecraft == null || minecraft.gameMode == null) return;
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }
}
