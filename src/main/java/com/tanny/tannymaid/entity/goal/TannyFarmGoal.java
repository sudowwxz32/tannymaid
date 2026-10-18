package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.entity.TannyMode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/** Cosecha cultivos maduros, los vuelve a sembrar y siembra tierra de cultivo vacía. */
public class TannyFarmGoal extends Goal {
    private static final int RADIUS = 8;

    private final TannyEntity tanny;
    private BlockPos job;
    private boolean harvest;
    private int cooldown;
    private int timeout;

    public TannyFarmGoal(TannyEntity tanny) {
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (tanny.getMode() != TannyMode.FARM || tanny.isWeakened() || tanny.getTarget() != null) return false;
        if (--cooldown > 0) return false;
        cooldown = 20;
        return findJob();
    }

    @Override
    public boolean canContinueToUse() {
        return job != null && timeout > 0 && tanny.getMode() == TannyMode.FARM
                && !tanny.isWeakened() && tanny.getTarget() == null && isStillValid();
    }

    @Override
    public void start() {
        timeout = 200;
        tanny.getNavigation().moveTo(job.getX() + 0.5, job.getY(), job.getZ() + 0.5, 1.0D);
    }

    @Override
    public void stop() {
        job = null;
    }

    @Override
    public void tick() {
        timeout--;
        tanny.getLookControl().setLookAt(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5);
        double d = tanny.distanceToSqr(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5);
        if (d < 2.6 * 2.6) {
            if (harvest) doHarvest(); else doPlant();
            job = null;
            cooldown = 5;
        } else if (timeout % 20 == 0) {
            tanny.getNavigation().moveTo(job.getX() + 0.5, job.getY(), job.getZ() + 0.5, 1.0D);
        }
    }

    private boolean findJob() {
        BlockPos center = tanny.getAnchor() != null ? tanny.getAnchor() : tanny.blockPosition();
        BlockPos best = null;
        boolean bestHarvest = false;
        double bestDist = Double.MAX_VALUE;
        boolean hasSeeds = findSeedSlot() >= 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -2, -RADIUS), center.offset(RADIUS, 2, RADIUS))) {
            BlockState state = tanny.level().getBlockState(pos);
            boolean isHarvest = state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
            boolean isPlant = !isHarvest && hasSeeds && state.getBlock() instanceof FarmBlock
                    && tanny.level().getBlockState(pos.above()).isAir();
            if (!isHarvest && !isPlant) continue;
            BlockPos target = isPlant ? pos.above() : pos;
            double d = tanny.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            if (d < bestDist) {
                bestDist = d;
                best = target.immutable();
                bestHarvest = isHarvest;
            }
        }
        job = best;
        harvest = bestHarvest;
        return job != null;
    }

    private boolean isStillValid() {
        BlockState state = tanny.level().getBlockState(job);
        if (harvest) return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state);
        return state.isAir() && tanny.level().getBlockState(job.below()).getBlock() instanceof FarmBlock;
    }

    private int findSeedSlot() {
        for (int i = 0; i < tanny.getInventory().getContainerSize(); i++) {
            ItemStack s = tanny.getInventory().getItem(i);
            if (s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof CropBlock) return i;
        }
        return -1;
    }

    private void doHarvest() {
        if (!(tanny.level() instanceof ServerLevel level)) return;
        BlockState state = level.getBlockState(job);
        if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return;
        List<ItemStack> drops = Block.getDrops(state, level, job, null);
        level.destroyBlock(job, false, tanny);
        Item seed = state.getBlock().asItem();
        boolean replant = false;
        for (ItemStack drop : drops) {
            if (!replant && drop.is(seed)) {
                drop.shrink(1);
                replant = true;
            }
            tanny.storeOrDrop(drop);
        }
        if (!replant) replant = tanny.consumeItem(seed);
        if (replant && crop.defaultBlockState().canSurvive(level, job)) {
            level.setBlock(job, crop.getStateForAge(0), Block.UPDATE_ALL);
        }
        tanny.swing(InteractionHand.MAIN_HAND);
    }

    private void doPlant() {
        if (!(tanny.level() instanceof ServerLevel level)) return;
        if (!level.getBlockState(job).isAir()) return;
        int slot = findSeedSlot();
        if (slot < 0) return;
        ItemStack stack = tanny.getInventory().getItem(slot);
        if (!(stack.getItem() instanceof BlockItem bi)) return;
        BlockState plant = bi.getBlock().defaultBlockState();
        if (!plant.canSurvive(level, job)) return;
        level.setBlock(job, plant, Block.UPDATE_ALL);
        stack.shrink(1);
        tanny.getInventory().setChanged();
        tanny.swing(InteractionHand.MAIN_HAND);
    }
}
