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
    public boolean mastery;     // Dominio Total: tiempo infinito
    public int pose;            // pose de ataque: 0 ninguna, 1 carga Kamehameha, 2 dispara Kamehameha, 3 Big Bang, 4 Puño del Dragón
    public int poseTimer;
    public int dragonFist;      // ticks restantes para dar el golpe del Puño del Dragón
    public int dragonCooldown;  // ticks de espera para volver a usarlo
    public int attackCooldown;  // espera corta entre Kamehameha / Big Bang
    public boolean ozaru;       // Modo Ozaru: mono gigante (tecla O)
    public int gender;          // 0 sin definir, 1 masculino, 2 femenino
    public int hair = -1;       // peinado base elegido (-1 = el predeterminado de cada transformación)
    public boolean tail;        // cola de saiyajin (café)

    public static final int POSE_KAME_CHARGE = 1, POSE_KAME_FIRE = 2, POSE_BIG_BANG = 3, POSE_DRAGON = 4;

    public boolean unlocked(Form f) { return f.tier() == 6 ? tier >= 5 && mastery : f.tier() <= tier; }

    /** El Modo Ozaru se desbloquea con el Dominio Total. */
    public boolean ozaruUnlocked() { return tier >= 5 && mastery; }

    // ---- Modo Ozaru: gigante, muy fuerte y resistente, pero lento para atacar
    public static final float OZARU_SCALE = 3.5F;

    public Form activeForm() { return transformed ? Form.byId(selected) : Form.BASE; }
    /** Fase que se ve en el aura (durante la carga ya se ve la fase destino). */
    public Form visibleForm() { return transformed || powering > 0 ? Form.byId(selected) : Form.BASE; }
    public int activeKaioken() { return transformed ? kaioken : 0; }
    public boolean powered() { return ozaru || transformed && (selected != 0 || kaioken > 0); }

    public float damageMultiplier() { return ozaru ? 12.0F : activeForm().damage * (1.0F + activeKaioken() / 20.0F); }
    public float punchBonus() { return ozaru ? 400.0F : activeForm().punch + activeKaioken() * 0.5F; }
    public float resistance() { return ozaru ? 0.75F : Math.min(0.92F, activeForm().resist + activeKaioken() / 400.0F); }
    public float speedBonus() { return ozaru ? 0.35F : activeForm().speed + activeKaioken() / 800.0F; }
    public int extraHealth() { return ozaru ? 240 : activeForm().hearts + activeKaioken() / 5; }
    public float agility() { return ozaru ? 1.0F : activeForm().agility + activeKaioken() / 50.0F; }
    public int maxKi() { return ozaru ? 1500 : activeForm().maxKi + activeKaioken() * 2; }
    /** Ki infinito (Legendario). */
    public boolean infiniteKi() { return transformed && activeForm().infiniteKi(); }
    public float flySpeed() { return activeForm().fly + activeKaioken() / 2000.0F; }

    /** Tiempo límite (ticks) de la fase elegida; el Kaioken lo reduce 10 s por cada x10. */
    /** Tiempo límite en ticks; -1 = infinito (Dominio Total). */
    public static int timeLimit(int form, int kaioken, boolean mastery) {
        Form f = Form.byId(form);
        if (f.legendary3()) return -1;                 // sin tiempo límite
        if (mastery) {
            // con Kaioken, de la Fase 3 al Ultra Ego deja de ser infinito pero sube a 40 minutos
            if (kaioken > 0 && f.ordinal() >= Form.SSJ3.ordinal()) return 40 * 60 * 20;
            return -1;
        }
        return timeLimit(form, kaioken);
    }

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
        t.putBoolean("Mastery", mastery);
        t.putInt("Pose", pose);
        t.putInt("PoseTimer", poseTimer);
        t.putInt("DragonFist", dragonFist);
        t.putInt("DragonCooldown", dragonCooldown);
        t.putInt("AttackCooldown", attackCooldown);
        t.putBoolean("Charging", charging);
        t.putInt("Powering", powering);
        t.putInt("PoweringTotal", poweringTotal);
        t.putBoolean("Ozaru", ozaru);
        t.putInt("Gender", gender);
        t.putInt("Hair", hair);
        t.putBoolean("HasHair", true);
        t.putBoolean("Tail", tail);
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
        s.mastery = t.getBoolean("Mastery");
        s.pose = t.getInt("Pose");
        s.poseTimer = t.getInt("PoseTimer");
        s.dragonFist = t.getInt("DragonFist");
        s.dragonCooldown = t.getInt("DragonCooldown");
        s.attackCooldown = t.getInt("AttackCooldown");
        s.charging = t.getBoolean("Charging");
        s.powering = t.getInt("Powering");
        s.poweringTotal = t.getInt("PoweringTotal");
        s.ozaru = t.getBoolean("Ozaru");
        s.gender = t.getInt("Gender");
        s.hair = t.getBoolean("HasHair") ? t.getInt("Hair") : -1;
        s.tail = t.getBoolean("Tail");
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
        b.writeBoolean(mastery);
        b.writeVarInt(pose);
        b.writeVarInt(poseTimer);
        b.writeVarInt(dragonFist);
        b.writeVarInt(dragonCooldown);
        b.writeBoolean(ozaru);
        b.writeVarInt(gender);
        b.writeVarInt(hair + 1);
        b.writeBoolean(tail);
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
        s.mastery = b.readBoolean();
        s.pose = b.readVarInt();
        s.poseTimer = b.readVarInt();
        s.dragonFist = b.readVarInt();
        s.dragonCooldown = b.readVarInt();
        s.ozaru = b.readBoolean();
        s.gender = b.readVarInt();
        s.hair = b.readVarInt() - 1;
        s.tail = b.readBoolean();
        return s;
    }
}
