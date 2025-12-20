package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Ship;
import com.starempires.orders.ModifyShipOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;

public class ModifyShipsPhaseUpdater extends PhaseUpdater {

    public ModifyShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.MODIFY_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.MODIFYSHIP);
        orders.forEach(o -> {
            final ModifyShipOrder order = (ModifyShipOrder) o;
            final Ship ship = order.getShip();
            ship.setDpRemaining(order.getDp());
            ship.toggleTransponder(order.isPublicMode());
            final String message = "Ship %s now has %d DP remaining and %s transponder".formatted(ship, order.getDp(),
                                           order.isPublicMode() ? "public" : "private");
            addNews(order, message);
            addNews(ship.getOwner(), message);
        });
    }
}