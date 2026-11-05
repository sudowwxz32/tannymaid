package com.linterna.item;

import com.linterna.data.LanternLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;

/** Mazo de energía: golpe pesado que lanza lejos; si caes golpeando, hace una onda expansiva. */
public class MaceItem extends SwordItem {
    private static final Tier TIER = new Tier() {
        @Override public int getUses() { return 0; }
        @Override public float getSpeed() { return 8.0F; }
        @Override public float getAttackDamageBonus() { return 11.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 0; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public MaceItem(Properties props) {
        super(TIER, 3, -3.1F, props.durability(0));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Vec3 push = target.position().subtract(attacker.position()).normalize().scale(1.6);
        target.setDeltaMovement(push.x, 0.6, push.z);
        target.hurtMarked = true;
        if (attacker instanceof Player p && p.level() instanceof ServerLevel lv && p.fallDistance > 1.5F) {
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(4, 1.5, 4), e -> e != p && e.isAlive())) {
                e.hurt(p.damageSources().playerAttack(p), 12.0F);
                Vec3 d = e.position().subtract(p.position()).normalize().scale(1.4);
                e.setDeltaMovement(d.x, 0.8, d.z);
                e.hurtMarked = true;
            }
            lv.sendParticles(LanternLogic.GREEN, p.getX(), p.getY() + 0.2, p.getZ(), 120, 3, 0.2, 3, 0);
            lv.playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.3F);
            p.fallDistance = 0;
        }
        return true;
    }
}
