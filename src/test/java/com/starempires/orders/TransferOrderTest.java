package com.starempires.orders;

import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferOrderTest extends BaseTest {

    private World destination;

    @BeforeEach
    public void before() {
        destination = createWorld("destination", ONE_COORDINATE, 12);
        destination.setOwner(empire1);
        empire1.addKnownWorld(destination);
    }

    @Test
    void testParseInvalidOrder() {
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid TRANSFER order")));
    }

    @Test
    void testParseNonExistentWorld() {
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "unknown 2 destination");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("You do not own world")));
    }

    @Test
    void testParseUnknownWorld() {
        world.setOwner(empire2);
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world 2 destination");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("You do not own world")));
    }

    @Test
    void testParseUnknownDestination() {
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world 2 unknown");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown world")));
    }

    @Test
    void testParseSameWorld() {
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world 2 world");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Cannot transfer to same world")));
    }

    @Test
    void testParseDestinationNotOwned() {
        destination.setOwner(null);
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world 2 destination");
        assertTrue(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("You do not currently own world")));
        assertEquals(2, order.getAmount());
        assertEquals(destination, order.getToWorld());
    }

    @Test
    void testParseUnknownRecipient() {
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world 2 destination empire2");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("You have no contact")));
    }

    @Test
    void testParseDestinationWrongOwner() {
        empire1.addKnownEmpire(empire2);
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world 2 destination empire2");
        assertTrue(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("does not currently own")));
    }

    @Test
    void testParseAllRU() {
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world max destination");
        assertTrue(order.isReady());
        assertEquals(0, order.getAmount());
        assertEquals(destination, order.getToWorld());
    }

    @Test
    void testParseExceedStockpile() {
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world 200 destination");
        assertTrue(order.isReady());
        assertEquals(200, order.getAmount());
        assertEquals(destination, order.getToWorld());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("stockpile present")));
    }

    @Test
    void testParseInvalidAmount() {
        final TransferOrder order = TransferOrder.parse(turnData, empire1, "world 0 destination");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid transfer amount")));
    }
}