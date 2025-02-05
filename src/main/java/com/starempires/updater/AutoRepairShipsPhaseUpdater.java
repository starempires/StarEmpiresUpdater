package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;

import java.util.Collection;

public class AutoRepairShipsPhaseUpdater extends PhaseUpdater {

    public AutoRepairShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.AUTO_REPAIR_SHIPS, turnData);
    }

    @Override
    public void update() {
        final Collection<Ship> ships = turnData.getAllShips();
        ships.stream().filter(Ship::isRepairable)
                .forEach(ship -> {
            final int repaired = ship.getAutoRepair();
            if (repaired > 0) {
                final Collection<Empire> empires = turnData.getEmpiresPresent(ship);
                addNews(empires, "Ship " + ship + " auto-repaired " + repaired + " DP.");
            }
        });
    }
}