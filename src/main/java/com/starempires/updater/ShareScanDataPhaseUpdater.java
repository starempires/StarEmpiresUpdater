package com.starempires.updater;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.ScanData;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ShareScanDataPhaseUpdater extends PhaseUpdater {

    public ShareScanDataPhaseUpdater(final TurnData turnData) {
        super(Phase.SHARE_SCAN_DATA, turnData);
    }

    /**
     * Merge all scan data from the donor empire to the recipients' ScanData
     * 
     * @param empire
     *            The donor empire sharing scan data
     * @param scans
     *            Map of empire to their ScanData
     */
    private void mergeAllDataShares(final Empire empire, final Map<Empire, ScanData> scans) {
        final Set<Empire> recipients = Sets.newHashSet();
        recipients.addAll(empire.getShareEmpires());
        final ScanData scan = empire.getScanData();
        recipients.forEach(recipient -> {
            final ScanData newScan = scans.get(recipient);
            newScan.mergeScanStatusAndShare(empire, scan);
            addNews(recipient,
                    "You have received " + plural(newScan.getCoordinates().size(), "sector") + " of scan data from empire " + empire);
        });
    }

    /**
     * Merge shared coordinates from donor empire to the recipients' ScanData
     * 
     * @param empire
     *            The donor empire sharing scan data
     * @param scans
     *            Map of empire to their ScanData
     */
    private void mergeSharedCoordinates(final Empire empire, final Map<Empire, ScanData> scans) {
        final Multimap<Empire, Coordinate> shareCoordinates = empire.getShareCoordinates();
        final ScanData scan = empire.getScanData();
        shareCoordinates.asMap().forEach((recipient, coordinates) -> {
            final ScanData recipientScan = scans.get(recipient);
            final int count = recipientScan.mergeScanStatusAndShare(empire, scan, coordinates);
            addNews(recipient, "You have received " + plural(count, "sector") + " of scan data from empire " + empire);
        });
    }

    private int mergeShipScan(final Empire empire, final Ship ship, final ScanData recipientScan) {
        final int radius = ship.getAvailableScan();
        final Set<Coordinate> coordinates = Coordinate.getSurroundingCoordinates(ship, radius);
        final ScanData scan = empire.getScanData();
        return recipientScan.mergeScanStatusAndShare(empire, scan, coordinates);
    }

    private void mergeSharedShipScan(final Empire empire, final Map<Empire, ScanData> scans) {
        final Multimap<Empire, Ship> shareShips = empire.getShareShips();
        shareShips.asMap().forEach((recipient, ships) -> {
            final ScanData recipientScan = scans.get(recipient);
            final AtomicInteger count = new AtomicInteger();
            ships.forEach(ship -> count.addAndGet(mergeShipScan(empire, ship, recipientScan)));
            addNews(recipient,
                    "You have received " + plural(count.get(), "sector") + " of scan data from empire " + empire);
        });
    }

    private void mergeSharedShipClassScan(final Empire empire, final Map<Empire, ScanData> scans) {
        final Multimap<Empire, ShipClass> shareShipClasses = empire.getShareShipClasses();
        shareShipClasses.asMap().forEach((recipient, shipClasses) -> {
            final ScanData recipientScan = scans.get(recipient);
            final AtomicInteger count = new AtomicInteger();
            shipClasses.forEach(shipClass -> {
                final Collection<Ship> ships = empire.getShips(shipClass);
                ships.forEach(ship -> count.addAndGet(mergeShipScan(empire, ship, recipientScan)));
            });
            addNews(recipient,
                    "You have received " + plural(count.get(), "sector") + " of scan data from empire " + empire);
        });
    }

    private void mergeNewScans(final Map<Empire, ScanData> scans) {
        scans.forEach(Empire::mergeScanStatusAndShare);
    }

    @Override
    public void update() {
        final Collection<Empire> empires = turnData.getAllEmpires();
        final Map<Empire, ScanData> scans = Maps.newHashMap();
        empires.forEach(empire -> scans.put(empire, new ScanData()));

        empires.forEach(empire -> {
            mergeAllDataShares(empire, scans);
            mergeSharedCoordinates(empire, scans);
            mergeSharedShipClassScan(empire, scans);
            mergeSharedShipScan(empire, scans);
        });
        mergeNewScans(scans);
    }
}