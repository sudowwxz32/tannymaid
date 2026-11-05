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
        if ((s.bearer() && s.on(LanternState.GHOST)) || (s.abduct >= Abduction.LIGHT && s.abduct <= Abduction.ASK)) {
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
        // el anillo siempre protege: caídas, fuego, lava, ahogo, asfixia, frío, rayos, cactus...
        if (s.bearer() && s.charge > 0 && LanternLogic.hasRing(p) && environmental(e.getSource())) {
            e.setCanceled(true);
            p.clearFire();
        }
    }

    private static boolean environmental(net.minecraft.world.damagesource.DamageSource src) {
        return src.is(DamageTypeTags.IS_FALL) || src.is(DamageTypeTags.IS_FIRE) || src.is(DamageTypeTags.IS_DROWNING)
                || src.is(DamageTypeTags.IS_FREEZING) || src.is(DamageTypeTags.IS_LIGHTNING) || src.is(DamageTypes.IN_WALL)
                || src.is(DamageTypes.CRAMMING) || src.is(DamageTypes.CACTUS) || src.is(DamageTypes.SWEET_BERRY_BUSH)
                || src.is(DamageTypes.FLY_INTO_WALL) || src.is(DamageTypes.STALAGMITE) || src.is(DamageTypes.FALLING_STALACTITE)
                || src.is(DamageTypes.FALLING_BLOCK) || src.is(DamageTypes.FALLING_ANVIL) || src.is(DamageTypes.HOT_FLOOR)
                || src.is(DamageTypes.MAGIC) || src.is(DamageTypes.WITHER) || src.is(DamageTypes.DRAGON_BREATH);
    }

    /** Escudo personal del anillo: reduce todo golpe y te salva de la muerte si tiene energía. */
    @SubscribeEvent
    public static void hurt(net.minecraftforge.event.entity.living.LivingHurtEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        LanternState s = LanternData.get(p);
        if (!s.bearer() || s.charge <= 0 || !LanternLogic.hasRing(p) || e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        net.minecraft.server.level.ServerLevel lv = p.serverLevel();
        float dmg = e.getAmount() * (s.on(LanternState.ARMOR) ? 0.35F : 0.6F);
        s.charge = Math.max(0, s.charge - (int) (dmg * 15));
        // burbuja verde donde te golpean
        for (int i = 0; i < 30; i++) {
            double a = lv.random.nextDouble() * Math.PI * 2, b = Math.acos(2 * lv.random.nextDouble() - 1);
            lv.sendParticles(LanternLogic.GREEN, p.getX() + Math.sin(b) * Math.cos(a) * 1.1, p.getY() + 1 + Math.cos(b) * 1.1,
                    p.getZ() + Math.sin(b) * Math.sin(a) * 1.1, 1, 0, 0, 0, 0);
        }
        lv.playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_HIT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
        if (p.getHealth() + p.getAbsorptionAmount() - dmg <= 0.5F && s.charge >= LanternState.MAX_CHARGE / 10) {
            // ¡golpe mortal! el anillo te salva
            dmg = 0;
            s.charge -= LanternState.MAX_CHARGE / 10;
            p.setHealth(Math.max(p.getHealth(), 8.0F));
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 2));
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 200, 2));
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 60, 4));
            lv.sendParticles(LanternLogic.GREEN, p.getX(), p.getY() + 1, p.getZ(), 200, 1.2, 1.2, 1.2, 0);
            lv.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 40, 0.4, 0.8, 0.4, 0.2);
            lv.playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.4F);
            for (net.minecraft.world.entity.LivingEntity m : lv.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                    p.getBoundingBox().inflate(5), m -> m != p)) {
                net.minecraft.world.phys.Vec3 d = m.position().subtract(p.position()).normalize().scale(1.5);
                m.setDeltaMovement(d.x, 0.6, d.z);
                m.hurtMarked = true;
            }
            LanternLogic.msg(p, "§a§l✦ ¡El anillo te protegió de la muerte! §7(-10 % de energía)");
        }
        e.setAmount(dmg);
        if (dmg <= 0) e.setCanceled(true);
        LanternData.set(p, s);
    }

    /** Al morir, el anillo y la lámpara se quedan contigo (y las armas de energía se desvanecen). */
    @SubscribeEvent
    public static void drops(net.minecraftforge.event.entity.living.LivingDropsEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        net.minecraft.nbt.ListTag keep = LanternData.root(p).getList(KEEP, 10);
        var it = e.getDrops().iterator();
        while (it.hasNext()) {
            net.minecraft.world.item.ItemStack st = it.next().getItem();
            if (st.is(ModRegistry.RING.get()) || st.is(ModRegistry.CANDELABRA_ITEM.get())) {
                keep.add(st.save(new net.minecraft.nbt.CompoundTag()));
                it.remove();
            } else if (st.is(ModRegistry.ENERGY_PICK.get()) || (st.hasTag() && st.getTag().getBoolean(Constructs.WEAPON))) {
                it.remove();
            }
        }
        if (!keep.isEmpty()) LanternData.root(p).put(KEEP, keep);
    }

    private static final String KEEP = "LinternaGuardado";

    @SubscribeEvent
    public static void respawned(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        net.minecraft.nbt.CompoundTag root = LanternData.root(p);
        if (!root.contains(KEEP)) return;
        net.minecraft.nbt.ListTag keep = root.getList(KEEP, 10);
        for (int i = 0; i < keep.size(); i++) {
            net.minecraft.world.item.ItemStack st = net.minecraft.world.item.ItemStack.of(keep.getCompound(i));
            if (st.is(ModRegistry.RING.get()) && !RingSlot.worn(p)) RingSlot.set(p, st);       // vuelve directo a tu dedo
            else if (!p.getInventory().add(st)) p.drop(st, false);
        }
        root.remove(KEEP);
        LanternState s = LanternData.get(p);
        s.worn = RingSlot.worn(p);
        LanternData.set(p, s);
        p.serverLevel().sendParticles(LanternLogic.GREEN, p.getX(), p.getY() + 1, p.getZ(), 80, 0.5, 1, 0.5, 0);
        p.sendSystemMessage(net.minecraft.network.chat.Component.literal("§aEl Anillo Verde y su Lámpara volvieron contigo."));
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
