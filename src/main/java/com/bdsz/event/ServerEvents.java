package com.bdsz.event;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.command.RazaCommand;
import com.bdsz.data.Form;
import com.bdsz.data.PowerLogic;
import com.bdsz.data.SaiyanData;
import com.bdsz.data.SaiyanState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BlockDragonSuperZ.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        RazaCommand.register(e.getDispatcher());
    }

    /** Fuerza: tus golpes hacen más daño. Resistencia: recibes menos. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void hurt(LivingHurtEvent e) {
        if (e.getSource().getEntity() instanceof ServerPlayer atk) {
            SaiyanState s = SaiyanData.get(atk);
            if (s.saiyan && s.powered()) e.setAmount(e.getAmount() * s.damageMultiplier());
        }
        if (e.getEntity() instanceof ServerPlayer vic) {
            SaiyanState s = SaiyanData.get(vic);
            if (s.saiyan && s.powered()) e.setAmount(e.getAmount() * (1.0F - s.resistance()));
        }
    }

    /** Ultra Instinto: esquiva automáticamente parte de los ataques. */
    @SubscribeEvent
    public static void attack(LivingAttackEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getSource().getEntity() == null) return;
        SaiyanState s = SaiyanData.get(p);
        if (s.saiyan && s.activeForm() == Form.ULTRA && p.getRandom().nextFloat() < 0.35F) {
            e.setCanceled(true);
            p.serverLevel().sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 12, 0.4, 0.6, 0.4, 0.02);
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 1.0F, 1.6F);
            PowerLogic.msg(p, "§f§oEsquivado");
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.END && e.player instanceof ServerPlayer p && p.tickCount % 20 == 0) {
            PowerLogic.secondTick(p);
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            SaiyanState s = SaiyanData.get(p);
            if (s.transformed) {
                s.transformed = false;
                SaiyanData.set(p, s);
            }
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) SaiyanData.set(p, SaiyanData.get(p));
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) SaiyanData.set(p, SaiyanData.get(p));
    }

    @SubscribeEvent
    public static void changeDim(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) SaiyanData.set(p, SaiyanData.get(p));
    }

    @SubscribeEvent
    public static void track(PlayerEvent.StartTracking e) {
        if (e.getTarget() instanceof ServerPlayer target && e.getEntity() instanceof ServerPlayer viewer) {
            SaiyanData.syncTo(target, viewer);
        }
    }
}
