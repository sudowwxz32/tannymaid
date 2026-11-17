package com.totallywaifu.event;

import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.data.Ach;
import com.totallywaifu.data.Collection;
import com.totallywaifu.entity.Sleep;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.network.Fx;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TotallyWaifu.MODID)
public final class ForgeEvents {
    private ForgeEvents() {}

    private static final String INTRO = "TW_Intro", INTRO_T = "TW_IntroT", AX = "TW_IntroX", AY = "TW_IntroY", AZ = "TW_IntroZ";
    /** Duración de la escena en el cielo (ticks). Debe coincidir con ClientFx. */
    public static final int INTRO_LEN = 170;

    // ================================================================== bienvenida: escena + caída con explosión
    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        Collection.catchUp(p);
        CompoundTag d = p.getPersistentData();
        if (Collection.takeGift(p)) {
            d.putInt(INTRO, 1);
            d.putInt(INTRO_T, 0);
        } else if (d.getInt(INTRO) == 0) {
            Ach.grant(p, "root");
        } else if (d.getInt(INTRO) == 2) {
            d.putInt(INTRO, 1);                 // se fue a mitad de la escena: la repite
            d.putInt(INTRO_T, 0);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % 100 == 0 && Collection.extrasComplete(p)) {           // Bendición ✦
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LUCK, 260, 0, true, false, true));
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HERO_OF_THE_VILLAGE, 260, 0, true, false, true));
            if (p.level().getEntitiesOfClass(WaifuEntity.class, p.getBoundingBox().inflate(16), w -> w.isOwner(p)).size() >= 3)
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 120, 0, true, false, true));
        }
        CompoundTag d = p.getPersistentData();
        int stage = d.getInt(INTRO);
        if (stage == 0) return;
        int t = d.getInt(INTRO_T) + 1;
        d.putInt(INTRO_T, t);
        ServerLevel lv = p.serverLevel();
        switch (stage) {
            case 1 -> {                                          // espera a que cargue el mundo
                if (t < 50) return;
                int ground = lv.getHeight(Heightmap.Types.MOTION_BLOCKING, p.getBlockX(), p.getBlockZ());
                double y = Math.min(lv.getMaxBuildHeight() - 6, Math.max(p.getY(), ground) + 55);
                d.putDouble(AX, p.getX());
                d.putDouble(AY, y);
                d.putDouble(AZ, p.getZ());
                hover(p, true);
                p.connection.teleport(p.getX(), y, p.getZ(), p.getYRot(), 0);
                Fx.send(p, Fx.INTRO, INTRO_LEN);
                lv.playSound(null, p.getX(), y, p.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.6F);
                d.putInt(INTRO, 2);
                d.putInt(INTRO_T, 0);
            }
            case 2 -> {                                          // flota en el cielo mientras le entregan la vara
                p.setDeltaMovement(Vec3.ZERO);
                p.fallDistance = 0;
                if (t % 10 == 0 && p.position().distanceToSqr(d.getDouble(AX), d.getDouble(AY), d.getDouble(AZ)) > 1.5)
                    p.connection.teleport(d.getDouble(AX), d.getDouble(AY), d.getDouble(AZ), p.getYRot(), p.getXRot());
                if (t % 20 == 0) lv.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 6, 1.2, 1.2, 1.2, 0.01);
                if (t < INTRO_LEN) return;
                ItemStack wand = new ItemStack(ModRegistry.WAND.get());
                if (!p.getInventory().add(wand)) p.drop(wand, false);
                hover(p, false);
                Fx.send(p, Fx.WAND, 0);
                lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                p.sendSystemMessage(Component.literal("§d♥ Totally Waifu?: §fRecibiste la §dVara de Transformación§f."));
                p.sendSystemMessage(Component.literal("§7  Úsala sobre una mesa de crafteo, horno, cofre, mesa de encantamientos o cama: se transforman en chicas (y al revés)."));
                p.sendSystemMessage(Component.literal("§7  Sobre animales y monstruos (¡y TNT!), se transforman en chicas para siempre. Tu colección: inventario → §d♥ Mis Waifus§7."));
                d.putInt(INTRO, 3);
                d.putInt(INTRO_T, 0);
            }
            case 3 -> {                                          // cae del cielo... ¡y aterriza con una explosión!
                p.fallDistance = 0;
                if (!(p.onGround() || p.isInWater() || t > 600)) return;
                Vec3 c = p.position();
                lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 0.5, c.z, 2, 0.6, 0.2, 0.6, 0);
                lv.sendParticles(ParticleTypes.FLASH, c.x, c.y + 1, c.z, 1, 0, 0, 0, 0);
                lv.sendParticles(ParticleTypes.HEART, c.x, c.y + 1, c.z, 20, 2.5, 0.6, 2.5, 0);
                lv.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 0.2, c.z, 80, 0.2, 0.1, 0.2, 0.6);
                lv.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.9F);
                lv.playSound(null, c.x, c.y, c.z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
                for (Mob m : lv.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(8))) {
                    Vec3 dir = m.position().subtract(c).normalize().scale(1.4);
                    m.setDeltaMovement(dir.x, 0.6, dir.z);
                    m.hurtMarked = true;
                }
                Fx.send(p, Fx.LANDED, 0);
                Ach.grant(p, "root");
                Ach.grant(p, "vara");
                d.remove(INTRO);
                d.remove(INTRO_T);
            }
            default -> d.remove(INTRO);
        }
    }

    private static void hover(ServerPlayer p, boolean on) {
        if (p.isCreative() || p.isSpectator()) return;
        p.getAbilities().mayfly = on;
        p.getAbilities().flying = on;
        p.onUpdateAbilities();
    }

    @SubscribeEvent
    public static void fall(LivingFallEvent e) {
        if (e.getEntity() instanceof Player p && p.getPersistentData().getInt(INTRO) >= 2) e.setCanceled(true);
        // Gela (slime) y Pía (gallina) te protegen de las caídas
        if (e.getEntity() instanceof Player p && !e.isCanceled()) {
            for (WaifuEntity w : p.level().getEntitiesOfClass(WaifuEntity.class, p.getBoundingBox().inflate(10),
                    w -> (w.kind == WaifuKind.SLIME || w.kind == WaifuKind.GALLINA) && w.isOwner(p))) {
                e.setDamageMultiplier(0);
                if (w.kind == WaifuKind.SLIME && e.getDistance() > 3) {
                    p.setDeltaMovement(p.getDeltaMovement().x, Math.min(1.0, e.getDistance() * 0.06), p.getDeltaMovement().z);
                    p.hurtMarked = true;
                    p.playSound(SoundEvents.SLIME_BLOCK_FALL, 1.0F, 1.0F);
                }
                break;
            }
        }
    }

    // ================================================================== la vara
    /** Al morir, la vara no se cae: se queda contigo. */
    @SubscribeEvent
    public static void drops(net.minecraftforge.event.entity.living.LivingDropsEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        net.minecraft.nbt.ListTag keep = new net.minecraft.nbt.ListTag();
        e.getDrops().removeIf(ie -> {
            if (!ie.getItem().is(ModRegistry.WAND.get())) return false;
            keep.add(ie.getItem().save(new CompoundTag()));
            return true;
        });
        if (!keep.isEmpty()) persisted(p).put("TW_KeepWand", keep);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.isEndConquered()) return;
        CompoundTag d = persisted(p);
        if (!d.contains("TW_KeepWand")) return;
        net.minecraft.nbt.ListTag l = d.getList("TW_KeepWand", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < l.size(); i++) {
            ItemStack st = ItemStack.of(l.getCompound(i));
            if (!p.getInventory().add(st)) p.drop(st, false);
        }
        d.remove("TW_KeepWand");
        p.displayClientMessage(Component.literal("§d✦ Tu Vara de Transformación se quedó contigo ♥"), true);
    }

    private static CompoundTag persisted(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    /** Con la vara en la mano, la mesa/horno/cofre/cama no abre su menú: se transforma. */
    @SubscribeEvent
    public static void useBlock(PlayerInteractEvent.RightClickBlock e) {
        if (e.getItemStack().is(ModRegistry.WAND.get()) && WaifuKind.fromBlock(e.getLevel().getBlockState(e.getPos()).getBlock()) != null) {
            e.setUseBlock(Event.Result.DENY);
            e.setUseItem(Event.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract e) {
        if (!e.getItemStack().is(ModRegistry.WAND.get())) return;
        boolean ok = WaifuKind.fromEntity(e.getTarget()) != null || e.getTarget() instanceof WaifuEntity;
        if (!ok) return;
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS);
        if (e.getEntity() instanceof ServerPlayer p && e.getHand() == InteractionHand.MAIN_HAND) {
            com.totallywaifu.item.WandItem.useOnEntity(p, e.getTarget());
            p.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    // ================================================================== Yume
    /** Clic izquierdo a Yume: a dormir. */
    @SubscribeEvent
    public static void attack(AttackEntityEvent e) {
        if (e.getTarget() instanceof WaifuEntity w && w.isOwner(e.getEntity())) {
            e.setCanceled(true);
            if (w.kind == WaifuKind.CAMA && e.getEntity() instanceof ServerPlayer p) Sleep.start(p, w);
        }
    }

    /** Dormir sin cama de verdad: en sus piernas. (cliente y servidor) */
    @SubscribeEvent
    public static void sleepSpot(SleepingLocationCheckEvent e) {
        if (e.getEntity() instanceof Player p && Sleep.lapOf(p) != null) e.setResult(Event.Result.ALLOW);
    }

    /** Si duermes en la cama que antes era Yume, también guarda tu punto de regreso. */
    @SubscribeEvent
    public static void bed(PlayerSleepInBedEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && Collection.isHerBed(p, e.getPos())) {
            Collection.setHome(p, p.level().dimension().location().toString(), e.getPos());
            p.displayClientMessage(Component.literal("§9Yume (cama)§7: ¡Dulces sueños! Recordaré este lugar ♥"), true);
        }
    }

    /** Nadie te lastima mientras duermes en sus piernas; las guardianas reducen el daño. */
    @SubscribeEvent
    public static void protect(LivingAttackEvent e) {
        if (e.getEntity() instanceof Player p && p.isSleeping() && Sleep.lapOf(p) != null) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent e) {
        LivingEntity v = e.getEntity();
        // tú golpeas a un animal → Luna lo caza por ti
        if (e.getSource().getEntity() instanceof ServerPlayer p && v instanceof Animal a && !(v instanceof net.minecraft.world.entity.TamableAnimal ta && ta.isTame())) {
            for (WaifuEntity w : p.level().getEntitiesOfClass(WaifuEntity.class, p.getBoundingBox().inflate(24),
                    w -> w.kind == WaifuKind.PERRO && w.isOwner(p) && w.getMode() != WaifuEntity.STAY)) {
                w.setTarget(a);
                w.say(p, "¡Yo lo atrapo! ♥");
            }
        }
        if (!(v instanceof ServerPlayer p)) return;
        // Ferra, Umbra y Luna te cuidan
        for (WaifuEntity w : p.level().getEntitiesOfClass(WaifuEntity.class, p.getBoundingBox().inflate(16), w -> w.isOwner(p))) {
            if (w.kind == WaifuKind.WARDEN) e.setAmount(e.getAmount() * 0.6F);
            else if (w.kind == WaifuKind.GOLEM) e.setAmount(e.getAmount() * 0.85F);
        }
        // Umbra aparece a tu lado si alguien te ataca
        if (e.getSource().getEntity() instanceof LivingEntity att && !(att instanceof Player) && !(att instanceof WaifuEntity)) {
            for (WaifuEntity w : p.level().getEntitiesOfClass(WaifuEntity.class, p.getBoundingBox().inflate(64),
                    w -> w.kind == WaifuKind.WARDEN && w.isOwner(p) && w.getMode() != WaifuEntity.STAY)) {
                if (w.distanceToSqr(p) > 12 * 12) {
                    ((ServerLevel) w.level()).sendParticles(ParticleTypes.SCULK_SOUL, w.getX(), w.getY() + 1.5, w.getZ(), 20, 0.4, 1, 0.4, 0.05);
                    w.teleportTo(p.getX() + 1, p.getY(), p.getZ() + 1);
                    w.playSound(SoundEvents.WARDEN_EMERGE, 1.0F, 1.2F);
                }
                w.setTarget(att);
            }
        }
    }
}
