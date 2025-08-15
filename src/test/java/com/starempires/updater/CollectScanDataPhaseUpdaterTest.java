package com.starempires.updater;

import com.starempires.objects.Coordinate;
import com.starempires.objects.Portal;
import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectScanDataPhaseUpdaterTest extends BaseTest {

    private CollectScanDataPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new CollectScanDataPhaseUpdater(turnData);
    }

    @Test
    void updateShipScan() {
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire);
        updater.update();
        Coordinate.getSurroundingCoordinatesWithoutOrigin(ship, ship.getAvailableScan())
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.SCANNED, empire.getScanStatus(coordinate));
                });
        assertEquals(ScanStatus.VISIBLE, empire.getScanStatus(ship));
    }

    @Test
    void updateShipScanDestroyed() {
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire);
        ship.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        updater.update();
        Coordinate.getSurroundingCoordinates(ship, ship.getAvailableScan())
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.UNKNOWN, empire.getScanStatus(coordinate));
                });
    }

    @Test
    void updateShipInNebula() {
        final Storm storm = createStorm("storm", ZERO_COORDINATE, 0);
        turnData.addStorm(storm);
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire);
        updater.update();
        Coordinate.getSurroundingCoordinatesWithoutOrigin(ship, ship.getAvailableScan())
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.UNKNOWN, empire.getScanStatus(coordinate));
                });
        assertEquals(ScanStatus.VISIBLE, empire.getScanStatus(ship));
    }

    @Test
    void updateNebulaNearby() {
        final Storm storm = createStorm("storm", ONE_COORDINATE, 0);
        turnData.addStorm(storm);
        final Ship ship = createShip(carrierClass, ZERO_COORDINATE, "ship", empire);
        updater.update();
        Coordinate.getSurroundingCoordinatesWithoutOrigin(ship, ship.getAvailableScan())
                .stream()
                .filter(coordinate -> !ONE_COORDINATE.equals(coordinate))
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.SCANNED, empire.getScanStatus(coordinate));
                });
        assertEquals(ScanStatus.VISIBLE, empire.getScanStatus(ship));
        assertEquals(ScanStatus.STALE, empire.getScanStatus(storm));
    }

    @Test
    void updateWorld() {
        final World world = createWorld("world", ZERO_COORDINATE, 5);
        world.setOwner(empire);
        turnData.addWorld(world);
        updater.update();
        Coordinate.getSurroundingCoordinatesWithoutOrigin(world, 1)
                .forEach(coordinate -> {
                    assertEquals(ScanStatus.UNKNOWN, empire.getScanStatus(coordinate));
                });
        assertEquals(ScanStatus.VISIBLE, empire.getScanStatus(world));
    }

    @Test
    void updatePortalScan() {
        final Portal portal = createPortal("portal", ZERO_COORDINATE, false);
        empire.addKnownPortal(portal);
        turnData.addPortal(portal);
        updater.update();
        assertEquals(ScanStatus.STALE, empire.getScanStatus(portal));
    }
}