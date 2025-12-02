package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.orders.AddPortalOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;
import java.util.Set;

public class AddPortalsPhaseUpdater extends PhaseUpdater {
    public AddPortalsPhaseUpdater(TurnData turnData) {
        super(Phase.ADD_PORTALS, turnData);
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
            final Set<Empire> empires = turnData.getEmpiresPresent(portal);
            empires.add(order.getEmpire());
            addNews(empires, "Portal %s has been added to sector %s".formatted(portal, portal.getCoordinate()));
        });
    }
}