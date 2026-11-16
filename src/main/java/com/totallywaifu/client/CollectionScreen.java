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
import java.util.HashMap;
import java.util.Map;

/** ♥ Mis Waifus: todas las chicas (en páginas), dónde están, y botones para llamarlas o guardarlas. */
public class CollectionScreen extends Screen {
    private static final int COLS = 6, ROWS = 3, PER = COLS * ROWS, CW = 64, CH = 70, GAP = 4;
    private static final String WAND = "Vara de Transformación sobre ";
    private static final Map<WaifuKind, String> HOW = new EnumMap<>(WaifuKind.class);

    static {
        String[][] h = {{"KOZUE", "una mesa de crafteo (reversible)"}, {"HOMURA", "un horno (reversible)"}, {"KURA", "un cofre (reversible)"},
                {"SHIORI", "una mesa de encantamientos (reversible)"}, {"CAMA", "una cama (reversible)"}, {"OVEJA", "una oveja"}, {"VACA", "una vaca"},
                {"CERDO", "un cerdo"}, {"GALLINA", "una gallina"}, {"PERRO", "un lobo o perro"}, {"GATO", "un gato u ocelote"}, {"GOLEM", "un gólem de hierro"},
                {"ALDEANA", "un aldeano (conserva sus tratos)"}, {"ZOMBIE", "un zombi"}, {"ESQUELETO", "un esqueleto"}, {"CREEPER", "un creeper"},
                {"ARANA", "una araña"}, {"BRUJA", "una bruja"}, {"ENDERMAN", "un enderman"}, {"SLIME", "un slime"}, {"CONEJO", "un conejo"},
                {"PANDA", "un panda"}, {"CABALLO", "un caballo, burro o mula"}, {"ABEJA", "una abeja"}, {"PIGLIN", "un piglin (Nether)"},
                {"WITHER", "un esqueleto del Nether"}, {"BLAZE", "un blaze (Nether)"}, {"GHAST", "un ghast (Nether)"}, {"DINA", "un bloque de TNT (¡salen las dos!)"},
                {"MITA", "un bloque de TNT (¡salen las dos!)"}, {"WARDEN", "un warden (¡con cuidado!)"}, {"ZORRO", "un zorro"}, {"AJOLOTE", "un ajolote"},
                {"ALLAY", "un allay"}, {"RANA", "una rana"}};
        for (String[] e : h) HOW.put(WaifuKind.valueOf(e[0]), WAND + e[1] + (WaifuKind.valueOf(e[0]).station() ? "" : " · para siempre"));
    }

    private static int page;
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

    private static int pages() { return (WaifuKind.ORDER.length + PER - 1) / PER; }
    private int left() { return (width - (COLS * CW + (COLS - 1) * GAP)) / 2; }
    private int top() { return Math.max(22, (height - (ROWS * CH + (ROWS - 1) * GAP)) / 2 + 4); }

    /** Posición de la chica en la página actual, o -1 si está en otra. */
    private int slot(WaifuKind k) {
        for (int i = 0; i < WaifuKind.ORDER.length; i++) if (WaifuKind.ORDER[i] == k) return i / PER == page ? i % PER : -1;
        return -1;
    }

    private int cx(int s) { return left() + (s % COLS) * (CW + GAP); }
    private int cy(int s) { return top() + (s / COLS) * (CH + GAP); }

    @Override
    protected void init() {
        page = Math.min(page, pages() - 1);
        for (CollectionData.Entry e : data.entries()) {
            WaifuKind k = WaifuKind.byId(e.kind());
            int s = slot(k);
            if (s < 0 || e.status() <= 0 || e.status() == 3) continue;
            int x = cx(s), y = cy(s) + CH - 15;
            Button call = Button.builder(Component.literal(e.status() == 2 ? "Llamar" : "Traer"),
                            b -> Net.CHANNEL.sendToServer(new CollectionAction(k.ordinal(), CollectionAction.SUMMON)))
                    .bounds(x + 2, y, 38, 13).tooltip(Tooltip.create(Component.literal(e.status() == 2 ? "Aparece a tu lado" : "Viene a tu lado"))).build();
            Button store = Button.builder(Component.literal("⬇"), b -> Net.CHANNEL.sendToServer(new CollectionAction(k.ordinal(), CollectionAction.STORE)))
                    .bounds(x + 42, y, CW - 44, 13).tooltip(Tooltip.create(Component.literal("Guardar en tu colección con todas sus cosas"))).build();
            store.active = e.status() == 1;
            addRenderableWidget(call);
            addRenderableWidget(store);
        }
        int by = top() + ROWS * (CH + GAP) + 2;
        Button prev = addRenderableWidget(Button.builder(Component.literal("◀"), b -> { page--; rebuildWidgets(); }).bounds(left(), by, 20, 16).build());
        Button next = addRenderableWidget(Button.builder(Component.literal("▶"), b -> { page++; rebuildWidgets(); })
                .bounds(left() + COLS * (CW + GAP) - GAP - 20, by, 20, 16).build());
        prev.active = page > 0;
        next.active = page < pages() - 1;
        if (data.home()) {
            addRenderableWidget(Button.builder(Component.literal("§9☾ Volver a donde dormí con Yume"), b -> {
                        Net.CHANNEL.sendToServer(new CollectionAction(WaifuKind.CAMA.ordinal(), CollectionAction.HOME));
                        onClose();
                    }).bounds(width / 2 - 85, by, 170, 16)
                    .tooltip(Tooltip.create(Component.literal("Te transporta al último lugar donde dormiste con Yume (en forma de chica o de cama)"))).build());
        }
    }

