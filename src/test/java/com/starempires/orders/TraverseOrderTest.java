package com.starempires.orders;

import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraverseOrderTest extends BaseTest {

    private Ship probe;

    @BeforeEach
    void setUp() {
        probe = createShip(probeClass, ZERO_COORDINATE, "probe", empire1);
        empire1.addKnownPortal(portal);
    }

    @Test
    void testParseInvalidOrder() {
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid TRAVERSE order")));
    }

    @Test
    void testParseMoversNotSameSector() {
        final Ship probe2 = createShip(probeClass, ONE_COORDINATE, "probe2", empire1);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe probe2 through portal");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("not all in same sector")));
    }

    @Test
    void testParseMoversLoaded() {
        final Ship carrier = createShip(carrierClass, ZERO_COORDINATE, "carrier", empire1);
        turnData.load(probe, carrier);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Loaded ships cannot move")));
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("No valid movers")));
    }

    @Test
    void testParseMoversNoEngines() {
        final Ship starbase = createShip(starbaseClass, ZERO_COORDINATE, "starbase", empire1);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "starbase through portal");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("No operational engines")));
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("No valid movers")));
    }

    @Test
    void testParseMoversAlreadyFired() {
        probe.setOrderedToFire(true);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("ordered to fire")));
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("No valid movers")));
    }

    @Test
    void testParseUnknownEntry() {
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through unknown");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown entry portal")));
    }

    @Test
    void testParseNotInEntrySector() {
        portal.setCoordinate(ONE_COORDINATE);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("not in same sector")));
    }

    @Test
    void testParseEntryCollapsed() {
        portal.setCollapsed(true);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal");
        assertTrue(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("collapsed")));
    }

    @Test
    void testParseSuccessNoExitPortal() {
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal");
        assertTrue(order.isReady());
        assertEquals(List.of(probe), order.getShips());
        assertEquals(portal, order.getEntry());
        assertNull(order.getExit());
    }

    @Test
    void testParseUnknownExit() {
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal unknown");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown exit")));
    }

    @Test
    void testParseNoNavData() {
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        empire1.addKnownPortal(exit);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal exit");
        assertTrue(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("No nav data")));
    }

    @Test
    void testParseDisconnectedPortals() {
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        empire1.addKnownPortal(exit);
        empire1.addNavData(exit);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal exit");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("not connected")));
    }

    @Test
    void testParseSuccessWithExitPortal() {
        final Portal exit = createPortal("exit", ONE_COORDINATE, false);
        empire1.addKnownPortal(exit);
        empire1.addNavData(exit);
        portal.addConnection(exit);
        final TraverseOrder order = TraverseOrder.parse(turnData, empire1, "probe through portal exit");
        assertTrue(order.isReady());
        assertEquals(List.of(probe), order.getShips());
        assertEquals(portal, order.getEntry());
        assertEquals(exit, order.getExit());
    }
}