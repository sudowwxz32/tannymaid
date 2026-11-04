package com.linterna.event;

import com.linterna.Linterna;
import com.linterna.command.LinternaCommand;
import com.linterna.data.*;
import com.linterna.network.ClientStates;
import com.linterna.registry.ModRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Linterna.MODID)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        LinternaCommand.register(e.getDispatcher());
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        LanternLogic.tick(p);
        if (p.tickCount % 20 == 0) Constructs.expire(p);
    }

    /** Intangible: atraviesas paredes (en el servidor y en el cliente). */
    @SubscribeEvent
    public static void ghost(LivingEvent.LivingTickEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        LanternState s = p.level().isClientSide ? ClientStates.get(p.getId()) : LanternData.get(p);
        if (s.bearer() && s.on(LanternState.GHOST)) {
            p.noPhysics = true;
            p.fallDistance = 0;
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            LanternState s = LanternData.get(p);
            Abduction.onLogin(p, s);
            LanternData.set(p, s);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Holding.release(p, false);
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking e) {
        if (e.getTarget() instanceof ServerPlayer t && e.getEntity() instanceof ServerPlayer p) {
            com.linterna.network.Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> p),
                    new com.linterna.network.SyncPacket(t.getId(), LanternData.get(t)));
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.Clone e) {
        if (e.isWasDeath() && e.getEntity() instanceof ServerPlayer p) {
            LanternState s = LanternData.get(p);
            s.powers &= LanternState.SUIT;
            LanternData.set(p, s);
        }
    }

    /** Durante el viaje de la luz no recibes daño; intangible: nada te toca (salvo el vacío). */
    @SubscribeEvent
    public static void attack(LivingAttackEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        LanternState s = LanternData.get(p);
        if (Abduction.immune(p, s) && !e.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            e.setCanceled(true);
            return;
        }
        if (s.bearer() && s.on(LanternState.GHOST) && !e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                && !e.getSource().is(DamageTypes.STARVE)) {
            e.setCanceled(true);
        }
        // volando con el anillo no te haces daño al aterrizar
        if (s.bearer() && e.getSource().is(DamageTypes.FALL) && (s.on(LanternState.FLY) || s.on(LanternState.ARMOR))) e.setCanceled(true);
    }

    /** Intangible: los monstruos no pueden fijarte como objetivo. */
    @SubscribeEvent
    public static void target(LivingChangeTargetEvent e) {
        if (e.getNewTarget() instanceof ServerPlayer p) {
            LanternState s = LanternData.get(p);
            if (s.bearer() && s.on(LanternState.GHOST)) e.setCanceled(true);
        }
    }

    /** Los objetos del anillo (pico) no se pueden tirar: se desvanecen. */
    @SubscribeEvent
    public static void toss(ItemTossEvent e) {
        net.minecraft.world.item.ItemStack it = e.getEntity().getItem();
        if (it.hasTag() && it.getTag().getBoolean(Constructs.WEAPON)) {
            e.getEntity().discard();          // las armas de energía se desvanecen al soltarlas
            return;
        }
        if (it.is(ModRegistry.ENERGY_PICK.get()) && e.getPlayer() instanceof ServerPlayer p) {
            e.setCanceled(true);
            LanternState s = LanternData.get(p);
            s.set(LanternState.PICK, false);
            LanternData.set(p, s);
            LanternLogic.removeItem(p, ModRegistry.ENERGY_PICK.get());
        }
    }

    /** Candelabro en la mano + clic izquierdo sobre un bloque: recarga (sin romper nada). */
    @SubscribeEvent
    public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (e.getItemStack().is(ModRegistry.CANDELABRA_ITEM.get())) {
            e.setCanceled(true);
            if (e.getEntity() instanceof ServerPlayer p) LanternLogic.startRecharge(p);
        }
    }
}
