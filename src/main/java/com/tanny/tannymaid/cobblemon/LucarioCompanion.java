package com.tanny.tannymaid.cobblemon;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.util.TannySpeech;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * El único Pokémon de Tanny: su Lucario. No se guarda en el mundo; Tanny lo saca
 * cuando entra en combate (o cuando el Master lo pide con /tanny lucario) y lo
 * guarda cuando la pelea termina.
 */
public class LucarioCompanion {
    public static final String TAG = "tannymaid_lucario";
    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();

    private static final int LUCARIO_LEVEL = 50;
    private static final float MELEE_DAMAGE = 8.0F;
    private static final float AURA_DAMAGE = 7.0F;

    private final TannyEntity tanny;
    private Mob lucario;
    private boolean manual;
    private int idleTicks;
    private int cooldown;
    private int attackCooldown;
    private int auraCooldown;
    private int repath;

    public LucarioCompanion(TannyEntity tanny) {
        this.tanny = tanny;
    }

    public static boolean isTannyPokemon(Entity entity) {
        return entity != null && entity.getTags().contains(TAG);
    }

    public static boolean isActive(UUID id) {
        return ACTIVE.contains(id);
    }

    public boolean isOut() {
        return lucario != null && lucario.isAlive() && !lucario.isRemoved();
    }

    public int getCooldownSeconds() {
        return cooldown / 20;
    }

    public void tick() {
        if (!(tanny.level() instanceof ServerLevel level)) return;
        if (cooldown > 0) cooldown--;
        if (attackCooldown > 0) attackCooldown--;
        if (auraCooldown > 0) auraCooldown--;

        // Lucario desapareció sin que Tanny lo guardara: se debilitó.
        if (lucario != null && (!lucario.isAlive() || lucario.isRemoved())) {
            ACTIVE.remove(lucario.getUUID());
            lucario = null;
            manual = false;
            cooldown = 1200;
            if (!tanny.isWeakened()) {
                TannySpeech.say(tanny, tanny.getOwnerPlayer(), TannySpeech.LUCARIO_FAINT);
            }
        }

        LivingEntity target = tanny.getTarget();
        boolean combat = target != null && target.isAlive() && !tanny.isWeakened();

        if (!isOut()) {
            if ((combat || manual) && cooldown == 0 && !tanny.isWeakened()) {
                summon(level);
            }
            return;
        }

        if (tanny.isWeakened() || lucario.level() != tanny.level()) {
            recall(false);
            return;
        }

        if (combat) {
            idleTicks = 0;
            fight(level, target);
        } else {
            idleTicks++;
            followTanny();
            if (!manual && idleTicks > 300) {
                recall(true);
            }
        }
    }

