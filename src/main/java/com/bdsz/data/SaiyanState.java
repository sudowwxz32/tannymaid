package com.bdsz.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Estado saiyajin de un jugador (se guarda en el jugador y se sincroniza con los clientes). */
public class SaiyanState {
    public boolean saiyan;
    public int selected;       // Form elegida en el menú
    public int kaioken;        // 0 = apagado, 10..100
    public boolean transformed;
    public boolean aura;

    public Form activeForm() {
        return transformed ? Form.byId(selected) : Form.BASE;
    }

    public int activeKaioken() {
        return transformed ? kaioken : 0;
    }

    public boolean powered() {
        return transformed && (selected != 0 || kaioken > 0);
    }

    /** Multiplicador total de daño (fase × kaioken). */
    public float damageMultiplier() {
        return activeForm().damage * (1.0F + activeKaioken() / 20.0F);
    }

    /** Reducción total del daño recibido (0..0.9). */
    public float resistance() {
        return Math.min(0.9F, activeForm().resist + activeKaioken() / 400.0F);
    }

    public float speedBonus() {
        return activeForm().speed + activeKaioken() / 1000.0F;
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putBoolean("Saiyan", saiyan);
        t.putInt("Selected", selected);
        t.putInt("Kaioken", kaioken);
        t.putBoolean("Transformed", transformed);
        t.putBoolean("Aura", aura);
        return t;
    }

    public static SaiyanState load(CompoundTag t) {
        SaiyanState s = new SaiyanState();
        s.saiyan = t.getBoolean("Saiyan");
        s.selected = t.getInt("Selected");
        s.kaioken = t.getInt("Kaioken");
        s.transformed = t.getBoolean("Transformed");
        s.aura = t.getBoolean("Aura");
        return s;
    }

    public void write(FriendlyByteBuf b) {
        b.writeBoolean(saiyan);
        b.writeVarInt(selected);
        b.writeVarInt(kaioken);
        b.writeBoolean(transformed);
        b.writeBoolean(aura);
    }

    public static SaiyanState read(FriendlyByteBuf b) {
        SaiyanState s = new SaiyanState();
        s.saiyan = b.readBoolean();
        s.selected = b.readVarInt();
        s.kaioken = b.readVarInt();
        s.transformed = b.readBoolean();
        s.aura = b.readBoolean();
        return s;
    }
}
