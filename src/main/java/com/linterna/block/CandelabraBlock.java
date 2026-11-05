package com.linterna.block;

import com.linterna.data.LanternLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/** Lámpara de Poder: lámpara de aceite antigua verde con llama de luz verde. Recarga el anillo (clic izquierdo o derecho). */
public class CandelabraBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(Block.box(4, 0, 4, 12, 4, 12), Block.box(5, 4, 5, 11, 13, 11), Block.box(7, 13, 4, 9, 16, 12));
    private static final DustParticleOptions FLAME = new DustParticleOptions(new Vector3f(0.35F, 1.0F, 0.45F), 0.6F);

    public CandelabraBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer sp && !sp.isShiftKeyDown()) LanternLogic.startRecharge(sp);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) LanternLogic.startRecharge(sp);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rnd) {
        double x = pos.getX() + 0.5, z = pos.getZ() + 0.5;
        // la llama verde dentro del cristal
        level.addParticle(FLAME, x + (rnd.nextDouble() - 0.5) * 0.1, pos.getY() + 0.4 + rnd.nextDouble() * 0.2, z + (rnd.nextDouble() - 0.5) * 0.1, 0, 0.01, 0);
        // humo de luz verde que sale por la chimenea
        if (rnd.nextInt(3) == 0) level.addParticle(FLAME, x, pos.getY() + 0.9, z, 0, 0.03, 0);
        if (rnd.nextInt(6) == 0) level.addParticle(ParticleTypes.END_ROD, x + (rnd.nextDouble() - 0.5) * 0.6, pos.getY() + 0.5,
                z + (rnd.nextDouble() - 0.5) * 0.6, 0, 0.015, 0);
        if (rnd.nextInt(40) == 0) level.playLocalSound(pos, net.minecraft.sounds.SoundEvents.BEACON_AMBIENT, net.minecraft.sounds.SoundSource.BLOCKS,
                0.3F, 1.6F, false);
    }
}
