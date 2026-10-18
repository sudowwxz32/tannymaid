package com.tanny.tannymaid.entity;

public enum TannyMode {
    FOLLOW("Seguir"),
    STAY("Quedarse aquí"),
    HOME("En casa"),
    FARM("Granja"),
    DEFEND("Defensa activa");

    private final String displayName;

    TannyMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /** Modos en los que Tanny camina detrás del Master. */
    public boolean follows() {
        return this == FOLLOW || this == DEFEND;
    }

    public static TannyMode byId(int id) {
        TannyMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : FOLLOW;
    }
}
