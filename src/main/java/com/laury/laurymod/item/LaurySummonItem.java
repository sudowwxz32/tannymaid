package com.laury.laurymod.item;

import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.registry.ModEntities;
import com.laury.laurymod.util.LauryLookup;
import com.laury.laurymod.util.LaurySpeech;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/** Cuerno de invocación: sopla para llamar a Laury. Solo puede existir una Laury por jugador. */
public class LaurySummonItem extends Item {

    public LaurySummonItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(stack);
        LauryEntity existing = LauryLookup.find(sp);
        if (existing != null) {
            existing.teleportToOwner(sp);
            LaurySpeech.say(existing, sp, "¡Jaja, ya estoy contigo, {p}! Solo existe una Laury... y es toda tuya.");
            return InteractionResultHolder.consume(stack);
        }
        if (LauryLookup.isBound(sp)) {
            String last = LauryLookup.describeLastPos(sp);
            LaurySpeech.system(sp, "Laury ya existe en este mundo" + (last != null ? " (última posición: " + last + ")" : "")
                    + ". Acércate a esa zona o usa /laury ven cuando esté cargada.");
            return InteractionResultHolder.fail(stack);
        }
        ServerLevel sl = (ServerLevel) level;
        LauryEntity laury = ModEntities.LAURY.get().create(sl);
        if (laury == null) return InteractionResultHolder.fail(stack);
        Vec3 look = sp.getLookAngle();
        Vec3 pos = sp.position().add(look.x * 2.0, 0, look.z * 2.0);
        laury.moveTo(pos.x, pos.y, pos.z, sp.getYRot() + 180.0F, 0.0F);
        laury.setYHeadRot(sp.getYRot() + 180.0F);
        laury.initFromSummon(sp);
        if (!sl.addFreshEntity(laury)) return InteractionResultHolder.fail(stack);
        LauryLookup.setStoredId(sp, laury.getUUID());
        sl.sendParticles(ParticleTypes.DRAGON_BREATH, pos.x, pos.y + 1, pos.z, 60, 0.6, 1.0, 0.6, 0.04);
        sl.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.2, pos.z, 30, 0.8, 0.1, 0.8, 0.02);
        sl.playSound(null, BlockPos.containing(pos), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(), SoundSource.PLAYERS, 1.0F, 0.8F);
        sl.playSound(null, BlockPos.containing(pos), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 0.5F, 1.6F);
        LaurySpeech.say(laury, sp, LaurySpeech.SUMMON);
        LaurySpeech.system(sp, "Escribe §9/laury§7 para ver sus órdenes, o háblale en el chat: §f\"Laury, hola\"§7.");
        if (!sp.getAbilities().instabuild) stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§9Sóplalo para invocar a Laury, la mujer dragón."));
        tip.add(Component.literal("§7Solo puede existir una Laury por jugador."));
    }
}
