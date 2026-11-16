package com.totallywaifu.client;

import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.network.CollectionAction;
import com.totallywaifu.network.CollectionData;
import com.totallywaifu.network.Net;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;

/** ♥ Mis Waifus: las chicas que has conseguido, dónde están, y botones para llamarlas o guardarlas. */
public class CollectionScreen extends Screen {
    private static final int CW = 82, CH = 150, GAP = 6;
    private static final String[] HOW = {
            "Corazón de Kozue: 3 tablones + tinte rosa (o el regalo de bienvenida)",
            "Corazón de Homura: 8 piedra labrada + tinte rosa en el centro",
            "Corazón de Kura: 8 tablones + tinte rosa en el centro",
            "Corazón de Shiori: libro, 2 diamantes, tinte rosa y 3 obsidiana"};
    private static final int[] COLOR = {0xFF8A6238, 0xFF5A5A62, 0xFFA27034, 0xFF3A2A56};
    private CollectionData data;
    private final Map<WaifuKind, WaifuEntity> dolls = new EnumMap<>(WaifuKind.class);

    private CollectionScreen(CollectionData d) {
        super(Component.literal("♥ Mis Waifus"));
        data = d;
    }

    public static void show(CollectionData d) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CollectionScreen s) {
            s.data = d;
            s.rebuildWidgets();
        } else {
            mc.setScreen(new CollectionScreen(d));
        }
    }

    private int left() { return (width - (4 * CW + 3 * GAP)) / 2; }
    private int top() { return (height - CH) / 2 + 6; }

    @Override
    protected void init() {
        for (CollectionData.Entry e : data.entries()) {
            WaifuKind k = WaifuKind.byId(e.kind());
            int x = left() + k.ordinal() * (CW + GAP), y = top() + CH - 44;
            if (e.status() == 0) continue;
            Button call = Button.builder(Component.literal(e.status() == 2 ? "Llamar" : "Traer"),
                            b -> Net.CHANNEL.sendToServer(new CollectionAction(k.ordinal(), CollectionAction.SUMMON)))
                    .bounds(x + 4, y, CW - 8, 18).tooltip(Tooltip.create(Component.literal(e.status() == 2 ? "Aparece a tu lado" : "Viene a tu lado"))).build();
            Button store = Button.builder(Component.literal("Guardar"), b -> Net.CHANNEL.sendToServer(new CollectionAction(k.ordinal(), CollectionAction.STORE)))
                    .bounds(x + 4, y + 20, CW - 8, 18).tooltip(Tooltip.create(Component.literal("Se guarda en tu colección con todas sus cosas"))).build();
            store.active = e.status() == 1;
            addRenderableWidget(call);
            addRenderableWidget(store);
        }
    }

    private WaifuEntity doll(WaifuKind k) {
        return dolls.computeIfAbsent(k, kk -> ModRegistry.TYPES.get(kk).get().create(Minecraft.getInstance().level));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        g.drawCenteredString(font, "§d§l♥ Mis Waifus", width / 2, top() - 18, 0xFFFFFFFF);
        String tip = null;
        for (CollectionData.Entry e : data.entries()) {
            WaifuKind k = WaifuKind.byId(e.kind());
            int x = left() + k.ordinal() * (CW + GAP), y = top();
            int c = COLOR[k.ordinal()];
            g.fill(x - 1, y - 1, x + CW + 1, y + CH + 1, 0xFFFFC0E0);
            g.fillGradient(x, y, x + CW, y + CH, c, 0xFF1A1420);
            g.drawCenteredString(font, k.color + "§l" + k.display, x + CW / 2, y + 4, 0xFFFFFFFF);
            g.drawCenteredString(font, "§7" + k.role, x + CW / 2, y + 14, 0xFFFFFFFF);
            WaifuEntity d = doll(k);
            if (e.status() == 0) {
                g.drawCenteredString(font, "§8?", x + CW / 2, y + 50, 0xFFFFFFFF);
                g.drawCenteredString(font, "§7No la tienes", x + CW / 2, y + 74, 0xFFFFFFFF);
                g.drawCenteredString(font, "§8(pasa el ratón)", x + CW / 2, y + 86, 0xFFFFFFFF);
                if (mx >= x && mx < x + CW && my >= y && my < y + CH) tip = HOW[k.ordinal()];
            } else {
                if (d != null) InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + CW / 2, y + 96, 30, (float) (x + CW / 2 - mx), (float) (y + 40 - my), d);
                String st = e.status() == 2 ? "§bGuardada" : "§aEn el mundo";
                g.drawCenteredString(font, st, x + CW / 2, y + 98, 0xFFFFFFFF);
                if (!e.where().isEmpty() && mx >= x && mx < x + CW && my >= y && my < y + 100) tip = e.where();
            }
        }
        super.render(g, mx, my, partial);
        if (tip != null) g.renderTooltip(font, font.split(Component.literal(tip), 180), mx, my);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
