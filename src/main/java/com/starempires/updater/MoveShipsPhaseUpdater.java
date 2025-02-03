package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Ship;
import com.starempires.orders.MoveOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;

public class MoveShipsPhaseUpdater extends PhaseUpdater {

    public MoveShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.MOVE_SHIPS, turnData);
    }

    private List<Ship> gatherValidMovers(@NonNull final Order order, final List<Ship> possibleMovers) {
        final List<Ship> validMovers = Lists.newArrayList();
        for (final Ship mover : possibleMovers) {
            if (!mover.isAlive()) {
                order.addResult("Omitting destroyed ship %s".formatted(mover));
            } else if (mover.isLoaded()) {
                order.addResult("Omitting loaded ship %s".formatted(mover));
            } else if (mover.getGunsActuallyFired() > 0) {
                order.addResult("Omitting attacking ship %s".formatted(mover));
            } else {
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
                    String moveText;
                    addNewsResult(order, empire, "Ship " + mover + " moved to destination " + order.getDestinationText());

                    final Collection<Empire> originNewsEmpires = turnData.getEmpiresPresent(mover);
                    originNewsEmpires.remove(empire);
                    MappableObject mapObject = turnData.getMappableObject(mover.getCoordinate());
                    if (mapObject == null) {
                        moveText = "moved out of sector " + destination;
                    }
                    else {
                        moveText = "departed " + mapObject.toString();
                    }
                    addNews(originNewsEmpires, "%s ship %s %s".formatted(empire, mover, moveText));
                    empire.moveShip(mover, destination);

                    final Collection<Empire> destinationNewsEmpires = turnData.getEmpiresPresent(mover);
                    destinationNewsEmpires.remove(empire);
                    final MappableObject destinationObject = turnData.getMappableObject(destination);
                    if (destinationObject == null) {
                        moveText = "sector " + destination;
                    }
                    else {
                        moveText = destinationObject.toString();
                    }
                    addNews(destinationNewsEmpires, "%s ship %s arrived at %s".formatted(empire, mover, moveText));
                } else {
                    addNewsResult(order, "Ship %s has insufficient operational engines (max move %d) to reach destination %s (distance %d)".formatted(mover, availableEngines, order.getDestination(), distance));
                }
            });
        });
    }
}