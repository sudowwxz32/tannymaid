package com.totallywaifu.event;

import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.data.Collection;
import com.totallywaifu.registry.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TotallyWaifu.MODID)
public final class ForgeEvents {
    private ForgeEvents() {}

    /** Bienvenida: recibes la Vara de Transformación (una vez). */
    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !Collection.takeGift(p)) return;
        ItemStack wand = new ItemStack(ModRegistry.WAND.get());
        if (!p.getInventory().add(wand)) p.drop(wand, false);
        p.sendSystemMessage(Component.literal("§d♥ Totally Waifu?: §fRecibiste la §dVara de Transformación§f."));
        p.sendSystemMessage(Component.literal("§7  Úsala sobre una mesa de crafteo, horno, cofre o mesa de encantamientos para que se transformen en chicas (y al revés)."));
        p.sendSystemMessage(Component.literal("§7  Sobre animales y monstruos, se transforman en chicas para siempre. Tu colección: inventario → §d♥ Mis Waifus§7."));
    }

    /** La vara sobre una criatura (antes que su interacción normal, por ejemplo el comercio del aldeano). */
    @SubscribeEvent
    public static void interact(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract e) {
        if (!e.getItemStack().is(ModRegistry.WAND.get())) return;
        boolean ok = com.totallywaifu.entity.WaifuKind.fromEntity(e.getTarget()) != null || e.getTarget() instanceof com.totallywaifu.entity.WaifuEntity;
        if (!ok) return;
        e.setCanceled(true);
        e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (e.getEntity() instanceof ServerPlayer p && e.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
            com.totallywaifu.item.WandItem.useOnEntity(p, e.getTarget());
            p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        }
    }
}
