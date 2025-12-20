package com.starempires.orders;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddShipOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final AddShipOrder order = AddShipOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void parseUnknownOwner() {
        final String params = ONE_COORDINATE + " unknown 1 fighter 5 private f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown owner")));
    }

    @Test
    void parseUnknownShipClass() {
        final String params = ONE_COORDINATE + " empire1 1 unknown 5 private f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown ship class")));
    }

    @Test
    void parseOneShipNamed() {
        final String params = ONE_COORDINATE + " empire1 1 fighter 3 private f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals(OrderType.ADDSHIP, order.getOrderType());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
        assertEquals(List.of("f1"), order.getNames());
        assertEquals(1, order.getCount());
        assertEquals(empire1, order.getOwner());
        assertEquals(3, order.getDp());
        assertFalse(order.isPublicMode());
        assertEquals(fighterClass, order.getShipClass());
        assertEquals(fighterClass, empire1.getShip("f1").getShipClass());
    }

    @Test
    void parseOneShipDPExceedsMax() {
        final String params = ONE_COORDINATE + " empire1 1 fighter 15 private f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("exceeds max")));
    }

    @Test
    void parseMultipleShipsNamed() {
        final String params = ONE_COORDINATE + " empire1 2 fighter 5 private f1 f2";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals(OrderType.ADDSHIP, order.getOrderType());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
        assertEquals(List.of("f1", "f2"), order.getNames());
        assertNull(order.getBasename());
        assertEquals(2, order.getCount());
        assertEquals(5, order.getDp());
        assertFalse(order.isPublicMode());
        assertEquals(empire1, order.getOwner());
        assertEquals(fighterClass, order.getShipClass());
    }

    @Test
    void parseMultipleShipsWildcardName() {
        final String params = ONE_COORDINATE + " empire1 2 fighter 5 private f*";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals(OrderType.ADDSHIP, order.getOrderType());
        assertEquals(ONE_COORDINATE, order.getCoordinate());
        assertEquals("f", order.getBasename());
        assertTrue(order.getNames().isEmpty());
        assertEquals(2, order.getCount());
        assertEquals(5, order.getDp());
        assertFalse(order.isPublicMode());
        assertEquals(empire1, order.getOwner());
        assertEquals(fighterClass, order.getShipClass());
    }

    @Test
    void parseMultipleShipsTooFewNames() {
        final String params = ONE_COORDINATE + " empire1 2 fighter 5 private f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertEquals(OrderType.ADDSHIP, order.getOrderType());
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("does not match count")));
    }
}