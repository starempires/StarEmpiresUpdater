package com.starempires.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class CoordinateTest {

    private final Coordinate c1 = new Coordinate(0, 0);
    private final Coordinate c2 = new Coordinate(1, 0);
    private final Coordinate[] radius1Coords = {
            new Coordinate(0, 0),
            new Coordinate(0, 1),
            new Coordinate(0, -1),
            new Coordinate(1, 0),
            new Coordinate(-1, 0),
            new Coordinate(-1, -1),
            new Coordinate(1, 1)
    };

    @Test
    public void testCompareTo() {
        assertEquals(0, c1.compareTo(new Coordinate(0,0)));
        assertTrue(c1.compareTo(new Coordinate(-1,-1)) > 0);
        assertTrue(c1.compareTo(new Coordinate(0,-1)) > 0);
        assertTrue(c1.compareTo(new Coordinate(1,1)) < 0);
        assertTrue(c1.compareTo(new Coordinate(0,1)) < 0);
    }

    @Test
    public void testToString() {
        assertEquals("(0,0)", c1.toString());
    }

    @Test
    public void testGetName() {
        assertEquals("0_0", c1.getName());
    }

    @Test
    public void testDelta() {
        final Coordinate delta = Coordinate.delta(c1, c2);
        assertEquals(1, delta.getOblique());
        assertEquals(0, delta.getY());
    }

    @Test
    public void testDistance() {
        assertEquals(1, Coordinate.distance(c1, c2));
        assertEquals(0, Coordinate.distance(c1, c1));
        assertEquals(1, Coordinate.distance(c2, new Coordinate(2, 1)));
        assertEquals(2, Coordinate.distance(c2, new Coordinate(2, -1)));
        final Coordinate c = new Coordinate(1,1);
        final Set<Coordinate> ring = Coordinate.getSurroundingCoordinates(new Coordinate(1,1), 1);
        ring.remove(c);
        for (final Coordinate coord: ring) {
            assertEquals(1, Coordinate.distance(c, coord));
        }
    }

    @Test
    public void testDistanceTo() {
        assertEquals(1, c1.distanceTo(c2));
    }

    @Test
    public void testParseList() {
        assertTrue(Coordinate.parse(Lists.newArrayList()).isEmpty());
        assertEquals(Lists.newArrayList(c1), Coordinate.parse(Lists.newArrayList("0,0")));
    }

    @Test
    public void testParse() {
        assertNull(Coordinate.parse(""));
        assertNull(Coordinate.parse("0,"));
        assertEquals(c1, Coordinate.parse("0,0"));
    }

    @Test
    public void testGetSurroundingCoordinatesObject() {
        final Storm storm = Storm.builder().coordinate(c1).build();
        assertEquals(Sets.newHashSet(Arrays.asList(radius1Coords)), Coordinate.getSurroundingCoordinates(storm, 1));
    }

    @Test
    public void testGetSurroundingCoordinates() {
        assertEquals(Sets.newHashSet(Arrays.asList(radius1Coords)), Coordinate.getSurroundingCoordinates(c1, 1));
    }

    @Test
    public void testGetSurroundingRing() {
        final Set<Coordinate> ring = Sets.newHashSet(Arrays.asList(radius1Coords));
        ring.remove(c1);
        assertEquals(ring, Coordinate.getSurroundingRing(1));
        assertEquals(Collections.emptySet(), Coordinate.getSurroundingRing(0));
    }

    @Test
    public void testTranslate() {
        assertEquals(c2, new Coordinate(0,0).translate(c2));
    }

    @Test
    public void testRotate() {
        assertEquals(new Coordinate(1,0), new Coordinate(1, 0).rotate(HexDirection.DIRECTION_0));
        assertEquals(new Coordinate(0, -1), new Coordinate(1, 0).rotate(HexDirection.DIRECTION_60));
        assertEquals(new Coordinate(-1, -1), new Coordinate(1, 0).rotate(HexDirection.DIRECTION_120));
        assertEquals(new Coordinate(-1, 0), new Coordinate(1, 0).rotate(HexDirection.DIRECTION_180));
        assertEquals(new Coordinate(0, 1), new Coordinate(1, 0).rotate(HexDirection.DIRECTION_240));
        assertEquals(new Coordinate(1, 1), new Coordinate(1, 0).rotate(HexDirection.DIRECTION_300));
    }

    @Test
    public void testHorizontalMirror() {
        assertEquals(new Coordinate(1, 1), new Coordinate(1, 0).horizontalMirror());
    }

    @Test
    public void testVerticalMirror() {
        assertEquals(new Coordinate(-1, -1), new Coordinate(1, 0).verticalMirror());
    }

    @Test
    public void testNegate() {
        assertEquals(new Coordinate(-1, 0), Coordinate.negate(new Coordinate(1, 0)));
    }
}