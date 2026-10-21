package com.bdsz.network;

import com.bdsz.data.PowerLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: elegir fase/kaioken (menú K), transformarse (P) o el aura (U). */
public class ActionPacket {
    public static final int SELECT = 0, TRANSFORM = 1, AURA = 2, BLAST = 3, FLY = 4;

    private final int action, form, kaioken;

    public ActionPacket(int action, int form, int kaioken) {
        this.action = action;
        this.form = form;
        this.kaioken = kaioken;
    }

    public static void encode(ActionPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.action);
        b.writeVarInt(p.form);
        b.writeVarInt(p.kaioken);
    }

    public static ActionPacket decode(FriendlyByteBuf b) {
        return new ActionPacket(b.readVarInt(), b.readVarInt(), b.readVarInt());
    }

    public static void handle(ActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            switch (p.action) {
                case SELECT -> PowerLogic.select(sp, p.form, p.kaioken);
                case TRANSFORM -> PowerLogic.toggleTransform(sp);
                case AURA -> PowerLogic.toggleAura(sp);
                case BLAST -> PowerLogic.kiBlast(sp);
                case FLY -> PowerLogic.toggleFlight(sp);
                default -> { }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
