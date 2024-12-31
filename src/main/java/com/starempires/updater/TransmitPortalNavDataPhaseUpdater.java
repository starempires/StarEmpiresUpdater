package com.starempires.updater;

import com.google.common.collect.Lists;
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

    private List<Empire> getRecipients(final Order order, final String[] empireNames) {
        final Empire empire = order.getEmpire();
        final List<Empire> recipients = Lists.newArrayList();
        for (final String empireName: empireNames) {
            final Empire recipient = turnData.getEmpire(empireName);
            if (empire.isKnownEmpire(recipient)) {
                recipients.add(empire);
            }
            else {
                addNewsResult(order, empire, "You are not in message contact with empire %s".formatted(recipient));
            }
        }
        return recipients;
    }

    private List<Portal> getPortals(final Order order, final String[] portalNames) {
        final Empire empire = order.getEmpire();
        final List<Portal> portals = Lists.newArrayList();

        for (final String portalName: portalNames) {
            final Portal portal = turnData.getPortal(portalName);
            if (portal == null) {
                addNewsResult(order, empire, "Unknown portal %s".formatted(portalName));
            }
            else if (empire.hasNavData(portal)) {
                portals.add(portal);
            }
            else {
                addNewsResult(order, empire, "You do not have navigation data for portal %s".formatted(portal));
            }
        }
        return portals;
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
                                    "Navigation data for portal " + portal + " given to " + recipient);
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