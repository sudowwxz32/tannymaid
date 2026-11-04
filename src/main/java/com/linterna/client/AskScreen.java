package com.linterna.client;

import com.linterna.network.ActionPacket;
import com.linterna.network.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** La pregunta de la luz verde: ¿Tienes miedo? */
public class AskScreen extends Screen {
    private final long opened = System.currentTimeMillis();

    public AskScreen() {
        super(Component.literal("¿Tienes miedo?"));
    }

    @Override
    protected void init() {
        int cx = width / 2, y = height / 2 + 30;
        addRenderableWidget(Button.builder(Component.literal("§fSí"), b -> answer(true)).bounds(cx - 110, y, 100, 24).build());
        addRenderableWidget(Button.builder(Component.literal("§a§lNo"), b -> answer(false)).bounds(cx + 10, y, 100, 24).build());
    }

    private void answer(boolean afraid) {
        Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.ANSWER, afraid ? 1 : 0));
        onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        float t = (System.currentTimeMillis() - opened) / 1000F;
        int a = (int) (Mth.clamp(t, 0, 1) * 170);
        g.fillGradient(0, 0, width, height, (a << 24) | 0x062A0E, ((a + 40) << 24) | 0x020A04);
        // anillo de luz verde
        int cx = width / 2, cy = height / 2 - 30;
        for (int i = 0; i < 64; i++) {
            double ang = i * Math.PI * 2 / 64 + t * 0.6;
            int x = cx + (int) (Math.cos(ang) * 70), y = cy + (int) (Math.sin(ang) * 70);
            g.fill(x - 1, y - 1, x + 2, y + 2, 0xFF4CFF6A);
        }
        float pulse = 0.8F + 0.2F * Mth.sin(t * 3F);
        int col = 0xFF000000 | ((int) (80 * pulse) << 16) | ((int) (255 * pulse) << 8) | (int) (110 * pulse);
        g.pose().pushPose();
        g.pose().translate(cx, cy - 8, 0);
        g.pose().scale(2.5F, 2.5F, 1);
        g.drawCenteredString(font, "¿Tienes miedo?", 0, 0, col);
        g.pose().popPose();
        g.drawCenteredString(font, "§7Una voz resuena dentro de la luz verde...", cx, cy + 22, 0xFFFFFFFF);
        super.render(g, mx, my, partial);
    }
}
