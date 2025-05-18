package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
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
    private static final String PORTALS_CAPTURE_REGEX = "(?<" + PORTALS_GROUP + ">" + ID_LIST_REGEX +")";
    private static final String REGEX = PORTALS_CAPTURE_REGEX + SPACE_REGEX + "to" + SPACE_REGEX + RECIPIENT_LIST_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    final List<Portal> portals;

    public static TransmitOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final TransmitOrder order = TransmitOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRANSMIT)
                .parameters(parameters)
                .portals(Lists.newArrayList())
                .recipients(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String[] portalNames = matcher.group(PORTALS_GROUP).split(SPACE_REGEX);
            final String[] recipientNames = matcher.group(RECIPIENT_LIST_GROUP).split(SPACE_REGEX);
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
            }
            else {
                order.setReady(true);
            }
            return order;
        } else {
            order.addError("Invalid TRANSMIT order: " + parameters);
        }
        return order;
    }

    public static TransmitOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = TransmitOrder.builder();
        EmpireBasedOrder.parseReady(node, turnData, OrderType.TRANSMIT, builder);
        return builder
                .portals(getTurnDataListFromJsonNode(node.get("portals"), turnData::getPortal))
                .build();
    }
}