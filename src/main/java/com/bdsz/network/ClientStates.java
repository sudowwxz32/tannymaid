package com.bdsz.network;

import com.bdsz.data.SaiyanState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Estados conocidos por el cliente (por id de entidad). Clase sin dependencias de cliente. */
public final class ClientStates {
    private static final Map<Integer, SaiyanState> STATES = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> TRANSFORM_AT = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> DRAGON_AT = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> OZARU_AT = new ConcurrentHashMap<>();
    /** Reloj del cliente (ticks), lo avanza ClientForgeEvents. */
    public static long clientTicks;
    private static final SaiyanState NONE = new SaiyanState();

    private ClientStates() {}

    public static void put(int id, SaiyanState s) {
        SaiyanState old = STATES.get(id);
        boolean newForm = old != null && s.transformed && (!old.transformed || old.selected != s.selected || old.kaioken != s.kaioken);
        if (newForm) TRANSFORM_AT.put(id, clientTicks);
        if (s.dragonFist > 0 && (old == null || old.dragonFist <= 0)) DRAGON_AT.put(id, clientTicks);
        if (s.ozaru && old != null && !old.ozaru) OZARU_AT.put(id, clientTicks);
        STATES.put(id, s);
    }

    /** Ticks desde que apareció el dragón del Puño del Dragón. */
    public static long ticksSinceDragon(int id) {
        Long t = DRAGON_AT.get(id);
        return t == null ? Long.MAX_VALUE : clientTicks - t;
    }

    /** Ticks desde la última transformación de ese jugador (para la pose y el temblor). */
    public static long ticksSinceTransform(int id) {
        Long t = TRANSFORM_AT.get(id);
        return t == null ? Long.MAX_VALUE : clientTicks - t;
    }

    /** Ticks desde que ese jugador se convirtió en Ozaru (para la animación de crecer y rugir). */
    public static long ticksSinceOzaru(int id) {
        Long t = OZARU_AT.get(id);
        return t == null ? Long.MAX_VALUE : clientTicks - t;
    }

    public static SaiyanState get(int id) {
        return STATES.getOrDefault(id, NONE);
    }

    public static void clear() {
        STATES.clear();
        TRANSFORM_AT.clear();
        OZARU_AT.clear();
        DRAGON_AT.clear();
    }
}
