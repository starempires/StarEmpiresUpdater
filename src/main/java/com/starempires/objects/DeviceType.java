package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public enum DeviceType {
    @JsonProperty("portal_hammer")
    PORTAL_HAMMER,
    @JsonProperty("ion_shield")
    ION_SHIELD;

    @Override
    public @NotNull String toString() {
        return name().toLowerCase().replace('_', ' ');
    }
}