package com.starempires.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serial;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates all scan information for an empire. Basically this is a map of
 * Coordinates to ScanRecords.
 * 
 * @author john
 */
//@JsonSerialize(keyUsing = ScanData.CoordinateSerializer.class)
@JsonSerialize(using = ScanData.CoordinateSerializer.class)
public class ScanData extends HashMap<Coordinate, ScanRecord> {

    @Serial
    private static final long serialVersionUID = 6189455626661977070L;

    static class CoordinateSerializer extends JsonSerializer<Map<Coordinate, ScanRecord>> {
        @Override
        public void serialize(Map<Coordinate, ScanRecord> map, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject(); // Start the JSON object
            for (Map.Entry<Coordinate, ScanRecord> entry : map.entrySet()) {
                String key = entry.getKey().getName();
                gen.writeObjectField(key, entry.getValue());
            }
            gen.writeEndObject(); // End the JSON object
        }
    }

    public ScanStatus getScanStatus(final Coordinate coordinate) {
        ScanStatus rv = ScanStatus.UNKNOWN;
        final ScanRecord record = get(coordinate);
        if (record != null) {
            rv = record.getScanStatus();
        }
        return rv;
    }

    private ScanRecord getOrCreate(final Coordinate coordinate) {
        ScanRecord record = get(coordinate);
        if (record == null) {
            record = new ScanRecord();
            put(coordinate, record);
        }
        return record;
    }

    public void setScanStatus(final Coordinate coordinate, final ScanStatus status) {
        final ScanRecord record = getOrCreate(coordinate);
        record.setScanStatus(status);
    }

    public void mergeScanStatus(final Coordinate coordinate, final ScanStatus status) {
        ScanRecord record = getOrCreate(coordinate);
        record.mergeScanStatus(status);
    }

    public void mergeScanStatus(final @NotNull Collection<Coordinate> coordinates, final ScanStatus status) {
        coordinates.forEach(coordinate -> mergeScanStatus(coordinate, status));
    }

    public void mergeScanStatus(final Coordinate coordinate, final @NotNull ScanRecord record) {
        mergeScanStatus(coordinate, record.getScanStatus());
    }

    public int mergeScanStatusAndShare(final Empire empire, final ScanData scan) {
        return mergeScanStatusAndShare(empire, scan, scan.getCoordinates());
    }

    public int mergeScanStatusAndShare(final Empire empire, final ScanData scan,
            final @NotNull Collection<Coordinate> coordinates) {
        int rv = 0;
        for (final Coordinate coordinate : coordinates) {
            final ScanRecord record = scan.get(coordinate);
            if (record != null) {
                if (record.getScanStatus().isMoreVisible(ScanStatus.UNKNOWN)) {
                    final ScanRecord existing = getOrCreate(coordinate);
                    existing.mergeScanStatusAndShare(record, empire);
                    rv++;
                }
            }
        }
        return rv;
    }

    public void setAllScan(final ScanStatus status) {
        values().forEach(record -> record.setScanStatus(status));
    }

    public void addShare(final Coordinate coordinate, final ScanStatus scanStatus, final Empire empire) {
        final ScanRecord record = getOrCreate(coordinate);
        record.addShare(empire, scanStatus);
    }

    /**
     * Get all coordinates
     * 
     * @return Collection of coordinates that are in this object
     */
    public Set<Coordinate> getCoordinates() {
        return keySet();
    }

    public void setLastTurnScanned(final Coordinate coordinate, final int lastTurnScanned) {
        final ScanRecord record = getOrCreate(coordinate);
        record.setLastTurnScanned(lastTurnScanned);
    }

    public int getLastTurnScanned(final Coordinate coordinate) {
        int rv = 0;
        final ScanRecord record = get(coordinate);
        if (record != null) {
            rv = record.getLastTurnScanned();
        }
        return rv;
    }
}