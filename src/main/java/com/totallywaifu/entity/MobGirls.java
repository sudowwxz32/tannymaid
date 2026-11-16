package com.totallywaifu.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/** Habilidades de las chicas transformadas desde animales y monstruos. */
final class MobGirls {
    private MobGirls() {}

    private static int timer(WaifuEntity w, String k) {
        return w.getPersistentData().getInt("TW_" + k);
    }

    private static void set(WaifuEntity w, String k, int v) {
        w.getPersistentData().putInt("TW_" + k, v);
    }

    /** Cuenta ticks y devuelve true cada 'every' ticks (persistente). */
    private static boolean every(WaifuEntity w, String k, int every) {
        int t = timer(w, k) + 1;
        if (t >= every) {
            set(w, k, 0);
            return true;
        }
        set(w, k, t);
        return false;
    }

    private static void give(WaifuEntity w, ItemStack st, String msg) {
        ItemStack rest = w.store(st);
        if (!rest.isEmpty()) w.spawnAtLocation(rest);
        w.cheer(40);
        Player p = w.ownerPlayer();
        if (p != null && msg != null && p.distanceToSqr(w) < 24 * 24) p.displayClientMessage(net.minecraft.network.chat.Component.literal(w.kind.color + w.kind.display + "§7: " + msg), true);
    }

