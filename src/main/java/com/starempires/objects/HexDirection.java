package com.starempires.objects;

public enum HexDirection {

    DIRECTION_0,
    DIRECTION_60,
    DIRECTION_120,
    DIRECTION_180,
    DIRECTION_240,
    DIRECTION_300;

    private static final HexDirection[] directions = {
            DIRECTION_0,
            DIRECTION_60,
            DIRECTION_120,
            DIRECTION_180,
            DIRECTION_240,
            DIRECTION_300,
    };

    public static HexDirection from(final int direction) {
        return directions[direction];
    }

    public static HexDirection opposite(final HexDirection direction) {
        return switch (direction) {
            case DIRECTION_0 -> DIRECTION_0;
            case DIRECTION_60 -> DIRECTION_300;
            case DIRECTION_120 -> DIRECTION_240;
            case DIRECTION_180 -> DIRECTION_180;
            case DIRECTION_240 -> DIRECTION_120;
            case DIRECTION_300 -> DIRECTION_60;
        };
    }
}