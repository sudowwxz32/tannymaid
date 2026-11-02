package com.bdsz.companion;

public enum KaoriMode {
    FOLLOW("Seguirte"),
    STAY("Quedarse aquí"),
    FARM("Agricultura"),
    DEFEND("Defensa activa");

    public final String display;

    KaoriMode(String display) {
        this.display = display;
    }

    public boolean follows() {
        return this == FOLLOW || this == DEFEND;
    }

    public static KaoriMode byId(int id) {
        KaoriMode[] v = values();
        return id >= 0 && id < v.length ? v[id] : FOLLOW;
    }
}
