package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/** Recoge objetos del suelo cercanos y los guarda en su inventario. */
public class TannyCollectItemsGoal extends Goal {
    private final TannyEntity tanny;
    private ItemEntity item;
    private int cooldown;
    private int timeout;

    public TannyCollectItemsGoal(TannyEntity tanny) {
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!tanny.isCollecting() || tanny.isWeakened() || tanny.getTarget() != null || tanny.isGathering()) return false;
        if (--cooldown > 0) return false;
        cooldown = 10;
        ServerPlayer owner = tanny.getOwnerPlayer();
        List<ItemEntity> items = tanny.level().getEntitiesOfClass(ItemEntity.class,
                tanny.getBoundingBox().inflate(8.0D, 3.0D, 8.0D),
                e -> e.isAlive() && !e.hasPickUpDelay() && e.getAge() > 40 && tanny.canStore(e.getItem())
                        && (owner == null || owner.level() != tanny.level() || e.distanceToSqr(owner) > 2.5 * 2.5));
        item = items.stream().min(Comparator.comparingDouble(e -> tanny.distanceToSqr(e))).orElse(null);
        return item != null;
    }

    @Override
    public boolean canContinueToUse() {
        return item != null && item.isAlive() && timeout > 0 && !tanny.isWeakened() && tanny.getTarget() == null;
    }

    @Override
    public void start() {
        timeout = 100;
        tanny.getNavigation().moveTo(item, 1.1D);
    }

    @Override
    public void stop() {
        item = null;
    }

    @Override
    public void tick() {
        timeout--;
        if (tanny.distanceToSqr(item) < 1.8 * 1.8) {
            ItemStack stack = item.getItem().copy();
            int before = stack.getCount();
            ItemStack rest = tanny.getInventory().addItem(stack);
            int taken = before - rest.getCount();
            if (taken > 0) {
                tanny.take(item, taken);
                tanny.level().playSound(null, tanny.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.3F, 1.4F);
            }
            if (rest.isEmpty()) item.discard(); else item.setItem(rest);
            item = null;
        } else if (timeout % 10 == 0) {
            tanny.getNavigation().moveTo(item, 1.1D);
        }
    }
}
