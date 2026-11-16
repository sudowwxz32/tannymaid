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
    private static final int COLS = 6, CW = 64, CH = 70, GAP = 4;
    private static final String WAND = "Vara de Transformación sobre ";
    private static final String[] HOW = {
            WAND + "una mesa de crafteo (puede volver a ser mesa)",
            WAND + "un horno (puede volver a ser horno)",
            WAND + "un cofre (puede volver a ser cofre)",
            WAND + "una mesa de encantamientos (puede volver a ser mesa)",
            WAND + "una oveja · para siempre", WAND + "una vaca · para siempre", WAND + "un cerdo · para siempre",
            WAND + "una gallina · para siempre", WAND + "un lobo/perro · para siempre", WAND + "un gato · para siempre",
            WAND + "un gólem de hierro · para siempre", WAND + "un aldeano · para siempre (conserva sus tratos)",
            WAND + "un zombi · para siempre", WAND + "un esqueleto · para siempre", WAND + "un creeper · para siempre",
            WAND + "una araña · para siempre", WAND + "una bruja · para siempre", WAND + "un enderman · para siempre"};
    private static final int[] COLOR = {0xFF8A6238, 0xFF5A5A62, 0xFFA27034, 0xFF3A2A56,
            0xFFB8A8B0, 0xFF4A3A30, 0xFFC07080, 0xFFB0A890, 0xFF6E6A66, 0xFFB07830,
            0xFF8C8880, 0xFF6A4A30, 0xFF2E6A3A, 0xFF7A7A7A, 0xFF3E8A3A, 0xFF4A2E2E, 0xFF4A2A5A, 0xFF2A1A3C};
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

    private int rows() { return (WaifuKind.values().length + COLS - 1) / COLS; }
    private int left() { return (width - (COLS * CW + (COLS - 1) * GAP)) / 2; }
    private int top() { return Math.max(18, (height - (rows() * CH + (rows() - 1) * GAP)) / 2 + 6); }
    private int cx(WaifuKind k) { return left() + (k.ordinal() % COLS) * (CW + GAP); }
    private int cy(WaifuKind k) { return top() + (k.ordinal() / COLS) * (CH + GAP); }

    @Override
    protected void init() {
        for (CollectionData.Entry e : data.entries()) {
            WaifuKind k = WaifuKind.byId(e.kind());
            int x = cx(k), y = cy(k) + CH - 15;
            if (e.status() == 0 || e.status() == 3) continue;
            Button call = Button.builder(Component.literal(e.status() == 2 ? "Llamar" : "Traer"),
                            b -> Net.CHANNEL.sendToServer(new CollectionAction(k.ordinal(), CollectionAction.SUMMON)))
                    .bounds(x + 2, y, 38, 13).tooltip(Tooltip.create(Component.literal(e.status() == 2 ? "Aparece a tu lado" : "Viene a tu lado"))).build();
            Button store = Button.builder(Component.literal("⬇"), b -> Net.CHANNEL.sendToServer(new CollectionAction(k.ordinal(), CollectionAction.STORE)))
                    .bounds(x + 42, y, CW - 44, 13).tooltip(Tooltip.create(Component.literal("Guardar en tu colección con todas sus cosas"))).build();
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
        int got = 0;
        for (CollectionData.Entry e : data.entries()) if (e.status() != 0) got++;
        g.drawCenteredString(font, "§d§l♥ Mis Waifus §r§7(" + got + "/" + data.entries().size() + ")", width / 2, top() - 13, 0xFFFFFFFF);
        String tip = null;
        for (CollectionData.Entry e : data.entries()) {
            WaifuKind k = WaifuKind.byId(e.kind());
            int x = cx(k), y = cy(k);
            int c = COLOR[k.ordinal() % COLOR.length];
            boolean hover = mx >= x && mx < x + CW && my >= y && my < y + CH - 16;
            g.fill(x - 1, y - 1, x + CW + 1, y + CH + 1, hover ? 0xFFFFFFFF : 0xFFFFC0E0);
            g.fillGradient(x, y, x + CW, y + CH, c, 0xFF1A1420);
            g.drawCenteredString(font, k.color + k.display, x + CW / 2, y + 2, 0xFFFFFFFF);
            WaifuEntity d = doll(k);
            if (e.status() == 0) {
                g.drawCenteredString(font, "§8?", x + CW / 2, y + 26, 0xFFFFFFFF);
                g.drawCenteredString(font, "§7Bloqueada", x + CW / 2, y + CH - 12, 0xFFFFFFFF);
                if (hover) tip = k.role + " · " + HOW[k.ordinal()];
            } else {
                if (d != null) {
                    g.enableScissor(x, y + 11, x + CW, y + CH - 15);
                    InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + CW / 2, y + CH - 16, 20, (float) (x + CW / 2 - mx), (float) (y + 20 - my), d);
                    g.disableScissor();
                }
                if (e.status() == 3) g.drawCenteredString(font, "§6Es un bloque", x + CW / 2, y + CH - 12, 0xFFFFFFFF);
                if (hover) tip = k.role + " · " + k.job + (e.status() == 2 ? " · §bGuardada" : e.status() == 3 ? " · §6Ahora es un bloque: usa la vara para despertarla"
                        : e.where().isEmpty() ? "" : " · §a" + e.where());
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
