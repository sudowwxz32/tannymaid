package com.linterna.client;

import com.linterna.data.Constructs;
import com.linterna.data.LanternState;
import com.linterna.network.ActionPacket;
import com.linterna.network.ClientStates;
import com.linterna.network.Net;
import com.linterna.registry.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Catálogo del anillo (tecla M): constructos de energía verde, todo en holograma. */
public class CatalogScreen extends Screen {
    private static final int W = 300, H = 176, CW = 68, CH = 62;
    private final long opened = System.currentTimeMillis();
    private ItemStack[] icons;

    public CatalogScreen() {
        super(Component.literal("Catálogo del anillo"));
    }

    @Override
    protected void init() {
        icons = new ItemStack[]{new ItemStack(Items.LIME_STAINED_GLASS), new ItemStack(Items.SHIELD), new ItemStack(Items.GLASS),
                new ItemStack(Items.LIME_CARPET), new ItemStack(Items.IRON_BARS), new ItemStack(ModRegistry.ENERGY_BOMB.get()),
                new ItemStack(ModRegistry.ENERGY_SWORD.get()), new ItemStack(Items.EMERALD)};
    }

    private int left() { return (width - W) / 2; }
    private int top() { return (height - H) / 2; }

    private int cardX(int i) { return left() + 10 + (i % 4) * (CW + 4); }
    private int cardY(int i) { return top() + 26 + (i / 4) * (CH + 6); }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = 0; i < Constructs.NAMES.length; i++) {
            if (mx >= cardX(i) && mx < cardX(i) + CW && my >= cardY(i) && my < cardY(i) + CH) {
                Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.CATALOG, i));
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        float t = (System.currentTimeMillis() - opened) / 1000F;
        int x = left(), y = top();
        g.fillGradient(x, y, x + W, y + H, 0x80063A14, 0x60021A08);
        g.fill(x, y, x + W, y + 1, 0xFF4CFF6A);
        g.fill(x, y + H - 1, x + W, y + H, 0xFF4CFF6A);
        g.fill(x, y, x + 1, y + H, 0xFF2AA040);
        g.fill(x + W - 1, y, x + W, y + H, 0xFF2AA040);
        for (int sy = y + 2; sy < y + H - 2; sy += 3) g.fill(x + 1, sy, x + W - 1, sy + 1, 0x0C6AFF80);
        int scan = (int) ((t * 50) % H);
        g.fill(x + 1, y + scan, x + W - 1, y + scan + 2, 0x306AFF80);
        LanternState s = Minecraft.getInstance().player == null ? new LanternState() : ClientStates.get(Minecraft.getInstance().player.getId());
        g.drawString(font, "◈ CATÁLOGO DEL ANILLO", x + 10, y + 8, 0xFFB8FFC4, true);
        String e = "Energía " + (int) Math.ceil(s.charge * 100.0 / LanternState.MAX_CHARGE) + "%";
        g.drawString(font, e, x + W - 10 - font.width(e), y + 8, 0xFF7CFF8C, false);
        String tip = "";
        for (int i = 0; i < Constructs.NAMES.length; i++) {
            int cx = cardX(i), cy = cardY(i);
            boolean hover = mx >= cx && mx < cx + CW && my >= cy && my < cy + CH;
            boolean can = s.charge >= Constructs.COST[i];
            g.fill(cx, cy, cx + CW, cy + CH, hover ? 0x705AFF78 : 0x3048E060);
            g.fill(cx, cy, cx + CW, cy + 1, 0xA07CFF8C);
            float bob = Mth.sin(t * 2 + i) * 1.5F;
            g.pose().pushPose();
            g.pose().translate(cx + CW / 2F - 12, cy + 6 + bob, 0);
            g.pose().scale(1.5F, 1.5F, 1);
            g.renderItem(icons[i], 0, 0);
            g.pose().popPose();
            g.fill(cx + 2, cy + 6, cx + CW - 2, cy + 32, 0x4032FF5A);         // tinte verde holograma
            g.drawCenteredString(font, Constructs.NAMES[i].length() > 11 ? Constructs.NAMES[i].substring(0, 10) + "." : Constructs.NAMES[i],
                    cx + CW / 2, cy + 36, can ? 0xFFDFFFE4 : 0xFF6A8A70);
            g.drawCenteredString(font, (Constructs.COST[i] * 100 / LanternState.MAX_CHARGE) + "%", cx + CW / 2, cy + 48, can ? 0xFF7CFF8C : 0xFFFF6A6A);
            if (hover) tip = Constructs.NAMES[i] + ": " + Constructs.TIPS[i];
        }
        if (!tip.isEmpty()) g.renderTooltip(font, Component.literal(tip), mx, my);
        super.render(g, mx, my, partial);
    }
}
