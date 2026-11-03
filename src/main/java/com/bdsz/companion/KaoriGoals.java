package com.bdsz.companion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/** Inteligencia de Kaori: seguir, quedarse, pelear, recolectar, agricultura y recoger objetos. */
public final class KaoriGoals {
    private KaoriGoals() {}

    /** Sentada descansando cuando su vida llegó a cero. */
    public static class Rest extends Goal {
        private final KaoriEntity k;

        public Rest(KaoriEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override public boolean canUse() { return k.isWeakened(); }

        @Override public void start() { k.getNavigation().stop(); }

        @Override public void tick() { k.getNavigation().stop(); }
    }

    public static class Follow extends Goal {
        private final KaoriEntity k;
        private final double speed;
        private final float start, stop;
        private ServerPlayer owner;
        private int recalc;

        public Follow(KaoriEntity k, double speed, float start, float stop) {
            this.k = k;
            this.speed = speed;
            this.start = start;
            this.stop = stop;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!k.getMode().follows() || k.isWeakened() || k.isGathering() || k.isFlying()) return false;
            ServerPlayer o = k.getOwnerPlayer();
            if (o == null || o.isSpectator() || o.level() != k.level() || k.distanceToSqr(o) < start * start) return false;
            owner = o;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return owner != null && owner.isAlive() && owner.level() == k.level() && k.getMode().follows() && !k.isWeakened()
                    && k.distanceToSqr(owner) > stop * stop;
        }

        private int stuck;
        private double lastD;

        @Override public void start() { recalc = 0; stuck = 0; lastD = Double.MAX_VALUE; }

