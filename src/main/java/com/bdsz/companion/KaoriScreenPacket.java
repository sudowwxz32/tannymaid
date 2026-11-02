package com.bdsz.companion;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor → cliente: abre el menú de Kaori con el estado de sus regalos. */
public class KaoriScreenPacket {
    public final int entityId, mode, seedsLeft;
    public final boolean collecting, hut, gathering;
    public final long senzuIn, riceIn;

    public KaoriScreenPacket(int entityId, int mode, boolean collecting, int seedsLeft, long senzuIn, long riceIn, boolean hut, boolean gathering) {
        this.entityId = entityId;
        this.mode = mode;
        this.collecting = collecting;
        this.seedsLeft = seedsLeft;
        this.senzuIn = senzuIn;
        this.riceIn = riceIn;
        this.hut = hut;
        this.gathering = gathering;
    }

    public static void encode(KaoriScreenPacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.entityId);
        b.writeVarInt(p.mode);
        b.writeBoolean(p.collecting);
        b.writeVarInt(p.seedsLeft);
        b.writeVarLong(p.senzuIn);
        b.writeVarLong(p.riceIn);
        b.writeBoolean(p.hut);
        b.writeBoolean(p.gathering);
    }

    public static KaoriScreenPacket decode(FriendlyByteBuf b) {
        return new KaoriScreenPacket(b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readVarInt(), b.readVarLong(), b.readVarLong(),
                b.readBoolean(), b.readBoolean());
    }

    public static void handle(KaoriScreenPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.bdsz.client.kaori.KaoriScreen.open(p)));
        ctx.get().setPacketHandled(true);
    }
}
