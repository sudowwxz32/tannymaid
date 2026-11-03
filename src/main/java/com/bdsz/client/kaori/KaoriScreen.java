package com.bdsz.client.kaori;

import com.bdsz.companion.KaoriActionPacket;
import com.bdsz.companion.KaoriActions;
import com.bdsz.companion.KaoriEntity;
import com.bdsz.companion.KaoriMode;
import com.bdsz.companion.KaoriResource;
import com.bdsz.companion.KaoriScreenPacket;
import com.bdsz.network.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

/**
 * Menú de Kaori, ordenado en tres pestañas:
 * Principal (regalos, órdenes y transformación) · Ayuda (fabricar, farmear, ordenar) · Casas y comercio.
 */
public class KaoriScreen extends Screen {
    private static final int W = 340, H = 232;
    // colores: carbón con rojo (su chaqueta) y dorado
    private static final int BG_TOP = 0xF61C1820, BG_BOT = 0xF6120F15, EDGE = 0xFFC8383A, EDGE_DK = 0xFF5E1418,
            GOLD = 0xFFF2C14E, TEXT = 0xFFF4E6D0, MUTED = 0xFFA89C94, PANEL = 0xFF26212B;
    private static final String[] RES = {"Madera", "Piedra", "Carbón", "Hierro", "Tierra", "Arena"};
    private static final String[] TABS = {"Principal", "Ayuda", "Casas y comercio"};
    private static final String[] HOUSES = {"Choza", "Casa segura de piedra", "Casa del Guerrero Z", "Base Cápsula"};
    private static final String[] HOUSE_TIPS = {"7×7: cama, cofre, horno, ahumador, barril y mesa de crafteo",
            "9×9 de piedra: puerta de hierro con botones, 2 camas, yunque, alto horno, herrería, caldero y pociones",
            "11×11: todo lo anterior + Generador Protector encendido, huerto de Semillas del Ermitaño y tazones de arroz",
            "13×13: todo lo anterior + mesa de encantamientos con librerías, huerto de trigo, telar y afiladora"};
    private static final long[] BOND = {0L, 24000L, 72000L, 144000L};
    private static int page;

    private final KaoriScreenPacket st;
    private final long openedAt;
    private EditBox craft, amount;

    public KaoriScreen(KaoriScreenPacket st) {
        super(Component.literal("Kaori"));
        this.st = st;
        this.openedAt = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
    }

