package com.totallywaifu.event;

import com.totallywaifu.TotallyWaifu;
import com.totallywaifu.data.Collection;
import com.totallywaifu.entity.WaifuKind;
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

    /** Bienvenida: como ya no existe la receta de la mesa de crafteo, recibes el corazón de Kozue. */
    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !Collection.takeGift(p) || Collection.has(p, WaifuKind.KOZUE)) return;
        ItemStack heart = new ItemStack(ModRegistry.HEARTS.get(WaifuKind.KOZUE).get());
        if (!p.getInventory().add(heart)) p.drop(heart, false);
        p.sendSystemMessage(Component.literal("§d♥ Totally Waifu?: §fLas mesas de crafteo, hornos, cofres y mesas de encantamientos ya no se fabrican: ¡ahora son chicas!"));
        p.sendSystemMessage(Component.literal("§7  Usa el §6Corazón de Kozue§7 para despertarla: con ella podrás craftear (y fabricar los corazones de las demás)."));
        p.sendSystemMessage(Component.literal("§7  Tus chicas se guardan en §d♥ Mis Waifus§7 (botón en tu inventario)."));
    }
}
