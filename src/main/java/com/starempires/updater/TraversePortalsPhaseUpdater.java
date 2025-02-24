package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.TraverseOrder;

import java.util.Collection;
import java.util.List;

public class TraversePortalsPhaseUpdater extends PhaseUpdater {

    public TraversePortalsPhaseUpdater(final TurnData turnData) {
        super(Phase.TRAVERSE_PORTALS, turnData);
    }

    private void traversePortal(final Order order, final Portal entry, final Portal exit, final List<Ship> ships) {
            final Empire empire = order.getEmpire();
        final Collection<Empire> entranceEmpires = turnData.getEmpiresPresent(entry);
        entranceEmpires.remove(empire);

        final Collection<Empire> exitEmpires = turnData.getEmpiresPresent(exit);
        exitEmpires.remove(empire);

        ships.forEach(ship -> {
            empire.traverseShip(ship, exit.getCoordinate());
            addNewsResult(order, empire,
                    "Ship " + ship + " traversed wormnet from " + entry + " to " + exit);
            addNews(entranceEmpires, "Ship " + ship + " entered portal " + entry);
            addNews(exitEmpires, "Ship " + ship + " exited portal " + exit);
        });

        empire.addPortalTraversed(entry);
        empire.addPortalTraversed(exit);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TRAVERSE);
        orders.forEach(o -> {
            TraverseOrder order = (TraverseOrder) o;
            final List<Ship> ships = order.getShips();
            final List<Ship> traversers = Lists.newArrayList();
            for (Ship ship : ships) {
                if (!ship.isAlive()) {
                    addNewsResult(order, "Omitting destroyed ship " + ship);
                } else if (ship.getAvailableEngines() < 1) {
                    order.addResult("Ship %s has no operational engines".formatted(ship));
                } else if (ship.isLoaded()) {
                    order.addResult("Loaded cargo %s will move with carrier".formatted(ship));
                } else if (ship.getGunsActuallyFired() > 0) {
                    order.addResult("Attacking ship %s cannot move".formatted(ship));
                }
                else {
                    traversers.add(ship);
                }
            }



            if (traversers.isEmpty()) {
                addNewsResult(order, "No valid movers found");
                return;
            }

            final Portal entry = order.getEntry();
            if (entry.isCollapsed()) {
                addNewsResult(order, "Entry portal " + entry + " is collapsed; no traversal possible");
                return;
             }

             Portal exit = order.getExit();
             if (exit == null) {
                 exit = entry.selectRandomConnection();
             }
             else if (!entry.isConnectedTo(exit)) {
                 addNewsResult(order, "Entry portal %s and exit portal %s are not connected".formatted(entry, exit));
                 return;
             }

             if (exit.isCollapsed()) {
                 addNewsResult(order, "Exit portal " + exit + " is collapsed; no wormnet traversal possible");
                 return;
             }
             traversePortal(order, entry, exit, traversers);
        });
    }
}