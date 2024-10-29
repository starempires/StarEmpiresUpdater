package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonValue;

public enum HullType {

    CAPITAL_SHIP,
    DEVICE,
    GUNSHIP,
    MISSILE,
    ORBITAL,
    SCOUT,
    TRANSPORT,
    WING;

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase().replace('_', ' ');
    }
}
