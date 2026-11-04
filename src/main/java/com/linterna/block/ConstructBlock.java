package com.linterna.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Bloque de energía verde semitransparente que crea el anillo. Desaparece solo después de un minuto. */
public class ConstructBlock extends HalfTransparentBlock {
    public static final int LIFE = 1200;

    public ConstructBlock(Properties props) {
        super(props);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(state, level, pos, old, moving);
        if (!level.isClientSide) level.scheduleTick(pos, this, LIFE + level.random.nextInt(40));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rnd) {
        level.removeBlock(pos, false);
        level.sendParticles(com.linterna.data.LanternLogic.GREEN, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
