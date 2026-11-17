package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.List;

/**
 * ♥ Mis Waifus: álbum de la colección.
 * Izquierda: categorías y tarjetas (con páginas). Derecha: la chica elegida en grande, su estado, su habilidad y los botones.
 */
public class CollectionScreen extends Screen {
    private static final int CW = 44, CH = 54, GAP = 4, COLS = 5, ROWS = 3, PER = COLS * ROWS;
    private static final String WAND = "Usa la Vara de Transformación sobre ";

    private record Cat(String name, WaifuKind... kinds) {}

    private static final Cat[] CATS = {
            new Cat("Todas", WaifuKind.DISPLAY),
            new Cat("Mesas", WaifuKind.KOZUE, WaifuKind.HOMURA, WaifuKind.KURA, WaifuKind.SHIORI, WaifuKind.CAMA, WaifuKind.YUNQUE, WaifuKind.MUELA, WaifuKind.TELAR,
                    WaifuKind.CORTAPIEDRAS, WaifuKind.HERRERIA, WaifuKind.CARTOGRAFIA, WaifuKind.AHUMADOR, WaifuKind.ALTO_HORNO),
            new Cat("Granja", WaifuKind.OVEJA, WaifuKind.VACA, WaifuKind.CERDO, WaifuKind.GALLINA, WaifuKind.CONEJO, WaifuKind.ABEJA, WaifuKind.PANDA, WaifuKind.CABALLO),
            new Cat("Amigas", WaifuKind.PERRO, WaifuKind.GATO, WaifuKind.GOLEM, WaifuKind.ALDEANA, WaifuKind.SLIME),
            new Cat("Monstruos", WaifuKind.ZOMBIE, WaifuKind.ESQUELETO, WaifuKind.CREEPER, WaifuKind.ARANA, WaifuKind.BRUJA, WaifuKind.ENDERMAN, WaifuKind.WARDEN),
            new Cat("Nether", WaifuKind.PIGLIN, WaifuKind.WITHER, WaifuKind.BLAZE, WaifuKind.GHAST),
            new Cat("TNT", WaifuKind.DINA, WaifuKind.MITA),
            new Cat("✦ Extras", WaifuKind.ZORRO, WaifuKind.AJOLOTE, WaifuKind.ALLAY, WaifuKind.RANA, WaifuKind.DELFIN, WaifuKind.TORTUGA, WaifuKind.LORO,
                    WaifuKind.OSO, WaifuKind.STRIDER, WaifuKind.SHULKER, WaifuKind.PHANTOM, WaifuKind.GUARDIAN)};

    private static final Map<WaifuKind, String> HOW = new EnumMap<>(WaifuKind.class);

