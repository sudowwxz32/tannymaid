package com.cz.client;

import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ClientStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Panel pequeño de chakra (esquina inferior izquierda): manto activo, colas y barra de chakra. */
public class ZorroHud implements IGuiOverlay {
    private float shown = -1;

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partial, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.player.isSpectator()) return;
        ZorroState s = ClientStates.get(mc.player.getId());
        if (!s.awakened) return;
        Font font = mc.font;
        Phase v = s.visible();
        int accent = v == Phase.NONE ? 0xFFFF9A3C : 0xFF000000 | v.cloakColor;
        int x = 4, y = h - 34, bw = 104;
        g.fill(x, y, x + bw + 6, y + 28, 0x70000000);
        g.fill(x, y, x + 2, y + 28, accent);
        String title = s.powering > 0 ? "Transformándose..." : v == Phase.NONE ? "Chakra del zorro" : v.display;
        small(g, font, title, x + 5, y + 3, accent);
        // colas (una marca por cola)
        for (int i = 0; i < v.tails; i++) g.fill(x + 5 + i * 6, y + 11, x + 9 + i * 6, y + 13, accent);
        shown = shown < 0 ? s.chakra : shown + (s.chakra - shown) * 0.25F;
        int by = y + 17;
        g.fill(x + 4, by, x + 4 + bw, by + 7, 0xC0000000);
        int fw = (int) (bw * Math.max(0, Math.min(1, shown / ZorroState.MAX_CHAKRA)));
        g.fill(x + 5, by + 1, x + 4 + fw, by + 6, 0xFFB8420E);
        g.fill(x + 5, by + 1, x + 4 + fw, by + 3, 0xFFFF9A3C);
        small(g, font, "Chakra " + s.chakra + (s.cooldown > 0 ? "  §cagotado " + s.cooldown / 20 + "s" : s.charging ? "  §ecargando" : ""), x + 6, by + 8, 0xFFFFFFFF);
    }

    private static void small(GuiGraphics g, Font font, String text, int x, int y, int color) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(0.72F, 0.72F, 1.0F);
        g.drawString(font, text, 0, 0, color, true);
        g.pose().popPose();
    }
}
