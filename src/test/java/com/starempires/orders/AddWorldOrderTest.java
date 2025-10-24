package com.starempires.orders;

import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddWorldOrderTest extends BaseTest {

    @Test
    void parseInvalidFormat() {
        final AddWorldOrder order = AddWorldOrder.parse(turnData, gm, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Invalid")));
    }

    @Test
    void parseNotGM() {
        final AddWorldOrder order = AddWorldOrder.parse(turnData, empire1, "foo");
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("available only to GM")));
    }

    @Test
    void testAddWorldUnowned() {
        final String name = "world1";
        final int production = 5;
        final int stockpile = 10;
        final String params = ONE_COORDINATE + " " + name + " " + production + " " + stockpile;
        final AddWorldOrder order = AddWorldOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        final World world = order.getWorld();
        assertEquals(ONE_COORDINATE, world.getCoordinate());
        assertEquals(name, world.getName());
        assertEquals(production, world.getProduction());
        assertEquals(stockpile, world.getStockpile());
        assertNull(world.getOwner());
    }

    @Test
    void testAddWorldOwned() {
        final String name = "world1";
        final int production = 5;
        final int stockpile = 10;
        final String params = ONE_COORDINATE + " " + name + " " + production + " " + stockpile + " " + empire1;
        final AddWorldOrder order = AddWorldOrder.parse(turnData, gm, params);
        assertTrue(order.isReady());
        final World world = order.getWorld();
        assertEquals(ONE_COORDINATE, world.getCoordinate());
        assertEquals(name, world.getName());
        assertEquals(production, world.getProduction());
        assertEquals(stockpile, world.getStockpile());
        assertEquals(empire1, world.getOwner());
    }

    @Test
    void testAddWorldUnknownEmpire() {
        final String name = "world1";
        final int production = 5;
        final int stockpile = 10;
        final String params = ONE_COORDINATE + " " + name + " " + production + " " + stockpile + " unknown";
        final AddWorldOrder order = AddWorldOrder.parse(turnData, gm, params);
        assertFalse(order.isReady());
        assertTrue(order.getResults().stream().anyMatch(s -> s.contains("Unknown owner")));
    }
}