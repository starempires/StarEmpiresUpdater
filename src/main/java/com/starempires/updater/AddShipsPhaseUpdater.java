package com.starempires.updater;

import com.starempires.TurnData;
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
            final List<Ship> ships = order.getShips();
            ships.forEach(ship -> {
                ship.getOwner().addShip(ship);
                addNews(order, "Added %s %s %s".formatted(ship.getOwner(), ship.getShipClass(), ship));
            });
        });
    }
}