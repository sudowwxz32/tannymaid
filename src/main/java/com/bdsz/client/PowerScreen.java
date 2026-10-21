package com.bdsz.client;

import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ActionPacket;
import com.bdsz.network.ClientStates;
import com.bdsz.network.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Menú (tecla K): elige la fase y el nivel de Kaioken. P transforma, U activa el aura. */
public class PowerScreen extends Screen {
    private int form;
    private int kaioken;

    public PowerScreen() {
        super(Component.literal("Block Dragon Super Z"));
    }

    private SaiyanState state() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? new SaiyanState() : ClientStates.get(mc.player.getId());
    }

    @Override
    protected void init() {
        SaiyanState s = state();
        form = s.selected;
        kaioken = s.kaioken;
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        int cx = width / 2;
        int top = Math.max(30, height / 2 - 110);
        Form[] forms = Form.values();
        for (int i = 0; i < forms.length; i++) {
            final int id = i;
            String label = (i == form ? "» " : "") + forms[i].display + (i == form ? " «" : "");
            addRenderableWidget(Button.builder(Component.literal(label), b -> { form = id; send(); })
                    .bounds(cx - 160, top + 14 + i * 22, 150, 20).build());
        }
        for (int i = 0; i <= 10; i++) {
            final int lvl = i * 10;
            String label = lvl == 0 ? "Apagado" : "x" + lvl;
            if (lvl == kaioken) label = "» " + label + " «";
            int col = i == 0 ? 0 : (i - 1) % 2;
            int row = i == 0 ? 0 : (i - 1) / 2 + 1;
            int x = i == 0 ? cx + 10 : cx + 10 + col * 76;
            int w = i == 0 ? 150 : 74;
            addRenderableWidget(Button.builder(Component.literal(label), b -> { kaioken = lvl; send(); })
                    .bounds(x, top + 14 + row * 22, w, 20).build());
        }
        SaiyanState s = state();
        int by = top + 14 + 8 * 22 + 6;
        addRenderableWidget(Button.builder(Component.literal(s.transformed ? "Destransformar (P)" : "Transformar (P)"),
                b -> { Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.TRANSFORM, 0, 0)); onClose(); })
                .bounds(cx - 160, by, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal(s.aura ? "Apagar aura (U)" : "Encender aura (U)"),
                b -> { Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.AURA, 0, 0)); onClose(); })
                .bounds(cx + 10, by, 150, 20).build());
    }

    private void send() {
        Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.SELECT, form, kaioken));
        rebuild();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = width / 2;
        int top = Math.max(30, height / 2 - 110);
        g.drawCenteredString(font, "§6§lBlock Dragon Super Z", cx, top - 18, 0xFFFFFF);
        g.drawString(font, "§eFases", cx - 160, top + 2, 0xFFFFFF);
        g.drawString(font, "§cKaioken", cx + 10, top + 2, 0xFFFFFF);
        Form f = Form.byId(form);
        float dmg = f.damage * (1.0F + kaioken / 20.0F);
        int res = Math.round(Math.min(0.9F, f.resist + kaioken / 400.0F) * 100);
        String stats = String.format("Selección: §f%s%s §7→ Fuerza x%.1f · Resistencia %d%%", f.display,
                kaioken > 0 ? " + Kaioken x" + kaioken : "", dmg, res);
        g.drawCenteredString(font, "§7" + stats, cx, top + 14 + 8 * 22 + 32, 0xFFFFFF);
        SaiyanState s = state();
        String now = s.transformed ? "§aTransformado" : "§7Estado base";
        g.drawCenteredString(font, now + "  §8|  §7Aura: " + (s.aura ? "§bencendida" : "§8apagada"), cx, top + 14 + 8 * 22 + 46, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (ClientModEvents.MENU.matches(key, scan)) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
