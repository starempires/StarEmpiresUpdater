package com.starempires.updater;

import com.starempires.objects.Coordinate;
import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectScanDataPhaseUpdaterTest extends BaseTest {

    private CollectScanDataPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        empire1.removeKnownWorld(world);
        world.setOwner(null);
        updater = new CollectScanDataPhaseUpdater(turnData);
    }

    @Test
    void updateShipScan() {
        turnData.removeStorm(storm);
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire1);
        updater.update();
        Coordinate.getSurroundingCoordinatesWithoutOrigin(ship, ship.getAvailableScan())
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.SCANNED, empire1.getScanStatus(coordinate));
                });
        assertEquals(ScanStatus.VISIBLE, empire1.getScanStatus(ship));
    }

    @Test
    void updateShipScanDestroyed() {
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire1);
        ship.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        updater.update();
        Coordinate.getSurroundingCoordinates(ship, ship.getAvailableScan())
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.UNKNOWN, empire1.getScanStatus(coordinate));
                });
    }

    @Test
    void updateShipInNebula() {
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire1);
        updater.update();
        Coordinate.getSurroundingCoordinatesWithoutOrigin(ship, ship.getAvailableScan())
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.UNKNOWN, empire1.getScanStatus(coordinate));
                });
        assertEquals(ScanStatus.VISIBLE, empire1.getScanStatus(ship));
    }

    @Test
    void updateNebulaNearby() {
        turnData.removeStorm(storm);
        storm.setCoordinate(ONE_COORDINATE);
        turnData.addStorm(storm);
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire1);
        updater.update();
        Coordinate.getSurroundingCoordinatesWithoutOrigin(ship, ship.getAvailableScan())
                .stream()
                .filter(coordinate -> !ONE_COORDINATE.equals(coordinate))
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.SCANNED, empire1.getScanStatus(coordinate));
                });
        assertEquals(ScanStatus.VISIBLE, empire1.getScanStatus(ship));
        assertEquals(ScanStatus.STALE, empire1.getScanStatus(storm));
    }

    @Test
    void updateWorld() {
        final World world = createWorld("world", ZERO_COORDINATE, 5);
        world.setOwner(empire1);
        turnData.addWorld(world);
        updater.update();
        Coordinate.getSurroundingCoordinatesWithoutOrigin(world, 1)
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.UNKNOWN, empire1.getScanStatus(coordinate));
                });
        assertEquals(ScanStatus.VISIBLE, empire1.getScanStatus(world));
    }

    @Test
    void updatePortalScan() {
        empire1.addKnownPortal(portal);
        turnData.addPortal(portal);
        updater.update();
        assertEquals(ScanStatus.STALE, empire1.getScanStatus(portal));
    }
}