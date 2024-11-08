package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;

import java.util.Collection;
import java.util.List;

public abstract class TransponderChangesPhaseUpdater extends PhaseUpdater {

    protected static final String ALL_SHIPS_TOKEN = "all";

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
        final Empire empire = order.getEmpire();
        final List<Ship> validShips = Lists.newArrayList();
        if (shipHandles.contains(ALL_SHIPS_TOKEN)) {
            final Collection<Ship> empireShips = empire.getShips();
            final int publicTransponderCount = (int)empireShips.stream().filter(Ship::isPublicTransponder).count();
            validShips.addAll(empireShips.stream().filter(ship -> !ship.isPublicTransponder()).toList());
            if (publicTransponderCount > 0) {
                addNewsResult(order, "Cannot modify transponder settings for " + publicTransponderCount + " " + plural(publicTransponderCount, "ship") + " with public transponders");
            }
        }
        else {
            shipHandles.forEach(shipHandle -> {
                if (shipHandle.startsWith("@")) { //ship class
                    final String shipClassName = shipHandle.substring(1);
                    final ShipClass shipClass = turnData.getShipClass(shipClassName);
                    if (shipClass == null || !empire.isKnownShipClass(shipClass)) {
                        addNewsResult(order, "You have no information about ship class " + shipClassName);
                    }
                    else {
                        final Collection<Ship> shipsOfClass = empire.getShips(shipClass);
                        final int publicTransponderCount = (int)shipsOfClass.stream().filter(Ship::isPublicTransponder).count();
                        validShips.addAll(shipsOfClass.stream().filter(ship -> !ship.isPublicTransponder()).toList());
                        if (publicTransponderCount > 0) {
                            addNewsResult(order, "Cannot modify transponder settings for " + publicTransponderCount + " " + shipClass + " " + plural(publicTransponderCount, "ship") + " with public transponders");
                        }
                    }
                }
                else {
                    final Ship ship = empire.getShip(shipHandle);
                    if (ship == null) {
                        addNewsResult(order, "You do not own ship " + shipHandle);
                    } else if (ship.isPublicTransponder()) {
                        addNewsResult(order, "Transponder for ship " + ship + " is public and cannot be modified.");
                    } else {
                        validShips.add(ship);
                    }
                }
            });
        }
        return validShips;
    }
}