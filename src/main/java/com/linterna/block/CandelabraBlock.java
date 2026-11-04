package com.linterna.block;

import com.linterna.data.LanternLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Candelabro de Poder: recarga el anillo con clic izquierdo (en la mano o colocado). */
public class CandelabraBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 15, 14);

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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rnd) {
        for (double[] c : new double[][]{{0.5, 0.95, 0.5}, {0.22, 0.8, 0.5}, {0.78, 0.8, 0.5}}) {
            level.addParticle(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.3F, 1.0F, 0.4F), 0.7F),
                    pos.getX() + c[0], pos.getY() + c[1], pos.getZ() + c[2], 0, 0.02, 0);
        }
        if (rnd.nextInt(5) == 0) level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0.01, 0);
    }
}
