package com.totallywaifu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/**
 * La ascensión (leyendas completas): te vuelves intangible, te elevas al cielo por encima de las nubes,
 * vuelves a ver al ente de luz "???"... y te presenta a Inari, la Diosa Zorro Blanca.
 */
@Mod.EventBusSubscriber(modid = TotallyWaifu.MODID, value = Dist.CLIENT)
public final class Ascend {
    private Ascend() {}

    static final int ACTOR_ID = -771177;
    static boolean active;
    static int t, len = 640;
    static ArmorStand cam;
    static WaifuEntity actor;
    static Vec3 base, camPos, camPrev;
    static double topY;
    static float yaw0;

    private static final Object[][] LINES = {
            {262, "...Vaya. Así que de verdad llegaste hasta aquí."},
            {312, "§eNo pensé que lograrías encontrarlas a todas."},
            {368, "Mesas, criaturas, extras y leyendas... despertaste a cada una con cariño."},
            {440, "Por eso te confío a alguien muy especial: la guardiana de los cielos."},
            {500, "§e§lInari§r§f, la Diosa Zorro Blanca. Ella nunca dejará que nadie te lastime."},
            {566, "Cuídala... y ella te cuidará a ti, por siempre ♥"}};

    static void start(int length) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        stop();
        active = true;
        t = 0;
        len = length;
        base = mc.player.getEyePosition();
        camPos = camPrev = base;
        topY = Math.min(mc.level.getMaxBuildHeight() - 10, Math.max(base.y + 70, 218));
        yaw0 = mc.player.getYRot();
        cam = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
        cam.setInvisible(true);
        place(yaw0, 0);
        cam.setOldPosAndRot();
        mc.setCameraEntity(cam);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BEACON_ACTIVATE, 1.4F, 0.8F));
        mc.getToasts().clear();
    }

    static void stop() {
        Minecraft mc = Minecraft.getInstance();
        removeActor();
        if (cam != null && mc.player != null) mc.setCameraEntity(mc.player);
        cam = null;
        active = false;
    }

    private static void removeActor() {
        Minecraft mc = Minecraft.getInstance();
        if (actor != null && mc.level != null) {
            mc.level.removeEntity(actor.getId(), Entity.RemovalReason.DISCARDED);
            actor.discard();
        }
        actor = null;
    }

    private static void place(float yaw, float pitch) {
        cam.setOldPosAndRot();
        cam.setPos(camPos.x, camPos.y - cam.getEyeHeight(), camPos.z);
        cam.setYRot(yaw);
        cam.setXRot(pitch);
        cam.setYHeadRot(yaw);
    }

    private static float ease(float k) {
        k = Mth.clamp(k, 0, 1);
        return k * k * (3 - 2 * k);
    }

    static void tick() {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || cam == null) {
            stop();
            return;
        }
        t++;
        var lv = mc.level;
        // ---- subir: intangible, atravesando el aire hasta más allá de las nubes
        float rise = ease((t - 30) / 200.0F);
        camPrev = camPos;
        camPos = new Vec3(base.x, base.y + (topY - base.y) * rise + Mth.sin(t * 0.04F) * 0.15F * rise, base.z);
        float yaw = yaw0 + rise * 35.0F + Mth.sin(t * 0.01F) * 2.0F;
        float pitch = t < 30 ? -t * 0.6F : t < 230 ? Mth.lerp((t - 30) / 200.0F, -18.0F, -4.0F) : t < 430 ? -4.0F : Mth.lerp(Mth.clamp((t - 430) / 40.0F, 0, 1), -4.0F, 8.0F) + Mth.sin(t * 0.03F) * 1.0F;
        if (t < 240 && t % 2 == 0) {                                   // luces que suben contigo
            for (int i = 0; i < 4; i++) {
                double a = Math.random() * Mth.TWO_PI, r = 1.5 + Math.random() * 3;
                lv.addParticle(i % 2 == 0 ? ParticleTypes.END_ROD : new DustParticleOptions(new Vector3f(1.0F, 0.85F, 0.4F), 1.4F),
                        camPos.x + Math.cos(a) * r, camPos.y - 3 + Math.random() * 2, camPos.z + Math.sin(a) * r, 0, 0.25, 0);
            }
        }
        // ---- Inari aparece desde la luz frente a ti
        Vec3 fwd = Vec3.directionFromRotation(0, yaw);
        if (t == 430 && actor == null) {
            actor = ModRegistry.TYPES.get(WaifuKind.DIOSA).get().create(lv);
            if (actor != null) {
                actor.actor = true;
                actor.setNoGravity(true);
                actor.noPhysics = true;
                actor.setId(ACTOR_ID);
                actor.setTransformTicks(30);
                lv.putNonPlayerEntity(ACTOR_ID, actor);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_RESONATE, 0.6F, 1.0F));
            }
        }
        if (actor != null) {
            boolean fox = t >= 520;
            if (t == 520) {
                actor.setFox(true, false);
                actor.setTransformTicks(30);
                actor.setNoGravity(true);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.FOX_SCREECH, 0.6F, 1.0F));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ILLUSIONER_CAST_SPELL, 0.7F, 1.0F));
            }
            float down = t < 470 ? (470 - t) / 40.0F * 3.0F : 0;              // baja flotando
            double dist = fox ? 8.0 : 3.4;
            Vec3 at = camPos.add(fwd.scale(dist)).add(0, (fox ? -2.0 : -1.45) + down + Mth.sin(t * 0.06F) * 0.12F, 0);
            actor.setOldPosAndRot();
            actor.setPos(at.x, at.y, at.z);
            float face = yaw + 180;
            actor.setYRot(face);
            actor.yBodyRot = actor.yBodyRotO = face;
            actor.yHeadRot = actor.yHeadRotO = face;
            if (t >= 470 && t < 500) actor.cheer(30);                     // reverencia y sonrisa
            if (t % 2 == 0) {
                lv.addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.85F, 0.4F), 1.6F), at.x + (Math.random() - 0.5) * 2, at.y + Math.random() * 2.5,
                        at.z + (Math.random() - 0.5) * 2, 0, 0.01, 0);
                lv.addParticle(ParticleTypes.CHERRY_LEAVES, at.x + (Math.random() - 0.5) * 6, at.y + 4, at.z + (Math.random() - 0.5) * 6, 0, -0.02, 0);
            }
        }
        place(yaw, pitch);
        if (t >= len) stop();
    }

    // ================================================================== pantalla
    static void hud(Minecraft mc, GuiGraphics g, int W, int H, float partial) {
        float tt = t + partial;
        Font f = mc.font;
        int cx = W / 2;
        // barras de cine
        int bar = (int) (H * 0.08F * Mth.clamp(tt / 30.0F, 0, 1));
        // 1) intangible: halo blanco que late en los bordes
        if (tt < 60) {
            float k = Mth.clamp(tt / 20.0F, 0, 1) * (1 - Mth.clamp((tt - 40) / 20.0F, 0, 1));
            int a = (int) (150 * k * (0.7F + 0.3F * Mth.sin(tt * 0.3F)));
            g.fillGradient(0, 0, W, H / 3, (a << 24) | 0xFFF6E0, 0x00FFFFFF);
            g.fillGradient(0, H * 2 / 3, W, H, 0x00FFFFFF, (a << 24) | 0xFFF6E0);
            g.drawCenteredString(f, "§f§o✦ Tu cuerpo se vuelve ligero... intangible ✦", cx, (int) (H * 0.7F), (Math.max(4, (int) (255 * k)) << 24) | 0xFFFFFF);
        }
        // 2) ¡leyendas completas! y sus recompensas, mientras subes
        if (tt > 40 && tt < 240) {
            float a = Mth.clamp((tt - 40) / 20.0F, 0, 1) * (1 - Mth.clamp((tt - 215) / 25.0F, 0, 1));
            int ia = Math.max(4, (int) (255 * a)) << 24;
            PoseStack ps = g.pose();
            ps.pushPose();
            float s = 2.6F + Mth.sin(tt * 0.12F) * 0.12F;
            ps.translate(cx, H * 0.2F, 0);
            ps.scale(s, s, 1);
            String title = "☀ ¡LEYENDAS COMPLETAS! ☀";
            int tw = f.width("§l" + title), x = -tw / 2;
            for (int i = 0; i < title.length(); i++) {
                String ch = String.valueOf(title.charAt(i));
                int col = Mth.hsvToRgb((0.1F + 0.06F * Mth.sin(tt * 0.08F + i * 0.4F)), 0.55F, 1.0F) & 0xFFFFFF;
                g.drawString(f, "§l" + ch, x, -4 + (int) (Mth.sin(tt * 0.2F + i * 0.5F) * 1.5F), ia | col, true);
                x += f.width("§l" + ch);
            }
            ps.popPose();
            g.drawCenteredString(f, "§eDespertaste a las 8 leyendas. Algo te llama desde el cielo...", cx, (int) (H * 0.2F) + 18, ia | 0xFFFFFF);
            if (tt > 80) {
                int y = (int) (H * 0.36F);
                g.drawCenteredString(f, "§6§lTesoros divinos", cx, y, ia | 0xFFFFFF);
                ClientFx.rewards(g, com.totallywaifu.data.Rewards.legends(), cx, y + 12, tt, 86, ia);
                g.drawCenteredString(f, "§a+" + com.totallywaifu.data.Rewards.LEGENDS_LEVELS + " niveles", cx, y + 38, ia | 0xFFFFFF);
                int yy = y + 50;
                for (FormattedCharSequence line : f.split(net.minecraft.network.chat.Component.literal("§6" + com.totallywaifu.data.Rewards.PERK_LEGENDS), Math.min(W - 40, 300))) {
                    g.drawCenteredString(f, line, cx, yy, ia | 0xFFFFFF);
                    yy += 10;
                }
            }
        }
        // 3) el cielo de ensueño y el ente de luz
        float dream = Mth.clamp((tt - 215) / 35.0F, 0, 1) * (1 - Mth.clamp((tt - 410) / 20.0F, 0, 1));
        if (dream > 0) {
            int A = (int) (200 * dream);
            g.fillGradient(0, 0, W, H, (A << 24) | 0x2A1446, (A << 24) | 0x6A4A20);
            for (int i = 0; i < 50; i++) {
                int x = (i * 97) % W, y = (int) ((i * 53 + tt * (0.2F + i % 3 * 0.15F)) % H);
                if ((i + (int) (tt / 6)) % 4 != 0) g.fill(x, H - y, x + 1, H - y + 1, ((int) (255 * dream) << 24) | (i % 3 == 0 ? 0xFFE0A0 : 0xFFFFFF));
            }
            float grow = Mth.clamp((tt - 235) / 24.0F, 0, 1) * dream;
            int baseY = (int) (H * 0.62F + Mth.sin(tt * 0.08F) * 4);
            if (grow > 0) ClientFx.light(g, cx, baseY, grow, tt);
            if (grow >= 0.95F) g.drawCenteredString(f, "§e§l???", cx, baseY - 118, 0xFFFFFFFF);
        }
        if (tt > 412 && tt < 440) {                                    // destello: la luz se vuelve ella
            float k = 1 - Math.abs(tt - 425) / 15.0F;
            g.fill(0, 0, W, H, ((int) (255 * Mth.clamp(k, 0, 1)) << 24) | 0xFFFCF0);
        }
        // 4) palabras del ente de luz
        int shown = 0;
        for (int i = LINES.length - 1; i >= 0 && shown < 2; i--) {
            int at = (int) LINES[i][0];
            if (tt < at) continue;
            String full = "§e§l???§r§f: " + LINES[i][1];
            int n = Math.min(full.length(), (int) ((tt - at) * 1.6F));
            int y = (int) (H * 0.82F) + (shown == 0 ? 0 : -14);
            int alpha = shown == 0 ? 255 : 130;
            if (tt > 430) g.fill(cx - 170, y - 3, cx + 170, y + 10, 0x60000000);
            for (FormattedCharSequence line : f.split(net.minecraft.network.chat.Component.literal(full.substring(0, n)), 340)) {
                g.drawCenteredString(f, line, cx, y, (alpha << 24) | 0xFFFFFF);
                break;
            }
            shown++;
        }
        if (bar > 0) {
            g.fill(0, 0, W, bar, 0xFF000000);
            g.fill(0, H - bar, W, H, 0xFF000000);
        }
        // 5) regreso: fundido blanco
        if (tt > len - 40) {
            int a = (int) (255 * Mth.clamp((tt - (len - 40)) / 30.0F, 0, 1));
            g.fill(0, 0, W, H, (a << 24) | 0xFFFFFF);
        }
    }

    // ================================================================== durante la escena: sin interfaz, sin mano, sin moverte
    @SubscribeEvent
    public static void gui(RenderGuiOverlayEvent.Pre e) {
        if (active) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent e) {
        if (active) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void player(RenderPlayerEvent.Pre e) {
        if (active && e.getEntity() == Minecraft.getInstance().player) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void input(MovementInputUpdateEvent e) {
        if (!active) return;
        var in = e.getInput();
        in.forwardImpulse = in.leftImpulse = 0;
        in.up = in.down = in.left = in.right = in.jumping = in.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void keys(InputEvent.InteractionKeyMappingTriggered e) {
        if (active) {
            e.setCanceled(true);
            e.setSwingHand(false);
        }
    }
}
