package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Prohibition {

    NONE,
    BLOCKADED,
    INTERDICTED;

    public static final class ProhibitionNoneFilter {
        @Override public boolean equals(Object other) { return other == Prohibition.NONE; }
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}