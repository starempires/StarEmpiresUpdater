package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Portal;
import com.starempires.orders.AddConnectionOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;

public class AddConnectionsPhaseUpdater extends PhaseUpdater {
    public AddConnectionsPhaseUpdater(TurnData turnData) {
        super(Phase.ADD_CONNECTIONS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.ADDCONNECTION);
        orders.forEach(o -> {
            final AddConnectionOrder order = (AddConnectionOrder) o;
            final Portal entry = order.getEntry();
            final Portal exit = order.getExit();
            entry.addConnection(exit);
            addNews(order, "Connected entry portal %s to exit portal %s".formatted(entry, exit));
        });
    }
}