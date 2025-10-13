package com.starempires.updater;

import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.objects.Storm;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordNewMapObjectsPhaseUpdaterTest extends BaseTest {

    private RecordNewMapObjectsPhaseUpdater updater;
    private Storm storm;

    @BeforeEach
    void setUp() {
        updater = new RecordNewMapObjectsPhaseUpdater(turnData);
        world.setOwner(null);
        empire1.removeKnownWorld(world);
        storm = createStorm("storm", ZERO_COORDINATE, 5);
        turnData.addWorld(world);
        turnData.addPortal(portal);
    }

    @Test
    void updateWorlds() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        updater.update();
        assertTrue(empire1.isKnownWorld(world));
    }

    @Test
    void updateWorldNebula() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.STALE);
        updater.update();
        assertFalse(empire1.isKnownWorld(world));
    }

    @Test
    void updateWorldNebulaScannedPreviously() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.STALE);
        empire1.addScanHistory(ZERO_COORDINATE, 1);
        updater.update();
        assertTrue(empire1.isKnownWorld(world));
    }

    @Test
    void updatePortals() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        updater.update();
        assertTrue(empire1.isKnownPortal(portal));
    }

    @Test
    void updatePortalNebula() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.STALE);
        updater.update();
        assertFalse(empire1.isKnownPortal(portal));
    }

    @Test
    void updatePortalNebulaScannedPreviously() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.STALE);
        empire1.addScanHistory(ZERO_COORDINATE, 1);
        updater.update();
        assertTrue(empire1.isKnownPortal(portal));
    }

    @Test
    void updateStorms() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.STALE);
        updater.update();
        assertTrue(empire1.isKnownStorm(storm));
    }

    @Test
    void updateKnownEmpiresNoWorldsOrShips() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        turnData.removeWorld(world);
        updater.update();
        assertTrue(empire1.getKnownEmpires().isEmpty());
    }

    @Test
    void updateKnownEmpiresFromWorld() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        world.setOwner(empire2);
        updater.update();
        assertTrue(empire1.isKnownEmpire(empire2));
    }

    @Test
    void updateKnownEmpiresUnownedWorld() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        updater.update();
        assertTrue(empire1.getKnownEmpires().isEmpty());
    }

    @Test
    void updateKnownEmpiresFromShip() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "newShip", empire2);
        ship.setPublicTransponder(true);
        updater.update();
        assertTrue(empire1.isKnownEmpire(empire2));
    }

    @Test
    void updateKnownEmpiresFromShipPrivateTransponderScanned() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.SCANNED);
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "newShip", empire2);
        ship.setPublicTransponder(false);
        updater.update();
        assertFalse(empire1.isKnownEmpire(empire2));
    }

    @Test
    void updateKnownEmpiresFromShipPrivateTransponderVisible() {
        empire1.addScan(ZERO_COORDINATE, ScanStatus.VISIBLE);
        final Ship ship = createShip(probeClass, ZERO_COORDINATE, "newShip", empire2);
        ship.setPublicTransponder(false);
        updater.update();
        assertTrue(empire1.isKnownEmpire(empire2));
    }
}