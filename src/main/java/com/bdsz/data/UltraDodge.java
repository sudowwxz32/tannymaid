package com.bdsz.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Ultra Instinto: el cuerpo se mueve solo. Esquiva golpes, proyectiles y explosiones sin que el jugador haga nada:
 * se teletransporta al cielo, a otro punto del suelo o detrás del atacante (y a veces contraataca).
 */
public final class UltraDodge {
    private UltraDodge() {}

    private static final String LAST = "bdsz_ui_dodge";
    private static final DustParticleOptions SILVER = new DustParticleOptions(new Vector3f(0.9F, 0.94F, 1.0F), 1.4F);

    private static boolean active(SaiyanState s) {
        return s.saiyan && s.transformed && !s.ozaru && s.activeForm().ultraInstinct();
    }

    private static boolean ready(ServerPlayer p, int gap) {
        long now = p.level().getGameTime();
        if (now - p.getPersistentData().getLong(LAST) < gap) return false;
        p.getPersistentData().putLong(LAST, now);
        return true;
    }

    /** Llamado al recibir un ataque: devuelve true si lo esquivó (y hay que cancelar el daño). */
    public static boolean onAttack(ServerPlayer p, DamageSource src) {
        SaiyanState s = SaiyanData.get(p);
        if (!active(s) || src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        boolean explosion = src.is(DamageTypeTags.IS_EXPLOSION);
        boolean projectile = src.getDirectEntity() instanceof Projectile;
        if (src.getEntity() == null && !explosion && !projectile) return false;     // caídas, fuego, hambre... no se esquivan
        Form f = s.activeForm();
        if (p.getRandom().nextFloat() >= f.dodgeChance()) return false;
        if (f != Form.ULTRA && ready(p, 6)) {
            Entity atk = src.getEntity();
            if (explosion) teleport(p, far(p, 8, 13), false);
            else if (atk instanceof LivingEntity le && !projectile && p.getRandom().nextFloat() < 0.35F) behind(p, le, f);
            else if (p.getRandom().nextBoolean()) teleport(p, sky(p), true);
            else teleport(p, far(p, 3, 7), false);
        } else {
            afterImage(p);
        }
        PowerLogic.msg(p, "§f§oEl cuerpo se movió solo...");
        return true;
    }

    /** Cada tick: ve venir proyectiles, TNT y creepers y se aparta antes del impacto. */
    public static void tick(ServerPlayer p) {
        SaiyanState s = SaiyanData.get(p);
        if (!active(s) || s.activeForm() == Form.ULTRA) return;
        ServerLevel lv = p.serverLevel();
        Vec3 me = p.position().add(0, p.getBbHeight() / 2, 0);
        // auras plateadas/blancas que dejan estela en el Perfecto y el Propio
        if (p.tickCount % 3 == 0 && s.activeForm() != Form.UI_SIGN && p.getDeltaMovement().horizontalDistanceSqr() > 0.01) {
            lv.sendParticles(SILVER, p.getX(), p.getY() + 1, p.getZ(), 1, 0.2, 0.4, 0.2, 0);
        }
        for (Entity e : lv.getEntities(p, p.getBoundingBox().inflate(8), x -> x instanceof Projectile || x instanceof PrimedTnt || x instanceof Creeper)) {
            if (e instanceof Projectile pr) {
                if (pr.getOwner() == p) continue;
                Vec3 v = pr.getDeltaMovement();
                if (v.lengthSqr() < 0.04) continue;
                Vec3 to = me.subtract(pr.position());
                double t = to.dot(v) / v.lengthSqr();
                if (t < 0 || t > 8) continue;                              // se aleja o tarda mucho
                if (pr.position().add(v.scale(t)).distanceToSqr(me) > 1.6 * 1.6) continue;  // no te va a dar
                if (!ready(p, 8)) return;
                Vec3 side = new Vec3(-v.z, 0, v.x).normalize().scale(p.getRandom().nextBoolean() ? 3.5 : -3.5);
                BlockPos spot = safeNear(p, p.blockPosition().offset((int) Math.round(side.x), 0, (int) Math.round(side.z)));
                teleport(p, spot != null ? Vec3.atBottomCenterOf(spot) : sky(p), spot == null);
                return;
            }
            boolean boom = (e instanceof PrimedTnt tnt && tnt.getFuse() < 20 && e.distanceToSqr(p) < 6 * 6)
                    || (e instanceof Creeper c && c.getSwelling(1.0F) > 0.5F && e.distanceToSqr(p) < 5 * 5);
            if (boom && ready(p, 10)) {
                teleport(p, far(p, 8, 12), false);
                return;
            }
        }
    }

    // ------------------------------------------------------------------ movimientos

    private static void behind(ServerPlayer p, LivingEntity atk, Form f) {
        Vec3 look = atk.getLookAngle().multiply(1, 0, 1).normalize();
        BlockPos spot = safeNear(p, BlockPos.containing(atk.position().subtract(look.scale(1.8))));
        if (spot == null) { teleport(p, far(p, 3, 6), false); return; }
        teleport(p, Vec3.atBottomCenterOf(spot), false);
        Vec3 d = atk.position().subtract(p.position());
        float yaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
        p.connection.teleport(p.getX(), p.getY(), p.getZ(), yaw, p.getXRot());
        // contraataque automático
        atk.hurt(p.damageSources().playerAttack(p), 4.0F);
        p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        p.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM, atk.getX(), atk.getY(0.5), atk.getZ(), 1, 0, 0, 0, 0);
    }

