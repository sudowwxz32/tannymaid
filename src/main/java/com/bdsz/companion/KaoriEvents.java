package com.bdsz.companion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Efecto del tazón de arroz: el hambre no baja durante un rato. */
@Mod.EventBusSubscriber(modid = com.bdsz.BlockDragonSuperZ.MODID)
public final class KaoriEvents {
    private KaoriEvents() {}

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % 5 != 0) return;
        if (KaoriActions.data(p).getLong("KaoriFullUntil") > p.level().getGameTime()) p.getFoodData().setExhaustion(0.0F);
    }
}
