package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
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
    private static final String SHIP_CLASSES_GROUP = "shipclasses";
    private static final String SHIP_CLASSES_CAPTURE_REGEX = "(?<" + SHIP_CLASSES_GROUP + ">" + ID_LIST_REGEX + ")";
    private static final String REGEX = SHIP_CLASSES_CAPTURE_REGEX + SPACE_REGEX + "to" + SPACE_REGEX  + RECIPIENT_LIST_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    final List<ShipClass> shipClasses;

    public static GiveOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final GiveOrder order = GiveOrder.builder()
                .empire(empire)
                .orderType(OrderType.GIVE)
                .parameters(parameters)
                .recipients(Lists.newArrayList())
                .shipClasses(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String[] shipClassNames = matcher.group(SHIP_CLASSES_GROUP).split(SPACE_REGEX);
            final String[] recipientNames = matcher.group(RECIPIENT_LIST_GROUP).split(SPACE_REGEX);
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

    public static GiveOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = GiveOrder.builder();
        EmpireBasedOrder.parseReady(node, turnData, OrderType.GIVE, builder);
        return builder
                .shipClasses(getTurnDataListFromJsonNode(node, turnData::getShipClass))
                .build();
    }
}