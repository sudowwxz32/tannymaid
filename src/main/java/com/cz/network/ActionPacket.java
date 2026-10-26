package com.cz.network;

import com.cz.data.ZorroLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: elegir manto, transformarse, cargar chakra. */
public class ActionPacket {
    public static final int SELECT = 0, TRANSFORM = 1, CHARGE = 2;
    private final int action, value;

    public ActionPacket(int action, int value) {
        this.action = action;
        this.value = value;
    }

    public static void encode(ActionPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.action);
        b.writeVarInt(p.value);
    }

    public static ActionPacket decode(FriendlyByteBuf b) {
        return new ActionPacket(b.readVarInt(), b.readVarInt());
    }

    public static void handle(ActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            switch (p.action) {
                case SELECT -> ZorroLogic.select(sp, p.value);
                case TRANSFORM -> ZorroLogic.toggleTransform(sp);
                case CHARGE -> ZorroLogic.toggleCharge(sp);
                default -> { }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
