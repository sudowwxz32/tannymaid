package com.tanny.tannymaid.item;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.registry.ModEntities;
import com.tanny.tannymaid.util.TannyLookup;
import com.tanny.tannymaid.util.TannySpeech;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/** Contrato de Tanny Maid: al usarlo, invoca a Tanny y la vincula al jugador. */
public class TannyContractItem extends Item {

    public TannyContractItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.PASS;
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        summon((ServerLevel) level, player, Vec3.atBottomCenterOf(pos), context.getItemInHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (player instanceof ServerPlayer sp) {
            Vec3 look = sp.getLookAngle();
            Vec3 pos = sp.position().add(look.x * 1.5, 0, look.z * 1.5);
            summon((ServerLevel) level, sp, pos, stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    private void summon(ServerLevel level, ServerPlayer player, Vec3 pos, ItemStack stack) {
        TannyEntity existing = TannyLookup.find(player);
        if (existing != null) {
            existing.teleportToOwner(player);
            TannySpeech.say(existing, player, "¡Ya estoy contigo, Master! No necesitas otro contrato, jejeje ♡");
            return;
        }
        TannyEntity tanny = ModEntities.TANNY.get().create(level);
        if (tanny == null) return;
        tanny.moveTo(pos.x, pos.y, pos.z, player.getYRot() + 180.0F, 0.0F);
        tanny.setYHeadRot(player.getYRot() + 180.0F);
        tanny.initFromContract(player);
        if (!level.addFreshEntity(tanny)) return;
        TannyLookup.setStoredId(player, tanny.getUUID());
        level.sendParticles(ParticleTypes.HEART, pos.x, pos.y + 1.6, pos.z, 8, 0.5, 0.4, 0.5, 0.02);
        level.sendParticles(ParticleTypes.CHERRY_LEAVES, pos.x, pos.y + 1.0, pos.z, 30, 0.6, 0.8, 0.6, 0.05);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.3F);
        TannySpeech.say(tanny, player, TannySpeech.SUMMON);
        TannySpeech.system(player, "Escribe §d/tanny§7 para ver todas las órdenes.");
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§dContrato de servicio de Tanny Maid ♡"));
        tooltip.add(Component.literal("§7Úsalo para invocar a Tanny y a su Lucario."));
    }
}
