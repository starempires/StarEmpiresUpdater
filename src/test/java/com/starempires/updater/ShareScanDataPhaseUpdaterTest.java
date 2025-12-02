package com.starempires.updater;

import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShareScanDataPhaseUpdaterTest extends BaseTest {

    private ShareScanDataPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new ShareScanDataPhaseUpdater(turnData);
        empire1.setAllScanStatus(ScanStatus.UNKNOWN);
        empire2.setAllScanStatus(ScanStatus.UNKNOWN);
    }

    @Test
    void mergeAllDataShares() {
        empire1.addEmpireScanAccess(empire2);
        empire1.addScan(ZERO_COORDINATE, ScanStatus.VISIBLE);
        updater.update();
        assertEquals(ScanStatus.VISIBLE, empire2.getScanData().getScanStatus(ZERO_COORDINATE));
    }

    @Test
    void mergeCoordinateShares() {
        empire1.addCoordinateScanAccess(empire2, List.of(ZERO_COORDINATE));
        empire1.addScan(ZERO_COORDINATE, ScanStatus.VISIBLE);
        updater.update();
        assertEquals(ScanStatus.VISIBLE, empire2.getScanData().getScanStatus(ZERO_COORDINATE));
    }

    @Test
    void mergeShipShares() {
        final Ship fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire1);
        empire1.addShipScanAccess(empire2, List.of(fighter));
        empire1.addScan(ZERO_COORDINATE, ScanStatus.VISIBLE);
        updater.update();
        assertEquals(ScanStatus.VISIBLE, empire2.getScanData().getScanStatus(ZERO_COORDINATE));
    }

    @Test
    void mergeShipClassShares() {
        final Ship fighter = createShip(fighterClass, ZERO_COORDINATE, "fighter", empire1);
        empire1.addShipClassScanAccess(empire2, List.of(fighterClass));;
        empire1.addScan(ZERO_COORDINATE, ScanStatus.VISIBLE);
        updater.update();
        assertEquals(ScanStatus.VISIBLE, empire2.getScanData().getScanStatus(ZERO_COORDINATE));
    }
}