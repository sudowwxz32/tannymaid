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

/** Menú de Kaori: regalos (semillas, arroz, cápsula), ayuda (fabricar, ordenar, farmear) y órdenes. */
public class KaoriScreen extends Screen {
    private static final int W = 330, H = 248;
    private static final int BG_TOP = 0xFF2B2F25, BG_BOT = 0xFF1D201A, EDGE = 0xFFE8783C, EDGE_DK = 0xFF8A3F1A,
            TEXT = 0xFFF2C38B, MUTED = 0xFFB9B39C;
    private static final String[] RES = {"Madera", "Piedra", "Carbón", "Hierro", "Tierra", "Arena"};

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

    private long elapsed() {
        return Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime() - openedAt;
    }

    @Override
    protected void init() {
        // pestañas
        addRenderableWidget(Button.builder(Component.literal((page == 0 ? "§6" : "") + "Principal"), b -> { page = 0; rebuildWidgets(); })
                .bounds(left() + W - 172, top() + 2, 80, 14).build());
        addRenderableWidget(Button.builder(Component.literal((page == 1 ? "§6" : "") + "Casas y comercio"), b -> { page = 1; rebuildWidgets(); })
                .bounds(left() + W - 90, top() + 2, 86, 14).build());
        add(left() + 8, top() + H - 26, 76, "Estado", KaoriActions.STATUS, 0, "Vida y modo actual");
        if (page == 1) { initPage2(); return; }
        int x = left() + 96, y = top() + 32, bw = 74, g = 3;
        add(x, y, bw, "✿ Semillas", KaoriActions.SENZU, 0, "3 Semillas del Ermitaño. Al terminarlas, esperas 2 días de Minecraft");
        add(x + bw + g, y, bw, "Arroz ×20", KaoriActions.RICE, 0, "20 tazones de arroz (recuperan vida, ki y hambre). Una vez al día");
        if (st.hut) add(x + 2 * (bw + g), y, bw + 2, "⌂ Guardar casa", KaoriActions.HUT, 0, "Guarda la casa en la cápsula (lo de los cofres pasa a su mochila)");
        else addRenderableWidget(Button.builder(Component.literal("⌂ Casas"), b -> { page = 1; rebuildWidgets(); })
                .bounds(x + 2 * (bw + g), y, bw + 2, 20).tooltip(Tooltip.create(Component.literal("Elige qué casa-cápsula lanzar"))).build());

        y += 44;
        craft = new EditBox(font, x, y, bw * 2 + g, 18, Component.literal("Fabricar"));
        craft.setMaxLength(40);
        craft.setHint(Component.literal("§7ej. antorchas 16, pico de hierro"));
        addRenderableWidget(craft);
        addRenderableWidget(Button.builder(Component.literal("⚒ Fabricar"), b -> send(KaoriActions.CRAFT, 0, craft.getValue()))
                .bounds(x + 2 * (bw + g), y - 1, bw + 2, 20).tooltip(Tooltip.create(Component.literal(
                        "Fabrica con lo que tenga en su mochila usando las recetas del juego. Te lo entrega a ti"))).build());
        y += 22;
        amount = new EditBox(font, x, y + 1, 22, 16, Component.literal("Cantidad"));
        amount.setMaxLength(2);
        amount.setValue("16");
        amount.setFilter(s -> s.matches("\\d*"));
        addRenderableWidget(amount);
        int rw = 37;
        for (int i = 0; i < RES.length; i++) {
            final int r = i;
            addRenderableWidget(Button.builder(Component.literal(RES[i]), b -> send(KaoriActions.GATHER, r, amount.getValue()))
                    .bounds(x + 25 + i * (rw + 1), y, rw, 18).tooltip(Tooltip.create(Component.literal(
                            "Farmea " + KaoriResource.byId(r).display + " (la cantidad de la izquierda). Usa la herramienta de su mochila"))).build());
        }
        y += 21;
        add(x, y, bw, "Parar", KaoriActions.STOP, 0, "Deja de farmear");
        add(x + bw + g, y, bw, "Ordenar cofres", KaoriActions.SORT, 0, "Ordena los cofres y barriles cercanos por tipo");
        add(x + 2 * (bw + g), y, bw + 2, st.collecting ? "Recoger: sí" : "Recoger: no", KaoriActions.COLLECT, 0, "Recoge objetos del suelo");

        y += 38;
        add(x, y, bw, mark(KaoriMode.FOLLOW) + "Seguirme", KaoriActions.FOLLOW, 0, "Te acompaña y te protege");
        add(x + bw + g, y, bw, mark(KaoriMode.STAY) + "Quedarse", KaoriActions.STAY, 0, "Te espera aquí");
        add(x + 2 * (bw + g), y, bw + 2, mark(KaoriMode.DEFEND) + "Defensa", KaoriActions.DEFEND, 0, "Ataca a todo monstruo que vea");
        y += 22;
        add(x, y, bw, mark(KaoriMode.FARM) + "Agricultura", KaoriActions.FARM, 0, "Cosecha, siembra y ara junto al agua (dale semillas y una azada)");
        add(x + bw + g, y, bw, "Venir", KaoriActions.TP, 0, "Viene a tu lado");
        add(x + 2 * (bw + g), y, bw + 2, "Mochila", KaoriActions.INVENTORY, 0, "Su inventario, armadura y arma");
        // transformaciones
        int fx = left() + 96, fy = top() + H - 26;
        String[] fn = {"Base", "SSJ 1", "SSJ 2"};
        String[] tips = {"Vuelve a su estado base", "Super Saiyajin: cabello dorado, más fuerza, velocidad, armadura y vida",
                "Super Saiyajin 2: aún más poder, aura con rayos"};
        for (int i = 0; i < 3; i++) {
            add(fx + i * 77, fy, 74, (st.form == i ? "§6▶ §r" : "") + fn[i], KaoriActions.FORM, i, tips[i]);
        }
    }

