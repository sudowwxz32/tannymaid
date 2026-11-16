package com.totallywaifu.event;

import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.entity.WaifuEntity;
import com.totallywaifu.entity.WaifuKind;
import com.totallywaifu.registry.ModRegistry;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TotallyWaifu.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEvents {
    private ModEvents() {}

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        for (WaifuKind k : WaifuKind.values()) e.put(ModRegistry.TYPES.get(k).get(), WaifuEntity.attributes(k).build());
    }
}
