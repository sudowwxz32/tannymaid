package com.bdsz.client;

import com.bdsz.data.HairStyles;
import com.bdsz.data.Lineage;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.AppearancePacket;
import com.bdsz.network.ClientStates;
import com.bdsz.network.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * Apariencia del saiyajin (/raza saiyajin): género → bloque (solo online, una vez) → peinado → cola.
 * A la izquierda se ve al personaje con lo que se va eligiendo.
 */
public class AppearanceScreen extends Screen {
    private static final int W = 340, H = 214;
    private static final int GENDER = 0, LINEAGE = 1, HAIR = 2, TAIL = 3;
    private final boolean online;
    private int step = GENDER;
    private int gender, hair = -1, lineage;      // lineage: 0 nada, ids de Lineage o Lineage.RANDOM
    private boolean tail;
    private SaiyanState original;
    private boolean saved;
    private int left, top;

    public AppearanceScreen(boolean online) {
        super(Component.literal("Apariencia saiyajin"));
        this.online = online;
    }

    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        if (original == null && mc.player != null) {
            SaiyanState s = ClientStates.get(mc.player.getId());
            original = SaiyanState.load(s.save());
            gender = s.gender;
            hair = s.hair;
            tail = s.tail;
        }
        left = (width - W) / 2;
        top = Math.max(4, (height - H) / 2);
        preview();
    }

    /** ¿Se muestra el paso del bloque? Solo online y si todavía no se eligió uno. */
    private boolean lineageStep() {
        return online && original != null && original.lineage == 0;
    }

    private List<Integer> steps() {
        List<Integer> l = new ArrayList<>();
        l.add(GENDER);
        if (lineageStep()) l.add(LINEAGE);
        if (gender != 0) l.add(HAIR);
        l.add(TAIL);
        return l;
    }

    private void preview() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || original == null) return;
        SaiyanState p = SaiyanState.load(original.save());
        p.gender = gender;
        p.hair = hair;
        p.tail = tail;
        ClientStates.put(mc.player.getId(), p);
    }

    /** Opciones de bloque según el género: hombres G, V o aleatorio; mujeres y sin definir: Normal o aleatorio. */
    private int[] lineageOptions() {
        return gender == 1 ? new int[]{Lineage.G.ordinal(), Lineage.V.ordinal(), Lineage.RANDOM}
                : new int[]{Lineage.NORMAL.ordinal(), Lineage.RANDOM};
    }

    private static String lineageName(int id) {
        if (id == Lineage.RANDOM) return "Aleatorio";
        Lineage l = Lineage.byId(id);
        return l == Lineage.G ? "Bloque G (naranja)" : l == Lineage.V ? "Bloque V (azul)" : l.display;
    }

    private static int lineageColor(int id) {
        return id == Lineage.RANDOM ? 0xD070FF : Lineage.byId(id).color;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int accent = 0xFFFFB830;
        g.fill(left - 1, top - 1, left + W + 1, top + H + 1, accent);
        g.fill(left, top, left + W, top + H, 0xF0101216);
        g.fill(left, top, left + W, top + 18, 0xFF1C2028);
        g.drawString(font, "§lApariencia saiyajin", left + 8, top + 5, 0xFFFFFFFF, true);
        List<Integer> st = steps();
        String[] names = {"Género", "Bloque", "Peinado", "Cola"};
        int sx = left + 132;
        for (int i = 0; i < st.size(); i++) {
            String t = (st.get(i) == step ? "§e" : "§7") + (i + 1) + " " + names[st.get(i)];
            g.drawString(font, t, sx, top + 5, 0xFFFFFFFF, true);
            sx += font.width(t) + 8;
        }
        g.fill(left + 6, top + 24, left + 106, top + H - 6, 0xFF1A1E26);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, left + 56, top + H - 22, 58, left + 56 - mx, top + 80 - my, mc.player);
        }
        int ox = left + 116, oy = top + 28;
        if (step == GENDER) {
            g.drawString(font, "§f¿Cuál es el género de tu saiyajin?", ox, oy, 0xFFFFFFFF, true);
            option(g, ox, oy + 18, 210, "Masculino", gender == 1, mx, my, 0);
            option(g, ox, oy + 42, 210, "Femenino", gender == 2, mx, my, 0);
            option(g, ox, oy + 66, 210, "No definir", gender == 0, mx, my, 0);
            small(g, gender == 0 ? "Mantienes tu apariencia normal; al transformarte" : gender == 2 ? "Cuerpo femenino y 7 peinados para elegir." : "Podrás elegir entre 6 peinados.", ox, oy + 96, 0xFF8A93A6);
            if (gender == 0) small(g, "usas el cabello de cada fase.", ox, oy + 105, 0xFF8A93A6);
            if (online && original != null && original.lineage != 0) {
                Lineage l = Lineage.byId(original.lineage);
                small(g, "Tu bloque: " + l.display + " (ya elegido)", ox, oy + 120, 0xFF000000 | l.color);
            }
        } else if (step == LINEAGE) {
            g.drawString(font, "§fElige tu bloque §7(partida online)", ox, oy, 0xFFFFFFFF, true);
            int[] opts = lineageOptions();
            for (int i = 0; i < opts.length; i++) {
                option(g, ox, oy + 16 + i * 24, 210, lineageName(opts[i]), lineage == opts[i], mx, my, lineageColor(opts[i]));
            }
            String desc = lineage == Lineage.RANDOM ? "Te puede tocar el Bloque G, V, A, B o R." : lineage == 0 ? "Elige una opción." : Lineage.byId(lineage).description;
            small(g, desc, ox, oy + 16 + opts.length * 24 + 4, 0xFFFFE08A);
            small(g, "Todos incluyen el Kaioken desde el inicio.", ox, oy + 16 + opts.length * 24 + 14, 0xFF8A93A6);
            small(g, "§c¡Solo se elige una vez!", ox, oy + 16 + opts.length * 24 + 24, 0xFFFFFFFF);
        } else if (step == HAIR) {
            g.drawString(font, "§fElige tu peinado §7(color base)", ox, oy, 0xFFFFFFFF, true);
            int i = 0;
            for (int id = 0; id < HairStyles.ALL.length; id++) {
                HairStyles.Style s = HairStyles.ALL[id];
                if (s.female() != (gender == 2)) continue;
                int bx = ox + (i % 2) * 106, by = oy + 14 + (i / 2) * 22;
                option(g, bx, by, 102, s.name(), hair == id, mx, my, s.color());
                i++;
            }
            small(g, "Al transformarte, tu peinado toma el color de la fase", ox, oy + 106, 0xFF8A93A6);
            small(g, "y se levanta y crece según su poder.", ox, oy + 115, 0xFF8A93A6);
        } else {
            g.drawString(font, "§f¿Quieres tener cola?", ox, oy, 0xFFFFFFFF, true);
            option(g, ox, oy + 18, 210, "Con cola (café)", tail, mx, my, 0x6A4022);
            option(g, ox, oy + 42, 210, "Sin cola", !tail, mx, my, 0);
            small(g, "En la Fase 4 la cola se vuelve roja.", ox, oy + 72, 0xFF8A93A6);
        }
        int by = top + H - 24;
        if (step != GENDER) button(g, ox, by, 70, "« Atrás", mx, my);
        boolean last = st.indexOf(step) == st.size() - 1;
        button(g, left + W - 86, by, 78, last ? "Listo ✔" : "Siguiente »", mx, my);
        super.render(g, mx, my, pt);
    }

    private void option(GuiGraphics g, int x, int y, int w, String text, boolean on, int mx, int my, int swatch) {
        boolean hover = in(mx, my, x, y, w, 18);
        g.fill(x, y, x + w, y + 18, on ? 0xC0FFB830 : hover ? 0x40FFFFFF : 0x30FFFFFF);
        if (on) outline(g, x, y, w, 18, 0xFFFFFFFF);
        if (swatch != 0) g.fill(x + 3, y + 5, x + 9, y + 11, 0xFF000000 | swatch);
        g.drawString(font, text, x + 13, y + 5, on ? 0xFF000000 : 0xFFE6E8EE, false);
    }

    private void button(GuiGraphics g, int x, int y, int w, String text, int mx, int my) {
        boolean hover = in(mx, my, x, y, w, 18);
        g.fill(x, y, x + w, y + 18, hover ? 0xFFFFC850 : 0xFFB8862A);
        g.drawCenteredString(font, text, x + w / 2, y + 5, 0xFFFFFFFF);
    }

    private void small(GuiGraphics g, String text, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.75F, 0.75F, 1.0F);
        g.drawString(font, text, 0, 0, color, true);
        g.pose().popPose();
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int ox = left + 116, oy = top + 28;
        if (step == GENDER) {
            if (in(mx, my, ox, oy + 18, 210, 18)) setGender(1);
            else if (in(mx, my, ox, oy + 42, 210, 18)) setGender(2);
            else if (in(mx, my, ox, oy + 66, 210, 18)) setGender(0);
        } else if (step == LINEAGE) {
            int[] opts = lineageOptions();
            for (int i = 0; i < opts.length; i++) {
                if (in(mx, my, ox, oy + 16 + i * 24, 210, 18)) { lineage = opts[i]; click(); }
            }
        } else if (step == HAIR) {
            int i = 0;
            for (int id = 0; id < HairStyles.ALL.length; id++) {
                if (HairStyles.ALL[id].female() != (gender == 2)) continue;
                int bx = ox + (i % 2) * 106, by = oy + 14 + (i / 2) * 22;
                if (in(mx, my, bx, by, 102, 18)) { hair = id; click(); preview(); }
                i++;
            }
        } else {
            if (in(mx, my, ox, oy + 18, 210, 18)) { tail = true; click(); preview(); }
            else if (in(mx, my, ox, oy + 42, 210, 18)) { tail = false; click(); preview(); }
        }
        List<Integer> st = steps();
        int idx = st.indexOf(step);
        int by = top + H - 24;
        if (step != GENDER && in(mx, my, ox, by, 70, 18)) {
            step = st.get(Math.max(0, idx - 1));
            click();
            return true;
        }
        if (in(mx, my, left + W - 86, by, 78, 18)) {
            click();
            if (step == LINEAGE && lineage == 0) return true;            // hay que elegir un bloque
            if (step == HAIR && (hair < 0 || HairStyles.ALL[hair].female() != (gender == 2))) hair = gender == 2 ? 6 : 0;
            if (idx < st.size() - 1) {
                step = st.get(idx + 1);
            } else {
                saved = true;
                Net.CHANNEL.sendToServer(new AppearancePacket(gender, gender == 0 ? -1 : hair, tail, lineageStep() ? lineage : 0));
                onClose();
            }
            preview();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private void setGender(int gnd) {
        if (gender != gnd) {
            gender = gnd;
            lineage = 0;                                   // las opciones de bloque cambian con el género
            HairStyles.Style st = HairStyles.get(hair);
            if (gnd == 0) hair = -1;
            else if (st == null || st.female() != (gnd == 2)) hair = gnd == 2 ? 6 : 0;
        }
        click();
        preview();
    }

    private static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.6F));
    }

    @Override
    public void removed() {
        Minecraft mc = Minecraft.getInstance();
        if (!saved && original != null && mc.player != null) ClientStates.put(mc.player.getId(), original);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
