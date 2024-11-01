package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Portal;

import java.util.List;

public class TransmitPortalNavDataPhaseUpdater extends PhaseUpdater {

    public TransmitPortalNavDataPhaseUpdater(final TurnData turnData) {
        super(Phase.TRANSMIT_PORTAL_NAV_DATA, turnData);
    }

    private List<Empire> getRecipients(final Order order, final List<String> empireNames) {
        final Empire empire = order.getEmpire();
        final List<Empire> recipients = Lists.newArrayList();
        empireNames.forEach(empireName -> {
            final Empire recipient = turnData.getEmpire(empireName);
            if (recipient == null) {
                addNewsResult(order, empire, "Unknown empire " + empireName);
            }
            else if (empire.isKnownEmpire(recipient)) {
                recipients.add(empire);
            }
            else {
                addNewsResult(order, empire, "You are not in message contact with empire " + empire);
            }
        });
        return recipients;
    }

    private List<Portal> getPortals(final Order order, final List<String> portalNames) {
        final Empire empire = order.getEmpire();
        final List<Portal> portals = Lists.newArrayList();

        portalNames.forEach(portalName -> {
            final Portal portal = turnData.getPortal(portalName);
            if (portal == null) {
                addNewsResult(order, empire, "Unknown portal " + portalName);
            }
            else if (empire.hasNavData(portal)) {
                portals.add(portal);
            }
            else {
                addNewsResult(order, empire, "You do not have navigation data for portal " + portal);
            }
        });
        return portals;
    }

    @Override
    public void update() {
        turnData.getOrders(OrderType.TRANSMIT).forEach(order -> {
            final Empire empire = order.getEmpire();

            final int index = order.indexOfIgnoreCase(Constants.TOKEN_TO);

            final List<String> portalNames = order.getParameterSubList(0, index);
            final List<String> empireNames = order.getParameterSubList(index + 1);

            final List<Empire> recipients = getRecipients(order, empireNames);
            final List<Portal> portals = getPortals(order, portalNames);

            portals.forEach(portal -> {
                recipients.forEach(recipient -> {
                    recipient.addKnownPortal(portal);
                    recipient.addNavData(portal);
                    addNewsResult(order, empire,
                            "Navigation data for portal " + portal + " given to " + recipient);
                    addNews(recipient, empire + " gave navigation data for portal " + portal + " to you");
                });
            });
        });
    }
}