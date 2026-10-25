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
    private final int gender, hair, lineage;
    private final boolean tail;

    public AppearancePacket(int gender, int hair, boolean tail, int lineage) {
        this.gender = gender;
        this.hair = hair;
        this.tail = tail;
        this.lineage = lineage;
    }

    public static void encode(AppearancePacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.gender);
        b.writeVarInt(p.hair + 1);
        b.writeBoolean(p.tail);
        b.writeVarInt(p.lineage);
    }

    public static AppearancePacket decode(FriendlyByteBuf b) {
        return new AppearancePacket(b.readVarInt(), b.readVarInt() - 1, b.readBoolean(), b.readVarInt());
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
            // bloque de linaje: solo online y solo una vez
            if (s.lineage == 0 && p.lineage != 0 && com.bdsz.command.RazaCommand.isOnline(sp)) {
                com.bdsz.data.Lineage chosen;
                if (p.lineage == com.bdsz.data.Lineage.RANDOM) {
                    com.bdsz.data.Lineage[] pool = com.bdsz.data.Lineage.RANDOM_POOL;
                    chosen = pool[sp.getRandom().nextInt(pool.length)];
                } else {
                    chosen = com.bdsz.data.Lineage.byId(p.lineage);
                    boolean male = s.gender == 1;
                    // masculino: G o V · femenino / sin definir: Normal
                    if (male ? (chosen != com.bdsz.data.Lineage.G && chosen != com.bdsz.data.Lineage.V) : chosen != com.bdsz.data.Lineage.NORMAL) chosen = null;
                }
                if (chosen != null) {
                    s.lineage = chosen.ordinal();
                    if (s.transformed && !s.unlocked(com.bdsz.data.Form.byId(s.selected))) s.transformed = false;
                    if (!s.unlocked(com.bdsz.data.Form.byId(s.selected))) s.selected = 0;
                    sp.sendSystemMessage(Component.literal((p.lineage == com.bdsz.data.Lineage.RANDOM ? "§d§l¡Te tocó el " : "§6§lElegiste el ")
                            + chosen.display + "! §r§7" + chosen.description + " §8(y el Kaioken)"));
                    sp.serverLevel().playSound(null, sp.blockPosition(), net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            SaiyanData.set(sp, s);
            sp.displayClientMessage(Component.literal("§6Apariencia guardada. §7(Cámbiala cuando quieras con §e/raza apariencia§7)"), false);
        });
        ctx.get().setPacketHandled(true);
    }
}
