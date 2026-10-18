package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.entity.TannyResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/** Busca, camina y pica los bloques que el Master pidió (máx. 64 por pedido). */
public class TannyGatherGoal extends Goal {
    private static final int RADIUS = 16;

    private final TannyEntity tanny;
    private BlockPos job;
    private int progress;
    private int needed;
    private int walkTicks;
    private int searchCooldown;
    private int lastStage = -1;

    public TannyGatherGoal(TannyEntity tanny) {
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return tanny.isGathering() && !tanny.isWeakened() && tanny.getTarget() == null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        job = null;
        searchCooldown = 0;
    }

    @Override
    public void stop() {
        clearProgress();
        job = null;
        tanny.getNavigation().stop();
        tanny.updateEquipment();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!(tanny.level() instanceof ServerLevel level)) return;
        TannyResource res = tanny.getGatherResource();
        if (res == null) return;

        if (job == null) {
            if (--searchCooldown > 0) return;
            searchCooldown = 10;
            job = findBlock(level, res);
            walkTicks = 0;
            progress = 0;
            if (job == null) {
                tanny.finishGather(false, "no_more");
            }
            return;
        }

        BlockState state = level.getBlockState(job);
        if (!res.matches(state)) {
            clearProgress();
            job = null;
            return;
        }

        ItemStack tool = tanny.getTools().getItem(res.toolSlot);
        if (res.needsTool && (tool.isEmpty() || !tool.isCorrectToolForDrops(state))) {
            tanny.finishGather(false, tool.isEmpty() ? "no_tool" : "weak_tool");
            return;
        }

        tanny.getLookControl().setLookAt(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5);
        double dist = tanny.getEyePosition().distanceToSqr(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5);
        if (dist > 4.2 * 4.2) {
            if (walkTicks++ % 20 == 0) {
                tanny.getNavigation().moveTo(job.getX() + 0.5, job.getY(), job.getZ() + 0.5, 1.1D);
            }
            if (walkTicks > 200) { // no llega: se salta este bloque
                tanny.skipGatherBlock(job);
                clearProgress();
                job = null;
            }
            return;
        }

        tanny.getNavigation().stop();
        if (tanny.getMainHandItem() != tool) {
            tanny.setItemSlot(EquipmentSlot.MAINHAND, tool);
        }
        if (progress == 0) {
            needed = breakTicks(level, state, tool);
        }
        progress++;
        if (progress % 5 == 1) {
            tanny.swing(InteractionHand.MAIN_HAND);
            SoundType sound = state.getSoundType(level, job, tanny);
            level.playSound(null, job, sound.getHitSound(), SoundSource.BLOCKS, sound.getVolume() * 0.5F, sound.getPitch() * 0.8F);
        }
        int stage = Math.min(9, progress * 10 / Math.max(1, needed));
        if (stage != lastStage) {
            level.destroyBlockProgress(tanny.getId(), job, stage);
            lastStage = stage;
        }
        if (progress >= needed) {
            mine(level, state, tool);
        }
    }

    private void mine(ServerLevel level, BlockState state, ItemStack tool) {
        BlockEntity be = level.getBlockEntity(job);
        List<ItemStack> drops = Block.getDrops(state, level, job, be, tanny, tool);
        level.destroyBlock(job, false, tanny);
        for (ItemStack drop : drops) {
            tanny.storeOrDrop(drop);
        }
        if (!tool.isEmpty()) {
            tool.hurtAndBreak(1, tanny, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            tanny.getTools().setChanged();
        }
        clearProgress();
        job = null;
        tanny.onGatheredBlock();
    }

    private void clearProgress() {
        if (job != null && tanny.level() instanceof ServerLevel level) {
            level.destroyBlockProgress(tanny.getId(), job, -1);
        }
        progress = 0;
        lastStage = -1;
    }

    private static int breakTicks(ServerLevel level, BlockState state, ItemStack tool) {
        float hardness = state.getDestroySpeed(level, BlockPos.ZERO);
        float speed = tool.isEmpty() ? 1.0F : Math.max(1.0F, tool.getDestroySpeed(state));
        boolean correct = !state.requiresCorrectToolForDrops() || (!tool.isEmpty() && tool.isCorrectToolForDrops(state));
        int ticks = (int) Math.ceil(hardness * (correct ? 30.0F : 100.0F) / speed);
        return Math.max(4, Math.min(120, ticks));
    }

    private BlockPos findBlock(ServerLevel level, TannyResource res) {
        BlockPos center = tanny.blockPosition();
        BlockPos home = tanny.getHomeInThisLevel();
        ServerPlayer owner = tanny.getOwnerPlayer();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -6, -RADIUS), center.offset(RADIUS, 6, RADIUS))) {
            if (!res.matches(level.getBlockState(pos))) continue;
            if (tanny.isGatherBlockSkipped(pos)) continue;
            if (home != null && pos.distSqr(home) < 8 * 8) continue; // no rompe la casa
            if (owner != null && owner.level() == level && owner.blockPosition().below().equals(pos)) continue;
            if (!isExposed(level, pos)) continue;
            double d = pos.distSqr(center);
            if (d < bestDist) {
                bestDist = d;
                best = pos.immutable();
            }
        }
        return best;
    }

    private static boolean isExposed(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState n = level.getBlockState(pos.relative(dir));
            if (n.isAir() || n.canBeReplaced()) return true;
        }
        return false;
    }
}
