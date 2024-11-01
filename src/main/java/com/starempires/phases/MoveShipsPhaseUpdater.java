package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;

import java.util.Collection;
import java.util.List;

public class MoveShipsPhaseUpdater extends PhaseUpdater {

    public MoveShipsPhaseUpdater(TurnData turnData) {
        super(Phase.MOVE_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.MOVE);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final int index = order.indexOfIgnoreCase(Constants.TOKEN_TO);
            final List<String> shipHandles = order.getParameterSubList(0, index);
            final String destinationCoords = order.getStringParameter(index + 1);
            final Coordinate destination = Coordinate.parse(destinationCoords);
            shipHandles.forEach(shipHandle -> {
                final Ship ship = empire.getShip(shipHandle);
                if (ship == null) {
                    addNewsResult(order, empire, "Unknown ship " + shipHandle);
                }
                else if (!ship.isAlive()) {
                    addNewsResult(order, empire, "Ship " + ship + " is destroyed");
                }
                else {
                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                    final int availableEngines = ship.getAvailableEngines();
                    final int distance = ship.distanceTo(destination);
                    if (distance <= availableEngines) {
                        newsEmpires.remove(empire);
                        addNewsResult(order, empire, "Ship " + ship + " moved to destination " + destination);
                        addNews(newsEmpires, "Ship " + ship + " moved out of sector " + ship.getCoordinate());
                        empire.moveShip(ship, destination);
                    }
                    else {
                        addNewsResult(order, empire, "Ship " + ship + " has insufficient operational engines (max move "
                                + availableEngines + ") to reach destination (distance " + distance + ")");
                    }
                }
            });
        });
    }
}