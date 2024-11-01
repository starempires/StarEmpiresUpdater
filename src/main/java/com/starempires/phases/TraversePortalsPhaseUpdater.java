package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;

import java.util.Collection;
import java.util.List;

public class TraversePortalsPhaseUpdater extends PhaseUpdater {

    public TraversePortalsPhaseUpdater(final TurnData turnData) {
        super(Phase.TRAVERSE_PORTALS, turnData);
    }

    private void traversePortal(final Order order, final Portal entrance, final Portal exit, final List<Ship> ships) {
        final Empire empire = order.getEmpire();
        final Collection<Empire> entranceEmpires = turnData.getEmpiresPresent(entrance);
        entranceEmpires.remove(empire);

        final Collection<Empire> exitEmpires = turnData.getEmpiresPresent(exit);
        exitEmpires.remove(empire);

        ships.forEach(ship -> {
            empire.traverseShip(ship, exit.getCoordinate());
            addNewsResult(order, empire,
                    "Ship " + ship + " traversed wormnet from " + entrance + " to " + exit);
            addNews(entranceEmpires, "Ship " + ship + " entered portal " + entrance);
            addNews(exitEmpires, "Ship " + ship + " exited portal " + exit);
        });

        empire.addPortalTraversed(entrance);
        empire.addPortalTraversed(exit);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TRAVERSE);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final int index = order.indexOfIgnoreCase(Constants.TOKEN_PORTAL);

            final List<String> shipHandles = order.getParameterSubList(0, index);
            final List<Ship> ships = empire.getShips(shipHandles);
            final String entranceName = order.getStringParameter(index + 1);
            final String exitName = order.getStringParameter(index + 2);
            final Portal entrance = turnData.getPortal(entranceName);
            if (entrance == null) {
                addNewsResult(order, empire, "No entrance portal " + entranceName + " found in sector "
                        + ships.stream().findAny().get().getCoordinate());
            }
            else if (entrance.isCollapsed()) {
                addNewsResult(order, empire, "Entrance portal " + entranceName + " is collapsed; no entry possible");
            }
            else {
                final Portal exit;
                if (exitName.equalsIgnoreCase(Constants.TOKEN_RANDOM)) {
                    exit = entrance.selectRandomConnection();
                    if (exit == null) {
                        addNewsResult(order, empire, "No valid exit portal found for entrance portal " + entrance);
                    }
                    else {
                        traversePortal(order, entrance, exit, ships);
                    }
                }
                else {
                    exit = turnData.getPortal(exitName);
                    if (exit == null) {
                        addNewsResult(order, empire,
                                "No exit portal " + exitName + " found for entrance portal " + entrance);
                    }
                    else if (exit.isCollapsed()) {
                        addNewsResult(order, empire,
                                "Exit portal " + exit + " is collapsed; no wormnet traversal possible");
                    }
                    else if (entrance.isConnectedTo(exit)) {
                        traversePortal(order, entrance, exit, ships);
                    }
                    else {
                        addNewsResult(order, empire, "Disjoint wormnet portals " + entrance + " and " + exit);
                    }
                }
            }
        });
    }
}