    private WaifuEntity doll(WaifuKind k) {
        return dolls.computeIfAbsent(k, kk -> ModRegistry.TYPES.get(kk).get().create(Minecraft.getInstance().level));
    }

    private static final Map<WaifuKind, Integer> COLOR = new HashMap<>();

    private static int color(WaifuKind k) {
        return COLOR.computeIfAbsent(k, kk -> 0xFF000000 | ((kk.eggA >> 1) & 0x7F7F7F));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        int got = 0, total = 0;
        for (CollectionData.Entry e : data.entries()) {
            if (WaifuKind.byId(e.kind()).surprise()) continue;
            total++;
            if (e.status() > 0) got++;
        }
        g.drawCenteredString(font, "§d§l♥ Mis Waifus §r§7(" + got + "/" + total + ")" + (data.surprise() ? " §6✦ ¡Colección completa!" : "")
                + " §8· página " + (page + 1) + "/" + pages(), width / 2, top() - 14, 0xFFFFFFFF);
        String tip = null;
        for (CollectionData.Entry e : data.entries()) {
            WaifuKind k = WaifuKind.byId(e.kind());
            int s = slot(k);
            if (s < 0) continue;
            int x = cx(s), y = cy(s);
            boolean hover = mx >= x && mx < x + CW && my >= y && my < y + CH - 16;
            boolean locked = e.status() == -1;
            int c = locked ? 0xFF2A1240 : color(k);
            g.fill(x - 1, y - 1, x + CW + 1, y + CH + 1, hover ? 0xFFFFFFFF : locked ? 0xFFB57AFF : 0xFFFFC0E0);
            g.fillGradient(x, y, x + CW, y + CH, c, 0xFF1A1420);
            if (locked) {
                g.drawCenteredString(font, "§d✦ Sorpresa ✦", x + CW / 2, y + 3, 0xFFFFFFFF);
                g.drawCenteredString(font, "§5?", x + CW / 2, y + 28, 0xFFFFFFFF);
                g.drawCenteredString(font, "§8completa la", x + CW / 2, y + CH - 22, 0xFFFFFFFF);
                g.drawCenteredString(font, "§8colección", x + CW / 2, y + CH - 12, 0xFFFFFFFF);
                if (hover) tip = "Waifu sorpresa: se desbloquea cuando completas tu colección";
                continue;
            }
            g.drawCenteredString(font, k.color + k.display, x + CW / 2, y + 2, 0xFFFFFFFF);
            if (e.status() == 0) {
                g.drawCenteredString(font, "§8?", x + CW / 2, y + 26, 0xFFFFFFFF);
                g.drawCenteredString(font, "§7Bloqueada", x + CW / 2, y + CH - 12, 0xFFFFFFFF);
                if (hover) tip = k.role + " · " + HOW.getOrDefault(k, "");
            } else {
                WaifuEntity d = doll(k);
                if (d != null) {
                    g.enableScissor(x, y + 11, x + CW, y + CH - 15);
                    int size = (int) (20 / Math.max(1.0F, k.scale * 0.85F));
                    InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + CW / 2, y + CH - 16, size, (float) (x + CW / 2 - mx), (float) (y + 20 - my), d);
                    g.disableScissor();
                }
                if (e.status() == 3) g.drawCenteredString(font, "§6Es un bloque", x + CW / 2, y + CH - 12, 0xFFFFFFFF);
                if (hover) tip = k.role + " · " + k.job + (e.status() == 2 ? " · §bGuardada" : e.status() == 3 ? " · §6Ahora es un bloque: usa la vara para despertarla (con sus cosas)"
                        : e.where().isEmpty() ? "" : " · §a" + e.where());
            }
        }
        super.render(g, mx, my, partial);
        if (tip != null) g.renderTooltip(font, font.split(Component.literal(tip), 200), mx, my);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