    private static int page;
    private static final String[] HOUSES = {"Choza", "Casa segura de piedra", "Casa del Guerrero Z", "Base Cápsula"};
    private static final String[] HOUSE_TIPS = {"7×7: cama, cofre, horno, ahumador, barril y mesa de crafteo",
            "9×9 de piedra: puerta de hierro con botones, 2 camas, yunque, alto horno, herrería, caldero y pociones",
            "11×11: todo lo anterior + Generador Protector encendido, huerto de Semillas del Ermitaño y tazones de arroz",
            "13×13: todo lo anterior + mesa de encantamientos con librerías, huerto de trigo, telar y afiladora"};
    private static final long[] BOND = {0L, 24000L, 72000L, 144000L};

    private void initPage2() {
        int x = left() + 96, y = top() + 34, bw = 112, g = 4;
        if (st.hut) {
            add(x, y, bw * 2 + g, "⌂ Guardar " + HOUSES[Math.max(0, Math.min(3, st.hutType))], KaoriActions.HUT, 0,
                    "Guarda la casa en la cápsula (lo de los cofres pasa a su mochila)");
        } else {
            for (int i = 0; i < 4; i++) {
                boolean open = st.bond >= BOND[i];
                String label = open ? "⌂ " + HOUSES[i] : "§8✖ " + HOUSES[i];
                String tip = HOUSE_TIPS[i] + (open ? "" : " · §cSe desbloquea pasando más tiempo con Kaori: faltan " + clock(BOND[i] - st.bond));
                add(x + (i % 2) * (bw + g), y + (i / 2) * 22, bw, label, KaoriActions.HUT, i, tip);
            }
        }
        y += 66;
        add(x, y, 74, "Alimentar", KaoriActions.SHIELD_FEED, 0, "Pone los lingotes de cobre de su mochila en el Generador Protector más cercano");
        add(x + 77, y, 74, "Encender", KaoriActions.SHIELD_POWER, 1, "Enciende el domo protector");
        add(x + 154, y, 74, "Apagar", KaoriActions.SHIELD_POWER, 0, "Apaga el domo protector");
        y += 46;
        add(x, y, 74, "Ofertas", KaoriActions.TRADE_INFO, 0, "Revisa a los aldeanos: las mejores ofertas y lo que ellos piden");
        add(x + 77, y, 74, "Comerciar", KaoriActions.TRADE_AUTO, 0,
                "Vende por ti lo que haya en su mochila y compra lo mejor con sus esmeraldas (30% de descuento)");
        add(x + 154, y, 74, "Conseguir", KaoriActions.TRADE_GET, 0, "Sale a conseguir lo que piden los aldeanos (madera, carbón, piedra, cosechas, caza...)");
    }

