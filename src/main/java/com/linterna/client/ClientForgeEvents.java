package com.linterna.client;

import com.linterna.Linterna;
import com.linterna.network.ActionPacket;
import com.linterna.network.ClientStates;
import com.linterna.network.Net;
import com.linterna.registry.ModRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Linterna.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        press(ClientModEvents.FLY, ActionPacket.FLY);
        press(ClientModEvents.ARMOR, ActionPacket.ARMOR);
        press(ClientModEvents.PICK, ActionPacket.PICK);
        press(ClientModEvents.GHOST, ActionPacket.GHOST);
        press(ClientModEvents.GUNS, ActionPacket.GUNS);
        press(ClientModEvents.HAMMER, ActionPacket.HAMMER);
        press(ClientModEvents.SUIT, ActionPacket.SUIT);
        while (ClientModEvents.CATALOG.consumeClick()) {
            if (ClientStates.get(mc.player.getId()).bearer()) mc.setScreen(new CatalogScreen());
        }
    }

    private static void press(KeyMapping k, int action) {
        while (k.consumeClick()) Net.CHANNEL.sendToServer(new ActionPacket(action, 0));
    }

    /** Clic izquierdo al aire con el Candelabro de Poder en la mano: recarga el anillo. */
    @SubscribeEvent
    public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty e) {
        if (e.getItemStack().is(ModRegistry.CANDELABRA_ITEM.get())) Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.RECHARGE, 0));
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientStates.clear();
    }
}
