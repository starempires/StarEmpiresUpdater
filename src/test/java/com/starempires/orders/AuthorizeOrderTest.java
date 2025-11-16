package com.starempires.orders;

import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizeOrderTest extends BaseTest {

    private Ship probe;

    @BeforeEach
    public void setUp() {
        empire1.addKnownEmpire(empire2);
        empire1.addKnownWorld(world);
        probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
    }

    @Test
    void parseInvalidFormat() {
        final AuthorizeOrder order = AuthorizeOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNoValidRecipients() {
        final AuthorizeOrder order = AuthorizeOrder.parse(turnData, empire1, ONE_COORDINATE + " to unknown");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("No valid recipients")));
    }

    @Test
    void parseAuthCoordinate() {
        final AuthorizeOrder order = AuthorizeOrder.parse(turnData, empire1, ZERO_COORDINATE + " " + ONE_COORDINATE + " to empire2");
        assertTrue(order.isReady());
        assertEquals(List.of(ZERO_COORDINATE, ONE_COORDINATE), order.getCoordinates());
        assertTrue(order.getMapObjects().isEmpty());
        assertTrue(order.getShips().isEmpty());
        assertFalse(order.isAllSectors());
        assertEquals(List.of(empire2), order.getRecipients());
    }

    @Test
    void parseAuthLocation() {
        final AuthorizeOrder order = AuthorizeOrder.parse(turnData, empire1, "@" + world + " to empire2");
        assertTrue(order.isReady());
        assertEquals(List.of(world), order.getMapObjects());
        assertTrue(order.getCoordinates().isEmpty());
        assertTrue(order.getShips().isEmpty());
        assertFalse(order.isAllSectors());
        assertEquals(List.of(empire2), order.getRecipients());
    }

    @Test
    void parseAuthUnknownLocation() {
        final AuthorizeOrder order = AuthorizeOrder.parse(turnData, empire1, "@unknown to empire2");
        assertFalse(order.isReady());
        assertTrue(order.getMapObjects().isEmpty());
        assertTrue(order.getCoordinates().isEmpty());
        assertTrue(order.getShips().isEmpty());
        assertFalse(order.isAllSectors());
        assertEquals(List.of(empire2), order.getRecipients());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown location")));
    }

    @Test
    void parseAuthShipLoaded() {
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        turnData.load(probe, carrier);
        final AuthorizeOrder order = AuthorizeOrder.parse(turnData, empire1, probe + " to empire2");
        assertFalse(order.isReady());
        assertTrue(order.getMapObjects().isEmpty());
        assertTrue(order.getCoordinates().isEmpty());
        assertFalse(order.isAllSectors());
        assertTrue(order.getShips().isEmpty());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Loaded ships cannot share")));
    }

    @Test
    void parseAuthShip() {
        final AuthorizeOrder order = AuthorizeOrder.parse(turnData, empire1, probe + " to empire2");
        assertTrue(order.isReady());
        assertTrue(order.getMapObjects().isEmpty());
        assertTrue(order.getCoordinates().isEmpty());
        assertFalse(order.isAllSectors());
        assertEquals(List.of(probe), order.getShips());
    }

    @Test
    void parseAuthAll() {
        final AuthorizeOrder order = AuthorizeOrder.parse(turnData, empire1, "all to empire2");
        assertTrue(order.isReady());
        assertTrue(order.getMapObjects().isEmpty());
        assertTrue(order.getCoordinates().isEmpty());
        assertTrue(order.getShips().isEmpty());
        assertTrue(order.isAllSectors());
    }
}