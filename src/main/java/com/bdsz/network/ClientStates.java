package com.bdsz.network;

import com.bdsz.data.SaiyanState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Estados conocidos por el cliente (por id de entidad). Clase sin dependencias de cliente. */
public final class ClientStates {
    private static final Map<Integer, SaiyanState> STATES = new ConcurrentHashMap<>();
    private static final SaiyanState NONE = new SaiyanState();

    private ClientStates() {}

    public static void put(int id, SaiyanState s) {
        STATES.put(id, s);
    }

    public static SaiyanState get(int id) {
        return STATES.getOrDefault(id, NONE);
    }

    public static void clear() {
        STATES.clear();
    }
}
