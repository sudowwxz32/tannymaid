package com.linterna.data;

import com.linterna.entity.RayoVerde;
import com.linterna.network.ActionPacket;
import com.linterna.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Los poderes del anillo: vuelo, armadura, pico, intangibilidad, armas, martillo, traje y la recarga. */
public final class LanternLogic {
    private LanternLogic() {}

    public static final DustParticleOptions GREEN = new DustParticleOptions(new Vector3f(0.3F, 1.0F, 0.4F), 1.3F);

    public static void msg(ServerPlayer p, String t) {
        p.displayClientMessage(Component.literal(t), true);
    }

    /** ¿Tiene el anillo encima? */
    public static boolean hasRing(Player p) {
        if (RingSlot.worn(p)) return true;
        for (ItemStack s : p.getInventory().items) if (s.is(ModRegistry.RING.get())) return true;
        for (ItemStack s : p.getInventory().offhand) if (s.is(ModRegistry.RING.get())) return true;
        return false;
    }

    // ------------------------------------------------------------------ teclas

    public static void key(ServerPlayer p, int action) {
        LanternState s = LanternData.get(p);
        if (!s.bearer()) return;
        if (!hasRing(p)) { msg(p, "§aNecesitas tener el Anillo Verde contigo."); return; }
        if (s.abduct != 0) return;
        if (s.charge <= 0 && !(action == ActionPacket.SUIT && s.on(LanternState.SUIT))) {
            msg(p, "§cEl anillo no tiene energía. §aRecárgalo con el Lámpara de Poder (clic izquierdo).");
            return;
        }
        switch (action) {
            case ActionPacket.FLY -> toggle(p, s, LanternState.FLY, "Vuelo");
            case ActionPacket.ARMOR -> toggle(p, s, LanternState.ARMOR, "Armadura de energía");
            case ActionPacket.PICK -> {
                toggle(p, s, LanternState.PICK, "Pico de energía");
                if (s.on(LanternState.PICK)) givePick(p); else removeItem(p, ModRegistry.ENERGY_PICK.get().asItem());
            }
            case ActionPacket.GHOST -> toggle(p, s, LanternState.GHOST, "Intangible");
            case ActionPacket.GUNS -> toggle(p, s, LanternState.GUNS, "Armas de energía");
            case ActionPacket.HAMMER -> hammer(p, s);
            case ActionPacket.SUIT -> {
                if (!s.suitUnlocked() && !s.on(LanternState.SUIT)) {
                    int left = (LanternState.SUIT_UNLOCK - s.useTicks) / 20;
                    msg(p, "§aEl traje aún no responde. Sigue usando el anillo: faltan " + left / 60 + " min " + left % 60 + " s.");
                    return;
                }
                toggle(p, s, LanternState.SUIT, "Traje del anillo");
            }
            default -> { }
        }
        applyFlight(p, s);
        LanternData.set(p, s);
    }

    private static void toggle(ServerPlayer p, LanternState s, int bit, String name) {
        boolean on = !s.on(bit);
        s.set(bit, on);
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(GREEN, p.getX(), p.getY() + 1, p.getZ(), on ? 60 : 20, 0.5, 0.9, 0.5, 0);
        lv.playSound(null, p.blockPosition(), on ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, on ? 1.4F : 1.0F);
        msg(p, (on ? "§a✦ " : "§7✧ ") + name + (on ? " activado" : " desactivado"));
    }

    private static void applyFlight(ServerPlayer p, LanternState s) {
        boolean fly = s.on(LanternState.FLY) || s.on(LanternState.GHOST);
        if (p.isCreative() || p.isSpectator()) return;
        if (fly != p.getAbilities().mayfly) {
            p.getAbilities().mayfly = fly;
            if (!fly) p.getAbilities().flying = false;
            else p.getAbilities().flying = true;
            p.getAbilities().setFlyingSpeed(fly ? 0.1F : 0.05F);
            p.onUpdateAbilities();
        }
    }

