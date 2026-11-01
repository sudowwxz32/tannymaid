package com.tanny.tannymaid.yunn;

import com.tanny.tannymaid.cobblemon.CobblemonBridge;
import com.tanny.tannymaid.cobblemon.GardevoirCompanion;
import com.tanny.tannymaid.cobblemon.LucarioCompanion;
import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Los Pokémon de Yunn:
 * <ul>
 *   <li><b>Chansey</b>: protege a Yunn, la cura a ella y al Master (Amortiguador), y cada 2 minutos cura a los Pokémon del equipo.</li>
 *   <li><b>Cinderace</b> (hembra): protege al Master con Balón Ígneo y Doble Patada.</li>
 * </ul>
 */
public final class YunnPals {
    private final YunnEntity yunn;
    private final Pal chansey = new Pal("chansey", 50, "Chansey de Yunn", true, false);
    private final Pal cinderace = new Pal("cinderace", 55, "Cinderace de Yunn", true, true);
    private int chanseyHealCd = 200, partyHealCd = 2400, yunnHealCd;

    YunnPals(YunnEntity yunn) {
        this.yunn = yunn;
    }

    boolean toggleChansey() {
        chansey.enabled = !chansey.enabled;
        if (!chansey.enabled) chansey.recall();
        return chansey.enabled;
    }

    boolean toggleCinderace() {
        cinderace.enabled = !cinderace.enabled;
        if (!cinderace.enabled) cinderace.recall();
        return cinderace.enabled;
    }

    void recallAll() {
        chansey.recall();
        cinderace.recall();
    }

    void save(CompoundTag t) {
        t.putBoolean("ChanseyOn", chansey.enabled);
        t.putBoolean("CinderaceOn", cinderace.enabled);
    }

    void load(CompoundTag t) {
        chansey.enabled = !t.contains("ChanseyOn") || t.getBoolean("ChanseyOn");
        cinderace.enabled = !t.contains("CinderaceOn") || t.getBoolean("CinderaceOn");
    }

    void tick(@Nullable ServerPlayer owner) {
        if (!(yunn.level() instanceof ServerLevel level)) return;
        boolean ok = CobblemonBridge.isAvailable() && owner != null;
        chansey.tick(level, ok, yunn, owner);
        cinderace.tick(level, ok && !yunn.isWeakened(), owner != null && owner.level() == level ? owner : yunn, owner);
        if (!chansey.isOut() || owner == null) return;
        Mob c = chansey.mob;
        if (chanseyHealCd > 0) chanseyHealCd--;
        if (yunnHealCd > 0) yunnHealCd--;
        if (partyHealCd > 0) partyHealCd--;
        // Amortiguador: cura a Yunn (también cuando está debilitada)
        if (yunnHealCd == 0 && yunn.getHealth() < yunn.getMaxHealth() * 0.8F && c.distanceToSqr(yunn) < 8 * 8) {
            yunnHealCd = 160;
            yunn.heal(8.0F);
            level.sendParticles(ParticleTypes.HEART, yunn.getX(), yunn.getY() + 2, yunn.getZ(), 4, 0.3, 0.2, 0.3, 0);
            level.playSound(null, c.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.NEUTRAL, 1.0F, 1.3F);
        }
        // cura al Master
        if (chanseyHealCd == 0 && owner.level() == level && owner.getHealth() < owner.getMaxHealth() * 0.7F && c.distanceToSqr(owner) < 12 * 12) {
            chanseyHealCd = 240;
            owner.heal(5.0F);
            level.sendParticles(YunnEntity.MINT, owner.getX(), owner.getY() + 1, owner.getZ(), 14, 0.4, 0.6, 0.4, 0);
            level.sendParticles(ParticleTypes.HEART, owner.getX(), owner.getY() + 2, owner.getZ(), 3, 0.3, 0.2, 0.3, 0);
            owner.displayClientMessage(Component.literal("§d✚ Chansey te curó con Amortiguador ♡"), true);
        }
        // cada 2 minutos revisa y cura a los Pokémon del equipo
        if (partyHealCd == 0 && owner.level() == level && c.distanceToSqr(owner) < 16 * 16) {
            partyHealCd = 2400;
            if (CobblemonBridge.healParty(owner) > 0) {
                level.sendParticles(YunnEntity.MINT, owner.getX(), owner.getY() + 1, owner.getZ(), 20, 0.5, 0.7, 0.5, 0);
                owner.displayClientMessage(Component.literal("§d✚ Chansey curó a todos tus Pokémon ♡"), true);
            }
        }
    }

