package com.tanny.tannymaid.client;

import com.tanny.tannymaid.network.Net;
import com.tanny.tannymaid.network.YunnActionPacket;
import com.tanny.tannymaid.yunn.YunnEntity;
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

/** Menú de Yunn: cuidados Pokémon, Pokédex y mapa, y órdenes. Las respuestas llegan al chat. */
public class YunnScreen extends Screen {
    private static final int W = 300, H = 214;
    private static final int BG_TOP = 0xFFFFF6FA, BG_BOT = 0xFFFBE3EE, EDGE = 0xFFE58AAB, EDGE_DK = 0xFFB0577A, TEXT = 0xFF6A2846;

    private final int entityId;
    private EditBox name;

    public YunnScreen(int entityId) {
        super(Component.literal("Yunn"));
        this.entityId = entityId;
    }

    public static void open(int entityId) {
        Minecraft.getInstance().setScreen(new YunnScreen(entityId));
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - H) / 2; }

    @Override
    protected void init() {
        int x = left() + 92, y = top() + 30, bw = 66, gap = 3;
        add(x, y, bw, "✚ Curar", YunnEntity.A_HEAL, "Cura a todo tu equipo: vida, estados y PP (cada 30 s)");
        add(x + bw + gap, y, bw, "✦ Potenciar", YunnEntity.A_BOOST, "Más amistad, +EVs y un IV al máximo (cada 10 min)");
        add(x + 2 * (bw + gap), y, bw, "☤ Botiquín", YunnEntity.A_KIT, "Pociones, revivir, caramelos raros y vitaminas (cada 10 min)");

        y += 40;
        name = new EditBox(font, x, y, bw * 2 + gap, 18, Component.literal("Pokémon"));
        name.setMaxLength(32);
        name.setHint(Component.literal("§7nombre (ej. pikachu)"));
        addRenderableWidget(name);
        addWithText(x + 2 * (bw + gap), y - 1, bw, "Pokédex", YunnEntity.A_DEX, "Ficha de Pokédex del Pokémon escrito");
        y += 22;
        addWithText(x, y, bw * 2 + gap, "¿Dónde aparece?", YunnEntity.A_WHERE, "En qué biomas aparece, rareza y niveles");
        add(x + 2 * (bw + gap), y, bw, "Este bioma", YunnEntity.A_HERE, "Qué Pokémon pueden aparecer donde estás");
        y += 22;
        add(x, y, bw, "Radar", YunnEntity.A_RADAR, "Pokémon salvajes cerca, con metros y dirección");
        add(x + bw + gap, y, bw * 2 + gap, "Aldea más cercana", YunnEntity.A_VILLAGE, "Dónde está la aldea más cercana (Centro Pokémon)");

        y += 38;
        add(x, y, bw, "Seguirme", YunnEntity.A_FOLLOW, "Te acompaña y te protege");
        add(x + bw + gap, y, bw, "Quedarse", YunnEntity.A_STAY, "Te espera aquí");
        add(x + 2 * (bw + gap), y, bw, "Venir", YunnEntity.A_TP, "Se teletransporta a tu lado");
        y += 22;
        add(x, y, bw * 3 / 2 + 1, "Chansey", YunnEntity.A_CHANSEY, "Saca o guarda a Chansey (cura a Yunn, a ti y a tus Pokémon)");
        add(x + bw * 3 / 2 + 1 + gap, y, bw * 3 / 2 + 2, "Cinderace", YunnEntity.A_CINDERACE, "Saca o guarda a Cinderace (te protege)");
    }

    private void add(int x, int y, int w, String label, int action, String tip) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> send(action, "")).bounds(x, y, w, 18)
                .tooltip(Tooltip.create(Component.literal(tip))).build());
    }

    private void addWithText(int x, int y, int w, String label, int action, String tip) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> send(action, name.getValue())).bounds(x, y, w, 18)
                .tooltip(Tooltip.create(Component.literal(tip))).build());
    }

    private void send(int action, String text) {
        Net.CHANNEL.sendToServer(new YunnActionPacket(entityId, action, text == null ? "" : text.trim()));
        onClose();   // la respuesta de Yunn llega al chat
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && name.isFocused() && !name.getValue().isBlank()) {
            send(YunnEntity.A_DEX, name.getValue());
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
        g.fillGradient(x + 2, y + 2, x + W - 2, y + 16, 0xFFF4A6C2, 0xFFE07FA3);
        g.drawString(font, "✚ Yunn · Enfermera Pokémon", x + 8, y + 5, 0xFFFFFFFF, true);
        // retrato de Yunn
        g.fill(x + 8, y + 22, x + 84, y + H - 8, 0xFFFFE9F2);
        g.fill(x + 8, y + H - 26, x + 84, y + H - 8, 0x22B0577A);
        Entity e = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(entityId) : null;
        if (e instanceof YunnEntity yunn) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x + 46, y + H - 16, 40, (float) (x + 46 - mx), (float) (y + 70 - my), yunn);
            String hp = String.format("♥ %.0f/%.0f", yunn.getHealth(), yunn.getMaxHealth());
            g.drawCenteredString(font, hp, x + 46, y + 24, yunn.isWeakened() ? 0xFF8A8A8A : 0xFFD43C6E);
        }
        int sx = x + 92;
        label(g, "Cuidados Pokémon", sx, y + 20);
        label(g, "Pokédex y mapa", sx, y + 60);
        label(g, "Órdenes", sx, y + 142);
        super.render(g, mx, my, partial);
    }

    private void label(GuiGraphics g, String text, int x, int y) {
        g.drawString(font, text, x, y, TEXT, false);
        g.fill(x + font.width(text) + 4, y + 4, left() + W - 8, y + 5, 0x66E58AAB);
    }
}