    @Nullable
    private static Mob nearestEnemy(WaifuEntity w, double r) {
        List<Mob> l = w.level().getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(r, r * 0.5, r),
                m -> m instanceof Enemy && m.isAlive() && w.hasLineOfSight(m));
        return l.stream().min(Comparator.comparingDouble(w::distanceToSqr)).orElse(null);
    }

    /** El dueño, si está cerca. */
    @Nullable
    private static Player near(WaifuEntity w, double r) {
        Player p = w.ownerPlayer();
        return p != null && p.isAlive() && p.distanceToSqr(w) < r * r ? p : null;
    }

    private static void buff(Player p, net.minecraft.world.effect.MobEffect e, int dur, int amp) {
        MobEffectInstance cur = p.getEffect(e);
        if (cur == null || cur.getDuration() < dur / 2) p.addEffect(new MobEffectInstance(e, dur, amp, true, false, true));
    }

    @Nullable
    private static Mob target(WaifuEntity w, double r) {
        LivingEntity tg = w.getTarget();
        if (tg instanceof Mob m && m.isAlive() && w.distanceToSqr(m) < r * r && w.hasLineOfSight(m)) return m;
        return nearestEnemy(w, r);
    }

    private static void line(ServerLevel lv, Vec3 a, Vec3 b, net.minecraft.core.particles.ParticleOptions pt, int n) {
        for (int i = 0; i <= n; i++) {
            Vec3 q = a.lerp(b, i / (double) n);
            lv.sendParticles(pt, q.x, q.y, q.z, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    /** Explosión que solo daña a los monstruos (Cree, Dina y Mita). */
    private static boolean blast(WaifuEntity w, ServerLevel lv, double r, float dmg) {
        List<Mob> near = lv.getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(r), m -> m instanceof Enemy && m.isAlive());
        Player p = w.ownerPlayer();
        boolean danger = near.size() >= 2 || near.stream().anyMatch(m -> m.getTarget() == w || (p != null && m.getTarget() == p));
        if (near.isEmpty() || !danger) return false;
        lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, w.getX(), w.getY() + 1, w.getZ(), r > 5 ? 3 : 1, r * 0.2, 0.3, r * 0.2, 0);
        lv.sendParticles(ParticleTypes.HEART, w.getX(), w.getY() + 1, w.getZ(), 12, 2, 1, 2, 0);
        w.playSound(SoundEvents.GENERIC_EXPLODE, 2.0F, 1.0F);
        for (Mob m : near) {
            m.hurt(w.damageSources().explosion(w, w), dmg);
            Vec3 d = m.position().subtract(w.position()).normalize().scale(1.4);
            m.setDeltaMovement(d.x, 0.6, d.z);
            m.hurtMarked = true;
        }
        return true;
    }

    /** Golpe cuerpo a cuerpo con efecto extra. */
    static void onHit(WaifuEntity w, net.minecraft.world.entity.Entity t) {
        if (!(t instanceof LivingEntity le)) return;
        switch (w.kind) {
            case WITHER -> le.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            case ABEJA -> le.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            case SLIME -> { le.setDeltaMovement(le.getDeltaMovement().add(0, 0.8, 0)); w.playSound(SoundEvents.SLIME_JUMP, 1.0F, 1.2F); }
            case WARDEN -> le.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0));
            case ZORRO, PERRO -> le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
            case PANDA -> { Vec3 d = le.position().subtract(w.position()).normalize().scale(1.2); le.setDeltaMovement(d.x, 0.4, d.z); }
            default -> { }
        }
    }

    static void always(WaifuEntity w, WaifuWork work, long t) {
        ServerLevel lv = (ServerLevel) w.level();
        boolean active = w.getMode() != WaifuEntity.STAY;
        extra(w, work, t, lv, active);
        switch (w.kind) {
            case OVEJA -> {
                if (every(w, "wool", 4800)) give(w, new ItemStack(Items.WHITE_WOOL, 1 + w.getRandom().nextInt(2)), "te guardé lana ♥");
                if (timer(w, "sheared") > 0) set(w, "sheared", timer(w, "sheared") - 1);
            }
            case VACA -> {
                if (every(w, "leather", 6000)) give(w, new ItemStack(Items.LEATHER, 1 + w.getRandom().nextInt(2)), "te guardé cuero");
                if (timer(w, "milk") > 0) set(w, "milk", timer(w, "milk") - 1);
            }
            case CERDO -> {
                if (w.onGround() && every(w, "dig", 3600)) {
                    ItemStack[] finds = {new ItemStack(Items.CARROT, 2), new ItemStack(Items.POTATO, 2), new ItemStack(Items.BEETROOT, 2), new ItemStack(Items.BROWN_MUSHROOM)};
                    lv.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState()),
                            w.getX(), w.getY() + 0.1, w.getZ(), 20, 0.4, 0.1, 0.4, 0.1);
                    w.playSound(SoundEvents.ROOTED_DIRT_BREAK, 1.0F, 1.0F);
                    give(w, finds[w.getRandom().nextInt(finds.length)].copy(), "¡encontré algo enterrado!");
                }
            }
            case GALLINA -> {
                if (every(w, "egg", 3000)) {
                    w.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 1.2F);
                    give(w, new ItemStack(Items.EGG), "¡puse un huevo!");
                }
                if (every(w, "feather", 4000)) give(w, new ItemStack(Items.FEATHER), null);
            }
            case GATO -> {
                if (t % 10 == 0) {
                    for (Creeper c : lv.getEntitiesOfClass(Creeper.class, w.getBoundingBox().inflate(7))) {
                        c.setSwellDir(-1);                               // los creepers le tienen miedo
                        Vec3 away = c.position().subtract(w.position()).normalize().scale(0.6);
                        c.setDeltaMovement(away.x, 0.2, away.z);
                        c.hurtMarked = true;
                        c.getNavigation().moveTo(c.getX() + away.x * 12, c.getY(), c.getZ() + away.z * 12, 1.3);
                    }
                    for (Phantom ph : lv.getEntitiesOfClass(Phantom.class, w.getBoundingBox().inflate(16))) ph.setTarget(null);
                }
                long day = lv.getDayTime() % 24000L;
                if (day > 0 && day < 40 && timer(w, "gift") == 0) {
                    ItemStack[] gifts = {new ItemStack(Items.STRING, 2), new ItemStack(Items.FEATHER, 2), new ItemStack(Items.RABBIT_HIDE), new ItemStack(Items.RABBIT_FOOT),
                            new ItemStack(Items.PHANTOM_MEMBRANE)};
                    give(w, gifts[w.getRandom().nextInt(gifts.length)].copy(), "¡buenos días! te traje un regalo ♥");
                    set(w, "gift", 1);
                } else if (day >= 40 && day < 100) set(w, "gift", 0);
            }
            case GOLEM -> {
                if (every(w, "poppy", 9000)) give(w, new ItemStack(Items.POPPY), "una flor para ti ♥");
            }
            case ESQUELETO -> {
                if (!active || work.cooldown() > 0) return;
                Mob m = nearestEnemy(w, 16);
                if (m == null) return;
                w.getLookControl().setLookAt(m, 30, 30);
                Arrow a = new Arrow(lv, w);
                double dx = m.getX() - w.getX(), dy = m.getY(0.33) - a.getY(), dz = m.getZ() - w.getZ();
                a.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.18, dz, 1.8F, 2.0F);
                a.setBaseDamage(4.0);
                a.pickup = AbstractArrow.Pickup.DISALLOWED;
                lv.addFreshEntity(a);
                if (w.getRandom().nextInt(3) == 0) {                     // lluvia de flechas
                    for (int i = -1; i <= 1; i += 2) {
                        Arrow b = new Arrow(lv, w);
                        b.shoot(dx + i * 1.2, dy + Math.sqrt(dx * dx + dz * dz) * 0.18, dz - i * 1.2, 1.7F, 3.0F);
                        b.setBaseDamage(3.0);
                        b.pickup = AbstractArrow.Pickup.DISALLOWED;
                        lv.addFreshEntity(b);
                    }
                }
                w.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                w.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.2F);
                work.cooldown(25);
            }
            case CREEPER -> {
                if (!active || work.cooldown() > 0 || t % 5 != 0) return;
                if (blast(w, lv, 4.5, 14.0F)) work.cooldown(160);     // ¡explosión controlada!
            }
            case ARANA -> {
                if (!active || t % 60 != 0) return;
                for (Mob m : lv.getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(10, 4, 10), m -> m instanceof Enemy && m.isAlive())) {
                    m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                    lv.sendParticles(ParticleTypes.ITEM_SNOWBALL, m.getX(), m.getY() + 0.5, m.getZ(), 10, 0.3, 0.4, 0.3, 0);
                }
            }
            case BRUJA -> {
                Player p = w.ownerPlayer();
                if (p != null && p.distanceToSqr(w) < 12 * 12 && p.getHealth() < p.getMaxHealth() * 0.5F && timer(w, "heal") <= 0) {
                    p.heal(6.0F);
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
                    lv.sendParticles(ParticleTypes.HEART, p.getX(), p.getY() + 1.8, p.getZ(), 6, 0.3, 0.3, 0.3, 0);
                    w.playSound(SoundEvents.WITCH_DRINK, 1.0F, 1.3F);
                    w.say(p, "¡Toma esto, te sentirás mejor!");
                    set(w, "heal", 300);
                }
                if (timer(w, "heal") > 0) set(w, "heal", timer(w, "heal") - 1);
                if (!active || work.cooldown() > 0) return;
                Mob m = nearestEnemy(w, 10);
                if (m == null) return;
                w.getLookControl().setLookAt(m, 30, 30);
                Vec3 a = w.position().add(0, 1.6, 0), b = m.position().add(0, m.getBbHeight() * 0.6, 0);
                for (int i = 0; i <= 12; i++) {
                    Vec3 q = a.lerp(b, i / 12.0);
                    lv.sendParticles(ParticleTypes.WITCH, q.x, q.y, q.z, 2, 0.05, 0.05, 0.05, 0);
                }
                m.hurt(w.damageSources().indirectMagic(w, w), 7.0F);
                if (!m.isInvertedHealAndHarm()) m.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
                else m.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                w.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                w.playSound(SoundEvents.WITCH_THROW, 1.0F, 1.0F);
                work.cooldown(50);
            }
            case ENDERMAN -> {
                LivingEntity tg = w.getTarget();
                if (tg != null && tg.isAlive() && w.distanceToSqr(tg) > 9 && work.cooldown() == 0) {
                    Vec3 behind = tg.position().add(tg.getLookAngle().scale(-1.5));
                    lv.sendParticles(ParticleTypes.PORTAL, w.getX(), w.getY() + 1, w.getZ(), 30, 0.3, 0.8, 0.3, 0.3);
                    w.teleportTo(behind.x, tg.getY(), behind.z);
                    w.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.2F);
                    lv.sendParticles(ParticleTypes.PORTAL, w.getX(), w.getY() + 1, w.getZ(), 30, 0.3, 0.8, 0.3, 0.3);
                    work.cooldown(40);
                }
            }
            case BLAZE -> {
                if (!active || work.cooldown() > 0) return;
                Mob m = target(w, 16);
                if (m == null) return;
                w.getLookControl().setLookAt(m, 30, 30);
                WaifuShot.fire(w, m, Items.FIRE_CHARGE, 6.0F, 1.6F, true, 1.4F);
                w.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.2F);
                w.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                work.cooldown(w.getRandom().nextInt(3) == 0 ? 8 : 28);    // a veces ráfaga
            }
            case GHAST -> {
                if (!active || work.cooldown() > 0) return;
                Mob m = target(w, 22);
                if (m == null) return;
                w.getLookControl().setLookAt(m, 30, 30);
                WaifuShot.fire(w, m, Items.FIRE_CHARGE, 13.0F, 3.2F, true, 1.1F);
                w.playSound(SoundEvents.GHAST_SHOOT, 1.0F, 1.3F);
                w.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                work.cooldown(70);
            }
            case DINA, MITA -> {
                if (!active) return;
                if (timer(w, "blast") > 0) set(w, "blast", timer(w, "blast") - 1);
                else if (t % 5 == 0 && blast(w, lv, w.kind == WaifuKind.DINA ? 6.0 : 5.5, w.kind == WaifuKind.DINA ? 24.0F : 21.0F)) {
                    set(w, "blast", 140);                          // ¡más fuerte que Cree!
                    return;
                }
                if (work.cooldown() > 0) return;
                Mob m = target(w, 15);
                if (m == null || w.distanceToSqr(m) < 9) return;
                w.getLookControl().setLookAt(m, 30, 30);
                WaifuShot.fire(w, m, Items.TNT, w.kind == WaifuKind.DINA ? 14.0F : 11.0F, 3.4F, false, 0.9F);
                w.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.4F);
                w.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                work.cooldown(w.kind == WaifuKind.DINA ? 45 : 28);
            }
            case WARDEN -> {
                if (!active) return;
                if (t % 100 == 0) {
                    for (Mob m : lv.getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(24), m -> m instanceof Enemy && m.isAlive())) {
                        m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));      // los siente
                        if (m.distanceToSqr(w) < 12 * 12) m.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120, 0));
                    }
                }
                if (work.cooldown() > 0) return;
                Mob m = target(w, 16);
                if (m == null || w.distanceToSqr(m) < 4) return;
                w.getLookControl().setLookAt(m, 30, 30);
                line(lv, w.position().add(0, w.getBbHeight() * 0.6, 0), m.position().add(0, m.getBbHeight() * 0.5, 0), ParticleTypes.SONIC_BOOM, 10);
                w.playSound(SoundEvents.WARDEN_SONIC_BOOM, 2.0F, 1.0F);
                m.hurt(w.damageSources().sonicBoom(w), 22.0F);
                Vec3 d = m.position().subtract(w.position()).normalize();
                m.setDeltaMovement(d.x * 2.0, 0.6, d.z * 2.0);
                m.hurtMarked = true;
                work.cooldown(60);
            }
            case ZORRO -> {
                LivingEntity tg = w.getTarget();
                if (tg != null && tg.isAlive() && w.onGround() && work.cooldown() == 0 && w.distanceToSqr(tg) > 9 && w.distanceToSqr(tg) < 64) {
                    Vec3 d = tg.position().subtract(w.position()).normalize();
                    w.setDeltaMovement(d.x * 1.1, 0.6, d.z * 1.1);                    // ¡salto de zorro!
                    w.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.3F);
                    work.cooldown(40);
                }
                if (every(w, "berry", 3000)) give(w, new ItemStack(w.getRandom().nextBoolean() ? Items.SWEET_BERRIES : Items.GLOW_BERRIES, 3), "¡bayas para ti!");
            }
            case RANA -> {
                if (!active || work.cooldown() > 0 || t % 10 != 0) return;
                Mob m = lv.getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(7), q -> q.isAlive() && q instanceof Enemy && q.getBbHeight() < 1.0F && w.hasLineOfSight(q))
                        .stream().min(Comparator.comparingDouble(w::distanceToSqr)).orElse(null);
                if (m == null) return;
                line(lv, w.position().add(0, 1.4, 0), m.position().add(0, 0.3, 0), new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.95F, 0.4F, 0.5F), 1.0F), 10);
                w.playSound(SoundEvents.FROG_TONGUE, 1.0F, 1.0F);
                boolean magma = m instanceof net.minecraft.world.entity.monster.MagmaCube;
                boolean slime = m instanceof net.minecraft.world.entity.monster.Slime;
                m.hurt(w.damageSources().mobAttack(w), 40.0F);
                if (slime) give(w, new ItemStack(magma ? Items.PEARLESCENT_FROGLIGHT : Items.SLIME_BALL, magma ? 1 : 2), magma ? "¡una luz de rana!" : null);
                work.cooldown(30);
            }
            default -> { }
        }
    }

    /** Habilidades "pasivas" de cada una (además de las de arriba). */
    private static void extra(WaifuEntity w, WaifuWork work, long t, ServerLevel lv, boolean active) {
        Player p = near(w, 12);
        switch (w.kind) {
            case OVEJA -> {
                if (p != null && t % 200 == 0) { buff(p, MobEffects.REGENERATION, 120, 0); p.setTicksFrozen(0); }
            }
            case VACA -> {
                if (p != null && t % 100 == 0) {
                    for (MobEffectInstance e : new java.util.ArrayList<>(p.getActiveEffects()))
                        if (e.getEffect().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) p.removeEffect(e.getEffect());
                }
            }
            case CERDO -> {
                if (w.onGround() && every(w, "truffle", 9000)) give(w, new ItemStack(w.getRandom().nextInt(4) == 0 ? Items.EMERALD : Items.GOLDEN_CARROT), "¡olfateé un tesoro!");
            }
            case GALLINA -> {
                if (p != null && p.fallDistance > 4 && t % 5 == 0) buff(p, MobEffects.SLOW_FALLING, 60, 0);
            }
            case PERRO -> {
                if (p != null && t % 80 == 0 && w.getTarget() != null) buff(p, MobEffects.DAMAGE_BOOST, 160, 0);
                if (p != null && t % 100 == 0 && p.getFoodData().getFoodLevel() < 14) {
                    for (int i = 0; i < w.bag.getContainerSize(); i++) {
                        ItemStack s = w.bag.getItem(i);
                        if (s.isEdible() && !s.is(Items.ROTTEN_FLESH)) {
                            ItemStack food = s.split(Math.min(3, s.getCount()));
                            if (!p.getInventory().add(food)) p.drop(food, false);
                            w.say(p, "¡Te traje comida! Come, que te veo cansado ♥");
                            w.cheer(40);
                            break;
                        }
                    }
                }
            }
            case GATO -> {
                if (p != null && t % 200 == 0 && !lv.isDay()) buff(p, MobEffects.NIGHT_VISION, 500, 0);
            }
            case GOLEM -> {
                if (p != null && t % 100 == 0) buff(p, MobEffects.DAMAGE_RESISTANCE, 160, 0);
            }
            case CABALLO -> {
                if (held.isEmpty() && !w.isVehicle() && !p.isPassenger()) {
                    w.getNavigation().stop();
                    p.startRiding(w);
                    w.say(p, "¡Sube! ¡Agárrate fuerte! ♥");
                    w.cheer(60);
                    return InteractionResult.CONSUME;
                }
            }
            case PIGLIN -> {
                if (held.is(Items.GOLD_INGOT)) {
                    if (!p.getAbilities().instabuild) held.shrink(1);
                    ServerLevel lv = (ServerLevel) w.level();
                    net.minecraft.world.level.storage.loot.LootTable lt = lv.getServer().getLootData().getLootTable(net.minecraft.world.level.storage.loot.BuiltInLootTables.PIGLIN_BARTERING);
                    List<ItemStack> got = lt.getRandomItems(new net.minecraft.world.level.storage.loot.LootParams.Builder(lv)
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, w)
                            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.PIGLIN_BARTER));
                    for (ItemStack g : got) if (!p.getInventory().add(g)) p.drop(g, false);
                    w.playSound(SoundEvents.PIGLIN_ADMIRING_ITEM, 1.0F, 1.3F);
                    w.cheer(60);
                    w.say(p, "¡Oro! ♥ Toma, un trato justo.");
                    return InteractionResult.CONSUME;
                }
            }
            case ALLAY -> {
                if (!held.isEmpty() && !held.is(Items.POPPY)) {
                    w.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, held.copyWithCount(1));
                    w.say(p, "¡Entendido! Buscaré todos los " + held.getHoverName().getString() + " que encuentre ♪");
                    w.playSound(SoundEvents.ALLAY_ITEM_GIVEN, 1.0F, 1.0F);
                    return InteractionResult.CONSUME;
                }
            }
            case PANDA -> {
                if (held.is(Items.BAMBOO) && w.getHealth() < w.getMaxHealth()) {
                    if (!p.getAbilities().instabuild) held.shrink(1);
                    w.heal(15.0F);
                    w.playSound(SoundEvents.PANDA_EAT, 1.0F, 1.2F);
                    w.cheer(60);
                    return InteractionResult.CONSUME;
                }
            }
            case ALDEANA -> {
                if (p != null && t % 600 == 0) { buff(p, MobEffects.HERO_OF_THE_VILLAGE, 1400, 0); buff(p, MobEffects.LUCK, 1400, 0); }
            }
            case ZOMBIE -> {
                if (t % 20 == 0) for (net.minecraft.world.entity.monster.Zombie z : lv.getEntitiesOfClass(net.minecraft.world.entity.monster.Zombie.class, w.getBoundingBox().inflate(16)))
                    if (z.getTarget() == w) z.setTarget(null);
            }
            case ESQUELETO -> {
                if (every(w, "quiver", 6000)) { give(w, new ItemStack(Items.ARROW, 8), "te guardé flechas"); give(w, new ItemStack(Items.BONE, 2), null); }
            }
            case CREEPER -> {
                if (every(w, "powder", 6000)) give(w, new ItemStack(Items.GUNPOWDER, 2), "¡pólvora para ti!");
            }
            case ARANA -> {
                if (every(w, "string", 4000)) give(w, new ItemStack(Items.STRING, 3), "te tejí hilo ♥");
            }
            case BRUJA -> {
                if (p != null && active && every(w, "brew", 2400)) {
                    buff(p, w.getRandom().nextBoolean() ? MobEffects.MOVEMENT_SPEED : MobEffects.DAMAGE_BOOST, 1200, 0);
                    w.playSound(SoundEvents.WITCH_DRINK, 1.0F, 1.2F);
                    w.say(p, "¡Una pócima para ti! ✦");
                }
            }
            case ENDERMAN -> {
                if (every(w, "pearl", 6000)) give(w, new ItemStack(Items.ENDER_PEARL), "una perla de ender ♥");
            }
            case SLIME -> {
                if (every(w, "slime", 4000)) give(w, new ItemStack(Items.SLIME_BALL, 2), "¡bolitas de slime!");
                if (p != null && t % 100 == 0) buff(p, MobEffects.JUMP, 160, 0);
            }
            case CONEJO -> {
                if (p != null && active && t % 100 == 0) { buff(p, MobEffects.MOVEMENT_SPEED, 160, 0); buff(p, MobEffects.JUMP, 160, 1); }
                if (every(w, "carrot", 5000)) give(w, new ItemStack(w.getRandom().nextInt(6) == 0 ? Items.RABBIT_FOOT : Items.CARROT, w.getRandom().nextInt(6) == 0 ? 1 : 3), "¡encontré algo rico!");
            }
            case PANDA -> {
                if (every(w, "bamboo", 4000)) give(w, new ItemStack(w.getRandom().nextBoolean() ? Items.BAMBOO : Items.SUGAR_CANE, 4), "¡bambú fresquito!");
                if (every(w, "sneeze", 7000)) {
                    w.playSound(SoundEvents.PANDA_SNEEZE, 1.0F, 1.2F);
                    lv.sendParticles(ParticleTypes.SNEEZE, w.getX(), w.getEyeY(), w.getZ(), 10, 0.2, 0.1, 0.2, 0.05);
                    ItemStack[] g = {new ItemStack(Items.SLIME_BALL), new ItemStack(Items.COCOA_BEANS, 2), new ItemStack(Items.APPLE), new ItemStack(Items.DIAMOND)};
                    give(w, g[w.getRandom().nextInt(w.getRandom().nextInt(20) == 0 ? 4 : 3)].copy(), "¡achú! ...¿y esto?");
                }
            }
            case CABALLO -> {
                if (p != null && active && t % 100 == 0 && !p.isPassenger()) buff(p, MobEffects.MOVEMENT_SPEED, 160, 0);
            }
            case ABEJA -> {
                if (active && t % 200 == 0) {                              // poliniza: los cultivos cercanos crecen
                    for (int i = 0; i < 6; i++) {
                        net.minecraft.core.BlockPos q = w.blockPosition().offset(w.getRandom().nextInt(13) - 6, w.getRandom().nextInt(5) - 2, w.getRandom().nextInt(13) - 6);
                        net.minecraft.world.level.block.state.BlockState st = lv.getBlockState(q);
                        if (st.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && !crop.isMaxAge(st)) {
                            crop.growCrops(lv, q, st);
                            lv.sendParticles(ParticleTypes.FALLING_NECTAR, q.getX() + 0.5, q.getY() + 0.8, q.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0);
                        }
                    }
                }
                if (every(w, "honey", 6000)) give(w, new ItemStack(Items.HONEY_BOTTLE), "¡miel para ti! ♥");
                if (every(w, "comb", 4500)) give(w, new ItemStack(Items.HONEYCOMB, 2), null);
            }
            case PIGLIN -> {
                if (every(w, "gold", 8000)) give(w, new ItemStack(Items.GOLD_NUGGET, 6), "¡oro! ♥");
            }
            case WITHER -> {
                if (every(w, "coal", 5000)) give(w, new ItemStack(Items.COAL, 4), "te traje carbón");
                if (every(w, "bone", 4000)) give(w, new ItemStack(Items.BONE, 2), null);
            }
            case BLAZE -> {
                if (p != null && t % 100 == 0) buff(p, MobEffects.FIRE_RESISTANCE, 200, 0);
                if (every(w, "rod", 6000)) give(w, new ItemStack(Items.BLAZE_ROD), "¡una vara de blaze!");
            }
            case GHAST -> {
                if (p != null && p.fallDistance > 4 && t % 5 == 0) buff(p, MobEffects.SLOW_FALLING, 80, 0);
                if (every(w, "tear", 8000)) give(w, new ItemStack(Items.GHAST_TEAR), "...una lágrima. Es para ti.");
                if (every(w, "gp", 5000)) give(w, new ItemStack(Items.GUNPOWDER, 2), null);
            }
            case DINA, MITA -> {
                if (every(w, "tntpowder", 5000)) give(w, new ItemStack(Items.GUNPOWDER, 3), "¡más pólvora! ¡BOOM! ♥");
            }
            case WARDEN -> {
                if (p != null && t % 100 == 0) buff(p, MobEffects.DAMAGE_RESISTANCE, 160, 1);
                if (every(w, "echo", 12000)) give(w, new ItemStack(Items.ECHO_SHARD), "...un eco. Te protegeré siempre.");
            }
            case ZORRO -> {
                if (t % 10 == 0) magnet(w, lv, 8, null);
            }
            case AJOLOTE -> {
                if (p != null && p.isInWater() && t % 60 == 0) { buff(p, MobEffects.WATER_BREATHING, 300, 0); buff(p, MobEffects.REGENERATION, 120, 0); buff(p, MobEffects.DOLPHINS_GRACE, 120, 0); }
                if (w.isInWater() && w.getHealth() < w.getMaxHealth() && t % 10 == 0) w.heal(1.0F);
            }
            case ALLAY -> {
                if (t % 5 == 0) magnet(w, lv, 16, w.getMainHandItem().isEmpty() ? null : w.getMainHandItem());
                if (p != null && t % 400 == 0 && p.getHealth() < p.getMaxHealth()) {
                    p.heal(4.0F);
                    w.playSound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 1.0F, 1.0F);
                    lv.sendParticles(ParticleTypes.NOTE, p.getX(), p.getY() + 2.1, p.getZ(), 3, 0.4, 0.1, 0.4, 1);
                }
            }
            case RANA -> {
                if (p != null && active && t % 100 == 0) buff(p, MobEffects.JUMP, 160, 1);
            }
            default -> { }
        }
    }

    /** Atrae objetos tirados hacia ella (Lumi: solo los iguales al que tiene en la mano). */
    private static void magnet(WaifuEntity w, ServerLevel lv, double r, @Nullable ItemStack like) {
        for (net.minecraft.world.entity.item.ItemEntity ie : lv.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, w.getBoundingBox().inflate(r, 4, r),
                ie -> ie.isAlive() && !ie.hasPickUpDelay() && (like == null || ie.getItem().is(like.getItem())))) {
            Vec3 d = w.position().add(0, 0.5, 0).subtract(ie.position()).normalize().scale(0.35);
            ie.setDeltaMovement(d);
            ie.hurtMarked = true;
        }
    }

    @Nullable
    static InteractionResult interact(WaifuEntity w, ServerPlayer p, ItemStack held) {
        switch (w.kind) {
            case OVEJA -> {
                if (held.is(Items.SHEARS)) {
                    if (timer(w, "sheared") > 0) {
                        w.say(p, "Mi lana todavía está creciendo...");
                    } else {
                        w.spawnAtLocation(new ItemStack(Items.WHITE_WOOL, 1 + w.getRandom().nextInt(3)));
                        w.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.2F);
                        held.hurtAndBreak(1, p, pl -> {});
                        set(w, "sheared", 2400);
                        w.cheer(40);
                    }
                    return InteractionResult.CONSUME;
                }
            }
            case VACA -> {
                if (held.is(Items.BUCKET)) {
                    if (timer(w, "milk") > 0) {
                        w.say(p, "Dame un ratito...");
                    } else {
                        held.shrink(1);
                        ItemStack milk = new ItemStack(Items.MILK_BUCKET);
                        if (!p.getInventory().add(milk)) p.drop(milk, false);
                        w.playSound(SoundEvents.COW_MILK, 1.0F, 1.2F);
                        set(w, "milk", 600);
                        w.cheer(40);
                    }
                    return InteractionResult.CONSUME;
                }
            }
            case GOLEM -> {
                if (held.is(Items.IRON_INGOT) && w.getHealth() < w.getMaxHealth()) {
                    if (!p.getAbilities().instabuild) held.shrink(1);
                    w.heal(25.0F);
                    w.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, 1.2F);
                    w.cheer(40);
                    return InteractionResult.CONSUME;
                }
            }
            case ALDEANA -> {
                if (w.getTradingPlayer() == null && !w.getOffers().isEmpty()) {
                    w.getNavigation().stop();
                    w.setTradingPlayer(p);
                    w.openTradingScreen(p, net.minecraft.network.chat.Component.literal(w.kind.display + " · Aldeana"), 1);
                    return InteractionResult.CONSUME;
                }
            }
            default -> { }
        }
        return null;
    }
}
