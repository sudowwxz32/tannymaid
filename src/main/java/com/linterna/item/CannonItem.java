package com.linterna.item;

import com.linterna.entity.RayoVerde;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Cañón de energía: clic derecho dispara una ráfaga de tres rayos verdes. */
public class CannonItem extends Item {
    public CannonItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            for (int i = -1; i <= 1; i++) {
                RayoVerde r = new RayoVerde(level, player, false);
                r.setPos(player.getX(), player.getEyeY() - 0.15, player.getZ());
                r.shootFromRotation(player, player.getXRot(), player.getYRot() + i * 3.0F, 0.0F, 2.8F, 0.3F);
                level.addFreshEntity(r);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8F, 1.8F);
        }
        player.getCooldowns().addCooldown(this, 8);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
