package com.bdsz.companion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Efecto del tazón de arroz: el hambre no baja durante un rato. */
@Mod.EventBusSubscriber(modid = com.bdsz.BlockDragonSuperZ.MODID)
public final class KaoriEvents {
    private KaoriEvents() {}

    /** Al craftear la cápsula (3 bloques de hierro, 3 de cobre, un diamante y una Semilla del Ermitaño) aparece Kaori. */
    @SubscribeEvent
    public static void crafted(net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && e.getCrafting().is(com.bdsz.registry.ModRegistry.KAORI_CAPSULE.get())) {
            p.server.execute(() -> KaoriActions.summon(p));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % 5 != 0) return;
        if (KaoriActions.data(p).getLong("KaoriFullUntil") > p.level().getGameTime()) p.getFoodData().setExhaustion(0.0F);
    }
}
