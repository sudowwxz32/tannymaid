package com.linterna.network;

import com.linterna.data.Abduction;
import com.linterna.data.Constructs;
import com.linterna.data.LanternLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: teclas del anillo, respuestas y el catálogo. */
public class ActionPacket {
    public static final int FLY = 0, ARMOR = 1, PICK = 2, GHOST = 3, GUNS = 4, HAMMER = 5, SUIT = 6, CATALOG = 7,
            ANSWER = 8, RECHARGE = 9;

    public final int action, arg;

    public ActionPacket(int action, int arg) {
        this.action = action;
        this.arg = arg;
    }

    public static void encode(ActionPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.action);
        b.writeVarInt(p.arg);
    }

    public static ActionPacket decode(FriendlyByteBuf b) {
        return new ActionPacket(b.readVarInt(), b.readVarInt());
    }

    public static void handle(ActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            switch (p.action) {
                case ANSWER -> Abduction.answer(sp, p.arg == 1);
                case CATALOG -> Constructs.create(sp, p.arg);
                case RECHARGE -> LanternLogic.startRecharge(sp);
                default -> LanternLogic.key(sp, p.action);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
