package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Prohibition {

    NONE,
    BLOCKADED,
    INTERDICTED;

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
