package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Portal;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.RemoveConnectionOrder;

import java.util.List;

public class RemoveConnectionsPhaseUpdater extends PhaseUpdater {

    public RemoveConnectionsPhaseUpdater(final TurnData turnData) {
        super(Phase.REMOVE_CONNECTIONS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.REMOVECONNECTION);
        orders.forEach(o -> {
            final RemoveConnectionOrder order = (RemoveConnectionOrder) o;
            final Portal entry = order.getEntry();
            final Portal exit = order.getExit();
            entry.removeConnection(exit);
            addNews(order, "Connection removed between %s and %s".formatted(entry, exit));
        });
    }
}