package com.starempires.orders;

import com.google.common.collect.Lists;
import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelocateShipOrderTest extends BaseTest {

    private Ship ship;

    @BeforeEach
    void setUp() {
        ship = createShip(fighterClass, ZERO_COORDINATE, "ship", empire1);
    }

    @Test
    void parseInvalidFormat() {
        final RelocateShipOrder order = RelocateShipOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final RelocateShipOrder order = RelocateShipOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseMoveShip() {
        final String params = "empire1 ship to " + ONE_COORDINATE;
        final RelocateShipOrder order = RelocateShipOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals(empire1, order.getOwner());
        assertEquals(Lists.newArrayList(ship), order.ships);
        assertEquals(ONE_COORDINATE, order.getCoordinate());
    }

    @Test
    void parseUnknownEmpire() {
        final String params = "foo ship to " + ONE_COORDINATE;
        final RelocateShipOrder order = RelocateShipOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown owning empire")));
    }

    @Test
    void parseMoveShipWrongOwner() {
        final String params = "empire2 ship to " + ONE_COORDINATE;
        final RelocateShipOrder order = RelocateShipOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown ship")));
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("No valid ships")));
    }
}