package com.laury.laurymod.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Mazo grande: golpe lento y muy fuerte que sacude a todos los enemigos cercanos. Cualquiera puede usarlo. */
public class DragonMaceItem extends SwordItem {

    public DragonMaceItem(Tier tier, int damage, float speed, Properties props) {
        super(tier, damage, speed, props);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        smash(attacker, target);
        return super.hurtEnemy(stack, target, attacker);
    }

    /** Onda de choque: daña y empuja a los enemigos alrededor del golpe. */
    public static void smash(LivingEntity attacker, LivingEntity target) {
        if (!(attacker.level() instanceof ServerLevel level)) return;
        level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5, target.getZ(), 2, 0.4, 0.2, 0.4, 0.0);
        level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 0.6F);
        double dx = target.getX() - attacker.getX();
        double dz = target.getZ() - attacker.getZ();
        target.knockback(1.4D, -dx, -dz);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3.0D),
                e -> e != attacker && e != target && e.isAlive() && e instanceof Enemy)) {
            e.hurt(level.damageSources().mobAttack(attacker), 6.0F);
            e.knockback(0.9D, attacker.getX() - e.getX(), attacker.getZ() - e.getZ());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§9Mazo de dragón"));
        tip.add(Component.literal("§7Onda de choque: daña y empuja a los enemigos cercanos."));
    }
}
