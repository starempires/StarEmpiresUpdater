package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.RelocateShipOrder;

import java.util.List;

public class RelocateShipsPhaseUpdater extends PhaseUpdater {

    public RelocateShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.RELOCATE_OBJECTS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.RELOCATESHIP);
        orders.forEach(o -> {
            final RelocateShipOrder order = (RelocateShipOrder) o;
            final Coordinate coordinate = order.getCoordinate();
            final Empire owner = order.getEmpire();
            final List<Ship> ships = order.getShips();
            ships.forEach(ship -> {
                ship.setCoordinate(coordinate);
                addNews(order, "Ship " + ship + " has been moved to sector " + coordinate);
            });
        });
    }
}