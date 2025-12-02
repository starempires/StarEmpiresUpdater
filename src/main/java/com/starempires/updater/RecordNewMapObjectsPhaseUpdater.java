package com.starempires.updater;

import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.ScanStatus;
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
        final Set<Empire> newKnownEmpires = Sets.newHashSet();
        empire.getScanCoordinates()
                .stream()
                .filter(coordinate -> empire.getScanStatus(coordinate).isMoreVisible(ScanStatus.STALE))
                .forEach(coordinate -> {
                    final World world = turnData.getWorld(coordinate);
                    if (world != null && world.isOwned()) {
                        newKnownEmpires.add(world.getOwner());
                    }
                    if (empire.getScanStatus(coordinate) == ScanStatus.VISIBLE) {
                        newKnownEmpires.addAll(turnData.getEmpiresPresent(coordinate));
                    } else {
                        turnData.getLiveShips(coordinate)
                                .stream()
                                .filter(ship -> ship.isTransponderSet(empire))
                                .forEach(ship -> {
                                    newKnownEmpires.add(ship.getOwner());
                                });
                    }
                });
        final Set<Empire> existingKnownEmpires = empire.getKnownEmpires();
        newKnownEmpires.removeAll(existingKnownEmpires);
        newKnownEmpires.remove(empire);
        newKnownEmpires.stream().sorted().forEach(knownEmpire -> addNews(empire, "You are now in contact with empire " + knownEmpire));
        newKnownEmpires.forEach(empire::addKnownEmpire);
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