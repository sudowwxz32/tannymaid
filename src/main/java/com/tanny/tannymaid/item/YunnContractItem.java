package com.tanny.tannymaid.item;

import com.tanny.tannymaid.registry.ModEntities;
import com.tanny.tannymaid.yunn.YunnEntity;
import com.tanny.tannymaid.yunn.YunnLookup;
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

/** Contrato de Yunn: invoca a la enfermera Pokémon y la vincula al jugador. */
public class YunnContractItem extends Item {
    public YunnContractItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext c) {
        if (c.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (!(c.getPlayer() instanceof ServerPlayer sp)) return InteractionResult.PASS;
        summon((ServerLevel) c.getLevel(), sp, Vec3.atBottomCenterOf(c.getClickedPos().relative(c.getClickedFace())), c.getItemInHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            Vec3 look = sp.getLookAngle();
            summon((ServerLevel) level, sp, sp.position().add(look.x * 1.5, 0, look.z * 1.5), stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private void summon(ServerLevel level, ServerPlayer player, Vec3 pos, ItemStack stack) {
        YunnEntity existing = YunnLookup.find(player);
        if (existing != null) {
            existing.teleportTo(player);
            existing.say(player, "¡Ya estoy contigo! No hace falta otro contrato ♡");
            return;
        }
        YunnEntity yunn = ModEntities.YUNN.get().create(level);
        if (yunn == null) return;
        yunn.moveTo(pos.x, pos.y, pos.z, player.getYRot() + 180.0F, 0.0F);
        yunn.init(player);
        if (!level.addFreshEntity(yunn)) return;
        YunnLookup.setStoredId(player, yunn.getUUID());
        level.sendParticles(ParticleTypes.HEART, pos.x, pos.y + 1.6, pos.z, 10, 0.5, 0.4, 0.5, 0.02);
        level.sendParticles(YunnEntity.PINK, pos.x, pos.y + 1.0, pos.z, 40, 0.6, 0.8, 0.6, 0);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.5F);
        yunn.say(player, "¡Hola! Soy Yunn, tu enfermera Pokémon. Chansey, Cinderace y yo cuidaremos de ti y de tu equipo ♡");
        player.sendSystemMessage(Component.literal("§d✚ Guía rápida: §fclic derecho§7 = su menú (curar, potenciar, Pokédex, mapa, aldea) · "
                + "§fShift + clic derecho§7 = seguir / quedarse · §f/yunn§7 = todas las órdenes"));
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§dContrato de la enfermera Pokémon Yunn ✚"));
        tip.add(Component.literal("§7Úsalo para invocar a Yunn con su Chansey y su Cinderace."));
    }
}
