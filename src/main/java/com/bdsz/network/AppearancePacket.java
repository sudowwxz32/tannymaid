package com.bdsz.network;

import com.bdsz.data.HairStyles;
import com.bdsz.data.SaiyanData;
import com.bdsz.data.SaiyanState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente → servidor: género, peinado y cola elegidos en la pantalla de apariencia. */
public class AppearancePacket {
    private final int gender, hair;
    private final boolean tail;

    public AppearancePacket(int gender, int hair, boolean tail) {
        this.gender = gender;
        this.hair = hair;
        this.tail = tail;
    }

    public static void encode(AppearancePacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.gender);
        b.writeVarInt(p.hair + 1);
        b.writeBoolean(p.tail);
    }

    public static AppearancePacket decode(FriendlyByteBuf b) {
        return new AppearancePacket(b.readVarInt(), b.readVarInt() - 1, b.readBoolean());
    }

    public static void handle(AppearancePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            SaiyanState s = SaiyanData.get(sp);
            if (!s.saiyan) return;
            s.gender = Math.max(0, Math.min(2, p.gender));
            HairStyles.Style st = HairStyles.get(p.hair);
            // el peinado debe corresponder al género (sin definir = cabello predeterminado de cada transformación)
            s.hair = st != null && s.gender != 0 && st.female() == (s.gender == 2) ? p.hair : -1;
            s.tail = p.tail;
            SaiyanData.set(sp, s);
            sp.displayClientMessage(Component.literal("§6Apariencia guardada. §7(Cámbiala cuando quieras con §e/raza apariencia§7)"), false);
        });
        ctx.get().setPacketHandled(true);
    }
}
