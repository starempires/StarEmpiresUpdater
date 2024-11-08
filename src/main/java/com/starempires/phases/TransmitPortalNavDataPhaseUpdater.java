package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Portal;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransmitPortalNavDataPhaseUpdater extends PhaseUpdater {

    // TRANSMIT portal1 [portal2 ...] TO empire1 [empire2 ...]
    final private String PORTALS_GROUP = "portals";
    final private String RECIPIENT_GROUP = "recipients";
    final private String PARAMETERS_REGEX = "^transmit (?<" + PORTALS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*>)\\s+to\\s+(?<" + RECIPIENT_GROUP + ">[\\w]+(?:\\s+[\\w]+)*$";
    final private Pattern PATTERN = Pattern.compile(PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);

    public TransmitPortalNavDataPhaseUpdater(final TurnData turnData) {
        super(Phase.TRANSMIT_PORTAL_NAV_DATA, turnData);
    }

    private List<Empire> getRecipients(final Order order, final String[] empireNames) {
        final Empire empire = order.getEmpire();
        final List<Empire> recipients = Lists.newArrayList();
        for (final String empireName: empireNames) {
            final Empire recipient = turnData.getEmpire(empireName);
            if (recipient == null) {
                addNewsResult(order, empire, "Unknown recipient %s".formatted(empireName));
            }
            else if (empire.isKnownEmpire(recipient)) {
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
        turnData.getOrders(OrderType.TRANSMIT).forEach(order -> {
            final Empire empire = order.getEmpire();
            Matcher matcher = PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final String[] portalNames = matcher.group(PORTALS_GROUP).split(" ");
                final String[] recipientNames = matcher.group(RECIPIENT_GROUP).split(" ");

                final List<Empire> recipients = getRecipients(order, recipientNames);
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
            };
        });
    }
}