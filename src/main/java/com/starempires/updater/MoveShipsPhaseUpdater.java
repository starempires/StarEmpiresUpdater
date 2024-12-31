package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.MoveOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;

public class MoveShipsPhaseUpdater extends PhaseUpdater {

    public MoveShipsPhaseUpdater(TurnData turnData) {
        super(Phase.MOVE_SHIPS, turnData);
    }

    private List<Ship> gatherValidMovers(@NonNull Order order, List<Ship> possibleMovers) {
        final List<Ship> validMovers = Lists.newArrayList();
        for (final Ship mover: possibleMovers) {
            if (!mover.isAlive()) {
                order.addResult("Omitting destroyed ship %s".formatted(mover));
            }
            else if (mover.isLoaded()) {
                order.addResult("Omitting loaded ship %s".formatted(mover));
            }
            else if (mover.getGunsActuallyFired() > 0) {
                order.addResult("Omitting attacking ship %s".formatted(mover));
            }
            else {
                validMovers.add(mover);
            }
        }
        return validMovers;
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.MOVE);
        orders.forEach(o -> {
            final MoveOrder order = (MoveOrder) o;
            final Empire empire = order.getEmpire();
            final List<Ship> movers = order.getShips();
            final List<Ship> validMovers = gatherValidMovers(order, movers);
            if (validMovers.isEmpty()) {
                order.addResult("No valid movers found");
                return;
            }

            final Coordinate destination = order.getDestination();
            validMovers.forEach(mover -> {
                 final int availableEngines = mover.getAvailableEngines();
                 final int distance = mover.distanceTo(destination);
                 if (distance <= availableEngines) {
                     final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(mover);
                     newsEmpires.remove(empire);
                     addNewsResult(order, empire, "Ship " + mover + " moved to destination " + order.getDestinationText());
                        addNews(newsEmpires, "Ship " + mover + " moved out of sector " + mover.getCoordinate());
                        empire.moveShip(mover, destination);
                    }
                    else {
                        addNewsResult(order, "Ship %s has insufficient operational engines (max move %d) to reach destination %s (distance %d)".formatted(mover, availableEngines, order.getDestination(), distance));
                    }
            });
        });
    }
}