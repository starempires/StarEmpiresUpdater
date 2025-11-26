package com.starempires.objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScanDataTest extends BaseTest {

    private ScanData scanData;

    @BeforeEach
    void setUp() {
        scanData = new ScanData();
    }

    @Test
    void getScanStatus() {
        scanData.setScanStatus(ZERO_COORDINATE, ScanStatus.STALE);
        assertEquals(ScanStatus.STALE, scanData.getScanStatus(ZERO_COORDINATE));
    }

    @Test
    void getScanStatusUpdated() {
        scanData.setScanStatus(ZERO_COORDINATE, ScanStatus.STALE);
        scanData.setScanStatus(ZERO_COORDINATE, ScanStatus.SCANNED);
        assertEquals(ScanStatus.SCANNED, scanData.getScanStatus(ZERO_COORDINATE));
    }

    @Test
    void getScanStatusUnknownCoordinate() {
        assertEquals(ScanStatus.UNKNOWN, scanData.getScanStatus(ZERO_COORDINATE));
    }

    @Test
    void mergeScanStatus() {
        scanData.setScanStatus(ZERO_COORDINATE, ScanStatus.STALE);
        scanData.mergeScanStatus(ZERO_COORDINATE, ScanStatus.SCANNED, 1);
        assertEquals(ScanStatus.SCANNED, scanData.getScanStatus(ZERO_COORDINATE));
        assertEquals(1, scanData.getLastTurnScanned(ZERO_COORDINATE));
    }

    @Test
    void testMergeScanStatusCollection() {
        scanData.mergeScanStatus(Set.of(ZERO_COORDINATE, ONE_COORDINATE), ScanStatus.SCANNED, 1);
        assertEquals(ScanStatus.SCANNED, scanData.getScanStatus(ZERO_COORDINATE));
        assertEquals(1, scanData.getLastTurnScanned(ZERO_COORDINATE));
        assertEquals(ScanStatus.SCANNED, scanData.getScanStatus(ONE_COORDINATE));
        assertEquals(1, scanData.getLastTurnScanned(ONE_COORDINATE));
    }

    @Test
    void mergeScanStatusAndShare() {
        ScanData empire1ScanData = new ScanData();
        scanData.setScanStatus(ZERO_COORDINATE, ScanStatus.VISIBLE);
        scanData.mergeScanStatusAndShare(empire1, empire1ScanData);
        assertEquals(ScanStatus.VISIBLE, scanData.getScanStatus(ZERO_COORDINATE));
    }

    @Test
    void testSerialization() throws JsonProcessingException {
        scanData.setScanStatus(ZERO_COORDINATE, ScanStatus.VISIBLE);
        final ObjectMapper mapper = new ObjectMapper();
        final String output = MAPPER.writeValueAsString(scanData);
        final ScanData deserialized = MAPPER.readValue(output, ScanData.class);
        assertEquals(scanData, deserialized);
    }
}