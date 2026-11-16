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

    static void always(WaifuEntity w, WaifuWork work, long t) {
        ServerLevel lv = (ServerLevel) w.level();
        boolean active = w.getMode() != WaifuEntity.STAY;
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
                w.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                w.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.2F);
                work.cooldown(25);
            }
            case CREEPER -> {
                if (!active || work.cooldown() > 0 || t % 5 != 0) return;
                List<Mob> near = lv.getEntitiesOfClass(Mob.class, w.getBoundingBox().inflate(4.5), m -> m instanceof Enemy && m.isAlive());
                Player p = w.ownerPlayer();
                boolean danger = near.size() >= 2 || near.stream().anyMatch(m -> m.getTarget() == w || (p != null && m.getTarget() == p));
                if (near.isEmpty() || !danger) return;
                // ¡explosión controlada! daña a los monstruos, no rompe bloques ni lastima a los tuyos
                lv.sendParticles(ParticleTypes.EXPLOSION_EMITTER, w.getX(), w.getY() + 1, w.getZ(), 1, 0, 0, 0, 0);
                lv.sendParticles(ParticleTypes.HAPPY_VILLAGER, w.getX(), w.getY() + 1, w.getZ(), 30, 2, 1, 2, 0);
                w.playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 1.2F);
                for (Mob m : near) {
                    m.hurt(w.damageSources().explosion(w, w), 14.0F);
                    Vec3 d = m.position().subtract(w.position()).normalize().scale(1.2);
                    m.setDeltaMovement(d.x, 0.5, d.z);
                    m.hurtMarked = true;
                }
                work.cooldown(160);
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
            default -> { }
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
