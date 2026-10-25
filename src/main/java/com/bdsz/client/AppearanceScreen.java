package com.bdsz.client;

import com.bdsz.data.HairStyles;
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

/**
 * Apariencia del saiyajin (/raza saiyajin): 1) género, 2) peinado (si eligió Masculino o Femenino), 3) cola.
 * A la izquierda se ve al personaje con lo que se va eligiendo.
 */
public class AppearanceScreen extends Screen {
    private static final int W = 330, H = 210;
    private int step;                 // 0 género, 1 peinado, 2 cola
    private int gender, hair = -1;
    private boolean tail;
    private SaiyanState original;
    private boolean saved;
    private int left, top;

    public AppearanceScreen() {
        super(Component.literal("Apariencia saiyajin"));
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

    /** Muestra en el personaje lo que se está eligiendo (solo en este cliente, hasta confirmar). */
    private void preview() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || original == null) return;
        SaiyanState p = SaiyanState.load(original.save());
        p.gender = gender;
        p.hair = hair;
        p.tail = tail;
        ClientStates.put(mc.player.getId(), p);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int accent = 0xFFFFB830;
        g.fill(left - 1, top - 1, left + W + 1, top + H + 1, accent);
        g.fill(left, top, left + W, top + H, 0xF0101216);
        g.fill(left, top, left + W, top + 18, 0xFF1C2028);
        g.drawString(font, "§lApariencia saiyajin", left + 8, top + 5, 0xFFFFFFFF, true);
        String[] steps = {"1 Género", "2 Peinado", "3 Cola"};
        for (int i = 0; i < 3; i++) {
            boolean on = i == step, skip = i == 1 && gender == 0;
            int sx = left + 150 + i * 60;
            g.drawString(font, (on ? "§e" : skip ? "§8" : "§7") + steps[i], sx, top + 5, 0xFFFFFFFF, true);
        }
        // personaje
        g.fill(left + 6, top + 24, left + 106, top + H - 6, 0xFF1A1E26);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, left + 56, top + H - 22, 58, left + 56 - mx, top + 80 - my, mc.player);
        }
        int ox = left + 116, oy = top + 28;
        if (step == 0) {
            g.drawString(font, "§f¿Cuál es el género de tu saiyajin?", ox, oy, 0xFFFFFFFF, true);
            option(g, ox, oy + 18, 200, "Masculino", gender == 1, mx, my);
            option(g, ox, oy + 42, 200, "Femenino", gender == 2, mx, my);
            option(g, ox, oy + 66, 200, "No definir", gender == 0, mx, my);
            small(g, gender == 0 ? "Mantienes tu apariencia normal; al transformarte" : gender == 2 ? "Cuerpo femenino: un poco de cadera y busto," : "Podrás elegir entre 6 peinados.", ox, oy + 96, 0xFF8A93A6);
            small(g, gender == 0 ? "usas el cabello de cada fase." : gender == 2 ? "y 7 peinados para elegir." : "", ox, oy + 105, 0xFF8A93A6);
        } else if (step == 1) {
            g.drawString(font, "§fElige tu peinado §7(color base)", ox, oy, 0xFFFFFFFF, true);
            int i = 0;
            for (int id = 0; id < HairStyles.ALL.length; id++) {
                HairStyles.Style st = HairStyles.ALL[id];
                if (st.female() != (gender == 2)) continue;
                int bx = ox + (i % 2) * 104, by = oy + 14 + (i / 2) * 22;
                option(g, bx, by, 100, st.name(), hair == id, mx, my);
                g.fill(bx + 3, by + 5, bx + 9, by + 11, 0xFF000000 | st.color());
                i++;
            }
            small(g, "Al transformarte, tu peinado toma el color de la fase", ox, oy + 106, 0xFF8A93A6);
            small(g, "y se levanta y crece según su poder.", ox, oy + 115, 0xFF8A93A6);
        } else {
            g.drawString(font, "§f¿Quieres tener cola?", ox, oy, 0xFFFFFFFF, true);
            option(g, ox, oy + 18, 200, "Con cola (café)", tail, mx, my);
            option(g, ox, oy + 42, 200, "Sin cola", !tail, mx, my);
            small(g, "En la Fase 4 la cola se vuelve roja.", ox, oy + 72, 0xFF8A93A6);
        }
        // botones de abajo
        int by = top + H - 24;
        if (step > 0) button(g, ox, by, 70, "« Atrás", mx, my);
        button(g, left + W - 86, by, 78, step == 2 ? "Listo ✔" : "Siguiente »", mx, my);
        super.render(g, mx, my, pt);
    }

    private void option(GuiGraphics g, int x, int y, int w, String text, boolean on, int mx, int my) {
        boolean hover = in(mx, my, x, y, w, 18);
        g.fill(x, y, x + w, y + 18, on ? 0xC0FFB830 : hover ? 0x40FFFFFF : 0x30FFFFFF);
        if (on) outline(g, x, y, w, 18, 0xFFFFFFFF);
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
        if (step == 0) {
            if (in(mx, my, ox, oy + 18, 200, 18)) setGender(1);
            else if (in(mx, my, ox, oy + 42, 200, 18)) setGender(2);
            else if (in(mx, my, ox, oy + 66, 200, 18)) setGender(0);
        } else if (step == 1) {
            int i = 0;
            for (int id = 0; id < HairStyles.ALL.length; id++) {
                if (HairStyles.ALL[id].female() != (gender == 2)) continue;
                int bx = ox + (i % 2) * 104, by = oy + 14 + (i / 2) * 22;
                if (in(mx, my, bx, by, 100, 18)) { hair = id; click(); preview(); }
                i++;
            }
        } else {
            if (in(mx, my, ox, oy + 18, 200, 18)) { tail = true; click(); preview(); }
            else if (in(mx, my, ox, oy + 42, 200, 18)) { tail = false; click(); preview(); }
        }
        int by = top + H - 24;
        if (step > 0 && in(mx, my, ox, by, 70, 18)) {
            step = step == 2 && gender == 0 ? 0 : step - 1;
            click();
            return true;
        }
        if (in(mx, my, left + W - 86, by, 78, 18)) {
            click();
            if (step == 0) step = gender == 0 ? 2 : 1;
            else if (step == 1) {
                if (hair < 0 || HairStyles.ALL[hair].female() != (gender == 2)) hair = gender == 2 ? 6 : 0;
                step = 2;
            } else {
                saved = true;
                Net.CHANNEL.sendToServer(new AppearancePacket(gender, gender == 0 ? -1 : hair, tail));
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
        // si se cierra sin confirmar, el personaje vuelve a como estaba
        Minecraft mc = Minecraft.getInstance();
        if (!saved && original != null && mc.player != null) ClientStates.put(mc.player.getId(), original);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
