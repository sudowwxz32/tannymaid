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

/** Bomba de energía: clic derecho para lanzarla; explota al chocar. */
public class BombItem extends Item {
    public BombItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack st = player.getItemInHand(hand);
        if (!level.isClientSide) {
            RayoVerde b = new RayoVerde(level, player, true);
            b.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            b.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.4F, 0.5F);
            level.addFreshEntity(b);
            level.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
        if (!player.getAbilities().instabuild) st.shrink(1);
        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.sidedSuccess(st, level.isClientSide);
    }
}
