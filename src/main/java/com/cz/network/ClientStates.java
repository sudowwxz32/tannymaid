package com.cz.network;

import com.cz.data.ZorroState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Estados que conoce el cliente (por id de entidad). */
public final class ClientStates {
    private static final Map<Integer, ZorroState> STATES = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> DONE_AT = new ConcurrentHashMap<>();
    private static final ZorroState NONE = new ZorroState();
    public static long clientTicks;

    private ClientStates() {}

    public static void put(int id, ZorroState s) {
        ZorroState old = STATES.get(id);
        if (old != null && s.phase != 0 && s.powering == 0 && (old.phase != s.phase || old.powering > 0)) DONE_AT.put(id, clientTicks);
        STATES.put(id, s);
    }

    public static ZorroState get(int id) {
        return STATES.getOrDefault(id, NONE);
    }

    /** Ticks desde que ese jugador terminó de transformarse (para el estallido y la onda). */
    public static long sinceDone(int id) {
        Long t = DONE_AT.get(id);
        return t == null ? Long.MAX_VALUE : clientTicks - t;
    }

    public static void clear() {
        STATES.clear();
        DONE_AT.clear();
    }
}
