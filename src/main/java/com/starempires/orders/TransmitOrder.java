package com.starempires.orders;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class TransmitOrder extends EmpireBasedOrder {

    // TRANSMIT portal1 [portal2 ...] TO empire1 [empire2 ...]
    private static final String PORTALS_GROUP = "portals";
    private static final String RECIPIENT_GROUP = "recipients";
    private static final String PARAMETERS_REGEX = "(?<" + PORTALS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*>)\\s+to\\s+(?<" + RECIPIENT_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)$";
    private static final Pattern PATTERN = Pattern.compile(PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);

    final List<Portal> portals;
    final List<Empire> recipients;

    public static TransmitOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final TransmitOrder order = TransmitOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSMIT)
                .parameters(parameters)
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String[] portalNames = matcher.group(PORTALS_GROUP).split("\\s+");
            final String[] recipientNames = matcher.group(RECIPIENT_GROUP).split("\\s+");
            for (String portalName: portalNames) {
                final Portal portal = turnData.getPortal(portalName);
                if (empire.isKnownPortal(portal)) {
                    order.addError("Unknown portal: " + portalName);
                } else if (!empire.hasNavData(portal)) {
                    order.addError("You do not have navigation data for portal: " + portal);
                } else {
                    order.portals.add(portal);
                }
            }

            if (order.portals.isEmpty()) {
                order.addError("No valid portals to transmit");
                order.setReady(false);
                return order;
            }

            for (String recipientName: recipientNames) {
                final Empire recipient = turnData.getEmpire(recipientName);
                if (empire.isKnownEmpire(recipient)) {
                    order.addError("You are not in message contact with empire %s".formatted(recipientName));
                } else if (empire.equals(recipient)) {
                    order.addError("No need to transmit portal nav data to yourself");
                } else if (empire.isGM()) {
                    order.addError("The GM politely declines your offer");
                } else {
                    order.recipients.add(recipient);
                    for (Portal portal: order.portals) {
                        recipient.addKnownPortal(portal);
                        recipient.addNavData(portal);
                        order.addOKResult(portal + " to " + recipientName);
                    }
                }
            }

            if (order.recipients.isEmpty()) {
                order.addError("No valid recipients");
                order.setReady(false);
            }
            return order;
        } else {
            order.addError("Invalid TRANSMIT order: " + parameters);
            order.setReady(false);
        }
        return order;
    }
}