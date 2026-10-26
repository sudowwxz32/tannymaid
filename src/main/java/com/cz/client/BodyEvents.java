package com.cz.client;

import com.cz.ChakraZorro;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Baja el modelo cuando está de rodillas o en cuatro patas, y dibuja los efectos de chakra alrededor. */
@Mod.EventBusSubscriber(modid = ChakraZorro.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BodyEvents {
    private BodyEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pre(RenderPlayerEvent.Pre e) {
        e.getPoseStack().pushPose();
        float shift = ZorroPoses.shift(e.getEntity());
        if (shift != 0) e.getPoseStack().translate(0, -shift / 16.0F * 0.9375F, 0);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void post(RenderPlayerEvent.Post e) {
        e.getPoseStack().popPose();
        if (e.getEntity() instanceof AbstractClientPlayer p) {
            ChakraFx.render(p, e.getPartialTick(), e.getPoseStack(), e.getMultiBufferSource());
        }
    }
}