    /** Un Pokémon de Yunn que la sigue (o al Master), aparece en lugares seguros y pelea. */
    private static final class Pal {
        final String species, nickname;
        final int level;
        final boolean female, fighter;
        boolean enabled = true;
        @Nullable Mob mob;
        @Nullable LivingEntity target;
        int cooldown, attackCd, specialCd, repath, scan, stuck;

        Pal(String species, int level, String nickname, boolean female, boolean fighter) {
            this.species = species;
            this.level = level;
            this.nickname = nickname;
            this.female = female;
            this.fighter = fighter;
        }

        boolean isOut() {
            return mob != null && mob.isAlive() && !mob.isRemoved();
        }

        void recall() {
            if (mob == null) return;
            Mob m = mob;
            mob = null;
            target = null;
            LucarioCompanion.untrack(m.getUUID());
            if (m.level() instanceof ServerLevel l && m.isAlive()) {
                l.sendParticles(YunnEntity.PINK, m.getX(), m.getY() + 0.6, m.getZ(), 15, 0.3, 0.4, 0.3, 0);
            }
            m.discard();
        }

        void tick(ServerLevel level, boolean allowed, LivingEntity anchor, @Nullable ServerPlayer owner) {
            if (cooldown > 0) cooldown--;
            if (attackCd > 0) attackCd--;
            if (specialCd > 0) specialCd--;
            if (mob != null && (!mob.isAlive() || mob.isRemoved())) {
                boolean fainted = !mob.isAlive();
                LucarioCompanion.untrack(mob.getUUID());
                mob = null;
                target = null;
                if (fainted) {
                    cooldown = 1200;
                    if (owner != null) owner.sendSystemMessage(Component.literal("§d[Yunn] §f¡" + nickname.replace(" de Yunn", "")
                            + " se debilitó! La curaré y volverá en 60 s."));
                }
            }
            if (!enabled || !allowed || anchor.level() != level) {
                recall();
                return;
            }
            if (mob == null) {
                if (cooldown == 0 && level.getGameTime() % 20 == 0) summon(level, anchor);
                return;
            }
            if (mob.level() != level) {
                recall();
                return;
            }
            if (mob.isInWall() || mob.isInLava() || mob.getY() < level.getMinBuildHeight() + 1) teleport(level, anchor);

            if (fighter || anchor instanceof YunnEntity) {
                if (--scan <= 0) {
                    scan = 8;
                    pick(anchor, owner);
                }
            }
            if (target != null && (!target.isAlive() || target.level() != level || target.distanceToSqr(anchor) > 20 * 20)) target = null;
            if (target != null) {
                fight(level, target);
            } else {
                follow(level, anchor);
            }
        }

        private void summon(ServerLevel level, LivingEntity anchor) {
            Mob m = CobblemonBridge.createPokemon(level, species, this.level, nickname, female);
            if (m == null) {
                cooldown = 1200;
                return;
            }
            Vec3 spot = GardevoirCompanion.safeSpot(level, m, anchor);
            m.moveTo(spot.x, spot.y, spot.z, anchor.getYRot(), 0);
            m.addTag(LucarioCompanion.TAG);
            m.setPersistenceRequired();
            LucarioCompanion.track(m.getUUID());
            if (!level.addFreshEntity(m)) {
                LucarioCompanion.untrack(m.getUUID());
                cooldown = 200;
                return;
            }
            mob = m;
            level.sendParticles(YunnEntity.PINK, spot.x, spot.y + 0.8, spot.z, 25, 0.4, 0.6, 0.4, 0);
            level.playSound(null, m.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.2F);
        }

        private void teleport(ServerLevel level, LivingEntity anchor) {
            Vec3 spot = GardevoirCompanion.safeSpot(level, mob, anchor);
            mob.moveTo(spot.x, spot.y, spot.z, mob.getYRot(), mob.getXRot());
            mob.setDeltaMovement(Vec3.ZERO);
            mob.fallDistance = 0;
            mob.getNavigation().stop();
            stuck = 0;
            level.sendParticles(YunnEntity.PINK, spot.x, spot.y + 0.8, spot.z, 12, 0.3, 0.5, 0.3, 0);
        }

