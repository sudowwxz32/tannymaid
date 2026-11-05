package com.linterna.item;

import com.linterna.entity.RayoVerde;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/** Lanza de energía: golpe largo; clic derecho lanza una jabalina de luz que atraviesa a los enemigos. */
public class LanceItem extends SwordItem {
    private static final Tier TIER = new Tier() {
        @Override public int getUses() { return 0; }
        @Override public float getSpeed() { return 8.0F; }
        @Override public float getAttackDamageBonus() { return 8.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 0; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public LanceItem(Properties props) {
        super(TIER, 3, -2.6F, props.durability(0));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            RayoVerde r = new RayoVerde(level, player, false);
            r.setDamage(16.0F);
            r.setPierce(true);
            r.setPos(player.getX(), player.getEyeY() - 0.15, player.getZ());
            r.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F);
            level.addFreshEntity(r);
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.3F);
        }
        player.getCooldowns().addCooldown(this, 16);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
