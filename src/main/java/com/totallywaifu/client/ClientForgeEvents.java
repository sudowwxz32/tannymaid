package com.totallywaifu.client;

import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.network.CollectionAction;
import com.totallywaifu.network.Net;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Pestaña "♥ Mis Waifus" pegada a tu inventario. */
@Mod.EventBusSubscriber(modid = TotallyWaifu.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    private static Button tab;

    @SubscribeEvent
    public static void init(ScreenEvent.Init.Post e) {
        tab = null;
        if (!(e.getScreen() instanceof InventoryScreen) && !(e.getScreen() instanceof CreativeModeInventoryScreen)) return;
        tab = Button.builder(Component.literal("§d♥ Mis Waifus"), b -> Net.CHANNEL.sendToServer(new CollectionAction(0, CollectionAction.OPEN)))
                .bounds(0, 0, 80, 16).tooltip(Tooltip.create(Component.literal("Tu colección de waifus: llámalas o guárdalas"))).build();
        place(e.getScreen());
        e.addListener(tab);
    }

    @SubscribeEvent
    public static void render(ScreenEvent.Render.Pre e) {
        if (tab != null) place(e.getScreen());
    }

    private static void place(net.minecraft.client.gui.screens.Screen s) {
        if (s instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> c) {
            tab.setX(c.getGuiLeft() + (c.getXSize() - 80) / 2);
            tab.setY(s instanceof CreativeModeInventoryScreen ? c.getGuiTop() + c.getYSize() + 4 : c.getGuiTop() - 18);
        }
    }
}
