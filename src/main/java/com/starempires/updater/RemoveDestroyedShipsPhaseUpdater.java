package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class RemoveDestroyedShipsPhaseUpdater extends PhaseUpdater {

    public RemoveDestroyedShipsPhaseUpdater(final Phase phase, final TurnData turnData) {
        super(phase, turnData);
    }

    @Override
    public void update() {
        final Collection<Ship> ships = turnData.getAllShips();
        final Set<Ship> destroyed = new HashSet<Ship>();
        ships.stream().filter(ship -> !ship.isAlive()).forEach(ship -> {
            final Collection<Empire> empires = turnData.getEmpiresPresent(ship);
            addNews(empires, "%s ship %s has been destroyed".formatted(ship.getOwner(), ship));
            destroyed.add(ship);
        });
        turnData.removeDestroyedShips(destroyed);
    }
}