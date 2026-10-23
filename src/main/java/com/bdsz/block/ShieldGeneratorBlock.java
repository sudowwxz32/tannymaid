package com.bdsz.block;

import com.bdsz.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Generador Protector: crea un domo de energía que no deja entrar a ningún enemigo.
 * Clic derecho con un lingote de cobre: +30 minutos de energía (Shift: toda la pila).
 * Clic derecho con la mano vacía: encender / apagar.
 */
public class ShieldGeneratorBlock extends BaseEntityBlock {
    public static final BooleanProperty ON = BlockStateProperties.LIT;

    public ShieldGeneratorBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(ON, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(ON);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShieldGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModRegistry.SHIELD_BE.get(), ShieldGeneratorBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player p && level.getBlockEntity(pos) instanceof ShieldGeneratorBlockEntity be) {
            be.setOwner(p.getUUID(), p.getName().getString());
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ShieldGeneratorBlockEntity be) || !(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.COPPER_INGOT)) {
            int n = player.isShiftKeyDown() ? held.getCount() : 1;
            n = be.addFuel(n);
            if (n <= 0) {
                sp.displayClientMessage(Component.literal("§eEl generador ya está lleno (máximo 24 horas)."), true);
            } else {
                if (!player.isCreative()) held.shrink(n);
                sp.displayClientMessage(Component.literal("§a+" + (n * 30) + " min de energía §7· " + be.status()), true);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT, net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.6F);
            }
            return InteractionResult.CONSUME;
        }
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!be.isOwner(player) && !player.isCreative()) {
            sp.displayClientMessage(Component.literal("§cEste generador pertenece a " + be.ownerName() + "."), true);
            return InteractionResult.CONSUME;
        }
        be.toggle(sp);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal("§bCrea un domo de energía de " + ShieldGeneratorBlockEntity.RADIUS + " bloques de radio."));
        tip.add(Component.literal("§7Ningún enemigo puede entrar; solo tú, aldeanos, gólems, gatos y perros."));
        tip.add(Component.literal("§6Cobre en la mano + clic derecho: §f+30 min de energía"));
        tip.add(Component.literal("§6Mano vacía + clic derecho: §fencender / apagar"));
    }
}
