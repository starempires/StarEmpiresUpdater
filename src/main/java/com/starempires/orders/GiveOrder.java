package com.starempires.orders;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.ShipClass;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class GiveOrder extends EmpireBasedOrder {

    // GIVE shipclass1 [shipclass2 ...] TO empire1 [empire2 ...]
    private static final String CLASSES_GROUP = "classes";
    private static final String RECIPIENT_GROUP = "recipients";
    private static final String PARAMETERS_REGEX = "(?<" + CLASSES_GROUP + ">[\\w]+(?:\\s+[\\w]+)*>)\\s+to\\s+(?<" + RECIPIENT_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)$";
    private static final Pattern PATTERN = Pattern.compile(PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);

    final List<ShipClass> shipClasses;
    final List<Empire> recipients;

    public static GiveOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final GiveOrder order = GiveOrder.builder()
                .empire(empire)
                .orderType(OrderType.GIVE)
                .parameters(parameters)
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String[] shipClassNames = matcher.group(CLASSES_GROUP).split("\\s+");
            final String[] recipientNames = matcher.group(RECIPIENT_GROUP).split("\\s+");
            for (String shipClassName: shipClassNames) {
                final ShipClass shipClass = turnData.getShipClass(shipClassName);
                if (empire.isKnownShipClass(shipClass)) {
                    order.addError("Unknown ship class: " + shipClassName);
                } else {
                    order.shipClasses.add(shipClass);
                }
            }

            if (order.shipClasses.isEmpty()) {
                order.addError("No valid ship classes to give");
                order.setReady(false);
                return order;
            }

            for (String recipientName: recipientNames) {
                final Empire recipient = turnData.getEmpire(recipientName);
                if (empire.isKnownEmpire(recipient)) {
                    order.addError("You are not in message contact with empire %s".formatted(recipientName));
                } else if (empire.equals(recipient)) {
                    order.addError("No need to give ship classes to yourself");
                } else if (empire.isGM()) {
                    order.addError("The GM politely declines your offer");
                } else {
                    order.recipients.add(recipient);
                    for (ShipClass shipClass: order.shipClasses) {
                        recipient.addKnownShipClass(shipClass);
                        order.addOKResult(shipClass + " to " + recipientName);
                    }
                }
            }

            if (order.recipients.isEmpty()) {
                order.addError("No valid recipients");
                order.setReady(false);
            }
            return order;
        } else {
            order.addError("Invalid GIVE order: " + parameters);
            order.setReady(false);
        }
        return order;
    }
}