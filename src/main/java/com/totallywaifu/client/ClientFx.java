package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.network.Fx;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Animaciones en primera persona:
 * <ul>
 *   <li>Intro de un mundo nuevo: una chica te entrega la Vara de Transformación con palabras en pantalla, caes del cielo y aterrizas con una explosión.</li>
 *   <li>Logros con animación (la vara, colección completa, waifu sorpresa).</li>
 *   <li>Dormir en las piernas de Yume: la miras desde abajo y tus ojos se cierran poco a poco.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = TotallyWaifu.MODID, value = Dist.CLIENT)
public final class ClientFx {
    private ClientFx() {}

    private static long ticks;
    private static long introAt = -1, flashAt = -1, landedAt = -1, bannerAt = -1, completeAt = -1, extrasAt = -1;
    private static int introLen = 170;
    private static boolean falling;
    private static float introYaw;
    private static String bannerTitle = "", bannerSub = "";
    private static ItemStack bannerIcon = ItemStack.EMPTY;
    private static int sleepGirl = -1;

    private static final String[][] LINES = {
            {"20", "§f...¿Me escuchas?"},
            {"48", "§fBienvenido a un mundo donde las cosas sueñan con ser algo más."},
            {"80", "§fToma esto: la §d§lVara de Transformación§r§f."},
            {"112", "§fMesas, hornos, camas, animales y monstruos... ¡se volverán chicas! ♥"},
            {"142", "§d§l¡Ahora ve! ♥"}};

