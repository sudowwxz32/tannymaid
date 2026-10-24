package com.bdsz.client;

import com.bdsz.BlockDragonSuperZ;
import com.bdsz.data.SaiyanState;
import com.bdsz.network.ClientStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Cambia el cuerpo que se dibuja: Ozaru gigante, o cuerpo delgado (como Alex) en Super Saiyajin Dios. */
@Mod.EventBusSubscriber(modid = BlockDragonSuperZ.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BodyRenderEvents {
    private BodyRenderEvents() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void pre(RenderPlayerEvent.Pre e) {
        if (!(e.getEntity() instanceof AbstractClientPlayer p)) return;
        SaiyanState s = ClientStates.get(p.getId());
        if (!s.saiyan) return;
        if (s.ozaru) {
            e.setCanceled(true);
            OzaruRenderer.render(p, e.getPartialTick(), e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight());
            return;
        }
        if ((s.transformed || s.powering > 0) && s.visibleForm().slimBody()) {
            EntityRenderer<? extends Player> slim = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().get("slim");
            if (slim instanceof PlayerRenderer pr && e.getRenderer() != pr) {
                e.setCanceled(true);
                float yaw = Mth.lerp(e.getPartialTick(), p.yRotO, p.getYRot());
                pr.render(p, yaw, e.getPartialTick(), e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight());
            }
        }
    }

    /** Aura de ki con llamas (se dibuja en el mundo, alrededor del jugador). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void post(RenderPlayerEvent.Post e) {
        if (e.getEntity() instanceof AbstractClientPlayer p) {
            EnergyCyclone.render(p, e.getPartialTick(), e.getPoseStack(), e.getMultiBufferSource());
            KiFlameAura.render(p, e.getPartialTick(), e.getPoseStack(), e.getMultiBufferSource());
        }
    }

    /** Como Ozaru no se ve el brazo humano en primera persona. */
    @SubscribeEvent
    public static void arm(RenderArmEvent e) {
        if (ClientStates.get(e.getPlayer().getId()).ozaru) e.setCanceled(true);
    }
}
