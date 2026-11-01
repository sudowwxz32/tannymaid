package com.tanny.tannymaid.item;

import com.tanny.tannymaid.yunn.YunnEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Cetro Corazón: el arma de Yunn (inventada para este mod). Pega como una espada de hierro mejorada y,
 * con clic derecho, lanza un Pulso Curativo que cura a quien lo usa y a los aliados cercanos.
 */
public class HeartScepterItem extends SwordItem {
    public static final Tier TIER = new Tier() {
        @Override public int getUses() { return 1400; }
        @Override public float getSpeed() { return 6.0F; }
        @Override public float getAttackDamageBonus() { return 3.0F; }
        @Override public int getLevel() { return 2; }
        @Override public int getEnchantmentValue() { return 22; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(Items.AMETHYST_SHARD); }
    };

    public HeartScepterItem(Properties p) {
        super(TIER, 3, -2.2F, p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            player.heal(4.0F);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.0),
                    e -> e != player && (e instanceof Player || e instanceof net.minecraft.world.entity.TamableAnimal t && t.isTame()
                            || com.tanny.tannymaid.cobblemon.CobblemonBridge.isPokemonEntity(e) && !com.tanny.tannymaid.cobblemon.CobblemonBridge.isWild(e)))) {
                e.heal(3.0F);
                sl.sendParticles(ParticleTypes.HEART, e.getX(), e.getY() + e.getBbHeight() + 0.3, e.getZ(), 2, 0.2, 0.1, 0.2, 0);
            }
            sl.sendParticles(YunnEntity.MINT, player.getX(), player.getY() + 1, player.getZ(), 30, 1.5, 0.6, 1.5, 0);
            sl.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.5F);
            stack.hurtAndBreak(2, player, p -> p.broadcastBreakEvent(hand));
        }
        player.getCooldowns().addCooldown(this, 200);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§dEl cetro de la enfermera Yunn ♡"));
        tip.add(Component.literal("§7Clic derecho: §fPulso Curativo §7(te cura y cura a tus aliados y Pokémon cercanos, cada 10 s)"));
    }
}