    private void summon(ServerLevel level) {
        Mob mob = CobblemonBridge.createLucario(level, LUCARIO_LEVEL);
        if (mob == null) {
            if (manual) {
                TannySpeech.say(tanny, tanny.getOwnerPlayer(),
                        "Lucario no puede salir... parece que Cobblemon no está instalado, Master.");
            }
            manual = false;
            cooldown = 1200;
            return;
        }
        Vec3 look = tanny.getLookAngle();
        double x = tanny.getX() + look.x * 1.5;
        double z = tanny.getZ() + look.z * 1.5;
        mob.moveTo(x, tanny.getY(), z, tanny.getYRot(), 0.0F);
        mob.addTag(TAG);
        mob.setPersistenceRequired();
        ACTIVE.add(mob.getUUID());
        if (!level.addFreshEntity(mob)) {
            ACTIVE.remove(mob.getUUID());
            cooldown = 200;
            return;
        }
        lucario = mob;
        idleTicks = 0;
        level.sendParticles(ParticleTypes.END_ROD, x, tanny.getY() + 0.8, z, 25, 0.4, 0.6, 0.4, 0.05);
        level.playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL, 1.0F, 1.2F);
        TannySpeech.say(tanny, tanny.getOwnerPlayer(), TannySpeech.LUCARIO_OUT);
    }

    private void fight(ServerLevel level, LivingEntity target) {
        lucario.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (--repath <= 0) {
            repath = 5;
            lucario.getNavigation().moveTo(target, 1.4D);
        }
        double distSqr = lucario.distanceToSqr(target);
        double reach = lucario.getBbWidth() + target.getBbWidth() + 1.6D;

        if (distSqr <= reach * reach && attackCooldown == 0) {
            // Golpe cuerpo a cuerpo (Palmeo / Puño Drenaje)
            target.hurt(level.damageSources().mobAttack(lucario), MELEE_DAMAGE);
            target.knockback(0.6D, lucario.getX() - target.getX(), lucario.getZ() - target.getZ());
            lucario.swing(InteractionHand.MAIN_HAND);
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 1.0F, 1.0F);
            attackCooldown = 20;
        } else if (distSqr < 14 * 14 && auraCooldown == 0 && lucario.hasLineOfSight(target)) {
            auraSphere(level, target);
        }
    }

    private void auraSphere(ServerLevel level, LivingEntity target) {
        Vec3 from = lucario.position().add(0, lucario.getBbHeight() * 0.6, 0);
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 delta = to.subtract(from);
        int steps = Math.max(4, (int) (delta.length() * 3));
        for (int i = 0; i <= steps; i++) {
            Vec3 p = from.add(delta.scale(i / (double) steps));
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
        level.sendParticles(ParticleTypes.SONIC_BOOM, to.x, to.y, to.z, 1, 0, 0, 0, 0);
        level.playSound(null, lucario.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.NEUTRAL, 1.0F, 1.4F);
        target.hurt(level.damageSources().mobAttack(lucario), AURA_DAMAGE);
        auraCooldown = 60;
        if (tanny.getRandom().nextInt(3) == 0) {
            TannySpeech.say(tanny, tanny.getOwnerPlayer(), TannySpeech.LUCARIO_AURA);
        }
    }

    private void followTanny() {
        double distSqr = lucario.distanceToSqr(tanny);
        if (distSqr > 16 * 16) {
            lucario.moveTo(tanny.getX(), tanny.getY(), tanny.getZ(), lucario.getYRot(), lucario.getXRot());
            lucario.getNavigation().stop();
        } else if (distSqr > 3.5 * 3.5) {
            if (--repath <= 0) {
                repath = 10;
                lucario.getNavigation().moveTo(tanny, 1.2D);
            }
        }
    }

    public void recall(boolean speak) {
        if (lucario == null) return;
        Mob mob = lucario;
        lucario = null;
        manual = false;
        ACTIVE.remove(mob.getUUID());
        if (mob.level() instanceof ServerLevel level && mob.isAlive()) {
            level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + 0.8, mob.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
        }
        mob.discard();
        if (speak) {
            TannySpeech.say(tanny, tanny.getOwnerPlayer(), TannySpeech.LUCARIO_BACK);
        }
    }

    /** /tanny lucario: sacar o guardar a Lucario a petición del Master. */
    public void toggleManual(ServerPlayer player) {
        if (isOut()) {
            recall(true);
            return;
        }
        if (!CobblemonBridge.isAvailable()) {
            TannySpeech.say(tanny, player, "Lucario no puede salir... parece que Cobblemon no está instalado, Master.");
            return;
        }
        if (tanny.isWeakened()) {
            TannySpeech.say(tanny, player, "Perdón, Master... primero necesito recuperarme.");
            return;
        }
        if (cooldown > 0) {
            TannySpeech.say(tanny, player, "Lucario todavía está descansando (" + getCooldownSeconds() + " s). ¡Ya casi!");
            return;
        }
        manual = true;
        if (tanny.level() instanceof ServerLevel level) {
            summon(level);
        }
    }

    public String status() {
        if (!CobblemonBridge.isLoaded()) return "no disponible (Cobblemon no instalado)";
        if (isOut()) return "fuera, acompañando a Tanny";
        if (cooldown > 0) return "descansando (" + getCooldownSeconds() + " s)";
        return "en su Poké Ball, listo";
    }
}
