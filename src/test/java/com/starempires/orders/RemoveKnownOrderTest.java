package com.starempires.orders;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoveKnownOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseUnknownRecipient() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, gm, "world world FROM foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown recipient")));
    }

    @Test
    void parseRemoveKnownWorld() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, gm, "world world FROM empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(world), order.getWorlds());
    }

    @Test
    void parseRemoveKnownPortal() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, gm, "portal portal FROM empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(portal), order.getPortals());
    }

    @Test
    void parseRemoveKnownStorm() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, gm, "storm storm FROM empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(storm), order.getStorms());
    }

    @Test
    void parseRemoveKnownShipclass() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, gm, "shipclass frigate FROM empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(frigateClass), order.getShipClasses());
    }

    @Test
    void parseRemoveKnownContact() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, gm, "contact empire2 FROM empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(empire2), order.getContacts());
    }

    @Test
    void parseUnknownObject() {
        final RemoveKnownOrder order = RemoveKnownOrder.parse(turnData, gm, "contact empire3 FROM empire1");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown")));
    }
}