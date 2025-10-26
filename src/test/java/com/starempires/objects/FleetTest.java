package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FleetTest extends BaseTest {

    private Fleet fleet;
    private Ship ship;
    @BeforeEach
    void setUp() {
        fleet = new Fleet();
        ship = createShip(fighterClass, ONE_COORDINATE, "f1", empire1);
        fleet.addShip(ship);
    }

    @Test
    void addGetters() {
        assertEquals(ship, fleet.getShipByHandle("f1"));
        assertTrue(fleet.getAllShips().contains(ship));
        assertTrue(fleet.getShipsByCoordinate(ONE_COORDINATE).contains(ship));
        assertTrue(fleet.getShipsByClass(fighterClass).contains(ship));
    }

    @Test
    void moveShips() {
        fleet.moveShips(Set.of(ship), ZERO_COORDINATE);
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertTrue(ship.getConditions().contains(ShipCondition.MOVED));
    }

    @Test
    void traverseShips() {
        fleet.traverseShips(Set.of(ship), ZERO_COORDINATE);
        assertEquals(ZERO_COORDINATE, ship.getCoordinate());
        assertTrue(ship.getConditions().contains(ShipCondition.TRAVERSED_WORMNET));
    }

    @Test
    void removeShip() {
        fleet.removeShip(ship);
        assertTrue(fleet.getAllShips().isEmpty());
    }

    @Test
    void serialNumberExists() {
        assertTrue(fleet.serialNumberExists(ship.getSerialNumber()));
    }

    @Test
    void getLargestBasenameNumber() {
        assertEquals(0, fleet.getLargestBasenameNumber(null));
        assertEquals(0, fleet.getLargestBasenameNumber(""));
        assertEquals(1, fleet.getLargestBasenameNumber("f"));
    }

    @Test
    void clearShipConditions() {
        ship.addCondition(ShipCondition.MOVED);
        fleet.clearShipConditions();
        assertTrue(ship.getConditions().isEmpty());
    }
}