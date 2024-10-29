package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonValue;
import org.jetbrains.annotations.NotNull;

public enum DeviceType {
    ION_GENERATOR,
    ION_SHIELD,
    POLLUTION_BOMB,
    PORTAL_HAMMER;

    @JsonValue
    @Override
    public @NotNull String toString() {
        return name().toLowerCase().replace('_', ' ');
    }
}
