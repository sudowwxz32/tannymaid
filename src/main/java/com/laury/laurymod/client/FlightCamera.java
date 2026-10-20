package com.laury.laurymod.client;

import com.laury.laurymod.LauryMod;
import com.laury.laurymod.entity.LauryEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Cámara del jinete: se ladea con el dragón en las curvas, se mece con cada aletazo
 * y abre el campo de visión con la velocidad (sobre todo en picada).
 */
@Mod.EventBusSubscriber(modid = LauryMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FlightCamera {
    private static float fovBoost;

    private FlightCamera() {}

    private static LauryEntity riddenDragon() {
        Player p = Minecraft.getInstance().player;
        if (p != null && p.getVehicle() instanceof LauryEntity l && l.isDragon() && !l.isWeakened()) return l;
        return null;
    }

    @SubscribeEvent
    public static void angles(ViewportEvent.ComputeCameraAngles e) {
        LauryEntity l = riddenDragon();
        if (l == null) return;
        float pt = (float) e.getPartialTick();
        float roll = Mth.lerp(pt, l.prevFlightRoll, l.flightRoll);
        float phase = Mth.lerp(pt, l.prevFlapPhase, l.flapPhase);
        float str = l.flapStrength;
        e.setRoll(e.getRoll() + roll * 0.55F + Mth.sin(phase) * 1.4F * str);
        e.setPitch(e.getPitch() + Mth.cos(phase) * 1.0F * str);
    }

    @SubscribeEvent
    public static void fov(ViewportEvent.ComputeFov e) {
        LauryEntity l = riddenDragon();
        float target = l == null ? 0.0F : Mth.clamp((l.flightSpeed - 0.5F) * 18.0F, 0.0F, 20.0F);
        fovBoost += (target - fovBoost) * 0.08F;
        if (fovBoost > 0.05F) e.setFOV(e.getFOV() + fovBoost);
    }
}
