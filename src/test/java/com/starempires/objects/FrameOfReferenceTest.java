package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrameOfReferenceTest extends BaseTest {
    private final static FrameOfReference FOR_MIRRORS = FrameOfReference.builder()
            .obliqueOffset(1)
            .yOffset(1)
            .horizontalMirror(true)
            .verticalMirror(true)
            .rotation(HexDirection.DIRECTION_60)
            .build();

    private final static FrameOfReference FOR_NO_MIRRORS = FrameOfReference.builder()
            .obliqueOffset(1)
            .yOffset(1)
            .rotation(HexDirection.DIRECTION_60)
            .build();

    @Test
    void toLocal() {
        assertEquals(new Coordinate(-1, 0), FOR_MIRRORS.toLocal(ZERO_COORDINATE));
        assertEquals(new Coordinate(1, 0), FOR_NO_MIRRORS.toLocal(ZERO_COORDINATE));
    }

    @Test
    void toGalactic() {
        assertEquals(ZERO_COORDINATE, FOR_MIRRORS.toGalactic(new Coordinate(-1, 0)));
        assertEquals(ZERO_COORDINATE, FOR_NO_MIRRORS.toGalactic(new Coordinate(1, 0)));
    }
}