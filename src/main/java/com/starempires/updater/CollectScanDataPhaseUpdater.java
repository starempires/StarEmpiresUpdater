package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectScanDataPhaseUpdater extends PhaseUpdater {

    public CollectScanDataPhaseUpdater(final TurnData turnData) {
        super(Phase.COLLECT_SCAN_DATA, turnData);
    }

    private void markPortalsStale(final Empire empire) {
        final Set<Portal> knownPortals = empire.getKnownPortals();
        knownPortals.forEach(portal -> empire.mergeScanStatus(portal, ScanStatus.STALE, turnData.getTurnNumber()));
    }

    private void addShipScanData(final Empire empire) {
        final Collection<Ship> ships = empire.getShips();
        ships.stream().filter(ship -> !ship.isLoaded()).forEach(ship -> {
            int scan = 0;
            if (ship.isAlive() && !turnData.isInNebula(ship)) {
                scan = ship.getAvailableScan();
            }
            final Collection<Coordinate> coordinates = Coordinate.getSurroundingCoordinates(ship, scan);
            final Set<Coordinate> nebulae = coordinates.stream().filter(turnData::isInNebula).collect(Collectors.toSet());
            coordinates.removeAll(nebulae);
            nebulae.forEach(coordinate -> empire.mergeScanStatus(coordinate, ScanStatus.STALE, empire.getLastTurnScanned(coordinate)));
            final int scanTurnNumber = turnData.getTurnNumber();
            empire.mergeScanStatus(coordinates, ScanStatus.SCANNED, scanTurnNumber + 1);
            empire.mergeScanStatus(ship, ScanStatus.VISIBLE, scanTurnNumber + 1);
        });
    }

    private void addWorldSectors(final Empire empire) {
        final Collection<World> worlds = turnData.getOwnedWorlds(empire);
        worlds.forEach(world -> empire.mergeScanStatus(world, ScanStatus.VISIBLE, turnData.getTurnNumber() + 1));
    }

    @Override
    public void update() {
        final Collection<Empire> empires = turnData.getAllEmpires();
        empires.forEach(empire -> {
            empire.setAllScanStatus(ScanStatus.STALE);
            markPortalsStale(empire);
            addShipScanData(empire);
            addWorldSectors(empire);
        });
    }
}