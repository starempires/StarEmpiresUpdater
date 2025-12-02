package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.RemoveShipOrder;

import java.util.List;

public class RemoveShipsPhaseUpdater extends PhaseUpdater {
    public RemoveShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.REMOVE_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.REMOVESHIP);
        orders.forEach(o -> {
            final RemoveShipOrder order = (RemoveShipOrder) o;
            final Empire owner = order.getOwner();
            order.getShips().forEach(ship -> {
                owner.removeShip(ship);
                final String message = "Ship %s has been removed".formatted(ship);
                addNews(owner, message);
                addNews(order.getEmpire(), message);
            });
        });
    }
}