        @Override
        public void stop() {
            owner = null;
            k.getNavigation().stop();
            k.setSprinting(false);
        }

        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            k.getLookControl().setLookAt(owner, 10.0F, k.getMaxHeadXRot());
            double d = k.distanceToSqr(owner);
            k.setSprinting(d > 7 * 7 || owner.isSprinting());
            if (--recalc <= 0) {
                recalc = adjustedTickDelay(5);
                // si se atasca (no se acerca en 2 s), o te alejas mucho, aparece a tu lado
                if (d >= lastD - 0.5) stuck += 5; else stuck = 0;
                lastD = d;
                if (d >= 16 * 16 || (stuck >= 40 && d > 6 * 6) || owner.isFallFlying() && d > 10 * 10 || owner.isPassenger() && d > 10 * 10) {
                    k.teleportNear(owner);
                    stuck = 0;
                    lastD = Double.MAX_VALUE;
                    return;
                }
                double sp = d > 10 * 10 ? speed * 1.45 : d > 5 * 5 ? speed * 1.2 : speed;
                if (owner.isSprinting()) sp *= 1.15;
                k.getNavigation().moveTo(owner, sp);
            }
        }
    }

    public static class Stay extends Goal {
        private final KaoriEntity k;
        private BlockPos anchor;
        private int recalc;

        public Stay(KaoriEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        private double r() { return k.getMode() == KaoriMode.FARM ? 10.0 : 3.0; }

        private double d(BlockPos p) { return k.distanceToSqr(p.getX() + 0.5, p.getY(), p.getZ() + 0.5); }

        @Override
        public boolean canUse() {
            if (k.getMode().follows() || k.isWeakened() || k.getTarget() != null || k.isGathering()) return false;
            BlockPos a = k.getAnchor();
            if (a == null || d(a) <= r() * r()) return false;
            anchor = a;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return anchor != null && !k.isWeakened() && k.getTarget() == null && anchor.equals(k.getAnchor()) && d(anchor) > 2.0;
        }

        @Override public void start() { recalc = 0; }

        @Override public void stop() { anchor = null; k.getNavigation().stop(); }

        @Override
        public void tick() {
            if (--recalc <= 0) {
                recalc = adjustedTickDelay(20);
                if (d(anchor) > 48 * 48) k.moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, k.getYRot(), k.getXRot());
                else k.getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 1.0);
            }
        }
    }

    /** Pelea fuerte: cuerpo a cuerpo con su arma o a puño limpio, ráfagas de ki a distancia y escudo. */
    public static class Combat extends Goal {
        private final KaoriEntity k;
        private int repath, cd, blastCd, waveCd;

        public Combat(KaoriEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = k.getTarget();
            return t != null && t.isAlive() && !k.isWeakened() && k.canAttack(t);
        }

        @Override public boolean canContinueToUse() { return canUse(); }

        @Override public void start() { k.setAggressive(true); repath = 0; }

        @Override
        public void stop() {
            k.setAggressive(false);
            k.stopUsingItem();
            k.getNavigation().stop();
        }

        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            LivingEntity t = k.getTarget();
            if (t == null) return;
            if (cd > 0) cd--;
            if (blastCd > 0) blastCd--;
            k.getLookControl().setLookAt(t, 30.0F, 30.0F);
            double dist = k.distanceToSqr(t);
            boolean sees = k.getSensing().hasLineOfSight(t);
            if (t instanceof Creeper c && c.getSwellDir() > 0 && dist < 4.5 * 4.5) {
                Vec3 away = k.position().subtract(t.position()).normalize().scale(4.0);
                k.getNavigation().moveTo(k.getX() + away.x, k.getY(), k.getZ() + away.z, 1.4);
                if (sees && blastCd <= 0) { k.kiBlast(t); blastCd = 40; }
                return;
            }
            if (waveCd > 0) waveCd--;
            if (sees && dist > 5 * 5 && dist < 26 * 26 && waveCd <= 0 && (t.getMaxHealth() >= 30 || t.getHealth() > 20)) {
                k.kiWave(t);
                waveCd = 160;
                blastCd = 30;
            } else if (sees && dist > 6 * 6 && dist < 24 * 24 && blastCd <= 0) {
                k.kiBlast(t);
                blastCd = 40;
            }
            if (--repath <= 0) {
                repath = dist > 16 * 16 ? 12 : 5;
                k.getNavigation().moveTo(t, 1.3);
            }
            double reach = k.getBbWidth() * 2.0F * k.getBbWidth() * 2.0F + t.getBbWidth() + 1.8;
            boolean shield = !k.getOffhandItem().isEmpty() && k.getOffhandItem().canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK);
            if (dist <= reach && cd <= 0 && sees) {
                if (k.isUsingItem()) k.stopUsingItem();
                k.swing(InteractionHand.MAIN_HAND);
                k.doHurtTarget(t);
                cd = 12;
            } else if (shield && dist <= reach + 4 && cd > 4 && !k.isUsingItem()) {
                k.startUsingItem(InteractionHand.OFF_HAND);
            }
        }
    }

    /** Recoge objetos del suelo cerca y los guarda en su mochila. */
    public static class Collect extends Goal {
        private final KaoriEntity k;
        private ItemEntity item;
        private int cooldown, timeout;

        public Collect(KaoriEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!k.isCollecting() || k.isWeakened() || k.getTarget() != null) return false;
            if (--cooldown > 0) return false;
            cooldown = 10;
            ServerPlayer o = k.getOwnerPlayer();
            List<ItemEntity> items = k.level().getEntitiesOfClass(ItemEntity.class, k.getBoundingBox().inflate(8, 3, 8),
                    e -> e.isAlive() && !e.hasPickUpDelay() && e.getAge() > 40 && k.canStore(e.getItem())
                            && (o == null || o.level() != k.level() || e.distanceToSqr(o) > 2.5 * 2.5));
            item = items.stream().min(Comparator.comparingDouble(k::distanceToSqr)).orElse(null);
            return item != null;
        }

        @Override
        public boolean canContinueToUse() {
            return item != null && item.isAlive() && timeout > 0 && !k.isWeakened() && k.getTarget() == null;
        }

        @Override public void start() { timeout = 100; k.getNavigation().moveTo(item, 1.1); }

        @Override public void stop() { item = null; }

        @Override
        public void tick() {
            timeout--;
            if (k.distanceToSqr(item) < 1.8 * 1.8) {
                ItemStack st = item.getItem().copy();
                int before = st.getCount();
                ItemStack rest = k.getBag().addItem(st);
                int taken = before - rest.getCount();
                if (taken > 0) {
                    k.pickup(item, taken);
                    k.level().playSound(null, k.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.3F, 1.4F);
                }
                if (rest.isEmpty()) item.discard(); else item.setItem(rest);
                item = null;
            } else if (timeout % 10 == 0) {
                k.getNavigation().moveTo(item, 1.1);
            }
        }
    }

    /** Agricultura: cosecha, vuelve a sembrar, siembra la tierra de cultivo vacía y ara tierra junto al agua. */
    public static class Farm extends Goal {
        private static final int R = 8;
        private final KaoriEntity k;
        private BlockPos job;
        private int kind;          // 0 cosechar, 1 sembrar, 2 arar
        private int cooldown, timeout;

        public Farm(KaoriEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (k.getMode() != KaoriMode.FARM || k.isWeakened() || k.getTarget() != null || k.isGathering()) return false;
            if (--cooldown > 0) return false;
            cooldown = 20;
            return find();
        }

        @Override
        public boolean canContinueToUse() {
            return job != null && timeout > 0 && k.getMode() == KaoriMode.FARM && !k.isWeakened() && k.getTarget() == null && valid();
        }

        @Override
        public void start() {
            timeout = 200;
            k.getNavigation().moveTo(job.getX() + 0.5, job.getY(), job.getZ() + 0.5, 1.0);
        }

        @Override public void stop() { job = null; }

        @Override
        public void tick() {
            timeout--;
            k.getLookControl().setLookAt(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5);
            if (k.distanceToSqr(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5) < 2.6 * 2.6) {
                switch (kind) {
                    case 0 -> harvest();
                    case 1 -> plant();
                    default -> till();
                }
                job = null;
                cooldown = 5;
            } else if (timeout % 20 == 0) {
                k.getNavigation().moveTo(job.getX() + 0.5, job.getY(), job.getZ() + 0.5, 1.0);
            }
        }

        private boolean find() {
            BlockPos c = k.getAnchor() != null ? k.getAnchor() : k.blockPosition();
            boolean seeds = seedSlot() >= 0, hoe = hoeSlot() >= 0;
            BlockPos best = null;
            int bestKind = 0;
            double bd = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-R, -2, -R), c.offset(R, 2, R))) {
                if (!k.level().isLoaded(p)) continue;
                BlockState s = k.level().getBlockState(p);
                int kk = -1;
                BlockPos t = p;
                if (s.getBlock() instanceof CropBlock crop && crop.isMaxAge(s)) kk = 0;
                else if (seeds && s.getBlock() instanceof FarmBlock && k.level().getBlockState(p.above()).isAir()) { kk = 1; t = p.above(); }
                else if (seeds && hoe && (s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK)) && k.level().getBlockState(p.above()).isAir() && nearWater(p)) kk = 2;
                if (kk < 0) continue;
                double d = k.distanceToSqr(t.getX() + 0.5, t.getY(), t.getZ() + 0.5) + kk * 6;   // primero cosecha
                if (d < bd) { bd = d; best = t.immutable(); bestKind = kk; }
            }
            job = best;
            kind = bestKind;
            return job != null;
        }

        private boolean nearWater(BlockPos p) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                for (int i = 1; i <= 2; i++) if (k.level().getFluidState(p.relative(d, i)).is(FluidTags.WATER)) return true;
            }
            return false;
        }

        private boolean valid() {
            BlockState s = k.level().getBlockState(job);
            return switch (kind) {
                case 0 -> s.getBlock() instanceof CropBlock crop && crop.isMaxAge(s);
                case 1 -> s.isAir() && k.level().getBlockState(job.below()).getBlock() instanceof FarmBlock;
                default -> s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK);
            };
        }

        private int seedSlot() {
            for (int i = 0; i < k.getBag().getContainerSize(); i++) {
                ItemStack s = k.getBag().getItem(i);
                if (s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof CropBlock) return i;
            }
            return -1;
        }

        private int hoeSlot() {
            return k.hoe().isEmpty() ? -1 : 0;
        }

        private void harvest() {
            if (!(k.level() instanceof ServerLevel lv)) return;
            BlockState s = lv.getBlockState(job);
            if (!(s.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(s)) return;
            List<ItemStack> drops = Block.getDrops(s, lv, job, null);
            lv.destroyBlock(job, false, k);
            Item seed = s.getBlock().asItem();
            boolean replant = false;
            for (ItemStack d : drops) {
                if (!replant && d.is(seed)) { d.shrink(1); replant = true; }
                k.storeOrDrop(d);
            }
            if (!replant) replant = k.consumeItem(seed);
            if (replant && crop.defaultBlockState().canSurvive(lv, job)) lv.setBlock(job, crop.getStateForAge(0), Block.UPDATE_ALL);
            k.swing(InteractionHand.MAIN_HAND);
        }

        private void plant() {
            if (!(k.level() instanceof ServerLevel lv) || !lv.getBlockState(job).isAir()) return;
            int slot = seedSlot();
            if (slot < 0) return;
            ItemStack st = k.getBag().getItem(slot);
            if (!(st.getItem() instanceof BlockItem bi)) return;
            BlockState plant = bi.getBlock().defaultBlockState();
            if (!plant.canSurvive(lv, job)) return;
            lv.setBlock(job, plant, Block.UPDATE_ALL);
            st.shrink(1);
            k.getBag().setChanged();
            k.swing(InteractionHand.MAIN_HAND);
            lv.playSound(null, job, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        private void till() {
            if (!(k.level() instanceof ServerLevel lv)) return;
            int slot = hoeSlot();
            if (slot < 0) return;
            lv.setBlock(job, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
            lv.playSound(null, job, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            ItemStack hoe = k.hoe();
            hoe.hurtAndBreak(1, k, e -> {});
            k.getEquip().setChanged();
            k.getBag().setChanged();
            k.swing(InteractionHand.MAIN_HAND);
        }
    }

    /** Farmear: busca, camina y rompe los bloques del recurso pedido usando la mejor herramienta de su mochila. */
    public static class Gather extends Goal {
        private static final int R = 16;
        private final KaoriEntity k;
        private BlockPos job;
        private int progress, needed, walk, search, stage = -1;

        public Gather(KaoriEntity k) {
            this.k = k;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override public boolean canUse() { return k.isGathering() && !k.isWeakened() && k.getTarget() == null; }

        @Override public boolean canContinueToUse() { return canUse(); }

        @Override public void start() { job = null; search = 0; }

        @Override
        public void stop() {
            clear();
            job = null;
            k.getNavigation().stop();
        }

        @Override public boolean requiresUpdateEveryTick() { return true; }

        @Override
        public void tick() {
            if (!(k.level() instanceof ServerLevel lv)) return;
            KaoriResource res = k.getGather();
            if (res == null) return;
            if (job == null) {
                if (--search > 0) return;
                search = 10;
                job = find(lv, res);
                walk = 0;
                progress = 0;
                if (job == null) k.finishGather("none");
                return;
            }
            BlockState s = lv.getBlockState(job);
            if (!res.matches(s)) { clear(); job = null; return; }
            ItemStack tool = k.bestTool(s);
            if (res.needsTool && (tool.isEmpty() || !tool.isCorrectToolForDrops(s))) { k.finishGather("tool"); return; }
            if (k.getMainHandItem() != tool) k.setItemSlot(EquipmentSlot.MAINHAND, tool);
            k.getLookControl().setLookAt(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5);
            if (k.getEyePosition().distanceToSqr(job.getX() + 0.5, job.getY() + 0.5, job.getZ() + 0.5) > 4.2 * 4.2) {
                if (walk++ % 20 == 0) k.getNavigation().moveTo(job.getX() + 0.5, job.getY(), job.getZ() + 0.5, 1.1);
                if (walk > 200) { k.skip(job); clear(); job = null; }
                return;
            }
            k.getNavigation().stop();
            if (progress == 0) needed = ticks(lv, s, tool);
            progress++;
            if (progress % 5 == 1) {
                k.swing(InteractionHand.MAIN_HAND);
                SoundType st = s.getSoundType(lv, job, k);
                lv.playSound(null, job, st.getHitSound(), SoundSource.BLOCKS, st.getVolume() * 0.5F, st.getPitch() * 0.8F);
            }
            int sg = Math.min(9, progress * 10 / Math.max(1, needed));
            if (sg != stage) { lv.destroyBlockProgress(k.getId(), job, sg); stage = sg; }
            if (progress >= needed) {
                BlockEntity be = lv.getBlockEntity(job);
                List<ItemStack> drops = Block.getDrops(s, lv, job, be, k, tool);
                lv.destroyBlock(job, false, k);
                for (ItemStack d : drops) k.storeOrDrop(d);
                if (!tool.isEmpty()) tool.hurtAndBreak(1, k, e -> {});
                k.getBag().setChanged();
                clear();
                job = null;
                k.onGathered();
            }
        }

        private void clear() {
            if (job != null && k.level() instanceof ServerLevel lv) lv.destroyBlockProgress(k.getId(), job, -1);
            progress = 0;
            stage = -1;
        }

        private static int ticks(ServerLevel lv, BlockState s, ItemStack tool) {
            float h = s.getDestroySpeed(lv, BlockPos.ZERO);
            float sp = tool.isEmpty() ? 1.0F : Math.max(1.0F, tool.getDestroySpeed(s));
            boolean ok = !s.requiresCorrectToolForDrops() || (!tool.isEmpty() && tool.isCorrectToolForDrops(s));
            return Math.max(4, Math.min(120, (int) Math.ceil(h * (ok ? 26.0F : 90.0F) / sp)));
        }

        private BlockPos find(ServerLevel lv, KaoriResource res) {
            BlockPos c = k.blockPosition();
            ServerPlayer o = k.getOwnerPlayer();
            BlockPos best = null;
            double bd = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(c.offset(-R, -6, -R), c.offset(R, 6, R))) {
                if (!lv.isLoaded(p) || !res.matches(lv.getBlockState(p)) || k.skipped(p)) continue;
                if (o != null && o.level() == lv && o.blockPosition().below().equals(p)) continue;
                if (k.hutPos != null && p.distSqr(k.hutPos) < 7 * 7) continue;     // no rompe su choza
                if (!exposed(lv, p)) continue;
                double d = p.distSqr(c);
                if (d < bd) { bd = d; best = p.immutable(); }
            }
            return best;
        }

        private static boolean exposed(ServerLevel lv, BlockPos p) {
            for (Direction d : Direction.values()) {
                BlockState n = lv.getBlockState(p.relative(d));
                if (n.isAir() || n.canBeReplaced()) return true;
            }
            return false;
        }
    }

    // ------------------------------------------------------------------ objetivos (proteger al dueño)

    public static class OwnerHurtBy extends TargetGoal {
        private final KaoriEntity k;
        private LivingEntity attacker;
        private int stamp;

        public OwnerHurtBy(KaoriEntity k) {
            super(k, false);
            this.k = k;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            ServerPlayer o = k.getOwnerPlayer();
            if (o == null || k.isWeakened()) return false;
            attacker = o.getLastHurtByMob();
            return attacker != null && o.getLastHurtByMobTimestamp() != stamp && canAttack(attacker, TargetingConditions.DEFAULT) && k.canAttack(attacker);
        }

        @Override
        public void start() {
            k.setTarget(attacker);
            ServerPlayer o = k.getOwnerPlayer();
            if (o != null) stamp = o.getLastHurtByMobTimestamp();
            super.start();
        }
    }

    public static class OwnerHurt extends TargetGoal {
        private final KaoriEntity k;
        private LivingEntity victim;
        private int stamp;

        public OwnerHurt(KaoriEntity k) {
            super(k, false);
            this.k = k;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            ServerPlayer o = k.getOwnerPlayer();
            if (o == null || k.isWeakened()) return false;
            victim = o.getLastHurtMob();
            return victim != null && o.getLastHurtMobTimestamp() != stamp && canAttack(victim, TargetingConditions.DEFAULT) && k.canAttack(victim);
        }

        @Override
        public void start() {
            k.setTarget(victim);
            ServerPlayer o = k.getOwnerPlayer();
            if (o != null) stamp = o.getLastHurtMobTimestamp();
            super.start();
        }
    }

    /** Defensa: ataca a los monstruos que se acercan (en modo Defensa activa, a todos los que vea). */
    public static class Defend extends TargetGoal {
        private final KaoriEntity k;
        private Mob found;

        public Defend(KaoriEntity k) {
            super(k, false);
            this.k = k;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (k.isWeakened() || k.getTarget() != null || k.tickCount % 10 != 0) return false;
            double r = k.getMode() == KaoriMode.DEFEND ? 16 : 8;
            ServerPlayer o = k.getOwnerPlayer();
            LivingEntity center = o != null && o.level() == k.level() && k.distanceToSqr(o) < 30 * 30 ? o : k;
            found = k.level().getEntitiesOfClass(Mob.class, center.getBoundingBox().inflate(r, 4, r),
                            m -> k.isDefendTarget(m) && (k.getMode() == KaoriMode.DEFEND || m.getTarget() == o || m.getTarget() == k
                                    || m.distanceToSqr(center) < 6 * 6))
                    .stream().min(Comparator.comparingDouble(center::distanceToSqr)).orElse(null);
            return found != null;
        }

        @Override
        public void start() {
            k.setTarget(found);
            super.start();
        }
    }
}
