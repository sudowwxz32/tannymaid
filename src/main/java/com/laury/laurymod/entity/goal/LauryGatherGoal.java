package com.laury.laurymod.entity.goal;

import com.laury.laurymod.entity.LauryEntity;
import com.laury.laurymod.entity.LauryResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/** Recolecta bloques (madera, piedra, carbón, hierro, tierra, arena) o caza animales para carne. */
public class LauryGatherGoal extends Goal {
    private static final int RADIUS = 16;

    private final LauryEntity laury;
    private BlockPos job;
    private Animal prey;
    private int progress, needed, walkTicks, searchCooldown, attackCooldown, lastStage = -1;

    public LauryGatherGoal(LauryEntity laury) {
        this.laury = laury;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override public boolean canUse() { return laury.isGathering() && !laury.isWeakened() && laury.getTarget() == null && !laury.isVehicle(); }
    @Override public boolean canContinueToUse() { return canUse(); }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    @Override public void start() { job = null; prey = null; searchCooldown = 0; }

    @Override
    public void stop() {
        clearProgress();
        job = null;
        prey = null;
        laury.getNavigation().stop();
        laury.updateEquipment();
    }

    @Override
    public void tick() {
        if (!(laury.level() instanceof ServerLevel level)) return;
        LauryResource res = laury.getGatherResource();
        if (res == null) return;
        if (res == LauryResource.CARNE) huntTick(level);
        else mineTick(level, res);
    }

    // ------------------------------------------------------------ caza
    private static boolean isMeat(ItemStack s) {
        FoodProperties f = s.getItem().getFoodProperties();
        return f != null && f.isMeat();
    }

    private void huntTick(ServerLevel level) {
        // recoger la carne que cayó
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, laury.getBoundingBox().inflate(6.0D),
                e -> e.isAlive() && isMeat(e.getItem()));
        for (ItemEntity e : drops) {
            int count = e.getItem().getCount();
            laury.storeOrDrop(e.getItem().copy());
            e.discard();
            laury.onGathered(count);
            if (!laury.isGathering()) return;
        }
        if (attackCooldown > 0) attackCooldown--;
        if (prey == null || !prey.isAlive()) {
            if (--searchCooldown > 0) return;
            searchCooldown = 20;
            List<Animal> list = level.getEntitiesOfClass(Animal.class, laury.getBoundingBox().inflate(28.0D, 8.0D, 28.0D),
                    a -> a.isAlive() && !a.isBaby() && (a instanceof Cow || a instanceof Pig || a instanceof Sheep
                            || a instanceof Chicken || a instanceof Rabbit));
            prey = list.stream().min(Comparator.comparingDouble(laury::distanceToSqr)).orElse(null);
            walkTicks = 0;
            if (prey == null && drops.isEmpty()) laury.finishGather("no_more");
            return;
        }
        laury.getLookControl().setLookAt(prey, 30.0F, 30.0F);
        double reach = laury.getBbWidth() * 2.0F * laury.getBbWidth() * 2.0F + prey.getBbWidth() + 1.5D;
        if (laury.distanceToSqr(prey) > reach) {
            if (walkTicks++ % 10 == 0) laury.getNavigation().moveTo(prey, 1.3D);
            if (walkTicks > 300) prey = null;
        } else if (attackCooldown == 0) {
            laury.doHurtTarget(prey);
            laury.swing(InteractionHand.MAIN_HAND);
            attackCooldown = 12;
        }
    }

    // ------------------------------------------------------------ minar
    private void mineTick(ServerLevel level, LauryResource res) {
        if (job == null) {
            if (--searchCooldown > 0) return;
            searchCooldown = 10;
            job = findBlock(level, res);
            walkTicks = 0;
            progress = 0;
            if (job == null) laury.finishGather("no_more");
            return;
        }
        BlockState state = level.getBlockState(job);
        if (!res.matches(state)) { clearProgress(); job = null; return; }
        ItemStack tool = laury.getTools().getItem(res.toolSlot);
        if (res.needsTool && (tool.isEmpty() || !tool.isCorrectToolForDrops(state))) {
            laury.finishGather(tool.isEmpty() ? "no_tool" : "weak_tool");
            return;
        }
        laury.getLookControl().setLookAt(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5);
        double dist = laury.getEyePosition().distanceToSqr(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5);
        if (dist > 4.3 * 4.3) {
            if (walkTicks++ % 20 == 0) laury.getNavigation().moveTo(job.getX() + 0.5, job.getY(), job.getZ() + 0.5, 1.15D);
            if (walkTicks > 200) { laury.skipGatherBlock(job); clearProgress(); job = null; }
            return;
        }
        laury.getNavigation().stop();
        if (laury.getMainHandItem() != tool) laury.setItemSlot(EquipmentSlot.MAINHAND, tool);
        if (progress == 0) needed = breakTicks(level, state, tool);
        progress++;
        if (progress % 5 == 1) {
            laury.swing(InteractionHand.MAIN_HAND);
            SoundType sound = state.getSoundType(level, job, laury);
            level.playSound(null, job, sound.getHitSound(), SoundSource.BLOCKS, sound.getVolume() * 0.5F, sound.getPitch() * 0.8F);
        }
        int stage = Math.min(9, progress * 10 / Math.max(1, needed));
        if (stage != lastStage) { level.destroyBlockProgress(laury.getId(), job, stage); lastStage = stage; }
        if (progress >= needed) {
            List<ItemStack> drops = Block.getDrops(state, level, job, level.getBlockEntity(job), laury, tool);
            level.destroyBlock(job, false, laury);
            int count = 0;
            for (ItemStack d : drops) { count += d.getCount(); laury.storeOrDrop(d); }
            if (!tool.isEmpty()) {
                tool.hurtAndBreak(1, laury, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                laury.getTools().setChanged();
            }
            clearProgress();
            job = null;
            laury.onGathered(1);
        }
    }

    private void clearProgress() {
        if (job != null && laury.level() instanceof ServerLevel level) level.destroyBlockProgress(laury.getId(), job, -1);
        progress = 0;
        lastStage = -1;
    }

    private static int breakTicks(ServerLevel level, BlockState state, ItemStack tool) {
        float hardness = state.getDestroySpeed(level, BlockPos.ZERO);
        float speed = tool.isEmpty() ? 1.0F : Math.max(1.0F, tool.getDestroySpeed(state));
        boolean correct = !state.requiresCorrectToolForDrops() || (!tool.isEmpty() && tool.isCorrectToolForDrops(state));
        return Math.max(4, Math.min(100, (int) Math.ceil(hardness * (correct ? 25.0F : 90.0F) / speed)));
    }

    private BlockPos findBlock(ServerLevel level, LauryResource res) {
        BlockPos c = laury.blockPosition();
        ServerPlayer owner = laury.getOwnerPlayer();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-RADIUS, -6, -RADIUS), c.offset(RADIUS, 6, RADIUS))) {
            if (!res.matches(level.getBlockState(p)) || laury.isGatherSkipped(p)) continue;
            if (owner != null && owner.level() == level && owner.blockPosition().below().equals(p)) continue;
            if (!exposed(level, p)) continue;
            double d = p.distSqr(c);
            if (d < bestD) { bestD = d; best = p.immutable(); }
        }
        return best;
    }

    private static boolean exposed(ServerLevel level, BlockPos p) {
        for (Direction dir : Direction.values()) {
            BlockState n = level.getBlockState(p.relative(dir));
            if (n.isAir() || n.canBeReplaced()) return true;
        }
        return false;
    }
}