    private String mark(KaoriMode m) {
        return st.mode == m.ordinal() ? "§6▶ §r" : "";
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
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && craft.isFocused() && !craft.getValue().isBlank()) {
            send(KaoriActions.CRAFT, 0, craft.getValue());
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        int x = left(), y = top();
        g.fill(x - 1, y - 1, x + W + 1, y + H + 1, EDGE_DK);
        g.fill(x, y, x + W, y + H, EDGE);
        g.fillGradient(x + 2, y + 2, x + W - 2, y + H - 2, BG_TOP, BG_BOT);
        g.fillGradient(x + 2, y + 2, x + W - 2, y + 17, 0xFFE8783C, 0xFFB5531F);
        g.drawString(font, "⚙ Kaori · Mecánica e inventora", x + 8, y + 5, 0xFFFFFFFF, true);
        // retrato
        g.fill(x + 8, y + 22, x + 88, y + H - 30, 0xFF34392C);
        g.fill(x + 8, y + H - 48, x + 88, y + H - 30, 0x33E8783C);
        Entity e = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(st.entityId) : null;
        if (e instanceof KaoriEntity k) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 48, y + H - 36, 40, (float) (x + 48 - mx), (float) (y + 80 - my), k);
            g.drawCenteredString(font, String.format("♥ %.0f/%.0f", k.getHealth(), k.getMaxHealth()), x + 48, y + 25,
                    k.isWeakened() ? 0xFF8A8A8A : 0xFFFF7A6A);
            if (k.isWeakened()) g.drawCenteredString(font, "descansando", x + 48, y + 36, MUTED);
        }
        int sx = x + 96;
        if (page == 1) {
            label(g, "Casas-cápsula", sx, y + 22);
            int unlocked = 0;
            for (long b : BOND) if (st.bond >= b) unlocked++;
            g.drawString(font, "§7Tiempo con Kaori: §f" + clock(st.bond) + " §7· casas: §e" + unlocked + "/4", sx, y + 82, 0xFFFFFFFF, false);
            label(g, "Generador Protector (cobre)", sx, y + 92);
            label(g, "Comercio con aldeanos", sx, y + 138);
            g.drawString(font, "§7Cerca de ella, los aldeanos te dan descuentos.", sx, y + 172, 0xFFFFFFFF, false);
            super.render(g, mx, my, partial);
            return;
        }
        label(g, "Regalos", sx, y + 21);
        // estado de las semillas y del arroz
        long senzu = Math.max(0, st.senzuIn - elapsed()), rice = Math.max(0, st.riceIn - elapsed());
        String s1 = st.seedsLeft > 0 ? "§a" + st.seedsLeft + " por comer" : senzu > 0 ? "§c⌛ " + clock(senzu) : "§a¡listas!";
        String s2 = rice > 0 ? "§c⌛ " + clock(rice) : "§a¡listo!";
        g.drawCenteredString(font, s1, sx + 37, y + 55, 0xFFFFFFFF);
        g.drawCenteredString(font, s2, sx + 37 + 77, y + 55, 0xFFFFFFFF);
        g.drawCenteredString(font, st.hut ? "§ecolocada" : "§7en la cápsula", sx + 37 + 154, y + 55, 0xFFFFFFFF);
        label(g, "Te ayuda: fabricar · farmear · ordenar", sx, y + 65);
        label(g, "Órdenes", sx, y + 159);
        label(g, "Transformación", sx, y + H - 37);
        super.render(g, mx, my, partial);
    }

    /** Tiempo restante en formato día/hora de Minecraft. */
    private static String clock(long ticks) {
        long d = ticks / 24000, h = (ticks % 24000) / 1000, m = (ticks % 1000) * 60 / 1000;
        return d > 0 ? d + "d " + h + "h" : h > 0 ? h + "h " + m + "m" : m + "m";
    }

    private void label(GuiGraphics g, String text, int x, int y) {
        g.drawString(font, text, x, y, TEXT, false);
        g.fill(x + font.width(text) + 4, y + 4, left() + W - 8, y + 5, 0x66E8783C);
    }
}
