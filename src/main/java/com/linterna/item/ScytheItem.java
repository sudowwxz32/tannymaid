package com.linterna.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;

/** Guadaña de energía: cada golpe corta a todos los enemigos alrededor. */
public class ScytheItem extends SwordItem {
    private static final Tier TIER = new Tier() {
        @Override public int getUses() { return 0; }
        @Override public float getSpeed() { return 8.0F; }
        @Override public float getAttackDamageBonus() { return 9.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 0; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public ScytheItem(Properties props) {
        super(TIER, 3, -2.8F, props.durability(0));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player p && !p.level().isClientSide) {
            for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(3.5, 1, 3.5),
                    e -> e != p && e != target && e.isAlive())) {
                e.hurt(p.damageSources().playerAttack(p), 10.0F);
                Vec3 push = e.position().subtract(p.position()).normalize().scale(0.8);
                e.push(push.x, 0.3, push.z);
            }
            p.level().playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.2F, 0.7F);
        }
        return true;
    }
}
