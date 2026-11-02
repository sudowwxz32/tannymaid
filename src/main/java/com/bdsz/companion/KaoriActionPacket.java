package com.bdsz.companion;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: un botón del menú de Kaori. */
public class KaoriActionPacket {
    public final int entityId, action, arg;
    public final String text;

    public KaoriActionPacket(int entityId, int action, int arg, String text) {
        this.entityId = entityId;
        this.action = action;
        this.arg = arg;
        this.text = text;
    }

    public static void encode(KaoriActionPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.entityId);
        b.writeVarInt(p.action);
        b.writeVarInt(p.arg);
        b.writeUtf(p.text, 64);
    }

    public static KaoriActionPacket decode(FriendlyByteBuf b) {
        return new KaoriActionPacket(b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readUtf(64));
    }

    public static void handle(KaoriActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) KaoriActions.handle(sp, p.entityId, p.action, p.arg, p.text);
        });
        ctx.get().setPacketHandled(true);
    }
}