    public static void on(int type, int arg) {
        Minecraft mc = Minecraft.getInstance();
        switch (type) {
            case Fx.INTRO -> {
                introAt = ticks;
                introLen = arg;
                flashAt = landedAt = -1;
                falling = false;
                introYaw = mc.player != null ? mc.player.getYRot() : 0;
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F));
            }
            case Fx.WAND -> {
                introAt = -1;
                flashAt = ticks;
                falling = true;
                if (mc.player != null) mc.player.swing(InteractionHand.MAIN_HAND);
            }
            case Fx.LANDED -> {
                falling = false;
                landedAt = ticks;
                banner("¡LOGRO DESBLOQUEADO!", "Conseguiste la Vara de Transformación", new ItemStack(ModRegistry.WAND.get()));
            }
            case Fx.COLLECTION -> {
                completeAt = ticks;
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F));
            }
            case Fx.EXTRAS -> {
                extrasAt = ticks;
                completeAt = -1;
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8F));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.END_PORTAL_SPAWN, 1.4F, 0.4F));
            }
            case Fx.SURPRISE -> {
                WaifuKind k = WaifuKind.byId(arg);
                banner(k.legend() ? "☀ ¡WAIFU LEYENDA! ☀" : "✦ ¡WAIFU EXTRA! ✦", k.display + " (" + k.role + ") despertó", new ItemStack(ModRegistry.EGGS.get(k).get()));
            }
            case Fx.ASCEND -> Ascend.start(arg);
            case Fx.GODDESS -> banner("☀ ¡DIOSA DESBLOQUEADA! ☀", "Inari, la Diosa Zorro Blanca, te acompaña", new ItemStack(ModRegistry.EGGS.get(WaifuKind.DIOSA).get()));
            case Fx.SLEEP_ON -> sleepGirl = arg;
            case Fx.SLEEP_OFF -> sleepGirl = -1;
            default -> { }
        }
    }

    private static void banner(String title, String sub, ItemStack icon) {
        bannerAt = ticks;
        bannerTitle = title;
        bannerSub = sub;
        bannerIcon = icon;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        ticks++;
        Ascend.tick();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            introAt = -1;
            sleepGirl = -1;
            falling = false;
            return;
        }
        if (falling && (mc.player.onGround() || mc.player.isInWater()) && flashAt >= 0 && ticks - flashAt > 10) falling = false;
        if (sleepGirl >= 0 && !mc.player.isSleeping() && mc.player.tickCount % 20 == 0) sleepGirl = -1;
    }

    // ================================================================== cámara
    @SubscribeEvent
    public static void camera(ViewportEvent.ComputeCameraAngles e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float t = ticks + (float) e.getPartialTick();
        if (introAt >= 0) {                                    // la escena: mirada al frente, flotando
            e.setYaw(introYaw);
            e.setPitch(-6.0F + Mth.sin(t * 0.05F) * 1.5F);
            e.setRoll(Mth.sin(t * 0.03F) * 1.2F);
        }
        if (sleepGirl >= 0 && mc.player.isSleeping() && mc.level != null) {
            Entity g = mc.level.getEntity(sleepGirl);
            if (g instanceof WaifuEntity w) {                   // la miras desde sus piernas
                Vec3 cam = e.getCamera().getPosition();
                Vec3 face = w.getPosition((float) e.getPartialTick()).add(0, 0.95 * w.kind.scale, 0);
                Vec3 d = face.subtract(cam);
                double h = Math.sqrt(d.x * d.x + d.z * d.z);
                e.setYaw((float) (Mth.atan2(-d.x, d.z) * Mth.RAD_TO_DEG) + Mth.sin(t * 0.03F) * 2.0F);
                e.setPitch((float) (-Mth.atan2(d.y, h) * Mth.RAD_TO_DEG) + Mth.sin(t * 0.05F) * 1.0F);
                e.setRoll(-8.0F);
            }
        }
        if (landedAt >= 0 && ticks - landedAt < 20) {           // ¡temblor del aterrizaje!
            float k = 1.0F - (ticks - landedAt) / 20.0F;
            e.setPitch(e.getPitch() + Mth.sin(t * 2.3F) * 3.0F * k);
            e.setYaw(e.getYaw() + Mth.cos(t * 2.9F) * 2.0F * k);
            e.setRoll(e.getRoll() + Mth.sin(t * 3.7F) * 4.0F * k);
        }
    }

    // ================================================================== pantalla
    @SubscribeEvent
    public static void hud(RenderGuiEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        GuiGraphics g = e.getGuiGraphics();
        float t = ticks + e.getPartialTick();
        int W = g.guiWidth(), H = g.guiHeight();
        if (sleepGirl >= 0 && mc.player.isSleeping()) sleep(mc, g, W, H, t);
        if (Ascend.active) Ascend.hud(mc, g, W, H, e.getPartialTick());
        if (introAt >= 0) intro(mc, g, W, H, t - introAt);
        if (falling) wind(g, W, H, t);
        if (flashAt >= 0 && t - flashAt < 25) {
            int a = (int) (255 * (1.0F - (t - flashAt) / 25.0F));
            g.fill(0, 0, W, H, (Mth.clamp(a, 0, 255) << 24) | 0xFFFFFF);
        }
        if (landedAt >= 0 && t - landedAt < 8) g.fill(0, 0, W, H, ((int) (200 * (1 - (t - landedAt) / 8.0F)) << 24) | 0xFFF0F8);
        if (completeAt >= 0) {
            if (t - completeAt > 300) completeAt = -1;
            else complete(mc, g, W, H, t - completeAt);
        }
        if (extrasAt >= 0) {
            if (t - extrasAt > 320) extrasAt = -1;
            else extras(mc, g, W, H, t - extrasAt);
        }
        if (bannerAt >= 0) {
            if (t - bannerAt > 120) bannerAt = -1;
            else banner(mc, g, W, t - bannerAt);
        }
    }

    private static void intro(Minecraft mc, GuiGraphics g, int W, int H, float t) {
        Font f = mc.font;
        // cielo de ensueño
        int a0 = t < 15 ? 255 - (int) (t / 15 * 90) : 165;
        g.fillGradient(0, 0, W, H, (a0 << 24) | 0x1A0C2E, (a0 << 24) | 0x3A1446);
        for (int i = 0; i < 40; i++) {                       // estrellitas
            float x = (i * 97 % W), y = ((i * 53 + t * (0.2F + i % 3 * 0.15F)) % H);
            int c = (i % 3 == 0 ? 0xFFFFC0E0 : 0xFFFFFFFF);
            if ((i + (int) (t / 6)) % 4 != 0) g.fill((int) x, H - (int) y, (int) x + 1, H - (int) y + 1, c);
        }
        // "???": un ente de luz aparece frente a ti
        float grow = Mth.clamp((t - 6) / 24.0F, 0, 1);
        int cx = W / 2, base = (int) (H * 0.62F + Mth.sin(t * 0.08F) * 4);
        if (grow > 0) light(g, cx, base, grow, t);
        if (grow >= 1) g.drawCenteredString(f, "§e§l???", cx, base - 118, 0xFFFFFFFF);
        // la vara brilla y viene hacia ti
        if (t > 80 && t < 140) {
            float k = Mth.clamp((t - 112) / 26.0F, 0, 1);
            float x = Mth.lerp(k, cx + 34, cx - 8), y = Mth.lerp(k, base - 70, H - 30);
            float s = Mth.lerp(k, 3.2F, 1.4F) * (1 + Mth.sin(t * 0.3F) * 0.05F);
            PoseStack ps = g.pose();
            ps.pushPose();
            ps.translate(x, y, 200);
            ps.scale(s, s, 1);
            g.renderItem(new ItemStack(ModRegistry.WAND.get()), -8, -8);
            ps.popPose();
            for (int i = 0; i < 6; i++) {
                float an = t * 0.12F + i * 1.047F;
                g.drawString(mc.font, "✦", (int) (x + Mth.cos(an) * 22 * s / 3) - 3, (int) (y + Mth.sin(an) * 22 * s / 3) - 4, i % 2 == 0 ? 0xFFFFD0F0 : 0xFFFFF0A0, false);
            }
        }
        // palabras que aparecen
        int shown = 0;
        for (int i = LINES.length - 1; i >= 0 && shown < 2; i--) {
            int at = Integer.parseInt(LINES[i][0]);
            if (t < at) continue;
            String full = "§e§l???§r§7: " + LINES[i][1];
            int n = Math.min(full.length(), (int) ((t - at) * 1.6F));
            String txt = full.substring(0, n);
            int y = (int) (H * 0.80F) + (shown == 0 ? 0 : -14);
            int alpha = shown == 0 ? 255 : 120;
            g.drawCenteredString(f, txt, cx, y, (alpha << 24) | 0xFFFFFF);
            shown++;
        }
        g.drawCenteredString(f, "§8Totally Waifu?", cx, 8, 0xFFFFFFFF);
        // destello final
        if (t > introLen - 15) {
            int a = (int) (255 * Mth.clamp((t - (introLen - 15)) / 15.0F, 0, 1));
            g.fill(0, 0, W, H, (a << 24) | 0xFFFFFF);
        }
    }

    /** El ente de luz: halo que gira, rayos, una silueta brillante y destellos. */
    static void light(GuiGraphics g, int cx, int base, float grow, float t) {
        PoseStack ps = g.pose();
        float pulse = 1 + Mth.sin(t * 0.12F) * 0.06F;
        int cy = base - 55;
        // resplandor (círculos suaves)
        for (int r = 70; r > 0; r -= 5) {
            float rr = r * grow * pulse;
            int a = (int) (10 + (70 - r) * 0.9F);
            int col = r > 40 ? 0xFFE6A0 : r > 20 ? 0xFFF4D0 : 0xFFFFFF;
            for (int dy = -(int) rr; dy <= rr; dy += 2) {
                int hw = (int) Math.sqrt(rr * rr - dy * dy);
                g.fill(cx - hw, cy + dy, cx + hw, cy + dy + 2, (Math.min(255, a / 3) << 24) | col);
            }
        }
        // rayos que giran
        ps.pushPose();
        ps.translate(cx, cy, 0);
        for (int i = 0; i < 12; i++) {
            ps.pushPose();
            ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(t * 0.8F + i * 30));
            int len = (int) ((80 + (i % 3) * 18) * grow * pulse);
            g.fillGradient(-1, 8, 1, len, 0x70FFF4C0, 0x00FFF4C0);
            ps.popPose();
        }
        ps.popPose();
        // silueta de luz (cabeza, cuerpo con vestido, brazo que te ofrece la vara)
        int s = (int) (grow * 100);
        if (s <= 0) return;
        float k = grow;
        int head = (int) (9 * k);
        int hy = cy - (int) (34 * k);
        for (int dy = -head; dy <= head; dy++) {
            int hw = (int) Math.sqrt(head * head - dy * dy);
            g.fill(cx - hw, hy + dy, cx + hw, hy + dy + 1, 0xF0FFFFFF);
        }
        g.fill(cx - (int) (11 * k), hy - head - (int) (4 * k), cx + (int) (11 * k), hy - head - (int) (2 * k), 0xC0FFE680);   // aureola
        for (int dy = 0; dy < (int) (70 * k); dy++) {                                                                     // vestido
            float q = dy / (70 * k);
            int hw = (int) ((5 + q * q * 20) * k);
            int a = (int) (240 - q * 120);
            g.fill(cx - hw, hy + head + 2 + dy, cx + hw, hy + head + 3 + dy, (a << 24) | (q > 0.6F ? 0xFFF0C8 : 0xFFFFFF));
        }
        int sy = hy + head + (int) (8 * k);
        g.fill(cx + (int) (5 * k), sy, cx + (int) (30 * k), sy + (int) (4 * k), 0xE0FFFFFF);                                // brazo extendido
        g.fill(cx - (int) (14 * k), sy, cx - (int) (5 * k), sy + (int) (22 * k), 0xC0FFFFFF);                               // brazo
        // destellos alrededor
        for (int i = 0; i < 10; i++) {
            float an = t * 0.05F + i * 0.628F;
            int rad = (int) ((48 + (i % 3) * 10) * k);
            int x = cx + (int) (Mth.cos(an) * rad), y = cy + (int) (Mth.sin(an) * rad * 0.8F);
            g.drawString(Minecraft.getInstance().font, "✦", x - 3, y - 4, (((int) (200 * k)) << 24) | (i % 2 == 0 ? 0xFFF0A0 : 0xFFFFFF), false);
        }
    }

    private static void wind(GuiGraphics g, int W, int H, float t) {
        for (int i = 0; i < 26; i++) {
            int x = (i * 151 + (int) (i * t * 0.7F)) % W;
            int len = 18 + (i * 37) % 30;
            int y = H - (int) ((t * (14 + i % 5 * 3) + i * 89) % (H + len));
            g.fill(x, y, x + 1, y + len, 0x60FFFFFF);
        }
        g.drawCenteredString(Minecraft.getInstance().font, "§d♥ ¡Allá vamos! ♥", W / 2, H / 4, 0xFFFFFFFF);
    }

    private static void banner(Minecraft mc, GuiGraphics g, int W, float t) {
        float in = Mth.clamp(t / 10.0F, 0, 1), out = Mth.clamp((t - 105) / 15.0F, 0, 1);
        int y = (int) Mth.lerp(in, -50, 14) - (int) (out * 70);
        int w = 230, x = W / 2 - w / 2, h = 42;
        int glow = (int) (120 + 60 * Mth.sin(t * 0.25F));
        g.fill(x - 3, y - 3, x + w + 3, y + h + 3, (glow << 24) | 0xFFD24A);
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFFFFE9A0);
        g.fillGradient(x, y, x + w, y + h, 0xF02A1240, 0xF0180A28);
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(x + 22, y + h / 2.0F, 100);
        float s = 1.8F + Mth.sin(t * 0.3F) * 0.1F;
        ps.scale(s, s, 1);
        g.renderItem(bannerIcon, -8, -8);
        ps.popPose();
        g.drawString(mc.font, "§6§l" + bannerTitle, x + 44, y + 9, 0xFFFFFFFF, true);
        g.drawString(mc.font, "§f" + bannerSub, x + 44, y + 24, 0xFFFFFFFF, true);
        for (int i = 0; i < 8; i++) {
            float an = t * 0.08F + i * 0.785F;
            g.drawString(mc.font, "✦", (int) (x + w / 2.0F + Mth.cos(an) * (w / 2.0F + 8)) - 3, (int) (y + h / 2.0F + Mth.sin(an) * (h / 2.0F + 6)) - 4,
                    i % 2 == 0 ? 0xFFFFE070 : 0xFFFF9AD8, false);
        }
    }

    private static final String[] HINTS = {"bosques fríos", "cuevas frondosas", "puestos de saqueadores", "pantanos", "océanos", "playas",
            "junglas", "tierras heladas", "lava del Nether", "ciudades del End", "noches sin dormir", "monumentos marinos"};

    /** Fila de recompensas que van apareciendo una a una. */
    static void rewards(GuiGraphics g, java.util.List<ItemStack> l, int cx, int y, float t, float from, int ia) {
        int n = l.size(), step = 26, x0 = cx - (n * step) / 2;
        for (int i = 0; i < n; i++) {
            float k = Mth.clamp((t - from - i * 6) / 8.0F, 0, 1);
            if (k <= 0) continue;
            int x = x0 + i * step;
            g.fill(x - 1, y - 1, x + 23, y + 23, ia | 0xFFE070);
            g.fill(x, y, x + 22, y + 22, ia | 0x2A1240);
            PoseStack ps = g.pose();
            ps.pushPose();
            ps.translate(x + 11, y + 11, 150);
            float s = k * (1 + (1 - k) * 0.6F);
            ps.scale(s, s, 1);
            g.renderItem(l.get(i), -8, -8);
            g.renderItemDecorations(Minecraft.getInstance().font, l.get(i), -8, -8);
            ps.popPose();
        }
    }

    private static void complete(Minecraft mc, GuiGraphics g, int W, int H, float t) {
        Font f = mc.font;
        float in = Mth.clamp(t / 20.0F, 0, 1), out = Mth.clamp((t - 270) / 30.0F, 0, 1);
        float a = in * (1 - out);
        g.fillGradient(0, 0, W, H, ((int) (210 * a) << 24) | 0x3A1030, ((int) (225 * a) << 24) | 0x6A3A10);
        for (int i = 0; i < 30; i++) {                       // corazones que suben
            int x = (i * 113) % W, y = H - (int) ((t * (1.2F + i % 4 * 0.4F) + i * 41) % (H + 20));
            g.drawString(f, i % 3 == 0 ? "✦" : "♥", x, y, (Math.max(4, (int) (255 * a)) << 24) | (i % 2 == 0 ? 0xFF7AC8 : 0xFFD24A), false);
        }
        if (a <= 0.02F) return;
        int ia = Math.max(4, (int) (255 * a)) << 24;
        PoseStack ps = g.pose();
        ps.pushPose();
        float s = 2.6F + Mth.sin(t * 0.15F) * 0.12F;
        ps.translate(W / 2.0F, H * 0.13F, 0);
        ps.scale(s, s, 1);
        g.drawCenteredString(f, "§6§l♥ ¡COLECCIÓN COMPLETA! ♥", 0, -4, ia | 0xFFFFFF);
        ps.popPose();
        g.drawCenteredString(f, "§eDespertaste a todas tus waifus. ¡Eres increíble!", W / 2, (int) (H * 0.13F) + 18, ia | 0xFFFFFF);
        if (t > 30) {
            g.drawCenteredString(f, "§6§lRecompensas", W / 2, (int) (H * 0.28F), ia | 0xFFFFFF);
            rewards(g, com.totallywaifu.data.Rewards.collection(), W / 2, (int) (H * 0.28F) + 12, t, 36, ia);
            g.drawCenteredString(f, "§a+" + com.totallywaifu.data.Rewards.COLLECTION_LEVELS + " niveles  §d" + com.totallywaifu.data.Rewards.PERK_COLLECTION,
                    W / 2, (int) (H * 0.28F) + 40, ia | 0xFFFFFF);
        }
        if (t > 110) {
            int y0 = (int) (H * 0.52F);
            g.drawCenteredString(f, "§d§l✦ ¡Se desbloquearon 12 waifus EXTRA! ✦", W / 2, y0, ia | 0xFFFFFF);
            g.drawCenteredString(f, "§7Búscalas por el mundo y transfórmalas con la vara", W / 2, y0 + 11, ia | 0xFFFFFF);
            int bw = Math.min(70, (W - 40) / 6 - 4), bh = 30, gap = 4, total = 6 * bw + 5 * gap, x0 = W / 2 - total / 2;
            for (int i = 0; i < 12; i++) {
                if (t < 120 + i * 5) continue;
                int x = x0 + (i % 6) * (bw + gap), y = y0 + 26 + (i / 6) * (bh + gap);
                g.fill(x - 1, y - 1, x + bw + 1, y + bh + 1, ia | 0xB57AFF);
                g.fillGradient(x, y, x + bw, y + bh, ia | 0x2A1240, ia | 0x120820);
                g.drawCenteredString(f, "§d?", x + bw / 2, y + 4, ia | 0xFFFFFF);
                for (FormattedCharSequence line : f.split(net.minecraft.network.chat.Component.literal("§7" + HINTS[i]), bw - 2)) {
                    g.drawCenteredString(f, line, x + bw / 2, y + 15, ia | 0xFFFFFF);
                    break;
                }
            }
        }
    }

    /** ✦ ¡Extras completos!: cielo cósmico, anillo de estrellas y tesoros legendarios. */
    private static void extras(Minecraft mc, GuiGraphics g, int W, int H, float t) {
        Font f = mc.font;
        float in = Mth.clamp(t / 25.0F, 0, 1), out = Mth.clamp((t - 290) / 30.0F, 0, 1);
        float a = in * (1 - out);
        int A = (int) (230 * a);
        g.fillGradient(0, 0, W, H, (A << 24) | 0x0A0626, (A << 24) | 0x2A0A3A);
        for (int i = 0; i < 70; i++) {                       // estrellas que titilan
            int x = (i * 137) % W, y = (i * 89) % H;
            float tw = 0.5F + 0.5F * Mth.sin(t * 0.2F + i);
            g.fill(x, y, x + 1 + (i % 7 == 0 ? 1 : 0), y + 1 + (i % 7 == 0 ? 1 : 0), ((int) (255 * a * tw) << 24) | 0xFFFFFF);
        }
        if (a <= 0.02F) return;
        int ia = Math.max(4, (int) (255 * a)) << 24;
        int cx = W / 2, cy = (int) (H * 0.30F);
        PoseStack ps = g.pose();
        // anillo de 12 estrellas (una por cada extra) que gira
        for (int i = 0; i < 12; i++) {
            float an = t * 0.03F + i * Mth.TWO_PI / 12;
            float k = Mth.clamp((t - 10 - i * 4) / 10.0F, 0, 1);
            int r = (int) (70 * k);
            int x = cx + (int) (Mth.cos(an) * r), y = cy + (int) (Mth.sin(an) * r * 0.45F);
            int col = Mth.hsvToRgb((i / 12.0F + t * 0.005F) % 1.0F, 0.5F, 1.0F) & 0xFFFFFF;
            ps.pushPose();
            ps.translate(x, y, 0);
            ps.scale(1.6F, 1.6F, 1);
            g.drawCenteredString(f, "✦", 0, -4, ia | col);
            ps.popPose();
        }
        // título arcoíris
        String title = "✦ ¡EXTRAS COMPLETOS! ✦";
        float s = 2.8F + Mth.sin(t * 0.12F) * 0.15F;
        ps.pushPose();
        ps.translate(cx, cy, 0);
        ps.scale(s, s, 1);
        int tw = f.width(title), x = -tw / 2;
        for (int i = 0; i < title.length(); i++) {
            String ch = String.valueOf(title.charAt(i));
            int col = Mth.hsvToRgb((i / (float) title.length() + t * 0.01F) % 1.0F, 0.6F, 1.0F) & 0xFFFFFF;
            g.drawString(f, "§l" + ch, x, -4 + (int) (Mth.sin(t * 0.2F + i * 0.5F) * 1.5F), ia | col, true);
            x += f.width("§l" + ch);
        }
        ps.popPose();
        g.drawCenteredString(f, "§dDespertaste a las 12 waifus extra. ¡Eres la leyenda de Totally Waifu?!", cx, cy + 26, ia | 0xFFFFFF);
        if (t > 50) {
            int y = (int) (H * 0.55F);
            g.drawCenteredString(f, "§b§lTesoros legendarios", cx, y, ia | 0xFFFFFF);
            rewards(g, com.totallywaifu.data.Rewards.extras(), cx, y + 12, t, 56, ia);
            g.drawCenteredString(f, "§a+" + com.totallywaifu.data.Rewards.EXTRAS_LEVELS + " niveles", cx, y + 40, ia | 0xFFFFFF);
            for (FormattedCharSequence line : f.split(net.minecraft.network.chat.Component.literal("§b" + com.totallywaifu.data.Rewards.PERK_EXTRAS), Math.min(W - 40, 360))) {
                g.drawCenteredString(f, line, cx, y + 54, ia | 0xFFFFFF);
                y += 10;
            }
        }
    }

    private static void sleep(Minecraft mc, GuiGraphics g, int W, int H, float t) {
        int timer = mc.player.getSleepTimer();
        float p = Mth.clamp((timer + mc.getFrameTime()) / 90.0F, 0, 1);
        float close = (float) Math.pow(p, 1.6);
        // la miras... y tus ojos se van cerrando
        int lid = (int) (H / 2.0F * close);
        int edge = 26;
        g.fill(0, 0, W, lid, 0xFF000000);
        g.fillGradient(0, lid, W, lid + edge, 0xFF000000, 0x00000000);
        g.fill(0, H - lid, W, H, 0xFF000000);
        g.fillGradient(0, H - lid - edge, W, H - lid, 0x00000000, 0xFF000000);
        g.fillGradient(0, 0, W, H, 0x30200830, 0x40100418);
        if (timer < 70) {
            int a = (int) (255 * Mth.clamp(1 - (timer - 40) / 30.0F, 0, 1));
            if (a > 4) g.drawCenteredString(mc.font, "§9Yume§f: Shh... duerme tranquilo. Yo te cuido toda la noche ♥", W / 2, (int) (H * 0.78F), (a << 24) | 0xFFFFFF);
        }
        if (timer > 45) {
            for (int i = 0; i < 3; i++) {
                float k = ((t * 0.02F) + i / 3.0F) % 1.0F;
                int x = W / 2 + 40 + (int) (k * 30) + i * 6, y = (int) (H * 0.5F - k * 50);
                int a = (int) (255 * Mth.sin(k * Mth.PI));
                g.drawString(mc.font, i == 2 ? "Z" : "z", x, y, (Math.max(4, a) << 24) | 0xC8D8FF, false);
            }
        }
    }
}
