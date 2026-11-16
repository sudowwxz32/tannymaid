package com.totallywaifu.client;

import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.menu.WaifuMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Panel de la waifu: retrato, modos, botones especiales, su mochila y tu inventario. */
public class WaifuScreen extends AbstractContainerScreen<WaifuMenu> {
    private static final int[][] THEME = {
            {0xFF8A6238, 0xFF3A2814, 0xFF54943F},   // Kozue: roble y verde hoja
            {0xFF56565C, 0xFF242428, 0xFFF08C28},   // Homura: piedra y fuego
            {0xFFA27034, 0xFF46300F, 0xFFDEB03C},   // Kura: cofre y oro
            {0xFF3A2A56, 0xFF120C1E, 0xFF5CDCD6},   // Shiori: obsidiana y diamante
            {0xFFEDE6E6, 0xFF8C7F86, 0xFFF4A9C8},   // Lana
            {0xFF4A3A30, 0xFF1A120E, 0xFFF0F0F0},   // Bella
            {0xFFF29CB0, 0xFF7A3848, 0xFFFFD0DC},   // Rosita
            {0xFFF2F0E6, 0xFF8A8270, 0xFFE8402C},   // Pía
            {0xFF8E8A86, 0xFF2C2A28, 0xFFC8342C},   // Luna
            {0xFFE0A050, 0xFF5A3A14, 0xFF7AD06A},   // Mika
            {0xFFC8C4BC, 0xFF4A4844, 0xFFC83228},   // Ferra
            {0xFF8A5A3A, 0xFF2E1E12, 0xFF3CB04C},   // Elena
            {0xFF3E7A48, 0xFF14281A, 0xFF5AB4C8},   // Zoe
            {0xFFBEBEBE, 0xFF3A3A3A, 0xFFE8E8E8},   // Nyra
            {0xFF4EA84A, 0xFF143A14, 0xFF222222},   // Cree
            {0xFF3A2E2E, 0xFF120C0C, 0xFFD02828},   // Aracne
            {0xFF4A2A5A, 0xFF180C20, 0xFF6ACC48},   // Morgana
            {0xFF221A2C, 0xFF08060C, 0xFFD66AF0}};  // Endra
    private Button follow, stay, work;

    public WaifuScreen(WaifuMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 242;
        inventoryLabelY = WaifuMenu.INV_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos + 64, y = topPos + 18;
        follow = addRenderableWidget(btn("Seguir", WaifuMenu.B_FOLLOW, x, y, "Te sigue a todas partes (y te defiende)"));
        stay = addRenderableWidget(btn("Quieta", WaifuMenu.B_STAY, x + 36, y, "Se queda aquí sin moverse"));
        work = addRenderableWidget(btn("Trabajar", WaifuMenu.B_WORK, x + 72, y, "Hace su oficio alrededor de este lugar"));
        int y2 = y + 22;
        switch (menu.kind) {
            case KURA -> addRenderableWidget(btn("Ordenar", WaifuMenu.B_SORT, x, y2, "Junta montones y ordena su cofre por tipo"));
            case SHIORI -> addRenderableWidget(btn("Dar XP", WaifuMenu.B_XP, x, y2, "Te entrega la experiencia que ha guardado"));
            default -> { }
        }
        addRenderableWidget(btn("♥ Guardar", WaifuMenu.B_HEART, leftPos + 116, y2, "La guarda en tu colección ♥ Mis Waifus (pestaña del inventario) con todas sus cosas"));
    }

    private Button btn(String text, int id, int x, int y, String tip) {
        int w = Math.max(34, font.width(text) + 8);
        if (id <= WaifuMenu.B_WORK) w = id == WaifuMenu.B_WORK ? 40 : 34;
        return Button.builder(Component.literal(text), b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                .bounds(x, y, w, 18).tooltip(Tooltip.create(Component.literal(tip))).build();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        int m = menu.mode();
        follow.active = m != WaifuEntity.FOLLOW;
        stay.active = m != WaifuEntity.STAY;
        work.active = m != WaifuEntity.WORK;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int[] th = menu.kind.ordinal() < THEME.length ? THEME[menu.kind.ordinal()]
                : new int[]{0xFF000000 | ((menu.kind.eggA >> 1) & 0x7F7F7F), 0xFF000000 | ((menu.kind.eggA >> 2) & 0x3F3F3F), 0xFF000000 | menu.kind.eggB};
        int x = leftPos, y = topPos;
        g.fill(x - 1, y - 1, x + imageWidth + 1, y + imageHeight + 1, th[1]);
        g.fill(x, y, x + imageWidth, y + imageHeight, th[2]);
        g.fillGradient(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, th[0], th[1]);
        g.fill(x + 1, y + 14, x + imageWidth - 1, y + 15, th[2]);
        // retrato
        g.fill(x + 6, y + 18, x + 60, y + 86, 0x60000000);
        if (menu.waifu != null) InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 33, y + 82, 30, (float) (x + 33 - mx), (float) (y + 36 - my), menu.waifu);
        // información
        String modeName = switch (menu.mode()) { case WaifuEntity.STAY -> "Quieta"; case WaifuEntity.WORK -> "Trabajando"; default -> "Siguiéndote"; };
        g.drawString(font, "§f" + modeName, x + 64, y + 62, 0xFFFFE0F0, false);
        if (menu.kind == WaifuKind.SHIORI) g.drawString(font, "XP guardada: §a" + menu.xp(), x + 64, y + 74, 0xFFFFE0F0, false);
        if (menu.kind == WaifuKind.KOZUE || menu.kind == WaifuKind.HOMURA) {
            g.drawString(font, menu.kind == WaifuKind.HOMURA ? "Cocinar →" : "Pedido →", x + (menu.kind == WaifuKind.HOMURA ? 98 : 106), y + 74, 0xFFFFE0F0, false);
            g.fill(x + WaifuMenu.ORDER_X - 2, y + WaifuMenu.ORDER_Y - 2, x + WaifuMenu.ORDER_X + 18, y + WaifuMenu.ORDER_Y + 18, th[2]);
        }
        g.drawString(font, "Mochila §7(la 1.ª fila es su reserva)", x + 8, y + WaifuMenu.BAG_Y - 10, 0xFFFFE0F0, false);
        for (Slot s : menu.slots) {
            g.fill(x + s.x - 1, y + s.y - 1, x + s.x + 17, y + s.y + 17, 0x80000000);
            g.fill(x + s.x, y + s.y, x + s.x + 16, y + s.y + 16, 0x60201820);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, "♥ " + title.getString(), 6, 4, 0xFFFFFFFF, true);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xFFFFE0F0, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        super.render(g, mx, my, partial);
        if ((menu.kind == WaifuKind.KOZUE || menu.kind == WaifuKind.HOMURA) && mx >= leftPos + WaifuMenu.ORDER_X && mx < leftPos + WaifuMenu.ORDER_X + 16
                && my >= topPos + WaifuMenu.ORDER_Y && my < topPos + WaifuMenu.ORDER_Y + 16 && menu.getCarried().isEmpty()
                && !menu.slots.get(63).hasItem()) {
            g.renderTooltip(font, font.split(Component.literal(menu.kind == WaifuKind.HOMURA
                    ? "§cQué cocinar: pon aquí un objeto (ej. hierro crudo) y Homura cocinará SOLO eso, tomándolo de su mochila. También puedes ponerlo en su horno (clic derecho)."
                    : "§6Pedido: pon aquí CUALQUIER objeto y Kozue lo fabricará (y las piezas intermedias) con su mochila y el cofre de Kura"), 180), mx, my);
        }
        renderTooltip(g, mx, my);
    }
}
