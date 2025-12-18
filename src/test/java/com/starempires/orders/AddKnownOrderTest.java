package com.starempires.orders;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddKnownOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseUnknownRecipient() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "world world TO foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown recipient")));
    }

    @Test
    void parseAddKnownWorld() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "world world TO empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(world), order.getWorlds());
    }

    @Test
    void parseAddKnownPortal() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "portal portal TO empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(portal), order.getPortals());
    }

    @Test
    void parseAddKnownNavData() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "navdata portal TO empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(portal), order.getNavData());
    }

    @Test
    void parseAddKnownNavDataUnknownPortal() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "navdata unknown TO empire1");
        assertFalse(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertTrue(order.getNavData().isEmpty());
    }

    @Test
    void parseAddKnownStorm() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "storm storm TO empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(storm), order.getStorms());
    }

    @Test
    void parseAddKnownShipclass() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "shipclass frigate TO empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(frigateClass), order.getShipClasses());
    }

    @Test
    void parseAddKnownContact() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "contact empire2 TO empire1");
        assertTrue(order.isReady());
        assertEquals(List.of(empire1), order.getRecipients());
        assertEquals(List.of(empire2), order.getContacts());
    }

    @Test
    void parseUnknownObject() {
        final AddKnownOrder order = AddKnownOrder.parse(turnData, gm, "contact empire3 TO empire1");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown")));
    }
}