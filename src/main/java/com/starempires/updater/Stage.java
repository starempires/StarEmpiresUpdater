package com.starempires.updater;

public enum Stage {
    ADMINISTRATION,
    ASTRONOMICS,
    COMBAT,
    INCOME,
    LOGISTICS,
    MAINTENANCE,
    MOVEMENT,
    RESEARCH,
    SCANNING;

    @Override
    public String toString() {
        String s = name().toLowerCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
