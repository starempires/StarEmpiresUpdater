package com.starempires.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmpireTest {

    private final Empire empire = Empire.builder().name("The Culture").empireType(EmpireType.ACTIVE).abbreviation("CU").frameOfReference(FrameOfReference.DEFAULT_FRAME_OF_REFERENCE).build();

    @Test
    void owns() {
        final World world = World.builder().owner(empire).build();
        assertTrue(empire.owns(world));
    }

    @Test
    void isKnownEmpire() {
        assertTrue(empire.isKnownEmpire(empire));
        final Empire other = Empire.builder().build();
        empire.addKnownEmpire(other);
        assertTrue(empire.isKnownEmpire(other));
    }

    @Test
    void isKnownWorld() {
        final World world = World.builder().build();
        empire.addKnownWorld(world);
        assertTrue(empire.isKnownWorld(world));
    }

    @Test
    void isKnownPortal() {
        final Portal portal  = Portal.builder().build();
        empire.addKnownPortal(portal);
        assertTrue(empire.isKnownPortal(portal));
    }

    @Test
    void isKnownStorm() {
        final Storm storm = Storm.builder().build();
        empire.addKnownStorm(storm);
        assertTrue(empire.isKnownStorm(storm));
    }

    @Test
    void isKnownShipClass() {
        final ShipClass shipClass = ShipClass.builder().build();
        empire.addKnownShipClass(shipClass);
        assertTrue(empire.isKnownShipClass(shipClass));
    }

    @Test
    void addNavData() {
        final Portal portal  = Portal.builder().build();
        empire.addNavData(portal);
        assertTrue(empire.hasNavData(portal));
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
        assertTrue(empire.getNewSerialNumber().startsWith(empire.getAbbreviation()));
    }

    @Test
    void buildShip() {
        final ShipClass shipClass = ShipClass.builder().dp(10).build();
        final World world = World.builder().coordinate(new Coordinate(0,0)).build();
        final Ship ship = empire.buildShip(shipClass,world, "foo", 1);
        assertEquals(empire, ship.getOwner());
        assertEquals(shipClass, ship.getShipClass());
        assertEquals(10, ship.getDpRemaining());
        assertEquals(1, ship.getTurnBuilt());
        assertTrue(empire.getShips().contains(ship));
    }
}