package com.linterna.network;

import com.linterna.data.LanternState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Estados de los jugadores vistos por el cliente. */
public final class ClientStates {
    private static final Map<Integer, LanternState> STATES = new ConcurrentHashMap<>();

    private ClientStates() {}

    public static void put(int id, LanternState s) { STATES.put(id, s); }

    public static LanternState get(int id) { return STATES.getOrDefault(id, EMPTY); }

    public static void clear() { STATES.clear(); }

    private static final LanternState EMPTY = new LanternState();
}
