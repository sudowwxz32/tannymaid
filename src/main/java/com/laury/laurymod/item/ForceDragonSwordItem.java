package com.laury.laurymod.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Espada de dragón de fuerza y salto: golpes contundentes. Solo Laury puede usarla. */
public class ForceDragonSwordItem extends SwordItem implements DragonWeapon {

    public ForceDragonSwordItem(Tier tier, int damage, float speed, Properties props) {
        super(tier, damage, speed, props);
    }

    /** Golpe contundente: lanza al enemigo por los aires y lo aturde. */
    public static void heavyBlow(LivingEntity attacker, LivingEntity target) {
        double dx = target.getX() - attacker.getX();
        double dz = target.getZ() - attacker.getZ();
        double len = Math.max(0.01, Math.sqrt(dx * dx + dz * dz));
        target.setDeltaMovement(target.getDeltaMovement().add(dx / len * 1.2, 0.75, dz / len * 1.2));
        target.hurtMarked = true;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        if (attacker.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(), 25, 0.4, 0.4, 0.4, 0.3);
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.NEUTRAL, 1.0F, 0.7F);
        }
    }

    /** Fuerza y salto para quien la empuña (Laury). */
    public static void empower(LivingEntity holder) {
        holder.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 1, true, false));
        holder.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, true, false));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§eEspada de fuerza y salto"));
        tip.add(Component.literal("§7Fuerza II y Salto II. Golpes contundentes que lanzan y aturden."));
        tip.add(Component.literal("§6Solo Laury puede empuñarla."));
    }
}
