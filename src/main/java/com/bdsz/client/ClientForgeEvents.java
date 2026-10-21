package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.Form;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ActionPacket;
import com.bdsz.network.ClientStates;
import com.bdsz.network.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = BlockDragonSuperZ.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        SaiyanState mine = ClientStates.get(mc.player.getId());
        while (ClientModEvents.MENU.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc);
            else if (mc.screen == null) mc.setScreen(new PowerScreen());
        }
        while (ClientModEvents.TRANSFORM.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc);
            else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.TRANSFORM, 0, 0));
        }
        while (ClientModEvents.AURA.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc);
            else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.AURA, 0, 0));
        }
        if (!mc.isPaused()) auras(mc, mc.level);
    }

    private static void notSaiyan(Minecraft mc) {
        mc.player.displayClientMessage(Component.literal("§cPrimero usa §e/raza sayajin"), true);
    }

    /** Aura de partículas alrededor de cada jugador con el aura activada. */
    private static void auras(Minecraft mc, ClientLevel level) {
        RandomSource rnd = level.random;
        for (Player p : level.players()) {
            SaiyanState s = ClientStates.get(p.getId());
            if (!s.saiyan || !s.aura || p.isInvisible() || p.distanceToSqr(mc.player) > 64 * 64) continue;
            Form f = s.activeForm();
            int k = s.activeKaioken();
            boolean self = p == mc.player && mc.options.getCameraType().isFirstPerson();
            float power = f == Form.BASE ? 0.6F : 1.0F + f.ordinal() * 0.15F + k / 100.0F;
            int count = (int) ((self ? 2 : 5) * power);
            Vector3f color = f == Form.BASE ? (k > 0 ? new Vector3f(1.0F, 0.15F, 0.1F) : new Vector3f(0.85F, 0.95F, 1.0F))
                    : new Vector3f(f.r(), f.g(), f.b());
            DustParticleOptions dust = new DustParticleOptions(color, 1.1F + power * 0.25F);
            DustParticleOptions red = new DustParticleOptions(new Vector3f(1.0F, 0.12F, 0.08F), 1.3F);
            double maxH = self ? 1.0 : p.getBbHeight() + 0.4;
            for (int i = 0; i < count; i++) {
                double a = rnd.nextDouble() * Math.PI * 2;
                double rad = 0.45 + rnd.nextDouble() * 0.35 * power;
                double x = p.getX() + Math.cos(a) * rad;
                double z = p.getZ() + Math.sin(a) * rad;
                double y = p.getY() + rnd.nextDouble() * maxH;
                double up = 0.06 + rnd.nextDouble() * 0.12 * power;
                level.addParticle(dust, x, y, z, 0, up, 0);
                if (k > 0 && f != Form.BASE && rnd.nextInt(2) == 0) level.addParticle(red, x, y, z, 0, up * 1.3, 0);
            }
            if ((f == Form.SSJ2 || f == Form.BLUE || f == Form.SSJ3) && rnd.nextInt(self ? 8 : 3) == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, p.getX() + (rnd.nextDouble() - 0.5) * 1.2,
                        p.getY() + rnd.nextDouble() * maxH, p.getZ() + (rnd.nextDouble() - 0.5) * 1.2, 0, 0.1, 0);
            }
            if (f == Form.ULTRA && rnd.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.END_ROD, p.getX() + (rnd.nextDouble() - 0.5) * 1.4,
                        p.getY() + rnd.nextDouble() * maxH, p.getZ() + (rnd.nextDouble() - 0.5) * 1.4, 0, 0.03, 0);
            }
            if (f == Form.GOD && rnd.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.FLAME, p.getX() + (rnd.nextDouble() - 0.5), p.getY() + 0.1,
                        p.getZ() + (rnd.nextDouble() - 0.5), 0, 0.12, 0);
            }
            if (p.tickCount % 40 == 0) {
                level.playLocalSound(p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS,
                        0.35F + power * 0.1F, 0.6F + f.ordinal() * 0.08F, false);
            }
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientStates.clear();
    }
}
