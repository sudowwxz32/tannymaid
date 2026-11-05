package com.linterna.client;

import com.linterna.Linterna;
import com.linterna.data.LanternState;
import com.linterna.network.ActionPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.ScreenEvent;
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

    /** Clic izquierdo al aire: con la Lámpara recarga; con el anillo (en la mano, o puesto y la mano vacía) dispara. */
    @SubscribeEvent
    public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty e) {
        if (e.getItemStack().is(ModRegistry.CANDELABRA_ITEM.get())) {
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.RECHARGE, 0));
            return;
        }
        LanternState s = ClientStates.get(e.getEntity().getId());
        if (!s.bearer()) return;
        if (e.getItemStack().is(ModRegistry.RING.get()) || (s.worn && e.getItemStack().isEmpty()))
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.SHOOT, 0));
    }

    /** Con el anillo puesto y la mano vacía, clic derecho al aire o a lo lejos: rayo de selección (o disparo si no hay nada). */
    @SubscribeEvent
    public static void rightClickEmpty(PlayerInteractEvent.RightClickEmpty e) {
        LanternState s = ClientStates.get(e.getEntity().getId());
        if (s.bearer() && s.worn && e.getHand() == InteractionHand.MAIN_HAND && e.getItemStack().isEmpty())
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.GRAB, 0));
    }

    /** De cerca: Shift + clic derecho con la mano vacía y el anillo puesto agarra al mob o al bloque. */
    @SubscribeEvent
    public static void rightClickEntity(PlayerInteractEvent.EntityInteract e) {
        if (e.getLevel().isClientSide && closeGrab(e.getEntity(), e.getHand(), e.getItemStack())) {
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.GRAB, 0));
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (e.getLevel().isClientSide && closeGrab(e.getEntity(), e.getHand(), e.getItemStack())) {
            Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.GRAB, 0));
            e.setCanceled(true);
        }
    }

    private static boolean closeGrab(net.minecraft.world.entity.player.Player p, InteractionHand hand, net.minecraft.world.item.ItemStack st) {
        LanternState s = ClientStates.get(p.getId());
        return s.bearer() && s.worn && hand == InteractionHand.MAIN_HAND && st.isEmpty() && p.isShiftKeyDown();
    }

    // ---- botón ◎ de la ranura del anillo en el inventario
    private static Button RING_BTN;

    @SubscribeEvent
    public static void invInit(ScreenEvent.Init.Post e) {
        RING_BTN = null;
        if (!(e.getScreen() instanceof InventoryScreen inv)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !ClientStates.get(mc.player.getId()).bearer()) return;
        RING_BTN = Button.builder(Component.literal("§a◎"), b -> Net.CHANNEL.sendToServer(new ActionPacket(ActionPacket.OPEN_RING, 0)))
                .bounds(inv.getGuiLeft() + 156, inv.getGuiTop() + 6, 14, 14)
                .tooltip(Tooltip.create(Component.literal("§aRanura del Anillo Verde"))).build();
        e.addListener(RING_BTN);
    }

    @SubscribeEvent
    public static void invRender(ScreenEvent.Render.Pre e) {
        if (RING_BTN != null && e.getScreen() instanceof InventoryScreen inv) {
            RING_BTN.setX(inv.getGuiLeft() + 156);
            RING_BTN.setY(inv.getGuiTop() + 6);
        }
    }

    /** Con el traje puesto se ocultan las capas exteriores de la skin para que el traje encaje con el cuerpo. */
    @SubscribeEvent
    public static void pre(net.minecraftforge.client.event.RenderPlayerEvent.Pre e) {
        if (ClientStates.get(e.getEntity().getId()).on(com.linterna.data.LanternState.SUIT)) {
            var m = e.getRenderer().getModel();
            m.jacket.visible = m.leftSleeve.visible = m.rightSleeve.visible = m.leftPants.visible = m.rightPants.visible = false;
            HIDDEN = true;
        }
    }

    @SubscribeEvent
    public static void post(net.minecraftforge.client.event.RenderPlayerEvent.Post e) {
        if (HIDDEN) {
            var m = e.getRenderer().getModel();
            m.jacket.visible = m.leftSleeve.visible = m.rightSleeve.visible = m.leftPants.visible = m.rightPants.visible = true;
            HIDDEN = false;
        }
    }

    private static boolean HIDDEN;

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientStates.clear();
    }
}
