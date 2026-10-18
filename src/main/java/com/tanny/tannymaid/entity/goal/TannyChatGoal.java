package com.tanny.tannymaid.entity.goal;

import com.tanny.tannymaid.entity.TannyEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Mientras conversan, Tanny se detiene y mira a su Master. */
public class TannyChatGoal extends Goal {
    private final TannyEntity tanny;
    private ServerPlayer owner;

    public TannyChatGoal(TannyEntity tanny) {
        this.tanny = tanny;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean valid(ServerPlayer o) {
        return o != null && tanny.isChatting() && !tanny.isWeakened() && tanny.getTarget() == null
                && o.level() == tanny.level() && tanny.distanceToSqr(o) < 7 * 7;
    }

    @Override
    public boolean canUse() {
        ServerPlayer o = tanny.getOwnerPlayer();
        if (!valid(o)) return false;
        owner = o;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return valid(owner);
    }

    @Override
    public void start() {
        tanny.getNavigation().stop();
    }

    @Override
    public void stop() {
        owner = null;
    }

    @Override
    public void tick() {
        tanny.getNavigation().stop();
        tanny.getLookControl().setLookAt(owner, 30.0F, 30.0F);
    }
}
