package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.objects.Ship;

import java.util.List;

public abstract class TransponderChangesPhaseUpdater extends PhaseUpdater {


    public TransponderChangesPhaseUpdater(final Phase phase, final TurnData turnData) {
        super(phase, turnData);
    }

    protected List<Empire> getTransponderEmpires(final Order order, final List<String> empireNames) {
        final Empire empire = order.getEmpire();

        final List<Empire> validEmpires = Lists.newArrayList();
        empireNames.forEach(empireName -> {
            final Empire transponderEmpire = turnData.getEmpire(empireName);
            if (transponderEmpire == null || !empire.isKnownEmpire(transponderEmpire)) {
                addNewsResult(order, empire, "You have no information about empire " + empireName);
            }
            else {
                validEmpires.add(transponderEmpire);
            }
        });
        return validEmpires;
    }

    protected List<Ship> getTransponderShips(final Order order, final List<String> shipHandles) {
        final List<Ship> validShips = Lists.newArrayList();
        final List<Ship> ships = getShipsByHandle(order, shipHandles);
        for (final Ship ship: ships) {
            if (ship.isPublicTransponder()) {
                addNewsResult(order, "Transponder for ship " + ship + " is public and cannot be modified.");
            }
            else {
                validShips.add(ship);
            }
        }
        return validShips;
    }
}