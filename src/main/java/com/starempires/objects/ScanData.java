package com.starempires.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
@JsonSerialize(using = ScanData.ScanDataSerializer.class)
@JsonDeserialize(using = ScanData.ScanDataDeserializer.class)
public class ScanData extends HashMap<Coordinate, ScanRecord> {

    static class ScanDataSerializer extends JsonSerializer<Map<Coordinate, ScanRecord>> {

        @Override
        public void serialize(Map<Coordinate, ScanRecord> map, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray(); // Start an array to hold each combined object

            for (Map.Entry<Coordinate, ScanRecord> entry : map.entrySet()) {
                final Coordinate coord = entry.getKey();
                final ScanRecord record = entry.getValue();

                gen.writeStartObject(); // Start an object for each map entry
                gen.writeFieldName("oblique"); // Add coordinate fields
                gen.writeNumber(coord.getOblique());
                gen.writeFieldName("y");
                gen.writeNumber(coord.getY());

                gen.writeFieldName("status"); // Add record status field
                gen.writeString(record.getScanStatus().toString());
                gen.writeFieldName("lastTurnScanned"); // Add record status field
                gen.writeNumber(record.getLastTurnScanned());

                gen.writeEndObject(); // End the object for this entry
            }

            gen.writeEndArray(); // End the array of entries
        }
    }

    static class ScanDataDeserializer extends JsonDeserializer<Map<Coordinate, ScanRecord>> {

        @Override
        public ScanData deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            final ScanData scanData = new ScanData();

            // Expect an array start token
            if (parser.currentToken() != JsonToken.START_ARRAY) {
                ctxt.reportInputMismatch(this.getClass(), "Expected an array");
            }
            parser.nextToken();

            // Process each object in the array
            while (parser.currentToken() == JsonToken.START_OBJECT) {
                int oblique = 0, y = 0;
                int lastTurnScanned = 0;
                String status = ScanStatus.UNKNOWN.toString();

                // Parse fields in each JSON object
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    final String fieldName = parser.currentName();
                    parser.nextToken();

                    switch (fieldName) {
                        case "oblique":
                            oblique = parser.getIntValue();
                            break;
                        case "y":
                            y = parser.getIntValue();
                            break;
                        case "status":
                            status = parser.getText();
                            break;
                        case "lastTurnScanned":
                            lastTurnScanned = parser.getIntValue();
                            break;
                        default:
                            ctxt.handleUnexpectedToken(Coordinate.class, parser);
                    }
                }

                // Create Coordinate and Record objects from parsed values
                final Coordinate coord = new Coordinate(oblique, y);
                final ScanRecord record = new ScanRecord(ScanStatus.valueOf(status.toUpperCase()), lastTurnScanned);

                // Add to the map
                scanData.put(coord, record);

                parser.nextToken(); // Move to the next array element or end
            }

            return scanData;
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

    public void mergeScanStatusAndShare(final Empire empire, final ScanData scan) {
        mergeScanStatusAndShare(empire, scan, scan.getCoordinates());
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