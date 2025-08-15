package com.starempires.updater;

import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordNewMapObjectsPhaseUpdaterTest extends BaseTest {

    private RecordNewMapObjectsPhaseUpdater updater;
    private World world;
    private Portal portal;
    private Storm storm;
    private Empire newEmpire;

    @BeforeEach
    void setUp() {
        updater = new RecordNewMapObjectsPhaseUpdater(turnData);
        world = createWorld("world", ONE_COORDINATE, 5);
        portal = createPortal("portal", ONE_COORDINATE, false);
        storm = createStorm("storm", ONE_COORDINATE, 5);
        turnData.addWorld(world);
        turnData.addPortal(portal);
        newEmpire = createEmpire("newEmpire");
    }

    @Test
    void updateWorlds() {
        empire.addScan(ONE_COORDINATE, ScanStatus.SCANNED);
        updater.update();
        assertTrue(empire.isKnownWorld(world));
    }

    @Test
    void updateWorldNebula() {
        empire.addScan(ONE_COORDINATE, ScanStatus.STALE);
        updater.update();
        assertFalse(empire.isKnownWorld(world));
    }

    @Test
    void updateWorldNebulaScannedPreviously() {
        empire.addScan(ONE_COORDINATE, ScanStatus.STALE);
        empire.addScanHistory(ONE_COORDINATE, 1);
        updater.update();
        assertTrue(empire.isKnownWorld(world));
    }

    @Test
    void updatePortals() {
        empire.addScan(ONE_COORDINATE, ScanStatus.SCANNED);
        updater.update();
        assertTrue(empire.isKnownPortal(portal));
    }

    @Test
    void updatePortalNebula() {
        empire.addScan(ONE_COORDINATE, ScanStatus.STALE);
        updater.update();
        assertFalse(empire.isKnownPortal(portal));
    }

    @Test
    void updatePortalNebulaScannedPreviously() {
        empire.addScan(ONE_COORDINATE, ScanStatus.STALE);
        empire.addScanHistory(ONE_COORDINATE, 1);
        updater.update();
        assertTrue(empire.isKnownPortal(portal));
    }

    @Test
    void updateStorms() {
        empire.addScan(ONE_COORDINATE, ScanStatus.STALE);
        updater.update();
        assertTrue(empire.isKnownStorm(storm));
    }

    @Test
    void updateKnownEmpiresNoWorldsOrShips() {
        empire.addScan(ONE_COORDINATE, ScanStatus.SCANNED);
        turnData.removeWorld(world);
        updater.update();
        assertTrue(empire.getKnownEmpires().isEmpty());
    }

    @Test
    void updateKnownEmpiresFromWorld() {
        empire.addScan(ONE_COORDINATE, ScanStatus.SCANNED);
        world.setOwner(newEmpire);
        updater.update();
        assertTrue(empire.isKnownEmpire(newEmpire));
    }

    @Test
    void updateKnownEmpiresUnownedWorld() {
        empire.addScan(ONE_COORDINATE, ScanStatus.SCANNED);
        updater.update();
        assertTrue(empire.getKnownEmpires().isEmpty());
    }

    @Test
    void updateKnownEmpiresFromShip() {
        empire.addScan(ONE_COORDINATE, ScanStatus.SCANNED);
        final Ship ship = createShip(probeClass, ONE_COORDINATE, "newShip", newEmpire);
        ship.setPublicTransponder(true);
        updater.update();
        assertTrue(empire.isKnownEmpire(newEmpire));
    }

    @Test
    void updateKnownEmpiresFromShipPrivateTransponderScanned() {
        empire.addScan(ONE_COORDINATE, ScanStatus.SCANNED);
        final Ship ship = createShip(probeClass, ONE_COORDINATE, "newShip", newEmpire);
        ship.setPublicTransponder(false);
        updater.update();
        assertFalse(empire.isKnownEmpire(newEmpire));
    }

    @Test
    void updateKnownEmpiresFromShipPrivateTransponderVisible() {
        empire.addScan(ONE_COORDINATE, ScanStatus.VISIBLE);
        final Ship ship = createShip(probeClass, ONE_COORDINATE, "newShip", newEmpire);
        ship.setPublicTransponder(false);
        updater.update();
        assertTrue(empire.isKnownEmpire(newEmpire));
    }
}