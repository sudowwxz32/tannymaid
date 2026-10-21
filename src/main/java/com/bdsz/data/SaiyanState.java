package com.bdsz.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Estado saiyajin de un jugador (guardado en el jugador y sincronizado con los clientes). */
public class SaiyanState {
    public boolean saiyan;
    public int selected;        // fase elegida en el menú
    public int kaioken;         // 0 = apagado, 10..100
    public boolean transformed;
    public boolean aura;
    public boolean flight;      // vuelo habilitado (tecla I)
    public int ki;
    public int timer;           // ticks restantes de transformación
    public int timerMax;
    public int cooldown;        // ticks de agotamiento tras acabarse el tiempo
    public boolean charging;    // recargando ki (tecla H)
    public int powering;        // ticks restantes de la carga antes de transformarse
    public int poweringTotal;
    public boolean totemReady;  // salvación tipo tótem disponible en esta transformación
    public int tier = 1;        // bloque de entrenamiento desbloqueado (1..5; 5 = Bloque Especial)

    public boolean unlocked(Form f) { return f.tier() <= tier; }

    public Form activeForm() { return transformed ? Form.byId(selected) : Form.BASE; }
    /** Fase que se ve en el aura (durante la carga ya se ve la fase destino). */
    public Form visibleForm() { return transformed || powering > 0 ? Form.byId(selected) : Form.BASE; }
    public int activeKaioken() { return transformed ? kaioken : 0; }
    public boolean powered() { return transformed && (selected != 0 || kaioken > 0); }

    public float damageMultiplier() { return activeForm().damage * (1.0F + activeKaioken() / 20.0F); }
    public float punchBonus() { return activeForm().punch + activeKaioken() * 0.5F; }
    public float resistance() { return Math.min(0.92F, activeForm().resist + activeKaioken() / 400.0F); }
    public float speedBonus() { return activeForm().speed + activeKaioken() / 800.0F; }
    public int extraHealth() { return activeForm().hearts + activeKaioken() / 5; }
    public float agility() { return activeForm().agility + activeKaioken() / 50.0F; }
    public int maxKi() { return activeForm().maxKi + activeKaioken() * 2; }
    public float flySpeed() { return activeForm().fly + activeKaioken() / 2000.0F; }

    /** Tiempo límite (ticks) de la fase elegida; el Kaioken lo reduce 10 s por cada x10. */
    public static int timeLimit(int form, int kaioken) {
        Form f = Form.byId(form);
        float minutes = f == Form.BASE ? 10.0F : f.minutes;
        int seconds = Math.max(30, (int) (minutes * 60) - kaioken);
        return seconds * 20;
    }

    public CompoundTag save() {
        CompoundTag t = new CompoundTag();
        t.putBoolean("Saiyan", saiyan);
        t.putInt("Selected", selected);
        t.putInt("Kaioken", kaioken);
        t.putBoolean("Transformed", transformed);
        t.putBoolean("Aura", aura);
        t.putBoolean("Flight", flight);
        t.putInt("Ki", ki);
        t.putInt("Timer", timer);
        t.putInt("TimerMax", timerMax);
        t.putInt("Cooldown", cooldown);
        t.putBoolean("KiInit", true);
        t.putBoolean("TotemReady", totemReady);
        t.putInt("Tier", tier);
        t.putBoolean("Charging", charging);
        t.putInt("Powering", powering);
        t.putInt("PoweringTotal", poweringTotal);
        return t;
    }

    public static SaiyanState load(CompoundTag t) {
        SaiyanState s = new SaiyanState();
        s.saiyan = t.getBoolean("Saiyan");
        s.selected = t.getInt("Selected");
        s.kaioken = t.getInt("Kaioken");
        s.transformed = t.getBoolean("Transformed");
        s.aura = t.getBoolean("Aura");
        s.flight = t.getBoolean("Flight");
        s.ki = t.getBoolean("KiInit") ? t.getInt("Ki") : 100;
        s.timer = t.getInt("Timer");
        s.timerMax = t.getInt("TimerMax");
        s.cooldown = t.getInt("Cooldown");
        s.totemReady = t.getBoolean("TotemReady");
        s.tier = Math.max(1, Math.min(5, t.getInt("Tier")));
        s.charging = t.getBoolean("Charging");
        s.powering = t.getInt("Powering");
        s.poweringTotal = t.getInt("PoweringTotal");
        return s;
    }

    public void write(FriendlyByteBuf b) {
        b.writeBoolean(saiyan);
        b.writeVarInt(selected);
        b.writeVarInt(kaioken);
        b.writeBoolean(transformed);
        b.writeBoolean(aura);
        b.writeBoolean(flight);
        b.writeVarInt(ki);
        b.writeVarInt(timer);
        b.writeVarInt(timerMax);
        b.writeVarInt(cooldown);
        b.writeBoolean(charging);
        b.writeVarInt(powering);
        b.writeVarInt(poweringTotal);
        b.writeBoolean(totemReady);
        b.writeVarInt(tier);
    }

    public static SaiyanState read(FriendlyByteBuf b) {
        SaiyanState s = new SaiyanState();
        s.saiyan = b.readBoolean();
        s.selected = b.readVarInt();
        s.kaioken = b.readVarInt();
        s.transformed = b.readBoolean();
        s.aura = b.readBoolean();
        s.flight = b.readBoolean();
        s.ki = b.readVarInt();
        s.timer = b.readVarInt();
        s.timerMax = b.readVarInt();
        s.cooldown = b.readVarInt();
        s.charging = b.readBoolean();
        s.powering = b.readVarInt();
        s.poweringTotal = b.readVarInt();
        s.totemReady = b.readBoolean();
        s.tier = b.readVarInt();
        return s;
    }
}
