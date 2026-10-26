package com.cz.event;

import com.cz.ChakraZorro;
import com.cz.command.ZorroCommand;
import com.cz.data.Phase;
import com.cz.data.ZorroData;
import com.cz.data.ZorroLogic;
import com.cz.data.ZorroState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChakraZorro.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        ZorroCommand.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        ZorroState s = ZorroData.get(p);
        if (!s.awakened) return;
        if (s.powering > 0) ZorroLogic.tickPowering(p, s);
        else if (s.charging) ZorroLogic.tickCharge(p, s);
        if (p.tickCount % 20 == 0) ZorroLogic.secondTick(p);
    }

    /** El manto hirviente (fase 3+) quema a quien te golpea cuerpo a cuerpo. */
    @SubscribeEvent
    public static void hurt(LivingHurtEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && e.getSource().getDirectEntity() instanceof LivingEntity atk) {
            Phase ph = ZorroData.get(p).active();
            if (ph.burns) atk.setSecondsOnFire(4);
        }
    }

    /** En cuatro patas (fases 4 y 5) el cuerpo es más bajo y largo. Funciona en servidor y cliente. */
    @SubscribeEvent
    public static void size(EntityEvent.Size e) {
        if (!(e.getEntity() instanceof Player p) || p.level() == null) return;
        ZorroState s;
        if (p.level().isClientSide) s = com.cz.network.ClientStates.get(p.getId());
        else if (p instanceof ServerPlayer sp && sp.connection != null) s = ZorroData.get(sp);
        else return;
        if (!s.active().quadruped) return;
        e.setNewSize(EntityDimensions.scalable(0.8F, 1.3F));
        e.setNewEyeHeight(1.05F);
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            ZorroState s = ZorroData.get(p);
            if (s.phase != 0 || s.powering > 0 || s.charging) {
                s.phase = 0;
                s.powering = 0;
                s.charging = false;
                ZorroData.set(p, s);
            }
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            ZorroState s = ZorroData.get(p);
            s.powering = 0;
            s.charging = false;
            ZorroData.set(p, s);
            p.refreshDimensions();
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        ZorroData.forget(e.getEntity());
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            ZorroData.set(p, ZorroData.get(p));
            p.refreshDimensions();
        }
    }

    @SubscribeEvent
    public static void changeDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) ZorroData.set(p, ZorroData.get(p));
    }

    @SubscribeEvent
    public static void track(PlayerEvent.StartTracking e) {
        if (e.getTarget() instanceof ServerPlayer t && e.getEntity() instanceof ServerPlayer v) ZorroData.syncTo(t, v);
    }
}
