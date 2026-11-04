package com.linterna.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Estado del portador del anillo. */
public class LanternState {
    public static final int MAX_CHARGE = 24000;          // ~20 minutos de vuelo
    public static final int SUIT_UNLOCK = 12000;         // 10 minutos usando el anillo desbloquean el traje

    public static final int FLY = 1, ARMOR = 2, PICK = 4, GHOST = 8, GUNS = 16, SUIT = 32;

    /** 0 = todavía no llegó la luz, 1 = tuvo miedo (el mod terminó), 2 = portador del anillo. */
    public int stage;
    public int charge;
    public int useTicks;
    public int powers;          // bits de los poderes activos
    public int hammer;          // ticks de la animación del martillo
    public int recharge;        // ticks de la recarga
    public int abduct;          // 0 nada, 1 luz, 2 subiendo, 3 pregunta, 4 cayendo
    public int held = -1;       // id de la entidad que sostiene con el rayo (-1 = nada)

    public boolean on(int bit) { return (powers & bit) != 0; }

    public void set(int bit, boolean v) { powers = v ? powers | bit : powers & ~bit; }

    public boolean bearer() { return stage == 2; }

    public boolean suitUnlocked() { return useTicks >= SUIT_UNLOCK; }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putInt("Stage", stage);
        t.putInt("Charge", charge);
        t.putInt("Use", useTicks);
        t.putInt("Powers", powers);
        t.putInt("Abduct", abduct);
        return t;
    }

    public static LanternState load(CompoundTag t) {
        LanternState s = new LanternState();
        s.stage = t.getInt("Stage");
        s.charge = t.getInt("Charge");
        s.useTicks = t.getInt("Use");
        s.powers = t.getInt("Powers");
        s.abduct = t.getInt("Abduct");
        return s;
    }

    public void write(FriendlyByteBuf b) {
        b.writeVarInt(stage);
        b.writeVarInt(charge);
        b.writeVarInt(useTicks);
        b.writeVarInt(powers);
        b.writeVarInt(hammer);
        b.writeVarInt(recharge);
        b.writeVarInt(abduct);
        b.writeVarInt(held + 1);
    }

    public static LanternState read(FriendlyByteBuf b) {
        LanternState s = new LanternState();
        s.stage = b.readVarInt();
        s.charge = b.readVarInt();
        s.useTicks = b.readVarInt();
        s.powers = b.readVarInt();
        s.hammer = b.readVarInt();
        s.recharge = b.readVarInt();
        s.abduct = b.readVarInt();
        s.held = b.readVarInt() - 1;
        return s;
    }
}
