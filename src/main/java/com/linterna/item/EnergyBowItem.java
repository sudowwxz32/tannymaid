package com.linterna.item;

import com.linterna.entity.RayoVerde;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Arco de energía: no necesita flechas, dispara flechas de luz verde. */
public class EnergyBowItem extends BowItem {
    public EnergyBowItem(Properties props) {
        super(props.durability(0));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int left) {
        float power = getPowerForTime(getUseDuration(stack) - left);
        if (power < 0.1F) return;
        if (!level.isClientSide) {
            RayoVerde r = new RayoVerde(level, user, false);
            r.setDamage(6.0F + 10.0F * power);
            r.setPos(user.getX(), user.getEyeY() - 0.1, user.getZ());
            r.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, power * 3.4F, 0.4F);
            level.addFreshEntity(r);
        }
        level.playSound(null, user.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.6F);
        level.playSound(null, user.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.5F, 2.0F);
    }
}
