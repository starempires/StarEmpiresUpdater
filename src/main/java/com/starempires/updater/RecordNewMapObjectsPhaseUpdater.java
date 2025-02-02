package com.starempires.updater;

import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import com.starempires.objects.Storm;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.Set;

public class RecordNewMapObjectsPhaseUpdater extends PhaseUpdater {

    public RecordNewMapObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.RECORD_NEW_MAP_OBJECTS, turnData);
    }

    private void addKnownWorlds(final Empire empire) {
        final Collection<World> worlds = turnData.getAllWorlds();
        worlds.stream()
                .filter(world -> empire.getScanStatus(world) != ScanStatus.UNKNOWN)
                .filter(world -> empire.getScanStatus(world) != ScanStatus.STALE || empire.getLastTurnScanned(world.getCoordinate()) > 0)
                .forEach(empire::addKnownWorld);
    }

    private void addKnownPortals(final Empire empire) {
        final Collection<Portal> portals = turnData.getAllPortals();
        portals.stream()
                .filter(portal -> empire.getScanStatus(portal) != ScanStatus.UNKNOWN)
                .filter(portal -> empire.getScanStatus(portal) != ScanStatus.STALE || empire.getLastTurnScanned(portal.getCoordinate()) > 0)
                .forEach(empire::addKnownPortal);
    }

    private void addKnownStorms(final Empire empire) {
        final Collection<Storm> storms = turnData.getAllStorms();
        storms.stream()
                .filter(portal -> empire.getScanStatus(portal) != ScanStatus.UNKNOWN)
                .forEach(empire::addKnownStorm);
    }

    private void addKnownEmpires(final Empire empire) {
        final Collection<Ship> ships = empire.getShips();
        final Set<Empire> knownEmpires = Sets.newHashSet();
        ships.forEach(ship -> {
            final Collection<Empire> shipEmpires = turnData.getEmpiresPresent(ship);
            shipEmpires.remove(empire);
            knownEmpires.addAll(shipEmpires);
            final int scan = ship.getAvailableScan();
            final Collection<Coordinate> coordinates = Coordinate.getSurroundingCoordinates(ship, scan);
            coordinates.forEach(coordinate -> {
                final Collection<Empire> scanEmpires = turnData.getEmpiresPresent(coordinate);
                scanEmpires.remove(empire);
                scanEmpires.forEach(scanEmpire -> {
                    final World world = turnData.getWorld(coordinate);
                    if (world.isOwned()) {
                        knownEmpires.add(world.getOwner());
                    }
                    final Collection<Ship> sectorShips = scanEmpire.getShips(coordinate);
                    sectorShips.stream().filter(sectorShip -> sectorShip.isTransponderSet(empire))
                            .forEach(sectorShip -> knownEmpires.add(scanEmpire));
                });
            });
        });
        empire.addKnownEmpires(knownEmpires);
    }

    @Override
    public void update() {
        final Collection<Empire> empires = turnData.getAllEmpires();
        empires.forEach(empire -> {
            addKnownWorlds(empire);
            addKnownPortals(empire);
            addKnownStorms(empire);
            addKnownEmpires(empire);
        });
    }
}