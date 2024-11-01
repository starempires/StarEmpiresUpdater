package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * All related scan information for a single Coordinate
 * 
 * @author john
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScanRecord {

    /** current ScanStatus */
    private ScanStatus scanStatus = ScanStatus.UNKNOWN;
    /** last turn scanned (or current turn if visible) */
    private int lastTurnScanned;
    /** Map of ScanStatus to empires that have shared that ScanStatus */
    @JsonIgnore
    private final Multimap<ScanStatus, Empire> shares = HashMultimap.create();

    /**
     * Replace this record's scan status with the given status if it is more visible
     */
    public void mergeScanStatus(final ScanStatus newStatus) {
        if (newStatus.isMoreVisible(scanStatus)) {
            this.scanStatus = newStatus;
        }
    }

    public void addShare(final Empire empire, final ScanStatus status) {
        shares.put(status, empire);
    }

    public void mergeScanStatusAndShare(final ScanRecord record, final Empire empire) {
        final ScanStatus status = record.getScanStatus();
        mergeScanStatus(status);
        addShare(empire, status);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("scan status", scanStatus)
                .append("last scanned turn", lastTurnScanned)
                .toString();
    }

    @JsonIgnore
    public boolean isAnyKnownStatus() {
        return scanStatus.isMoreVisible(ScanStatus.UNKNOWN);
    }
}