        private void follow(ServerLevel level, LivingEntity anchor) {
            double d = mob.distanceToSqr(anchor);
            stuck = d > 5 * 5 && mob.getDeltaMovement().horizontalDistanceSqr() < 0.0025 ? stuck + 1 : 0;
            if (d > 14 * 14 || stuck > 40) {
                teleport(level, anchor);
            } else if (d > 2.6 * 2.6) {
                if (--repath <= 0) {
                    repath = 6;
                    mob.getNavigation().moveTo(anchor, d > 7 * 7 ? 1.9D : 1.4D);
                }
            } else {
                mob.getNavigation().stop();
                mob.getLookControl().setLookAt(anchor, 30, 30);
            }
        }

        private void pick(LivingEntity anchor, @Nullable ServerPlayer owner) {
            if (target != null && target.isAlive()) return;
            target = null;
            LivingEntity a = anchor.getLastHurtByMob();
            if (a != null && ok(a)) {
                target = a;
                return;
            }
            if (!fighter) return;                              // Chansey solo devuelve los golpes que le dan a Yunn
            if (owner != null) {
                LivingEntity b = owner.getLastHurtByMob();
                if (b != null && owner.tickCount - owner.getLastHurtByMobTimestamp() < 100 && ok(b)) {
                    target = b;
                    return;
                }
            }
            List<Mob> near = anchor.level().getEntitiesOfClass(Mob.class, anchor.getBoundingBox().inflate(10, 5, 10),
                    m -> m instanceof Enemy && ok(m));
            double best = Double.MAX_VALUE;
            for (Mob m : near) {
                double s = m.distanceToSqr(anchor);
                if (s < best) {
                    best = s;
                    target = m;
                }
            }
        }

        private boolean ok(LivingEntity e) {
            return e.isAlive() && e != mob && !(e instanceof Player) && !(e instanceof TannyEntity) && !(e instanceof YunnEntity)
                    && !LucarioCompanion.isTannyPokemon(e) && !CobblemonBridge.isPokemonEntity(e);
        }

        private void fight(ServerLevel level, LivingEntity t) {
            mob.getLookControl().setLookAt(t, 30, 30);
            if (--repath <= 0) {
                repath = 5;
                mob.getNavigation().moveTo(t, 1.9D);
            }
            double d = mob.distanceToSqr(t);
            if (fighter) {
                // Cinderace: Balón Ígneo a distancia y Doble Patada de cerca
                if (specialCd == 0 && d < 14 * 14 && mob.hasLineOfSight(t)) {
                    specialCd = 50;
                    Vec3 a = mob.position().add(0, mob.getBbHeight() * 0.5, 0), b = t.position().add(0, t.getBbHeight() * 0.5, 0);
                    Vec3 delta = b.subtract(a);
                    int steps = Math.max(4, (int) (delta.length() * 3));
                    for (int i = 0; i <= steps; i++) {
                        Vec3 q = a.add(delta.scale(i / (double) steps));
                        level.sendParticles(ParticleTypes.FLAME, q.x, q.y, q.z, 1, 0.04, 0.04, 0.04, 0);
                    }
                    level.sendParticles(ParticleTypes.LAVA, b.x, b.y, b.z, 6, 0.3, 0.3, 0.3, 0);
                    level.playSound(null, mob.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 0.8F, 1.2F);
                    t.hurt(level.damageSources().mobAttack(mob), 11.0F);
                    t.setSecondsOnFire(4);
                } else if (attackCd == 0 && d < 3.0 * 3.0) {
                    attackCd = 18;
                    mob.swing(InteractionHand.MAIN_HAND);
                    t.hurt(level.damageSources().mobAttack(mob), 8.0F);
                    t.knockback(0.7, mob.getX() - t.getX(), mob.getZ() - t.getZ());
                    level.sendParticles(ParticleTypes.CRIT, t.getX(), t.getY(0.5), t.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
                    level.playSound(null, t.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            } else if (attackCd == 0 && d < 3.0 * 3.0) {
                // Chansey: Doble Bofetón para proteger a Yunn
                attackCd = 24;
                mob.swing(InteractionHand.MAIN_HAND);
                t.hurt(level.damageSources().mobAttack(mob), 5.0F);
                t.knockback(1.0, mob.getX() - t.getX(), mob.getZ() - t.getZ());
                level.playSound(null, t.blockPosition(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.NEUTRAL, 1.0F, 1.3F);
            }
        }
    }
}