    static {
        String[][] h = {{"KOZUE", "una mesa de crafteo. Puede volver a ser mesa."}, {"HOMURA", "un horno. Puede volver a ser horno."},
                {"KURA", "un cofre. Puede volver a ser cofre."}, {"SHIORI", "una mesa de encantamientos. Puede volver a ser mesa."},
                {"CAMA", "una cama. Puede volver a ser cama."}, {"YUNQUE", "un yunque. Puede volver a ser yunque."},
                {"MUELA", "una afiladora. Puede volver a ser afiladora."}, {"TELAR", "un telar. Puede volver a ser telar."},
                {"CORTAPIEDRAS", "un cortapiedras. Puede volver a ser cortapiedras."}, {"HERRERIA", "una mesa de herrería. Puede volver a ser mesa."},
                {"CARTOGRAFIA", "una mesa de cartografía. Puede volver a ser mesa."}, {"AHUMADOR", "un ahumador. Puede volver a ser ahumador."},
                {"ALTO_HORNO", "un alto horno. Puede volver a ser alto horno."}, {"OVEJA", "una oveja."}, {"VACA", "una vaca o champiñaca."}, {"CERDO", "un cerdo."},
                {"GALLINA", "una gallina."}, {"PERRO", "un lobo o perro."}, {"GATO", "un gato u ocelote."}, {"GOLEM", "un gólem de hierro."},
                {"ALDEANA", "un aldeano (conserva sus tratos)."}, {"ZOMBIE", "un zombi."}, {"ESQUELETO", "un esqueleto."}, {"CREEPER", "un creeper."},
                {"ARANA", "una araña."}, {"BRUJA", "una bruja."}, {"ENDERMAN", "un enderman."}, {"SLIME", "un slime o cubo de magma."},
                {"CONEJO", "un conejo."}, {"PANDA", "un panda."}, {"CABALLO", "un caballo, burro, mula o llama."}, {"ABEJA", "una abeja."},
                {"PIGLIN", "un piglin (Nether)."}, {"WITHER", "un esqueleto del Nether."}, {"BLAZE", "un blaze (Nether)."}, {"GHAST", "un ghast (Nether)."},
                {"DINA", "un bloque de TNT: ¡aparecen las dos hermanas!"}, {"MITA", "un bloque de TNT: ¡aparecen las dos hermanas!"},
                {"WARDEN", "un warden (¡ten mucho cuidado!)."}, {"ZORRO", "un zorro (bosques fríos)."}, {"AJOLOTE", "un ajolote (cuevas frondosas)."},
                {"ALLAY", "un allay (puestos de saqueadores y mansiones)."}, {"RANA", "una rana (pantanos)."},
                {"DELFIN", "un delfín (océanos)."}, {"TORTUGA", "una tortuga (playas)."}, {"LORO", "un loro (junglas)."},
                {"OSO", "un oso polar (tierras heladas)."}, {"STRIDER", "un strider (lagos de lava del Nether)."}, {"SHULKER", "un shulker (ciudades del End)."},
                {"PHANTOM", "un phantom (aparecen si pasas noches sin dormir)."}, {"GUARDIAN", "un guardián (monumentos oceánicos)."}};
        for (String[] e : h) {
            WaifuKind k = WaifuKind.valueOf(e[0]);
            HOW.put(k, WAND + e[1] + (k.station() ? "" : " Se queda así para siempre."));
        }
    }

    private static int cat, page;
    private static WaifuKind selected = WaifuKind.KOZUE;
    private CollectionData data;
    private final Map<WaifuKind, CollectionData.Entry> byKind = new EnumMap<>(WaifuKind.class);
    private final Map<WaifuKind, WaifuEntity> dolls = new EnumMap<>(WaifuKind.class);
    private int px, py, pw, ph;

    private CollectionScreen(CollectionData d) {
        super(Component.literal("♥ Mis Waifus"));
        setData(d);
    }

    private void setData(CollectionData d) {
        data = d;
        byKind.clear();
        for (CollectionData.Entry e : d.entries()) byKind.put(WaifuKind.byId(e.kind()), e);
    }

