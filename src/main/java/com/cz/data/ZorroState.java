package com.cz.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Estado del chakra del zorro de un jugador (guardado en el jugador y sincronizado con los clientes). */
public class ZorroState {
    public static final int MAX_CHAKRA = 200;

    public boolean awakened;    // despertó el chakra del zorro
    public int chakra = MAX_CHAKRA;
    public int selected = 1;    // manto elegido en el menú
    public int phase;           // manto activo (0 = ninguno)
    public int powering;        // ticks que faltan de la transformación
    public int poweringTotal;
    public int target;          // manto en el que se está transformando
    public boolean charging;    // cargando chakra (tecla J)
    public int cooldown;        // agotamiento tras quedarse sin chakra

    public Phase active() { return Phase.byId(phase); }
    /** Fase que se ve: durante la transformación ya aparece la de destino (va creciendo). */
    public Phase visible() { return powering > 0 ? Phase.byId(target) : Phase.byId(phase); }
    public float progress() { return powering > 0 ? 1.0F - (float) powering / Math.max(1, poweringTotal) : 1.0F; }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putBoolean("Awakened", awakened);
        t.putInt("Chakra", chakra);
        t.putInt("Selected", selected);
        t.putInt("Phase", phase);
        t.putInt("Cooldown", cooldown);
        t.putBoolean("Init", true);
        return t;
    }

    public static ZorroState load(CompoundTag t) {
        ZorroState s = new ZorroState();
        s.awakened = t.getBoolean("Awakened");
        s.chakra = t.getBoolean("Init") ? t.getInt("Chakra") : MAX_CHAKRA;
        s.selected = Math.max(1, Math.min(5, t.getInt("Selected")));
        s.phase = Math.max(0, Math.min(5, t.getInt("Phase")));
        s.cooldown = t.getInt("Cooldown");
        return s;
    }

    public void write(FriendlyByteBuf b) {
        b.writeBoolean(awakened);
        b.writeVarInt(chakra);
        b.writeVarInt(selected);
        b.writeVarInt(phase);
        b.writeVarInt(powering);
        b.writeVarInt(poweringTotal);
        b.writeVarInt(target);
        b.writeBoolean(charging);
        b.writeVarInt(cooldown);
    }

    public static ZorroState read(FriendlyByteBuf b) {
        ZorroState s = new ZorroState();
        s.awakened = b.readBoolean();
        s.chakra = b.readVarInt();
        s.selected = b.readVarInt();
        s.phase = b.readVarInt();
        s.powering = b.readVarInt();
        s.poweringTotal = b.readVarInt();
        s.target = b.readVarInt();
        s.charging = b.readBoolean();
        s.cooldown = b.readVarInt();
        return s;
    }

    public ZorroState copy() {
        FriendlyByteBuf b = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        write(b);
        return read(b);
    }
}
