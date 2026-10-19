package com.laury.laurymod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Guadaña: mientras la empuñas da velocidad, fuerza y regeneración. Laury o el jugador. */
public class DragonScytheItem extends SwordItem {

    public DragonScytheItem(Tier tier, int damage, float speed, Properties props) {
        super(tier, damage, speed, props);
    }

    public static void bless(LivingEntity holder) {
        holder.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, true, false));
        holder.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false));
        holder.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player && entity.tickCount % 20 == 0
                && (selected || player.getOffhandItem() == stack)) {
            bless(player);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§2Guadaña de dragón"));
        tip.add(Component.literal("§7En la mano: Velocidad II, Fuerza y Regeneración."));
    }
}