    public static void show(CollectionData d) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CollectionScreen s) {
            s.setData(d);
            s.rebuildWidgets();
        } else {
            mc.setScreen(new CollectionScreen(d));
        }
    }

    private int count(WaifuKind[] ks) {
        int n = 0;
        for (WaifuKind k : ks) if (status(k) > 0) n++;
        return n;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int np = Mth.clamp(page - (int) Math.signum(delta), 0, pages() - 1);
        if (np != page) { page = np; rebuildWidgets(); }
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        WaifuKind[] l = list();
        int i = Arrays.asList(l).indexOf(selected);
        int ni = i;
        switch (key) {
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT -> ni = i + 1;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT -> ni = i - 1;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN -> ni = i + COLS;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_UP -> ni = i - COLS;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER -> {
                int st = status(selected);
                if (st == 1 || st == 2) Net.CHANNEL.sendToServer(new CollectionAction(selected.ordinal(), CollectionAction.SUMMON));
                return true;
            }
            default -> { return super.keyPressed(key, scan, mods); }
        }
        if (i < 0) ni = 0;
        ni = Mth.clamp(ni, 0, l.length - 1);
        selected = l[ni];
        page = ni / PER;
        rebuildWidgets();
        return true;
    }

    private int status(WaifuKind k) {
        CollectionData.Entry e = byKind.get(k);
        return e == null ? 0 : e.status();
    }

    private WaifuKind[] list() { return CATS[cat].kinds; }
    private int pages() { return Math.max(1, (list().length + PER - 1) / PER); }
    private int gx() { return px + 8; }
    private int gy() { return py + 50; }
    private int dx() { return px + 8 + COLS * (CW + GAP) + 4; }
    private int dw() { return px + pw - 8 - dx(); }

    private int cardX(int i) { return gx() + (i % COLS) * (CW + GAP); }
    private int cardY(int i) { return gy() + (i / COLS) * (CH + GAP); }

    @Override
    protected void init() {
        pw = Math.min(width - 16, 420);
        ph = Math.min(height - 12, 252);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        page = Mth.clamp(page, 0, pages() - 1);
        // categorías
        int tx = px + 8;
        for (int i = 0; i < CATS.length; i++) {
            final int ci = i;
            String label = pw < 400 ? CATS[i].name.replace("Monstruos", "Monstr.") : CATS[i].name;
            int w = font.width(label) + 8;
            boolean full = i > 0 && count(CATS[i].kinds) == CATS[i].kinds.length;
            Button b = Button.builder(Component.literal((i == cat ? "§d" : full ? "§6" : "") + label), x -> {
                cat = ci;
                page = 0;
                rebuildWidgets();
            }).bounds(tx, py + 32, w, 14).tooltip(Tooltip.create(Component.literal(CATS[i].name + ": " + count(CATS[i].kinds) + " de " + CATS[i].kinds.length
                    + (full ? " §6✓ ¡completa!" : "")))).build();
            b.active = i != cat;
            addRenderableWidget(b);
            tx += w + 2;
        }
        // páginas
        int by = gy() + ROWS * (CH + GAP) + 1;
        Button prev = addRenderableWidget(Button.builder(Component.literal("◀"), b -> { page--; rebuildWidgets(); }).bounds(gx(), by, 18, 14).build());
        Button next = addRenderableWidget(Button.builder(Component.literal("▶"), b -> { page++; rebuildWidgets(); })
                .bounds(gx() + COLS * (CW + GAP) - GAP - 18, by, 18, 14).build());
        prev.active = page > 0;
        next.active = page < pages() - 1;
        // botones de la elegida
        WaifuKind k = selected;
        int st = status(k);
        int bx = dx() + 4, bw = dw() - 8, yb = py + ph - 22;
        if (st == 1 || st == 2) {
            addRenderableWidget(Button.builder(Component.literal(st == 2 ? "♥ Llamar" : "♥ Traer"),
                            b -> Net.CHANNEL.sendToServer(new CollectionAction(k.ordinal(), CollectionAction.SUMMON)))
                    .bounds(bx, yb, bw / 2 - 1, 16).tooltip(Tooltip.create(Component.literal(st == 2 ? "Aparece a tu lado" : "Viene a tu lado (aunque esté lejos, si su zona está cargada)"))).build());
            Button store = addRenderableWidget(Button.builder(Component.literal("Guardar"),
                            b -> Net.CHANNEL.sendToServer(new CollectionAction(k.ordinal(), CollectionAction.STORE)))
                    .bounds(bx + bw / 2 + 1, yb, bw / 2 - 1, 16).tooltip(Tooltip.create(Component.literal("Se guarda en tu colección con todas sus cosas"))).build());
            store.active = st == 1;
        }
        if (k == WaifuKind.CAMA && data.home()) {
            addRenderableWidget(Button.builder(Component.literal("§9☾ Ir a donde dormimos"), b -> {
                        Net.CHANNEL.sendToServer(new CollectionAction(WaifuKind.CAMA.ordinal(), CollectionAction.HOME));
                        onClose();
                    }).bounds(bx, yb - 18, bw, 16)
                    .tooltip(Tooltip.create(Component.literal("Te transporta al último lugar donde dormiste con Yume (en forma de chica o de cama)"))).build());
        }
    }

    private WaifuEntity doll(WaifuKind k) {
        return dolls.computeIfAbsent(k, kk -> ModRegistry.TYPES.get(kk).get().create(Minecraft.getInstance().level));
    }

    private static int tint(WaifuKind k, int shift) {
        return 0xFF000000 | ((k.eggA >> shift) & (shift == 1 ? 0x7F7F7F : 0x3F3F3F));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        WaifuKind[] l = list();
        for (int i = 0; i < PER; i++) {
            int idx = page * PER + i;
            if (idx >= l.length) break;
            int x = cardX(i), y = cardY(i);
            if (mx >= x && mx < x + CW && my >= y && my < y + CH) {
                if (selected != l[idx]) {
                    selected = l[idx];
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.6F, 0.4F));
                    rebuildWidgets();
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        float t = (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.tickCount : 0) + partial;
        // marco del álbum
        g.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFFFFB6DA);
        g.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF5A2A58);
        g.fillGradient(px, py, px + pw, py + ph, 0xF02A1640, 0xF0140A22);
        for (int i = 0; i < 14; i++) {                                // corazones que flotan al fondo
            int x = px + 6 + (i * 71) % (pw - 12), y = py + ph - 8 - (int) ((t * (0.25F + i % 3 * 0.1F) + i * 37) % (ph - 16));
            g.drawString(font, i % 3 == 0 ? "✦" : "♥", x, y, i % 2 == 0 ? 0x30FF8AD0 : 0x30FFD070, false);
        }
        // título y progreso
        int got = 0, total = 0;
        for (WaifuKind k : WaifuKind.values()) {
            if (k.surprise()) continue;
            total++;
            if (status(k) > 0) got++;
        }
        g.drawString(font, "§d§l♥ Mis Waifus", px + 8, py + 6, 0xFFFFFFFF, true);
        int eg = 0, et = 0;
        for (WaifuKind k : WaifuKind.values()) if (k.surprise()) { et++; if (status(k) > 0) eg++; }
        String t1 = (data.surprise() ? "§6★ ♥ " : "§7★ ♥ ") + got + "/" + total, t2 = (data.extras() ? "§b★ ✦ " : data.surprise() ? "§7★ ✦ " : "§8★ ✦ ") + eg + "/" + et;
        int x2 = px + pw - 8 - font.width(t2), x1 = x2 - 10 - font.width(t1);
        g.drawString(font, t1, x1, py + 6, 0xFFFFFFFF, true);
        g.drawString(font, t2, x2, py + 6, 0xFFFFFFFF, true);
        List<Component> trophyTip = null;
        if (my >= py + 4 && my < py + 16 && mx >= x1 && mx < x2 + font.width(t2)) {
            boolean first = mx < x2 - 5;
            trophyTip = new ArrayList<>();
            trophyTip.add(Component.literal(first ? "§6§lRecompensa: colección completa" : "§b§lRecompensa: extras completos"));
            trophyTip.add(Component.literal((first ? data.surprise() : data.extras()) ? "§a✓ ¡Conseguida!" : first ? "§7Consigue a las " + total + " waifus" : "§7Consigue a las " + et + " extras"));
            for (net.minecraft.world.item.ItemStack st : first ? com.totallywaifu.data.Rewards.collection() : com.totallywaifu.data.Rewards.extras())
                trophyTip.add(Component.literal("§f• " + st.getCount() + "× " + st.getHoverName().getString()));
            trophyTip.add(Component.literal("§a• +" + (first ? com.totallywaifu.data.Rewards.COLLECTION_LEVELS : com.totallywaifu.data.Rewards.EXTRAS_LEVELS) + " niveles"));
            trophyTip.add(Component.literal("§d• " + (first ? com.totallywaifu.data.Rewards.PERK_COLLECTION : com.totallywaifu.data.Rewards.PERK_EXTRAS)));
        }
        int bx0 = px + 8, bx1 = px + pw - 8, byy = py + 19;
        g.fill(bx0 - 1, byy - 1, bx1 + 1, byy + 7, 0xFF8A4A86);
        g.fill(bx0, byy, bx1, byy + 6, 0xFF1A0E26);
        int fillW = (int) ((bx1 - bx0) * (got / (float) Math.max(1, total)));
        g.fillGradient(bx0, byy, bx0 + fillW, byy + 6, data.surprise() ? 0xFFFFD24A : 0xFFFF8AD0, data.surprise() ? 0xFFC08A1A : 0xFFC04A9A);
        if (fillW > 4) g.fill(bx0 + fillW - 2, byy, bx0 + fillW, byy + 6, 0xFFFFE0F0);
        if (data.surprise()) {
            int ew = (int) ((bx1 - bx0) * (eg / (float) Math.max(1, et)));
            g.fill(bx0, byy + 4, bx0 + ew, byy + 6, 0xFF7AD8FF);
        }
        String pct = (int) (100 * got / (float) Math.max(1, total)) + "%";
        g.drawString(font, "§8" + pct, bx1 - font.width(pct) - 2, byy - 1, 0xFFFFFFFF, false);
        // tarjetas
        WaifuKind[] l = list();
        String tip = null;
        for (int i = 0; i < PER; i++) {
            int idx = page * PER + i;
            if (idx >= l.length) break;
            WaifuKind k = l[idx];
            int st = status(k);
            int x = cardX(i), y = cardY(i);
            boolean hover = mx >= x && mx < x + CW && my >= y && my < y + CH;
            boolean sel = k == selected;
            int lift = hover ? 1 : 0;
            int border = sel ? 0xFFFFE070 : hover ? 0xFFFFFFFF : st == -1 ? 0xFF8A5ACC : st > 0 ? 0xFFFF9ACC : 0xFF4A3A5A;
            g.fill(x - 1, y - 1 - lift, x + CW + 1, y + CH + 1 - lift, border);
            g.fillGradient(x, y - lift, x + CW, y + CH - lift, st > 0 ? tint(k, 1) : 0xFF231A30, 0xFF120C1A);
            if (st > 0) {
                WaifuEntity d = doll(k);
                if (d != null) {
                    g.enableScissor(x, y - lift, x + CW, y + CH - 11 - lift);
                    int size = (int) (17 / Math.max(1.0F, k.scale * 0.85F));
                    InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + CW / 2, y + CH - 12 - lift, size, (float) (x + CW / 2 - mx), (float) (y + 14 - my), d);
                    g.disableScissor();
                }
                int dot = st == 1 ? 0xFF5CE07A : st == 2 ? 0xFF6AC8FF : 0xFFFFB040;
                g.fill(x + CW - 6, y + 2 - lift, x + CW - 2, y + 6 - lift, dot);
            } else {
                PoseStack ps = g.pose();
                ps.pushPose();
                ps.translate(x + CW / 2.0F, y + 20 - lift, 0);
                ps.scale(2, 2, 1);
                g.drawCenteredString(font, st == -1 ? "§5✦" : "§8?", 0, -4, 0xFFFFFFFF);
                ps.popPose();
            }
            String name = st == -1 ? "???" : k.display;
            g.fill(x, y + CH - 11 - lift, x + CW, y + CH - lift, 0x90000000);
            g.drawCenteredString(font, (st > 0 ? k.color : "§7") + trim(name, CW - 2), x + CW / 2, y + CH - 10 - lift, 0xFFFFFFFF);
            if (hover) tip = st == -1 ? "§5Extra: completa tu colección para desbloquearla" : (st > 0 ? k.color : "§7") + k.display + " §8· §7" + k.role;
        }
        if (l.length > PER) g.drawCenteredString(font, "§7" + (page + 1) + " / " + pages(), gx() + (COLS * (CW + GAP) - GAP) / 2, gy() + ROWS * (CH + GAP) + 4, 0xFFFFFFFF);
        detail(g, mx, my, t);
        super.render(g, mx, my, partial);
        if (tip != null) g.renderTooltip(font, Component.literal(tip), mx, my);
        else if (trophyTip != null) g.renderComponentTooltip(font, trophyTip, mx, my);
    }

    private String trim(String s, int w) {
        if (font.width(s) <= w) return s;
        while (s.length() > 1 && font.width(s + "…") > w) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    /** Panel derecho: la chica elegida. */
    private void detail(GuiGraphics g, int mx, int my, float t) {
        WaifuKind k = selected;
        int st = status(k);
        int x = dx(), y = py + 50, w = dw(), bottom = py + ph - 26;
        g.fill(x - 1, y - 1, x + w + 1, py + ph - 3, 0xFF8A4A86);
        g.fillGradient(x, y, x + w, py + ph - 4, st > 0 ? tint(k, 2) : 0xFF1A1226, 0xFF0E0816);
        boolean locked = st == -1;
        // retrato grande
        int portraitH = 80;
        if (st > 0) {
            float glow = 0.5F + 0.5F * Mth.sin(t * 0.1F);
            int r = 26 + (int) (glow * 4);
            for (int i = r; i > 0; i -= 3) g.fill(x + w / 2 - i, y + 50 - i, x + w / 2 + i, y + 50 + i, ((int) (6 + (r - i) * 0.6F) << 24) | 0xFFC0E8);
            WaifuEntity d = doll(k);
            if (d != null) {
                g.enableScissor(x, y, x + w, y + portraitH);
                int size = (int) (36 / Math.max(1.0F, k.scale * 0.8F));
                InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + w / 2, y + portraitH - 4, size, (float) (x + w / 2 - mx), (float) (y + 30 - my), d);
                g.disableScissor();
            }
        } else {
            PoseStack ps = g.pose();
            ps.pushPose();
            ps.translate(x + w / 2.0F, y + 44, 0);
            ps.scale(4, 4, 1);
            g.drawCenteredString(font, locked ? "§5✦" : "§8?", 0, -4, 0xFFFFFFFF);
            ps.popPose();
        }
        int ty = y + portraitH + 2;
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x + w / 2.0F, ty, 0);
        ps.scale(1.4F, 1.4F, 1);
        g.drawCenteredString(font, locked ? "§5???" : (st > 0 ? k.color : "§7") + "§l" + k.display, 0, 0, 0xFFFFFFFF);
        ps.popPose();
        ty += 14;
        if (!locked) {
            g.drawCenteredString(font, "§7" + k.role, x + w / 2, ty, 0xFFFFFFFF);
            ty += 11;
        }
        String state = switch (st) {
            case -1 -> "§5Waifu extra";
            case 1 -> "§a● En el mundo";
            case 2 -> "§b● Guardada en tu colección";
            case 3 -> "§6● Ahora es un bloque";
            default -> "§8● Todavía no la tienes";
        };
        g.drawCenteredString(font, state, x + w / 2, ty, 0xFFFFFFFF);
        ty += 11;
        if (!locked) {                                              // estadísticas
            String[] nm = {"Vida", "Ataque", "Defensa", "Rapidez"};
            float[] v = {(float) Math.min(1, k.health / 120.0), (float) Math.min(1, k.attack / 30.0), (float) Math.min(1, k.armor / 20.0), (float) Math.min(1, k.speed / 0.42)};
            int[] col = {0xFFFF6A8A, 0xFFFFB040, 0xFF6AC8FF, 0xFF7AE07A};
            for (int i = 0; i < 4; i++) {
                int cxs = x + 5 + (i % 2) * (w / 2), cys = ty + (i / 2) * 10, bwid = w / 2 - 34;
                g.drawString(font, "§7" + nm[i], cxs, cys, 0xFFFFFFFF, false);
                int bx = cxs + 34 - 5;
                g.fill(bx, cys + 2, bx + bwid, cys + 6, 0xFF2A1A34);
                g.fill(bx, cys + 2, bx + (int) (bwid * (st > 0 ? v[i] : v[i] * 0.3F)), cys + 6, st > 0 ? col[i] : 0xFF555060);
            }
            ty += 22;
        }
        CollectionData.Entry e = byKind.get(k);
        if (st == 1 && e != null && !e.where().isEmpty()) {
            for (FormattedCharSequence line : font.split(Component.literal("§a" + e.where()), w - 8)) {
                g.drawCenteredString(font, line, x + w / 2, ty, 0xFFFFFFFF);
                ty += 9;
            }
            ty += 2;
        }
        String text = locked ? "Completa tu colección para descubrir quién es."
                : st > 0 ? k.job + "." + (st == 3 ? " §6Usa la vara sobre su bloque para despertarla (con todas sus cosas)." : "")
                : HOW.getOrDefault(k, "");
        int limit = bottom - (k == WaifuKind.CAMA && data.home() ? 18 : 0) - 2;
        for (FormattedCharSequence line : font.split(Component.literal("§f" + text), w - 10)) {
            if (ty + 9 > limit) break;
            g.drawString(font, line, x + 5, ty, 0xFFE8D8F0, false);
            ty += 9;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
