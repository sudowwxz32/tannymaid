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
    private static int blastDelay;

    private ClientForgeEvents() {}

    /** Con la raza saiyajin, los corazones normales se reemplazan por la barra de vida del panel. */
    @SubscribeEvent
    public static void hideHearts(net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && ClientStates.get(mc.player.getId()).saiyan
                && e.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            e.setCanceled(true);
        }
    }

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
        while (ClientModEvents.FLY.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc);
            else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.FLY, 0, 0));
        }
        // B: ráfaga de ki (mantener pulsado = disparo continuo)
        if (blastDelay > 0) blastDelay--;
        boolean b = false;
        while (ClientModEvents.BLAST.consumeClick()) b = true;
        if ((b || ClientModEvents.BLAST.isDown()) && mc.screen == null && blastDelay == 0) {
            if (!mine.saiyan) notSaiyan(mc);
            else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.BLAST, 0, 0));
            blastDelay = 8;
        }
        // ataques especiales: R Kamehameha, G Big Bang, Z Puño del Dragón
        while (ClientModEvents.KAMEHAMEHA.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc); else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.KAMEHAMEHA, 0, 0));
        }
        while (ClientModEvents.BIG_BANG.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc); else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.BIG_BANG, 0, 0));
        }
        while (ClientModEvents.DRAGON_FIST.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc); else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.DRAGON_FIST, 0, 0));
        }
        // O: Modo Ozaru (otra vez O para volver)
        while (ClientModEvents.OZARU.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc); else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.OZARU, 0, 0));
        }
        // H: pulsar para empezar a recargar ki, pulsar otra vez para detener
        while (ClientModEvents.CHARGE.consumeClick()) {
            if (!mine.saiyan) notSaiyan(mc);
            else Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.CHARGE_TOGGLE, 0, 0));
        }
        if (!mc.isPaused()) {
            ClientStates.clientTicks++;
            ClientFlight.tick(mc);
            auras(mc, mc.level);
        }
    }

    private static void notSaiyan(Minecraft mc) {
        mc.player.displayClientMessage(Component.literal("§cPrimero usa §e/raza saiyajin"), true);
    }

    /** Aura de partículas: fuego de ki en las fases dramáticas, brillo sereno en Ultra Instinto. */
    private static void auras(Minecraft mc, ClientLevel level) {
        RandomSource rnd = level.random;
        for (Player p : level.players()) {
            SaiyanState s = ClientStates.get(p.getId());
            boolean forced = s.charging || s.powering > 0;
            if (!s.saiyan || s.ozaru || !(s.aura || forced) || p.isInvisible() || p.distanceToSqr(mc.player) > 64 * 64) continue;
            Form f = s.visibleForm();
            int k = s.transformed || s.powering > 0 ? s.kaioken : 0;
            boolean self = p == mc.player && mc.options.getCameraType().isFirstPerson();
            double maxH = self ? 1.0 : p.getBbHeight() + 0.4;
            if (f.calm() && k == 0) {
                calmAura(level, rnd, p, maxH, self);
                continue;
            }
            float power = f == Form.BASE ? 0.6F : 1.0F + f.ordinal() * 0.12F + k / 100.0F;
            if (forced) power += 0.6F;
            int count = (int) ((self ? 1 : 2) * power);
            Vector3f color = f == Form.BASE ? (k > 0 ? new Vector3f(1.0F, 0.15F, 0.1F) : new Vector3f(0.85F, 0.95F, 1.0F))
                    : new Vector3f(f.ar(), f.ag(), f.ab());
            int oc = f == Form.BASE ? (k > 0 ? 0x700808 : 0x6FA8D8) : f.outerColor();
            Vector3f outer = new Vector3f(((oc >> 16) & 255) / 255F, ((oc >> 8) & 255) / 255F, (oc & 255) / 255F);
            DustParticleOptions dust = new DustParticleOptions(color, 1.1F + power * 0.25F);
            net.minecraft.core.particles.DustColorTransitionOptions fire =
                    new net.minecraft.core.particles.DustColorTransitionOptions(color, outer, 1.3F + power * 0.3F);
            DustParticleOptions red = new DustParticleOptions(new Vector3f(1.0F, 0.12F, 0.08F), 1.3F);
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
            // lenguas de fuego: nacen en los pies y suben rápido cambiando al color exterior
            float drama = Math.max(f.drama(), k > 0 ? 0.4F : 0.0F);
            int flames = (int) ((self ? 1 : 3) * drama * (forced ? 2 : 1));
            for (int i = 0; i < flames; i++) {
                double a = rnd.nextDouble() * Math.PI * 2;
                double rad = 0.35 + rnd.nextDouble() * 0.3;
                level.addParticle(fire, p.getX() + Math.cos(a) * rad, p.getY() + rnd.nextDouble() * 0.4, p.getZ() + Math.sin(a) * rad,
                        Math.cos(a) * 0.02, 0.25 + rnd.nextDouble() * 0.25 * drama, Math.sin(a) * 0.02);
            }
            if (drama > 0.6F && rnd.nextInt(self ? 4 : 2) == 0) {
                level.addParticle(ParticleTypes.SMALL_FLAME, p.getX() + (rnd.nextDouble() - 0.5) * 0.9, p.getY() + rnd.nextDouble() * 0.5,
                        p.getZ() + (rnd.nextDouble() - 0.5) * 0.9, 0, 0.08, 0);
            }
            if (f.lightningAura() && rnd.nextInt(14) == 0) {
                level.playLocalSound(p.getX(), p.getY() + 1, p.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS,
                        0.12F + rnd.nextFloat() * 0.1F, 1.7F + rnd.nextFloat() * 0.3F, false);
            }
            if ((f.lightningAura() || f == Form.BLUE) && rnd.nextInt(self ? 4 : 2) == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, p.getX() + (rnd.nextDouble() - 0.5) * 1.2,
                        p.getY() + rnd.nextDouble() * maxH, p.getZ() + (rnd.nextDouble() - 0.5) * 1.2, 0, 0.1, 0);
            }
            if (f == Form.ULTRA_EGO && rnd.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.WITCH, p.getX() + (rnd.nextDouble() - 0.5) * 1.4,
                        p.getY() + rnd.nextDouble() * maxH, p.getZ() + (rnd.nextDouble() - 0.5) * 1.4, 0, 0.1, 0);
            }
            if ((f == Form.SSJ4 || f == Form.GOD) && rnd.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.FLAME, p.getX() + (rnd.nextDouble() - 0.5) * 1.1, p.getY() + 0.1,
                        p.getZ() + (rnd.nextDouble() - 0.5) * 1.1, 0, 0.12, 0);
            }
            if (f == Form.ROSE && rnd.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.CHERRY_LEAVES, p.getX() + (rnd.nextDouble() - 0.5) * 1.6,
                        p.getY() + rnd.nextDouble() * maxH, p.getZ() + (rnd.nextDouble() - 0.5) * 1.6, 0, 0.05, 0);
            }
            if (f == Form.BEAST && rnd.nextInt(2) == 0) {
                level.addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 1.0F), p.getX() + (rnd.nextDouble() - 0.5) * 1.3,
                        p.getY() + rnd.nextDouble() * maxH, p.getZ() + (rnd.nextDouble() - 0.5) * 1.3, 0, 0.2, 0);
            }
            if (f == Form.LEGENDARY && rnd.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, p.getX() + (rnd.nextDouble() - 0.5) * 1.4, p.getY() + 0.1,
                        p.getZ() + (rnd.nextDouble() - 0.5) * 1.4, 0, 0.05, 0);
            }
            // polvo que levanta el aura en el suelo
            if (drama > 0.5F && p.onGround() && rnd.nextInt(forced ? 1 : 3) == 0) {
                double a = rnd.nextDouble() * Math.PI * 2;
                level.addParticle(ParticleTypes.POOF, p.getX() + Math.cos(a) * 0.8, p.getY() + 0.05, p.getZ() + Math.sin(a) * 0.8,
                        Math.cos(a) * 0.12, 0.01, Math.sin(a) * 0.12);
            }
            if (p.getAbilities().flying && s.flight && rnd.nextInt(2) == 0) {
                level.addParticle(dust, p.getX(), p.getY() - 0.1, p.getZ(), 0, -0.15, 0);
            }
            if (p.tickCount % 40 == 0) {
                level.playLocalSound(p.getX(), p.getY(), p.getZ(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS,
                        0.35F + power * 0.1F, 0.6F + f.ordinal() * 0.06F, false);
            }
            if (drama > 0.5F && p.tickCount % 30 == 0) {
                level.playLocalSound(p.getX(), p.getY(), p.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F + drama * 0.5F, 0.7F, false);
            }
        }
    }

    /** Ultra Instinto: chispas plateadas que suben despacio, sin ruido. */
    private static void calmAura(ClientLevel level, RandomSource rnd, Player p, double maxH, boolean self) {
        if (rnd.nextInt(self ? 3 : 1) == 0) {
            double a = rnd.nextDouble() * Math.PI * 2;
            level.addParticle(ParticleTypes.END_ROD, p.getX() + Math.cos(a) * 0.55, p.getY() + rnd.nextDouble() * maxH,
                    p.getZ() + Math.sin(a) * 0.55, 0, 0.02, 0);
        }
        if (rnd.nextInt(3) == 0) {
            level.addParticle(new DustParticleOptions(new Vector3f(0.86F, 0.9F, 1.0F), 0.7F), p.getX() + (rnd.nextDouble() - 0.5) * 0.9,
                    p.getY() + rnd.nextDouble() * maxH, p.getZ() + (rnd.nextDouble() - 0.5) * 0.9, 0, 0.03, 0);
        }
    }

    /** Clic izquierdo al aire con una Lista de Entrenamiento. */
    @SubscribeEvent
    public static void leftClickAir(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty e) {
        if (e.getItemStack().getItem() instanceof com.bdsz.item.TrainingListItem) {
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.USE_LIST, 0, 0));
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientStates.clear();
    }
}
