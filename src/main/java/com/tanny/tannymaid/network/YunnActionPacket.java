package com.tanny.tannymaid.network;

import com.tanny.tannymaid.yunn.YunnEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: un botón del menú de Yunn (con texto para la Pokédex o el mapa). */
public class YunnActionPacket {
    final int entityId, action;
    final String text;

    public YunnActionPacket(int entityId, int action, String text) {
        this.entityId = entityId;
        this.action = action;
        this.text = text;
    }

    public static void encode(YunnActionPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.entityId);
        b.writeVarInt(p.action);
        b.writeUtf(p.text, 64);
    }

    public static YunnActionPacket decode(FriendlyByteBuf b) {
        return new YunnActionPacket(b.readVarInt(), b.readVarInt(), b.readUtf(64));
    }

    public static void handle(YunnActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            Entity e = sp.level().getEntity(p.entityId);
            if (e instanceof YunnEntity y && y.distanceToSqr(sp) < 64 * 64) y.doAction(sp, p.action, p.text);
        });
        ctx.get().setPacketHandled(true);
    }
}