    private static Vec3 sky(ServerPlayer p) {
        for (int up = 9; up >= 4; up--) {
            BlockPos b = p.blockPosition().above(up);
            if (p.level().isEmptyBlock(b) && p.level().isEmptyBlock(b.above())) return Vec3.atBottomCenterOf(b);
        }
        return p.position();
    }

    private static Vec3 far(ServerPlayer p, int min, int max) {
        for (int i = 0; i < 16; i++) {
            double a = p.getRandom().nextDouble() * Math.PI * 2, r = min + p.getRandom().nextDouble() * (max - min);
            BlockPos spot = safeNear(p, BlockPos.containing(p.getX() + Math.cos(a) * r, p.getY(), p.getZ() + Math.sin(a) * r));
            if (spot != null) return Vec3.atBottomCenterOf(spot);
        }
        return sky(p);
    }

    /** Un punto del suelo cercano donde se puede estar de pie. */
    private static BlockPos safeNear(ServerPlayer p, BlockPos base) {
        for (int dy = 0; dy <= 4; dy++) {
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                BlockPos b = base.above(dy * sgn);
                if (p.level().isEmptyBlock(b) && p.level().isEmptyBlock(b.above())
                        && p.level().getBlockState(b.below()).isFaceSturdy(p.level(), b.below(), net.minecraft.core.Direction.UP)
                        && p.level().getFluidState(b).isEmpty()) return b;
            }
        }
        return null;
    }

    private static void teleport(ServerPlayer p, Vec3 to, boolean air) {
        afterImage(p);
        p.teleportTo(to.x, to.y, to.z);
        p.fallDistance = 0;
        if (air) p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 50, 0, true, false));
        ServerLevel lv = p.serverLevel();
        lv.sendParticles(ParticleTypes.END_ROD, to.x, to.y + 1, to.z, 14, 0.3, 0.6, 0.3, 0.03);
        lv.playSound(null, BlockPos.containing(to), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 0.8F, 1.6F);
    }

    /** Imagen residual plateada donde estaba. */
    private static void afterImage(ServerPlayer p) {
        ServerLevel lv = p.serverLevel();
        for (int i = 0; i < 18; i++) {
            lv.sendParticles(SILVER, p.getX() + (lv.random.nextDouble() - 0.5) * 0.5, p.getY() + lv.random.nextDouble() * 1.8,
                    p.getZ() + (lv.random.nextDouble() - 0.5) * 0.5, 1, 0, 0, 0, 0);
        }
        lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 1.0F, 1.8F);
    }
}