    private static void givePick(ServerPlayer p) {
        removeItem(p, ModRegistry.ENERGY_PICK.get().asItem());
        ItemStack pick = new ItemStack(ModRegistry.ENERGY_PICK.get());
        ItemStack main = p.getMainHandItem();
        if (main.isEmpty()) p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pick);
        else if (!p.getInventory().add(pick)) p.drop(pick, false);
    }

    public static void removeItem(Player p, net.minecraft.world.item.Item item) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            if (p.getInventory().getItem(i).is(item)) p.getInventory().setItem(i, ItemStack.EMPTY);
        }
    }

    // ------------------------------------------------------------------ disparos del anillo

    private static final java.util.Map<java.util.UUID, Long> LAST_SHOT = new java.util.concurrent.ConcurrentHashMap<>();

    /** Pequeño proyectil de energía que sale del anillo. */
    public static void ringShot(ServerPlayer p) {
        LanternState s = LanternData.get(p);
        if (!s.bearer() || s.abduct != 0 || !hasRing(p)) return;
        boolean inHand = p.getMainHandItem().is(ModRegistry.RING.get()) || p.getOffhandItem().is(ModRegistry.RING.get());
        if (!inHand && !(RingSlot.worn(p) && p.getMainHandItem().isEmpty())) return;
        long now = p.level().getGameTime();
        Long last = LAST_SHOT.get(p.getUUID());
        if (last != null && now - last < 5) return;
        LAST_SHOT.put(p.getUUID(), now);
        if (!spend(p, s, 40)) return;
        ServerLevel lv = p.serverLevel();
        Vec3 look = p.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 from = p.getEyePosition().subtract(0, 0.3, 0).add(right.scale(0.3)).add(look.scale(0.4));
        RayoVerde r = new RayoVerde(lv, p, false);
        r.setDamage(5.0F);
        r.setSize(0.22F);
        r.setPos(from.x, from.y, from.z);
        r.shoot(look.x, look.y, look.z, 3.4F, 0.2F);
        lv.addFreshEntity(r);
        lv.sendParticles(GREEN, from.x, from.y, from.z, 6, 0.05, 0.05, 0.05, 0);
        lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.9F, 1.8F);
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.25F, 2.0F);
        p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        LanternData.quiet(p, s);
    }

    // ------------------------------------------------------------------ martillo

    private static void hammer(ServerPlayer p, LanternState s) {
        if (s.hammer > 0) return;
        if (!spend(p, s, 250)) return;
        s.hammer = 16;
        msg(p, "§a✦ ¡Martillo!");
    }

    /** El golpe del martillo: destroza todo lo que tiene delante. */
    private static void hammerHit(ServerPlayer p) {
        ServerLevel lv = p.serverLevel();
        Vec3 look = p.getLookAngle();
        Vec3 c = p.position().add(look.x * 3.5, 0.5 + look.y * 2.0, look.z * 3.5);
        lv.playSound(null, BlockPos.containing(c), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.6F);
        lv.playSound(null, BlockPos.containing(c), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5F, 0.5F);
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 1, 0, 0, 0, 0);
        lv.sendParticles(GREEN, c.x, c.y, c.z, 200, 2.5, 1.5, 2.5, 0);
        int r = 3;
        BlockPos cp = BlockPos.containing(c);
        for (BlockPos q : BlockPos.betweenClosed(cp.offset(-r, -r, -r), cp.offset(r, r, r))) {
            if (q.distSqr(cp) > r * r + 1) continue;
            BlockState st = lv.getBlockState(q);
            if (st.isAir() || st.getDestroySpeed(lv, q) < 0 || st.is(Blocks.OBSIDIAN) || st.is(ModRegistry.CANDELABRA.get())) continue;
            lv.destroyBlock(q, lv.random.nextInt(4) == 0, p);
        }
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(4.5), e -> e != p)) {
            e.hurt(lv.damageSources().playerAttack(p), 30.0F);
            Vec3 push = e.position().subtract(p.position()).normalize().scale(2.2);
            e.setDeltaMovement(push.x, 0.9, push.z);
            e.hurtMarked = true;
        }
    }

    // ------------------------------------------------------------------ recarga

    public static void startRecharge(ServerPlayer p) {
        LanternState s = LanternData.get(p);
        if (!s.bearer() || s.recharge > 0) return;
        boolean holding = p.getMainHandItem().is(ModRegistry.CANDELABRA_ITEM.get()) || p.getOffhandItem().is(ModRegistry.CANDELABRA_ITEM.get());
        if (!holding && !nearCandelabra(p)) return;
        if (!hasRing(p)) { msg(p, "§aNecesitas tener el Anillo Verde contigo para recargarlo."); return; }
        if (s.charge >= LanternState.MAX_CHARGE) { msg(p, "§aEl anillo ya está al 100 %."); return; }
        s.recharge = 60;
        LanternData.set(p, s);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 1.3F);
        msg(p, "§a✦ El anillo se recarga...");
        p.sendSystemMessage(Component.literal("§2§o«Que la luz verde me guíe y me dé valor.»"));
    }

    private static boolean nearCandelabra(ServerPlayer p) {
        BlockPos b = p.blockPosition();
        for (BlockPos q : BlockPos.betweenClosed(b.offset(-4, -2, -4), b.offset(4, 3, 4))) {
            if (p.level().getBlockState(q).is(ModRegistry.CANDELABRA.get())) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ energía

    public static boolean spend(ServerPlayer p, LanternState s, int amount) {
        if (s.charge < amount) {
            msg(p, "§cNo hay suficiente energía en el anillo (§f" + pct(s) + "%§c). Recárgalo.");
            return false;
        }
        s.charge -= amount;
        s.useTicks += amount / 4;
        return true;
    }

    public static int pct(LanternState s) {
        return (int) Math.ceil(s.charge * 100.0 / LanternState.MAX_CHARGE);
    }

    // ------------------------------------------------------------------ cada tick

    public static void tick(ServerPlayer p) {
        LanternState s = LanternData.get(p);
        if (s.abduct != 0) {
            Abduction.tick(p, s);
            return;
        }
        if (s.stage == 0) {
            if (p.tickCount % 20 == 0) Abduction.check(p, s);
            return;
        }
        if (!s.bearer()) return;
        boolean dirty = false;
        ServerLevel lv = p.serverLevel();
        // recarga
        if (s.recharge > 0) {
            s.recharge--;
            s.charge = Math.min(LanternState.MAX_CHARGE, s.charge + LanternState.MAX_CHARGE / 50);
            if (s.recharge % 3 == 0) {
                for (int i = 0; i < 6; i++) {
                    double a = (s.recharge + i) * 0.7;
                    lv.sendParticles(GREEN, p.getX() + Math.cos(a) * 1.2, p.getY() + 0.2 + (60 - s.recharge) / 30.0, p.getZ() + Math.sin(a) * 1.2, 1, 0, 0, 0, 0);
                }
            }
            if (s.recharge == 0 || s.charge >= LanternState.MAX_CHARGE) {
                s.recharge = 0;
                lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.6F);
                lv.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1, p.getZ(), 40, 0.5, 0.8, 0.5, 0.3);
                msg(p, "§a✦ Anillo recargado: " + pct(s) + "%");
            }
            dirty = true;
        }
        // martillo
        if (s.hammer > 0) {
            s.hammer--;
            if (s.hammer == 6) hammerHit(p);
            dirty = true;
        }
        // si ya no tiene el anillo, se apaga todo
        if (s.powers != 0 && !hasRing(p)) {
            off(p, s, "§7Sin el anillo, tus poderes se apagan.");
            dirty = true;
        }
        // consumo de energía
        int drain = 0;
        if (s.on(LanternState.FLY) && p.getAbilities().flying) drain += 1;
        if (s.on(LanternState.ARMOR)) drain += 2;
        if (s.on(LanternState.PICK)) drain += 1;
        if (s.on(LanternState.GHOST)) drain += 3;
        if (s.on(LanternState.GUNS)) drain += 2;
        if (Holding.isHolding(p)) drain += 1;
        if (drain > 0) {
            s.charge -= drain;
            s.useTicks += 1;
            if (s.charge <= 0) {
                s.charge = 0;
                off(p, s, "§cEl anillo se quedó sin energía. §aRecárgalo con el Lámpara de Poder.");
                Holding.release(p, false);
            } else if (s.charge == LanternState.MAX_CHARGE / 10 || s.charge == LanternState.MAX_CHARGE / 10 - 1) {
                msg(p, "§eAnillo al 10 %. Busca tu Lámpara de Poder.");
            }
            if (p.tickCount % 20 == 0) dirty = true;
        }
        // efectos de los poderes
        if (p.tickCount % 20 == 0) effects(p, s);
        if (s.on(LanternState.FLY) && !p.isCreative() && !p.getAbilities().mayfly) applyFlight(p, s);
        if (s.on(LanternState.GUNS) && p.tickCount % 8 == 0) shoot(p);
        if (s.on(LanternState.PICK) && p.tickCount % 20 == 0 && !hasItem(p, ModRegistry.ENERGY_PICK.get().asItem())) givePick(p);
        if ((p.getAbilities().flying || s.on(LanternState.GHOST)) && p.tickCount % 3 == 0) {
            lv.sendParticles(GREEN, p.getX(), p.getY() + 0.8, p.getZ(), 2, 0.3, 0.5, 0.3, 0);   // estela verde
        }
        Holding.tick(p);
        if (p.tickCount % 10 == 0) {
            boolean worn = RingSlot.worn(p);
            if (worn != s.worn) { s.worn = worn; dirty = true; }
            // el anillo apenas protege: fuego y agua no le hacen nada al portador
            if (hasRing(p) && s.charge > 0) {
                p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false, false));
                p.removeEffect(MobEffects.POISON);
                p.removeEffect(MobEffects.WITHER);
                p.clearFire();
            }
        }
        s.held = LanternData.get(p).held;          // Holding pudo soltar lo que sostenía
        // silueta verde de resalte en el personaje mientras usa el anillo
        boolean glow = (s.powers & (LanternState.FLY | LanternState.ARMOR | LanternState.GHOST | LanternState.GUNS)) != 0;
        if (glow != p.hasGlowingTag()) {
            if (glow) Glow.on(p); else Glow.off(p);
        }
        if (dirty) LanternData.set(p, s); else LanternData.quiet(p, s);
    }

    private static boolean hasItem(Player p, net.minecraft.world.item.Item item) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) if (p.getInventory().getItem(i).is(item)) return true;
        return false;
    }

    public static void off(ServerPlayer p, LanternState s, String why) {
        boolean pick = s.on(LanternState.PICK);
        s.powers &= LanternState.SUIT;          // el traje se queda puesto
        if (pick) removeItem(p, ModRegistry.ENERGY_PICK.get().asItem());
        p.removeEffect(MobEffects.INVISIBILITY);
        applyFlight(p, s);
        msg(p, why);
    }

    private static void effects(ServerPlayer p, LanternState s) {
        if (s.on(LanternState.ARMOR)) {
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, true, false, true));
        }
        if (s.on(LanternState.SUIT)) {
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false));
        }
        if (s.on(LanternState.GHOST)) {
            p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, true, false, false));
            // los monstruos te pierden de vista
            for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(24), m -> m.getTarget() == p)) m.setTarget(null);
        }
    }

    /** Las armas de energía disparan solas a los monstruos cercanos. */
    private static void shoot(ServerPlayer p) {
        ServerLevel lv = p.serverLevel();
        LivingEntity target = null;
        double best = 24 * 24;
        for (Mob m : lv.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(24), m -> m instanceof Enemy && m.isAlive())) {
            double d = m.distanceToSqr(p);
            if (d < best && p.hasLineOfSight(m)) { best = d; target = m; }
        }
        if (target == null) return;
        int side = (p.tickCount / 8) % 2 == 0 ? 1 : -1;
        Vec3 look = p.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 right = new Vec3(-look.z, 0, look.x);
        Vec3 from = p.position().add(0, 1.9, 0).add(right.scale(0.8 * side));
        Vec3 dir = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(from).normalize();
        RayoVerde r = new RayoVerde(lv, p, false);
        r.setPos(from.x, from.y, from.z);
        r.shoot(dir.x, dir.y, dir.z, 2.4F, 0.5F);
        lv.addFreshEntity(r);
        lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.4F, 2.0F);
    }
}
