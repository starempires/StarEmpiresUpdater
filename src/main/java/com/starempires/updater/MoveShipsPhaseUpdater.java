package com.starempires.updater;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Ship;
import com.starempires.orders.MoveOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MoveShipsPhaseUpdater extends PhaseUpdater {

    public MoveShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.MOVE_SHIPS, turnData);
    }

    private List<Ship> gatherValidMovers(@NonNull final Order order, final List<Ship> possibleMovers) {
        final List<Ship> validMovers = Lists.newArrayList();
        for (final Ship mover : possibleMovers) {
            if (!mover.isAlive()) {
                order.addResult("Omitting destroyed ship %s".formatted(mover));
            } else if (mover.getGunsActuallyFired() > 0) {
                order.addResult("Attacking ship %s cannot move".formatted(mover));
            } else if (mover.isLoaded()) {
                order.addResult("Loaded cargo %s will move with carrier".formatted(mover));
            } else {
                validMovers.add(mover);
            }
        }
        return validMovers;
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.MOVE);
        Multimap<MoveOrder, Ship> movingShips = ArrayListMultimap.create();
        orders.forEach(o -> {
            final MoveOrder order = (MoveOrder) o;
            addOrderText(order);
            final List<Ship> movers = order.getShips();
            // remove ships ordered to move which cannot legally move
            final List<Ship> validMovers = gatherValidMovers(order, movers);
            validMovers.sort(IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR);
            if (validMovers.isEmpty()) {
                order.addResult("No valid movers found");
                return;
            }

            final Coordinate destination = order.getDestination();
            // remove valid movers which are trying to move beyond their engine capacity
            validMovers.forEach(mover -> {
                final int availableEngines = mover.getAvailableEngines();
                final int distance = mover.distanceTo(destination);
                if (distance <= availableEngines) {
                    movingShips.put(order, mover);
                } else {
                    addNews(order, "Ship %s has insufficient operational engines (max move %d) to reach destination %s (distance %d)".formatted(mover, availableEngines, mover.getOwner().toLocal(order.getDestination()), distance));
                }
            });
        });

        for (Map.Entry<MoveOrder, Ship> entry: movingShips.entries()) {
            final MoveOrder order = entry.getKey();
            final Empire empire = order.getEmpire();
            final Ship mover = entry.getValue();

             // report departing ships to all empires present in origin sectors
            final Collection<Empire> originEmpires = turnData.getEmpiresPresent(mover);
            final MappableObject originObject = turnData.getMappableObject(mover.getCoordinate());
            final MappableObject destinationObject = turnData.getMappableObject(order.getDestination());

            originEmpires.forEach(originEmpire -> {
                String originText;
                if (originObject == null) {
                    originText = "sector " + originEmpire.toLocal(mover.getCoordinate());
                }
                else {
                    originText = originObject.toString();
                }
                if (originEmpire.equals(empire)) {
                    String destinationText;
                    if (destinationObject == null) {
                        destinationText = "sector " + originEmpire.toLocal(order.getDestination());
                    }
                    else {
                        destinationText = destinationObject.toString();
                    }
                    addNews(originEmpire, "%s ship %s moved from %s to %s".formatted(mover.getOwner(), mover, originText, destinationText));
                }
                else {
                    addNews(originEmpire, "%s ship %s departed %s".formatted(mover.getOwner(), mover, originText));
                }
            });
        }

        // move the ships
        for (Map.Entry<MoveOrder, Ship> entry: movingShips.entries()) {
            final MoveOrder order = entry.getKey();
            final Empire empire = order.getEmpire();
            final Ship mover = entry.getValue();
            empire.moveShip(mover, order.getDestination());
        }

        // report arriving ships to all empires present in destination sectors
        for (Map.Entry<MoveOrder, Ship> entry: movingShips.entries()) {
            final MoveOrder order = entry.getKey();
            final Empire empire = order.getEmpire();
            final Ship mover = entry.getValue();
            final Collection<Empire> destinationEmpires = turnData.getEmpiresPresent(mover);
            destinationEmpires.remove(empire);
            final MappableObject destinationObject = turnData.getMappableObject(order.getDestination());

            destinationEmpires.forEach(destinationEmpire -> {
                String moveText;
                if (destinationObject == null) {
                    moveText = "sector " + destinationEmpire.toLocal(mover.getCoordinate());
                }
                else {
                    moveText = destinationObject.toString();
                }
                addNews(destinationEmpire, "%s ship %s arrived at %s".formatted(empire, mover, moveText));
            });
        }
    }
}