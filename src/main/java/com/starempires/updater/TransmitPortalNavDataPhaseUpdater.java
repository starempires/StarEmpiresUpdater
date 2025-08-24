package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.TransmitOrder;

import java.util.List;

public class TransmitPortalNavDataPhaseUpdater extends PhaseUpdater {

    public TransmitPortalNavDataPhaseUpdater(final TurnData turnData) {
        super(Phase.TRANSMIT_PORTAL_NAV_DATA, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TRANSMIT);
        orders.forEach(o -> {
            final TransmitOrder order = (TransmitOrder) o;
            final Empire empire = order.getEmpire();
            for (Portal portal: order.getPortals()) {
                if (empire.hasNavData(portal)) {
                    order.getRecipients().forEach(recipient -> {
                        if (empire.isKnownEmpire(recipient)) {
                            recipient.addKnownPortal(portal);
                            recipient.addNavData(portal);
                            addNewsResult(order, empire,
                                    "You gave navigation data for portal " + portal + " given to " + recipient);
                            addNews(recipient, empire + " gave navigation data for portal " + portal + " to you");
                        }
                        else {
                            addNewsResult(order, empire, "You are not in message contact with %s".formatted(recipient));
                        }
                    });
                }
                else {
                    addNewsResult(order, "You do not have navigation data for portal " + portal);
                }
            }
        });
    }
}