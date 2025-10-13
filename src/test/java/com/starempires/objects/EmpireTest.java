package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmpireTest extends BaseTest {

    @Test
    void owns() {
        assertTrue(empire1.owns(world));
    }

    @Test
    void isKnownEmpire() {
        assertTrue(empire1.isKnownEmpire(empire1));
        final Empire other = Empire.builder().build();
        empire1.addKnownEmpire(other);
        assertTrue(empire1.isKnownEmpire(other));
    }

    @Test
    void isKnownWorld() {
        assertTrue(empire1.isKnownWorld(world));
    }

    @Test
    void isKnownPortal() {
        empire1.addKnownPortal(portal);
        assertTrue(empire1.isKnownPortal(portal));
    }

    @Test
    void isKnownStorm() {
        empire1.addKnownStorm(storm);
        assertTrue(empire1.isKnownStorm(storm));
    }

    @Test
    void isKnownShipClass() {
        final ShipClass shipClass = ShipClass.builder().build();
        empire1.addKnownShipClass(shipClass);
        assertTrue(empire1.isKnownShipClass(shipClass));
    }

    @Test
    void addNavData() {
        empire1.addNavData(portal);
        assertTrue(empire1.hasNavData(portal));
    }

    @Test
    void setAllScanStatus() {
    }

    @Test
    void mergeScanStatus() {
    }

    @Test
    void testMergeScanStatus() {
    }

    @Test
    void testMergeScanStatus1() {
    }

    @Test
    void mergeScanStatusAndShare() {
    }

    @Test
    void getScanStatus() {
    }

    @Test
    void testGetScanStatus() {
    }

    @Test
    void removeCoordinateScanAccess() {
    }

    @Test
    void addCoordinateScanAccess() {
    }

    @Test
    void testAddCoordinateScanAccess() {
    }

    @Test
    void addEmpireScanAccess() {
    }

    @Test
    void removeEmpireScanAccess() {
    }

    @Test
    void removeShipScanAccess() {
    }

    @Test
    void addShipScanAccess() {
    }

    @Test
    void testAddShipScanAccess() {
    }

    @Test
    void addShipClassScanAccess() {
    }

    @Test
    void testAddShipClassScanAccess() {
    }

    @Test
    void removeShipClassScanAccess() {
    }

    @Test
    void testRemoveShipClassScanAccess() {
    }

    @Test
    void getScanCoordinates() {
    }

    @Test
    void addKnownEmpires() {
    }

    @Test
    void addScan() {
    }

    @Test
    void addScanHistory() {
    }

    @Test
    void removeKnownWorld() {
    }

    @Test
    void removeKnownPortal() {
    }

    @Test
    void removeKnownEmpire() {
    }

    @Test
    void removeKnownShipClass() {
    }

    @Test
    void toLocal() {
    }

    @Test
    void toGalactic() {
    }

    @Test
    void getLastTurnScanned() {
    }

    @Test
    void getWorldLastTurnScanned() {
    }

    @Test
    void getPortalLastTurnScanned() {
    }

    @Test
    void computeMaxScanExtent() {
        empire1.mergeScanStatus(ONE_COORDINATE, ScanStatus.VISIBLE, 0);
        assertEquals(1, empire1.computeMaxScanExtent());
    }

    @Test
    void addSharedScan() {
    }

    @Test
    void addPortalTraversed() {
    }

    @Test
    void addShip() {
    }

    @Test
    void getShip() {
    }

    @Test
    void getShips() {
    }

    @Test
    void testGetShips() {
    }

    @Test
    void testGetShips1() {
    }

    @Test
    void getLiveShips() {
    }

    @Test
    void getStarbase() {
    }

    @Test
    void moveShip() {
    }

    @Test
    void traverseShip() {
    }

    @Test
    void removeShip() {
    }

    @Test
    void getNewSerialNumber() {
        assertTrue(empire1.getNewSerialNumber().startsWith(empire1.getAbbreviation()));
    }

    @Test
    void buildShip() {
        final ShipClass shipClass = ShipClass.builder().dp(10).build();
        final Ship ship = empire1.buildShip(shipClass,world, "foo", 1);
        assertEquals(empire1, ship.getOwner());
        assertEquals(shipClass, ship.getShipClass());
        assertEquals(10, ship.getDpRemaining());
        assertEquals(1, ship.getTurnBuilt());
        assertTrue(empire1.getShips().contains(ship));
    }
}