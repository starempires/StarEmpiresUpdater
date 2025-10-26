package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Portal;
import com.starempires.orders.AddPortalOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;

public class AddPortalsPhaseUpdater extends PhaseUpdater {
    public AddPortalsPhaseUpdater(TurnData turnData) {
        super(Phase.ADD_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.ADDPORTAL);
        orders.forEach(o -> {
            final AddPortalOrder order = (AddPortalOrder) o;
            final Portal portal = Portal.builder()
                    .coordinate(order.getCoordinate())
                    .name(order.getName())
                    .build();
            turnData.addPortal(portal);
            addNews(order, "Added portal " + portal + " in sector " + portal.getCoordinate());
        });
    }
}