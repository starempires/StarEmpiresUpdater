package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonValue;
import org.jetbrains.annotations.NotNull;

public enum DeviceType {
    ION_SHIELD,
    PORTAL_HAMMER;

    @JsonValue
    @Override
    public @NotNull String toString() {
        return name().toLowerCase().replace('_', ' ');
    }
}