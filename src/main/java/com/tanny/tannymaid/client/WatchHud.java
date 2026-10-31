package com.tanny.tannymaid.client;

import com.mojang.math.Axis;
import com.tanny.tannymaid.network.WatchPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Medidor de metros hasta el Pokémon que Tanny encontró (arriba al centro de la pantalla). */
public final class WatchHud implements IGuiOverlay {
    private static boolean active;
    private static int entityId = -1;
    private static Component name = Component.empty();
    private static Vec3 lastPos = Vec3.ZERO;
    private static long lastPacket;
    private static long shownAt;

    public static void receive(WatchPacket p) {
        Minecraft mc = Minecraft.getInstance();
        long now = mc.level != null ? mc.level.getGameTime() : 0;
        if (!p.active) {
            active = false;
            return;
        }
        if (!active || p.entityId != entityId) shownAt = now;
        active = true;
        entityId = p.entityId;
        name = p.name;
        lastPos = new Vec3(p.x, p.y, p.z);
        lastPacket = now;
    }

    public static void clear() {
        active = false;
        entityId = -1;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partial, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (!active || mc.player == null || mc.level == null || mc.options.hideGui) return;
        long now = mc.level.getGameTime();
        if (now - lastPacket > 60) {
            active = false;
            return;
        }
        Vec3 target = lastPos;
        Entity e = mc.level.getEntity(entityId);
        if (e != null && e.isAlive()) target = e.getPosition(partial).add(0, e.getBbHeight() * 0.5, 0);
        Vec3 eye = mc.player.getEyePosition(partial);
        double dist = eye.distanceTo(target);

        Font font = mc.font;
        int bw = 156, bh = 30;
        int x = (w - bw) / 2, y = 22;
        float pop = Math.min(1.0F, (now - shownAt + partial) / 6.0F);
        float pulse = 0.5F + 0.5F * Mth.sin((now + partial) * 0.25F);
        g.pose().pushPose();
        g.pose().translate(w / 2.0F, y, 0);
        g.pose().scale(0.6F + 0.4F * pop, 0.6F + 0.4F * pop, 1);
        g.pose().translate(-w / 2.0F, -y, 0);

        g.fill(x - 1, y - 1, x + bw + 1, y + bh + 1, 0xFFB0577A);
        g.fillGradient(x, y, x + bw, y + bh, 0xE0FBEFF2, 0xE0EBD3DB);
        Component title = Component.literal("✿ Tanny: ¡").append(name).append("!");
        g.drawString(font, title, x + 6, y + 4, 0xFF5A1E35, false);

        // Medidor: se llena a medida que te acercas (0–64 m)
        int mx = x + 6, my = y + 17, mw = bw - 52, mh = 7;
        float fill = (float) Mth.clamp(1.0 - dist / 64.0, 0.0, 1.0);
        g.fill(mx - 1, my - 1, mx + mw + 1, my + mh + 1, 0xFF6E4457);
        g.fill(mx, my, mx + mw, my + mh, 0xFF3A2236);
        int col = dist < 8 ? 0xFF6BD68A : dist < 24 ? 0xFFF2C14E : 0xFFE58AAB;
        g.fill(mx, my, mx + (int) (mw * fill), my + mh, col);
        g.fill(mx, my, mx + (int) (mw * fill), my + 2, 0x66FFFFFF);
        String meters = (int) Math.round(dist) + " m";
        g.drawString(font, meters, x + bw - 40, y + 16, dist < 8 ? 0xFF2E8A4E : 0xFF5A1E35, false);

        // Flecha que apunta hacia el Pokémon
        double dx = target.x - eye.x, dz = target.z - eye.z;
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float rel = Mth.wrapDegrees(targetYaw - mc.player.getViewYRot(partial));
        int ax = x + bw - 9, ay = y + 15;
        g.pose().pushPose();
        g.pose().translate(ax, ay, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(rel));
        int ac = 0xFF000000 | (int) (0xB0 + 0x4F * pulse) << 16 | 0x577A;
        g.fill(-1, -5, 1, 4, ac);
        g.fill(-3, -3, 3, -2, ac);
        g.fill(-2, -4, 2, -3, ac);
        g.pose().popPose();

        g.pose().popPose();
    }
}
