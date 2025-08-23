package com.starempires.updater;

import com.google.common.collect.Sets;
import com.starempires.TurnData;
import com.starempires.objects.Ship;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public abstract class RemoveDestroyedShipsPhaseUpdater extends PhaseUpdater {

    public RemoveDestroyedShipsPhaseUpdater(final Phase phase, final TurnData turnData) {
        super(phase, turnData);
    }

    @Override
    public void update() {
        final Collection<Ship> ships = turnData.getAllShips();
        final Set<Ship> destroyed = Sets.newHashSet();
        ships.stream().filter(ship -> !ship.isAlive()).forEach(destroyed::add);
        destroyed.stream()
                 .sorted(Comparator.comparing(Ship::getOwner).thenComparing(Ship::getName))
                 .forEach(ship -> {
        });
        turnData.removeDestroyedShips(destroyed);
    }
}