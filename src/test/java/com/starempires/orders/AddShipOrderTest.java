package com.starempires.orders;

import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddShipOrderTest extends BaseTest {

    @BeforeEach
    void setUp() {
    }

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
        final String params = ONE_COORDINATE + " unknown 1 fighter f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown owner")));
    }

    @Test
    void parseUnknownShipClass() {
        final String params = ONE_COORDINATE + " empire1 1 unknown f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown ship class")));
    }

    @Test
    void parseOneShipNamed() {
        final String params = ONE_COORDINATE + " empire1 1 fighter f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        final Ship ship = order.getShips().get(0);
        assertEquals(ONE_COORDINATE, ship.getCoordinate());
        assertEquals(fighterClass.getDp(), ship.getDpRemaining());
        assertEquals("f1", ship.getName());
        assertEquals(empire1, ship.getOwner());
        assertEquals(fighterClass, ship.getShipClass());
        assertEquals(turnData.getTurnNumber(), ship.getTurnBuilt());
    }

    @Test
    void parseMultipleShipsNamed() {
        final String params = ONE_COORDINATE + " empire1 2 fighter f1 f2";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals("f1", order.getShips().get(0).getName());
        assertEquals("f2", order.getShips().get(1).getName());
    }

    @Test
    void parseMultipleShipsWildcardName() {
        final String params = ONE_COORDINATE + " empire1 2 fighter f*";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        assertEquals("f1", order.getShips().get(0).getName());
        assertEquals("f2", order.getShips().get(1).getName());
    }

    @Test
    void parseMultipleShipsTooFewNames() {
        final String params = ONE_COORDINATE + " empire1 2 fighter f1";
        final AddShipOrder order = AddShipOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Number of names does not match number of ships")));
    }
}