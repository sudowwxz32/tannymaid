package com.laury.laurymod.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Espada de dragón de fuego. Solo Laury puede usarla. */
public class FireDragonSwordItem extends SwordItem implements DragonWeapon {

    public FireDragonSwordItem(Tier tier, int damage, float speed, Properties props) {
        super(tier, damage, speed, props);
    }

    public static void ignite(LivingEntity attacker, LivingEntity target) {
        target.setSecondsOnFire(8);
        if (attacker.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.6), target.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
            level.playSound(null, target.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 0.6F, 1.2F);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§cEspada de dragón de fuego"));
        tip.add(Component.literal("§7Incendia a los enemigos durante 8 segundos."));
        tip.add(Component.literal("§6Solo Laury puede empuñarla."));
    }
}
