package com.starempires.orders;

import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DenyOrderTest extends BaseTest {

    private static final int RADIUS = 3;
    private Ship probe;

    @BeforeEach
    public void setUp() {
        empire1.addKnownEmpire(empire2);
        empire1.addKnownWorld(world);
        probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
    }

    @Test
    void parseInvalidFormat() {
        final DenyOrder order = DenyOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNoValidRecipients() {
        final DenyOrder order = DenyOrder.parse(turnData, empire1, ONE_COORDINATE + " " + RADIUS + " to unknown");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("No valid recipients")));
    }

    @Test
    void parseDenyCoordinate() {
        final DenyOrder order = DenyOrder.parse(turnData, empire1, ONE_COORDINATE + " " + RADIUS + " to empire2");
        assertTrue(order.isReady());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
        assertNull(order.getMapObject());
        assertEquals(RADIUS, order.getRadius());
        assertTrue(order.getShips().isEmpty());
        assertFalse(order.isAllSectors());
        assertEquals(List.of(empire2), order.getRecipients());
    }

    @Test
    void parseDenyLocation() {
        final DenyOrder order = DenyOrder.parse(turnData, empire1, "@" + world + " " + RADIUS + " to empire2");
        assertTrue(order.isReady());
        assertEquals(world, order.getMapObject());
        assertNull(order.getCoordinate());
        assertEquals(RADIUS, order.getRadius());
        assertTrue(order.getShips().isEmpty());
        assertFalse(order.isAllSectors());
        assertEquals(List.of(empire2), order.getRecipients());
    }

    @Test
    void parseDenyShipLoaded() {
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        turnData.load(probe, carrier);
        final DenyOrder order = DenyOrder.parse(turnData, empire1, probe + " to empire2");
        assertTrue(order.isReady());
        assertNull(order.getMapObject());
        assertNull(order.getCoordinate());
        assertFalse(order.isAllSectors());
        assertEquals(0, order.getRadius());
        assertTrue(order.getShips().contains(probe));
    }

    @Test
    void parseDenyShip() {
        final DenyOrder order = DenyOrder.parse(turnData, empire1, probe + " to empire2");
        assertTrue(order.isReady());
        assertNull(order.getMapObject());
        assertNull(order.getCoordinate());
        assertFalse(order.isAllSectors());
        assertEquals(0, order.getRadius());
        assertEquals(List.of(probe), order.getShips());
    }

    @Test
    void parseDenyAll() {
        final DenyOrder order = DenyOrder.parse(turnData, empire1, "all to empire2");
        assertTrue(order.isReady());
        assertNull(order.getMapObject());
        assertNull(order.getCoordinate());
        assertEquals(0, order.getRadius());
        assertTrue(order.getShips().isEmpty());
        assertTrue(order.isAllSectors());
    }
}