package com.starempires.objects;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanRecordTest extends BaseTest {

    private ScanRecord scanRecord;

    @BeforeEach
    void setUp() {
        scanRecord = new ScanRecord();
    }

    @Test
    void mergeScanStatusMoreVisible() {
        scanRecord.setScanStatus(ScanStatus.UNKNOWN);
        scanRecord.mergeScanStatus(ScanStatus.VISIBLE);
        assertEquals(ScanStatus.VISIBLE, scanRecord.getScanStatus());
    }

    @Test
    void mergeScanStatusLessVisible() {
        scanRecord.setScanStatus(ScanStatus.SCANNED);
        scanRecord.mergeScanStatus(ScanStatus.STALE);
        assertEquals(ScanStatus.SCANNED, scanRecord.getScanStatus());
    }

    @Test
    void addShare() {
        scanRecord.addShare(empire1, ScanStatus.VISIBLE);
        assertTrue(scanRecord.getShares().get(ScanStatus.VISIBLE).contains(empire1));
    }

    @Test
    void mergeScanStatusAndShare() {
        final ScanRecord scanRecord2 = new ScanRecord(ScanStatus.SCANNED, 1);
        scanRecord.mergeScanStatusAndShare(scanRecord2, empire1);
        assertEquals(ScanStatus.SCANNED, scanRecord.getScanStatus());
        assertTrue(scanRecord.getShares().get(ScanStatus.SCANNED).contains(empire1));
    }
}