package com.tanny.tannymaid.network;

import com.tanny.tannymaid.entity.TannyEntity;
import com.tanny.tannymaid.util.TannyLookup;
import com.tanny.tannymaid.util.TannySpeech;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: la tecla de Tanny (G) abre su menú desde cualquier lugar. */
public class OpenMenuPacket {
    public static void handle(OpenMenuPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            TannyEntity tanny = TannyLookup.find(sp);
            if (tanny != null) tanny.openInventory(sp);
            else TannySpeech.system(sp, "Tanny no está cargada ahora. Acércate a donde la dejaste o usa un §dContrato de Tanny Maid§7.");
        });
        ctx.get().setPacketHandled(true);
    }
}
