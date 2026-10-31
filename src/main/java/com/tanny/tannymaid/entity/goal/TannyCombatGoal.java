package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Pelea de verdad con su equipo:
 * <ul>
 *   <li>Espada (o la herramienta que tenga) cuerpo a cuerpo, con su daño y encantamientos.</li>
 *   <li>Arco a distancia si tiene flechas en la mochila (o Infinidad); cambia a la espada si el enemigo se acerca.</li>
 *   <li>Escudo: lo levanta contra arqueros, creepers a punto de explotar y entre golpe y golpe.</li>
 * </ul>
 */
public class TannyCombatGoal extends Goal {
    private final TannyEntity tanny;
    private int repath;
    private int attackCooldown;
    private int shotCooldown;
    private int seeTime;

    public TannyCombatGoal(TannyEntity tanny) {
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = tanny.getTarget();
        return t != null && t.isAlive() && !tanny.isWeakened() && tanny.canAttack(t);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        tanny.setAggressive(true);
        repath = 0;
        seeTime = 0;
    }

    @Override
    public void stop() {
        tanny.setAggressive(false);
        tanny.stopUsingItem();
        tanny.getNavigation().stop();
        tanny.updateEquipment();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = tanny.getTarget();
        if (target == null) return;
        if (attackCooldown > 0) attackCooldown--;
        if (shotCooldown > 0) shotCooldown--;
        tanny.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double dist = tanny.distanceToSqr(target);
        boolean sees = tanny.getSensing().hasLineOfSight(target);
        seeTime = sees ? seeTime + 1 : 0;

        boolean canShoot = tanny.hasBowReady();
        boolean ranged = canShoot && sees && dist > 6.5 * 6.5 && dist < 22 * 22;
        if (ranged) {
            bowTick(target, dist);
        } else {
            meleeTick(target, dist);
        }
    }

    // ------------------------------------------------------------------ arco

    private void bowTick(LivingEntity target, double dist) {
        ItemStack bow = tanny.getBow();
        if (tanny.getMainHandItem() != bow) {
            tanny.stopUsingItem();
            tanny.holdInMainHand(bow);
        }
        // se acerca un poco si está muy lejos; si no, se queda firme para apuntar
        if (dist > 15 * 15) {
            if (--repath <= 0) {
                repath = 10;
                tanny.getNavigation().moveTo(target, 1.1D);
            }
        } else {
            tanny.getNavigation().stop();
        }
        if (!tanny.isUsingItem()) {
            if (shotCooldown == 0 && seeTime > 5) tanny.startUsingItem(InteractionHand.MAIN_HAND);
            return;
        }
        if (tanny.getUsedItemHand() != InteractionHand.MAIN_HAND) {
            tanny.stopUsingItem();
            return;
        }
        int ticks = tanny.getTicksUsingItem();
        if (ticks >= 20) {
            tanny.stopUsingItem();
            if (tanny.isShotSafe(target)) {
                tanny.shootArrow(target, BowItem.getPowerForTime(ticks));
            }
            shotCooldown = 12;
        }
    }

    // ------------------------------------------------------------------ cuerpo a cuerpo y escudo

    private void meleeTick(LivingEntity target, double dist) {
        if (tanny.isUsingItem() && tanny.getUsedItemHand() == InteractionHand.MAIN_HAND) tanny.stopUsingItem();
        ItemStack weapon = tanny.getMeleeWeapon();
        if (tanny.getMainHandItem() != weapon) tanny.holdInMainHand(weapon);

        boolean creeperDanger = target instanceof Creeper c && c.getSwellDir() > 0 && dist < 4.5 * 4.5;
        if (creeperDanger) {
            // ¡Va a explotar! Retrocede con el escudo en alto
            Vec3 away = tanny.position().subtract(target.position()).normalize().scale(4.0);
            tanny.getNavigation().moveTo(tanny.getX() + away.x, tanny.getY(), tanny.getZ() + away.z, 1.35D);
            raiseShield();
            return;
        }
        if (--repath <= 0) {
            repath = dist > 16 * 16 ? 12 : 5;
            tanny.getNavigation().moveTo(target, 1.25D);
        }
        double reach = tanny.getBbWidth() * 2.0F * tanny.getBbWidth() * 2.0F + target.getBbWidth() + 0.6;
        boolean inReach = dist <= reach + 1.2;

        boolean underFire = target instanceof RangedAttackMob && dist > 4 * 4;
        boolean betweenSwings = inReach && attackCooldown > 4;
        if (underFire || betweenSwings) {
            raiseShield();
        } else {
            lowerShield();
        }

        if (inReach && attackCooldown <= 0 && tanny.getSensing().hasLineOfSight(target)) {
            lowerShield();
            tanny.swing(InteractionHand.MAIN_HAND);
            tanny.doHurtTarget(target);
            attackCooldown = tanny.attackInterval();
        }
    }

    private void raiseShield() {
        if (!tanny.getOffhandItem().isEmpty() && tanny.getOffhandItem().canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK)
                && !tanny.isUsingItem()) {
            tanny.startUsingItem(InteractionHand.OFF_HAND);
        }
    }

    private void lowerShield() {
        if (tanny.isUsingItem() && tanny.getUsedItemHand() == InteractionHand.OFF_HAND) tanny.stopUsingItem();
    }
}
