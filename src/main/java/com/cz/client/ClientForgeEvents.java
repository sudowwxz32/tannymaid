package com.cz.client;

import com.cz.ChakraZorro;
import com.cz.data.Phase;
import com.cz.data.ZorroState;
import com.cz.network.ActionPacket;
import com.cz.network.ClientStates;
import com.cz.network.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = ChakraZorro.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        ZorroState mine = ClientStates.get(mc.player.getId());
        while (ClientModEvents.MENU.consumeClick()) {
            if (!mine.awakened) notAwake(mc); else if (mc.screen == null) mc.setScreen(new PhaseScreen());
        }
        while (ClientModEvents.TRANSFORM.consumeClick()) {
            if (!mine.awakened) notAwake(mc); else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.TRANSFORM, 0));
        }
        while (ClientModEvents.CHARGE.consumeClick()) {
            if (!mine.awakened) notAwake(mc); else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.CHARGE, 0));
        }
        if (!mc.isPaused()) {
            ClientStates.clientTicks++;
            particles(mc, mc.level);
        }
    }

    private static void notAwake(Minecraft mc) {
        mc.player.displayClientMessage(Component.literal("§cPrimero usa §e/zorro despertar"), true);
    }

    /** Chakra que sube del manto, vapor del manto hirviente y chakra que se absorbe al transformarse. */
    private static void particles(Minecraft mc, ClientLevel level) {
        RandomSource rnd = level.random;
        for (Player p : level.players()) {
            ZorroState s = ClientStates.get(p.getId());
            if (!s.awakened || p.isInvisible() || p.distanceToSqr(mc.player) > 64 * 64) continue;
            Phase v = s.visible();
            if (v == Phase.NONE) continue;
            boolean self = p == mc.player && mc.options.getCameraType().isFirstPerson();
            Vector3f c = new Vector3f(v.r(), v.g(), v.b());
            DustColorTransitionOptions dust = new DustColorTransitionOptions(c, new Vector3f(0.3F, 0.0F, 0.0F), 1.0F + v.ordinal() * 0.15F);
            double h = p.getBbHeight();
            for (int i = 0; i < (self ? 1 : 2 + v.ordinal() / 2); i++) {
                double a = rnd.nextDouble() * Math.PI * 2, r = 0.4 + rnd.nextDouble() * 0.3;
                level.addParticle(dust, p.getX() + Math.cos(a) * r, p.getY() + rnd.nextDouble() * h, p.getZ() + Math.sin(a) * r, 0, 0.08, 0);
            }
            if (v.burns && rnd.nextInt(self ? 4 : 2) == 0) {
                level.addParticle(ParticleTypes.CLOUD, p.getX() + (rnd.nextDouble() - 0.5) * 0.8, p.getY() + rnd.nextDouble() * h,
                        p.getZ() + (rnd.nextDouble() - 0.5) * 0.8, 0, 0.05, 0);          // vapor del calor
                level.addParticle(ParticleTypes.BUBBLE_POP, p.getX() + (rnd.nextDouble() - 0.5) * 0.9, p.getY() + rnd.nextDouble() * h,
                        p.getZ() + (rnd.nextDouble() - 0.5) * 0.9, 0, 0.02, 0);
            }
            if (v.beast && rnd.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.SMALL_FLAME, p.getX() + (rnd.nextDouble() - 0.5), p.getY() + 0.1, p.getZ() + (rnd.nextDouble() - 0.5), 0, 0.05, 0);
            }
            if (s.powering > 0 && !self) {
                for (int i = 0; i < 3; i++) {
                    double a = rnd.nextDouble() * Math.PI * 2, rr = 3.0 + rnd.nextDouble() * 2;
                    double sx = p.getX() + Math.cos(a) * rr, sy = p.getY() + rnd.nextDouble() * 2.5, sz = p.getZ() + Math.sin(a) * rr;
                    level.addParticle(dust, sx, sy, sz, (p.getX() - sx) * 0.1, (p.getY() + 1 - sy) * 0.1, (p.getZ() - sz) * 0.1);
                }
            }
        }
    }

    /** La cámara tiembla con la transformación (más en las fases altas) y al terminar. */
    @SubscribeEvent
    public static void camera(ViewportEvent.ComputeCameraAngles e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        float amount = 0;
        for (Player p : mc.level.players()) {
            double d = p.distanceTo(mc.player);
            if (d > 32) continue;
            ZorroState s = ClientStates.get(p.getId());
            if (!s.awakened) continue;
            float near = (float) (1.0 - d / 32.0);
            if (s.powering > 0) amount = Math.max(amount, near * (0.3F + s.progress() * 1.4F) * (0.6F + s.target * 0.3F));
            long since = ClientStates.sinceDone(p.getId());
            if (since < 20) amount = Math.max(amount, near * (20 - since) / 20.0F * (1.5F + s.phase * 0.5F));
        }
        if (amount > 0.01F) {
            double t = mc.level.getGameTime() + e.getPartialTick();
            e.setYaw(e.getYaw() + (float) Math.sin(t * 2.3) * amount * 0.6F);
            e.setPitch(e.getPitch() + (float) Math.cos(t * 2.9) * amount * 0.6F);
            e.setRoll(e.getRoll() + (float) Math.sin(t * 3.7) * amount * 0.8F);
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientStates.clear();
    }
}
