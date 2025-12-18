package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.AddShipOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;

public class AddShipsPhaseUpdater extends PhaseUpdater {
    public AddShipsPhaseUpdater(TurnData turnData) {
        super(Phase.ADD_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.ADDSHIP);
        orders.forEach(o -> {
            final AddShipOrder order = (AddShipOrder) o;
            final Empire owner = order.getOwner();
            final int startingNumber = owner.getLargestBasenameNumber(order.getBasename());
            for (int i = 0; i < order.getCount(); ++i) {
                final String name;
                if (order.getBasename() != null) {
                    name = order.getBasename() + (startingNumber + i + 1);
                }
                else {
                    name = order.getNames().getFirst();
                }
                final Ship ship = owner.buildShip(order.getShipClass(), order.getCoordinate(), name, turnData.getTurnNumber());
                final String message = "Added %s %s %s in sector %s".formatted(owner, ship.getShipClass(), ship, ship.getCoordinate());
                addNews(order, message);
                addNews(ship.getOwner(), message);
            }
        });
    }
}