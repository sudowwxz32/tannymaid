package com.totallywaifu.network;

import com.totallywaifu.data.Collection;
import com.totallywaifu.entity.WaifuKind;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: abrir/refrescar la colección, llamar o guardar a una chica. */
public record CollectionAction(int kind, int action) {
    public static final int OPEN = 0, SUMMON = 1, STORE = 2, HOME = 3, INARI = 4;

    public static void write(CollectionAction m, FriendlyByteBuf b) {
        b.writeVarInt(m.kind);
        b.writeVarInt(m.action);
    }

    public static CollectionAction read(FriendlyByteBuf b) {
        return new CollectionAction(b.readVarInt(), b.readVarInt());
    }

    public static void handle(CollectionAction m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer p = ctx.get().getSender();
            if (p == null) return;
            WaifuKind k = WaifuKind.byId(m.kind);
            if (m.action == SUMMON) Collection.summon(p, k);
            else if (m.action == STORE) Collection.store(p, k);
            else if (m.action == INARI) {
                com.totallywaifu.entity.Inari.order(p, "cambiar");
                return;
            } else if (m.action == HOME) {
                Collection.goHome(p);
                return;
            }
            Collection.send(p);
        });
        ctx.get().setPacketHandled(true);
    }
}
