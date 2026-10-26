package com.cz.client;

import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ActionPacket;
import com.cz.network.ClientStates;
import com.cz.network.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Menú para elegir el manto (1 a 5 colas). Clic, rueda o flechas para elegir; Enter o Y para transformarse. */
public class PhaseScreen extends Screen {
    private static final String[] DESC = {
            "", "La ira despierta. Manto rojo con orejas y una cola.",
            "De rodillas, el chakra estalla. Dos colas.",
            "Furia. El manto hierve, burbujea y quema. Tres colas.",
            "Cae a cuatro patas. Cuerpo rojo, ojos blancos, colmillos.",
            "Huesos de zorro cubren el cuerpo como armadura. Cinco colas."};
    private static final int ROW = 30, W = 250;
    private int sel;

    public PhaseScreen() {
        super(Component.literal("Chakra del Zorro"));
    }

    private ZorroState st() {
        return ClientStates.get(minecraft.player.getId());
    }

    @Override
    protected void init() {
        sel = Math.max(1, Math.min(5, st().selected));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - (ROW * 5 + 40)) / 2; }

    private void select(int i) {
        i = Math.max(1, Math.min(5, i));
        if (i != sel) {
            sel = i;
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.SELECT, sel));
        }
    }

    private void transform() {
        Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.SELECT, sel));
        Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.TRANSFORM, 0));
        onClose();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        ZorroState s = st();
        int x = left(), y = top();
        g.fill(x - 6, y - 6, x + W + 6, y + ROW * 5 + 34, 0xC0100404);
        g.fill(x - 6, y - 6, x + W + 6, y - 4, 0xFFFF6A1A);
        g.drawCenteredString(font, "§6§lCHAKRA DEL ZORRO", width / 2, y, 0xFFFFFF);
        g.drawCenteredString(font, "§7Chakra: §e" + s.chakra + "§7/" + ZorroState.MAX_CHAKRA
                + (s.phase > 0 ? "   §7Activo: §c" + s.active().display : ""), width / 2, y + 11, 0xFFFFFF);
        int ry = y + 26;
        for (int i = 1; i <= 5; i++) {
            Phase p = Phase.byId(i);
            int rowY = ry + (i - 1) * ROW;
            boolean on = i == sel, hover = mx >= x && mx < x + W && my >= rowY && my < rowY + ROW - 2;
            int col = 0xFF000000 | p.cloakColor;
            g.fill(x, rowY, x + W, rowY + ROW - 2, on ? 0xA0602010 : hover ? 0x60401008 : 0x50000000);
            if (on) {
                g.fill(x, rowY, x + 2, rowY + ROW - 2, col);
                g.fill(x + W - 2, rowY, x + W, rowY + ROW - 2, col);
            }
            g.drawString(font, (on ? "§l" : "") + p.display, x + 6, rowY + 3, col, true);
            for (int t = 0; t < p.tails; t++) g.fill(x + W - 10 - t * 7, rowY + 4, x + W - 5 - t * 7, rowY + 9, col);
            boolean enough = s.chakra >= p.minChakra;
            String stats = "§7+" + p.health / 2 + "❤ +" + p.damage + "⚔ +" + Math.round(p.speed * 100) + "% vel  "
                    + (enough ? "§a" : "§c") + p.minChakra + " chakra";
            small(g, stats, x + 6, rowY + 13);
            small(g, "§8" + DESC[i], x + 6, rowY + 20);
        }
        int by = ry + ROW * 5 + 2;
        g.drawCenteredString(font, "§7Clic/rueda: elegir   §eEnter/Y§7: transformarse   §eN§7: cerrar", width / 2, by, 0xFFFFFF);
        super.render(g, mx, my, partial);
    }

    private void small(GuiGraphics g, String text, int x, int y) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.75F, 0.75F, 1F);
        g.drawString(font, text, 0, 0, 0xFFFFFF, false);
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = left(), ry = top() + 26;
        if (mx >= x && mx < x + W && my >= ry && my < ry + ROW * 5) {
            int i = (int) ((my - ry) / ROW) + 1;
            if (i == sel && button == 0) transform(); else select(i);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        select(sel + (delta > 0 ? -1 : 1));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_UP) { select(sel - 1); return true; }
        if (key == GLFW.GLFW_KEY_DOWN) { select(sel + 1); return true; }
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_5) { select(key - GLFW.GLFW_KEY_0); return true; }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || ClientModEvents.TRANSFORM.matches(key, scan)) { transform(); return true; }
        if (ClientModEvents.MENU.matches(key, scan)) { onClose(); return true; }
        return super.keyPressed(key, scan, mods);
    }
}