    public static void open(KaoriScreenPacket p) {
        Minecraft.getInstance().setScreen(new KaoriScreen(p));
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - H) / 2; }
    private int cx() { return left() + 100; }

    private long elapsed() {
        return Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime() - openedAt;
    }

    // ------------------------------------------------------------------ botones

    @Override
    protected void init() {
        craft = null;
        int tx = left() + W - 8;
        for (int i = TABS.length - 1; i >= 0; i--) {
            int w = font.width(TABS[i]) + 12;
            tx -= w + 2;
            final int t = i;
            Button b = Button.builder(Component.literal((page == i ? "§e" : "§7") + TABS[i]), x -> { page = t; rebuildWidgets(); })
                    .bounds(tx, top() + 3, w, 13).build();
            addRenderableWidget(b);
        }
        // bajo el retrato
        add(left() + 8, top() + H - 48, 84, "Mochila", KaoriActions.INVENTORY, 0, "Su inventario, armadura, herramientas y antorcha");
        add(left() + 8, top() + H - 26, 84, "Estado", KaoriActions.STATUS, 0, "Vida y modo actual");
        switch (page) {
            case 1 -> initHelp();
            case 2 -> initHouses();
            default -> initMain();
        }
    }

    private void initMain() {
        int x = cx(), bw = 74, g = 5;
        int y = top() + 36;
        add(x, y, bw, "✿ Semillas", KaoriActions.SENZU, 0, "3 Semillas del Ermitaño. Al terminarlas, esperas 2 días de Minecraft");
        add(x + bw + g, y, bw, "Arroz ×20", KaoriActions.RICE, 0, "20 tazones de arroz (recuperan vida, ki y hambre). Una vez al día");
        if (st.hut) add(x + 2 * (bw + g), y, bw, "⌂ Guardar casa", KaoriActions.HUT, 0, "Guarda la casa en la cápsula (lo de los cofres pasa a su mochila)");
        else addRenderableWidget(Button.builder(Component.literal("⌂ Casas"), b -> { page = 2; rebuildWidgets(); })
                .bounds(x + 2 * (bw + g), y, bw, 20).tooltip(Tooltip.create(Component.literal("Elige qué casa-cápsula lanzar"))).build());
        y = top() + 86;
        add(x, y, bw, mark(KaoriMode.FOLLOW) + "Seguirme", KaoriActions.FOLLOW, 0, "Te acompaña y te protege");
        add(x + bw + g, y, bw, mark(KaoriMode.STAY) + "Quedarse", KaoriActions.STAY, 0, "Te espera aquí");
        add(x + 2 * (bw + g), y, bw, mark(KaoriMode.DEFEND) + "Defensa", KaoriActions.DEFEND, 0, "Ataca a todo monstruo que vea");
        y += 23;
        add(x, y, bw, mark(KaoriMode.FARM) + "Agricultura", KaoriActions.FARM, 0, "Cosecha, siembra y ara junto al agua (semillas y azada)");
        add(x + bw + g, y, bw, "Venir", KaoriActions.TP, 0, "Viene a tu lado");
        add(x + 2 * (bw + g), y, bw, st.collecting ? "Recoger: §así" : "Recoger: §cno", KaoriActions.COLLECT, 0, "Recoge objetos del suelo");
        y = top() + 160;
        String[] fn = {"Base", "SSJ 1", "SSJ 2", "Dios"};
        int[] ids = {0, 1, 2, 4};
        String[] tips = {"Vuelve a su estado base", "Super Saiyajin: cabello dorado, más fuerza, velocidad, armadura y vida",
                "Super Saiyajin 2: aún más poder, aura con rayos", "Super Saiyajin Dios: cabello rojo y aura divina, su forma más fuerte"};
        int fw = (3 * bw + 2 * g - 3 * 4) / 4;
        for (int i = 0; i < 4; i++) add(x + i * (fw + 4), y, fw, (st.form == ids[i] ? "§e▶ " : "") + fn[i], KaoriActions.FORM, ids[i], tips[i]);
    }

    private void initHelp() {
        int x = cx(), bw = 74, g = 5;
        int y = top() + 36;
        craft = new EditBox(font, x + 1, y + 1, 2 * bw + g - 2, 18, Component.literal("Fabricar"));
        craft.setMaxLength(40);
        craft.setHint(Component.literal("§7ej. antorchas 16, pico de hierro"));
        addRenderableWidget(craft);
        addRenderableWidget(Button.builder(Component.literal("⚒ Fabricar"), b -> send(KaoriActions.CRAFT, 0, craft.getValue()))
                .bounds(x + 2 * (bw + g), y, bw, 20).tooltip(Tooltip.create(Component.literal(
                        "Fabrica con lo que tenga en su mochila usando las recetas del juego. Te lo entrega a ti"))).build());
        y = top() + 86;
        for (int i = 0; i < RES.length; i++) {
            final int r = i;
            addRenderableWidget(Button.builder(Component.literal(RES[i]), b -> send(KaoriActions.GATHER, r, amount.getValue()))
                    .bounds(x + (i % 3) * (bw + g), y + (i / 3) * 23, bw, 20).tooltip(Tooltip.create(Component.literal(
                            "Farmea " + KaoriResource.byId(r).display + ". Usa la herramienta de su cinturón o mochila"))).build());
        }
        y += 48;
        amount = new EditBox(font, x + 60, y + 1, 30, 18, Component.literal("Cantidad"));
        amount.setMaxLength(2);
        amount.setValue("16");
        amount.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(amount);
        add(x + 2 * (bw + g), y, bw, "■ Parar", KaoriActions.STOP, 0, "Deja de farmear");
        y = top() + 184;
        add(x, y, bw + bw / 2, "Ordenar cofres", KaoriActions.SORT, 0, "Ordena los cofres y barriles cercanos por tipo");
        add(x + bw + bw / 2 + g, y, 3 * bw + 2 * g - bw - bw / 2 - g, st.collecting ? "Recoger objetos: §así" : "Recoger objetos: §cno",
                KaoriActions.COLLECT, 0, "Recoge objetos del suelo");
    }

    private void initHouses() {
        int x = cx(), bw = 74, g = 5, hw = (3 * bw + 2 * g - g) / 2;
        int y = top() + 36;
        if (st.hut) {
            add(x, y, 3 * bw + 2 * g, "⌂ Guardar " + HOUSES[Math.max(0, Math.min(3, st.hutType))], KaoriActions.HUT, 0,
                    "Guarda la casa en la cápsula (lo de los cofres pasa a su mochila)");
        } else {
            for (int i = 0; i < 4; i++) {
                boolean open = st.bond >= BOND[i];
                String label = open ? "⌂ " + HOUSES[i] : "§8✖ " + HOUSES[i];
                String tip = HOUSE_TIPS[i] + (open ? "" : " · §cSe desbloquea pasando más tiempo con Kaori: faltan " + clock(BOND[i] - st.bond));
                add(x + (i % 2) * (hw + g), y + (i / 2) * 23, hw, label, KaoriActions.HUT, i, tip);
            }
        }
        y = top() + 120;
        add(x, y, bw, "Alimentar", KaoriActions.SHIELD_FEED, 0, "Pone los lingotes de cobre de su mochila en el Generador Protector más cercano");
        add(x + bw + g, y, bw, "Encender", KaoriActions.SHIELD_POWER, 1, "Enciende el domo protector");
        add(x + 2 * (bw + g), y, bw, "Apagar", KaoriActions.SHIELD_POWER, 0, "Apaga el domo protector");
        y = top() + 166;
        add(x, y, bw, "Ofertas", KaoriActions.TRADE_INFO, 0, "Revisa a los aldeanos: las mejores ofertas y lo que ellos piden");
        add(x + bw + g, y, bw, "Comerciar", KaoriActions.TRADE_AUTO, 0,
                "Vende por ti lo que haya en su mochila y compra lo mejor con sus esmeraldas (30% de descuento)");
        add(x + 2 * (bw + g), y, bw, "Conseguir", KaoriActions.TRADE_GET, 0, "Sale a conseguir lo que piden los aldeanos (madera, carbón, piedra, cosechas, caza...)");
    }

    private String mark(KaoriMode m) {
        return st.mode == m.ordinal() ? "§e▶ §r" : "";
    }

    private void add(int x, int y, int w, String label, int action, int arg, String tip) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> send(action, arg, "")).bounds(x, y, w, 20)
                .tooltip(Tooltip.create(Component.literal(tip))).build());
    }

    private void send(int action, int arg, String text) {
        Net.CHANNEL.sendToServer(new KaoriActionPacket(st.entityId, action, arg, text == null ? "" : text.trim()));
        onClose();
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && craft != null && craft.isFocused() && !craft.getValue().isBlank()) {
            send(KaoriActions.CRAFT, 0, craft.getValue());
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        int x = left(), y = top();
        g.fill(x - 1, y - 1, x + W + 1, y + H + 1, EDGE_DK);
        g.fill(x, y, x + W, y + H, EDGE);
        g.fillGradient(x + 1, y + 1, x + W - 1, y + H - 1, BG_TOP, BG_BOT);
        g.fillGradient(x + 1, y + 1, x + W - 1, y + 19, 0xFFB52B30, 0xFF6E1519);
        g.fill(x + 1, y + 19, x + W - 1, y + 20, GOLD);
        g.drawString(font, "⚙ Kaori", x + 8, y + 6, 0xFFFFFFFF, true);
        g.drawString(font, "§7mecánica e inventora", x + 8 + font.width("⚙ Kaori") + 6, y + 6, 0xFFFFFFFF, false);
        // retrato
        g.fill(x + 8, y + 26, x + 92, y + H - 52, PANEL);
        g.fill(x + 8, y + 26, x + 92, y + 27, 0x66F2C14E);
        Entity e = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(st.entityId) : null;
        if (e instanceof KaoriEntity k) {
            KaoriRenderer.PREVIEW = true;
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 50, y + H - 58, 42, (float) (x + 50 - mx), (float) (y + 90 - my), k);
            KaoriRenderer.PREVIEW = false;
            float hp = k.getHealth() / Math.max(1, k.getMaxHealth());
            g.fill(x + 12, y + 31, x + 88, y + 35, 0xFF3A1418);
            g.fill(x + 12, y + 31, x + 12 + (int) (76 * hp), y + 35, k.isWeakened() ? 0xFF8A8A8A : 0xFFE0453A);
            g.drawCenteredString(font, String.format("♥ %.0f/%.0f", k.getHealth(), k.getMaxHealth()), x + 50, y + 38, k.isWeakened() ? MUTED : 0xFFFF9A8A);
            if (k.isWeakened()) g.drawCenteredString(font, "descansando", x + 50, y + 49, MUTED);
        }
        int sx = cx();
        switch (page) {
            case 1 -> {
                section(g, "Fabricar", sx, y + 25);
                section(g, "Farmear recursos", sx, y + 75);
                g.drawString(font, "Cantidad:", sx, y + 140, MUTED, false);
                section(g, "Orden", sx, y + 173);
            }
            case 2 -> {
                section(g, "Casas-cápsula", sx, y + 25);
                int unlocked = 0;
                for (long b : BOND) if (st.bond >= b) unlocked++;
                g.drawString(font, "§7Tiempo con Kaori: §f" + clock(st.bond) + "  §7casas: §e" + unlocked + "/4", sx, y + 86, TEXT, false);
                section(g, "Generador Protector (cobre)", sx, y + 109);
                section(g, "Comercio con aldeanos", sx, y + 155);
                g.drawString(font, "§7Cerca de ella, los aldeanos te hacen descuento.", sx, y + 190, TEXT, false);
            }
            default -> {
                section(g, "Regalos", sx, y + 25);
                long senzu = Math.max(0, st.senzuIn - elapsed()), rice = Math.max(0, st.riceIn - elapsed());
                String s1 = st.seedsLeft > 0 ? "§a" + st.seedsLeft + " por comer" : senzu > 0 ? "§c⌛ " + clock(senzu) : "§a¡listas!";
                String s2 = rice > 0 ? "§c⌛ " + clock(rice) : "§a¡listo!";
                g.drawCenteredString(font, s1, sx + 37, y + 60, 0xFFFFFFFF);
                g.drawCenteredString(font, s2, sx + 37 + 79, y + 60, 0xFFFFFFFF);
                g.drawCenteredString(font, st.hut ? "§ecolocada" : "§7en la cápsula", sx + 37 + 158, y + 60, 0xFFFFFFFF);
                section(g, "Órdenes", sx, y + 75);
                section(g, "Transformación", sx, y + 149);
                g.drawString(font, "§8Clic derecho: menú · Shift: mochila · J: holograma", sx, y + H - 14, MUTED, false);
            }
        }
        super.render(g, mx, my, partial);
    }

    /** Tiempo restante en formato día/hora de Minecraft. */
    private static String clock(long ticks) {
        long d = ticks / 24000, h = (ticks % 24000) / 1000, m = (ticks % 1000) * 60 / 1000;
        return d > 0 ? d + "d " + h + "h" : h > 0 ? h + "h " + m + "m" : m + "m";
    }

    private void section(GuiGraphics g, String text, int x, int y) {
        g.fill(x - 2, y - 1, x, y + 9, GOLD);
        g.drawString(font, text, x + 3, y, GOLD, false);
        g.fill(x + font.width(text) + 8, y + 4, left() + W - 8, y + 5, 0x55F2C14E);
    }
}
