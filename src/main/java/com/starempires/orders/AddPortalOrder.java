package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class AddPortalOrder extends Order {

    // ADDPORTAL coord name

    final static private String REGEX = COORDINATE_CAPTURE_REGEX + SPACE_REGEX + ID_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private Portal portal;

    public static AddPortalOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final AddPortalOrder order = AddPortalOrder.builder()
                .empire(empire)
                .orderType(OrderType.ADDPORTAL)
                .parameters(parameters)
                .gmOnly(OrderType.ADDPORTAL.isGmOnly())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String portalName = matcher.group(ID_GROUP);
            final String coordText = matcher.group(COORDINATE_GROUP);

            final Coordinate coordinate = Coordinate.parse(coordText);

            order.portal = Portal.builder()
                    .name(portalName)
                    .coordinate(coordinate)
                    .build();
            order.setReady(true);
        } else {
            order.addError("Invalid ADDPORTAL order: " + parameters);
        }

        return order;
    }

    public static AddPortalOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddPortalOrder.builder();
        Order.parseReady(node, turnData, OrderType.ADDPORTAL, builder);
        return builder
                .portal(getTurnDataItemFromJsonNode(node.get("portal"), turnData::getPortal))
                .gmOnly(true)
                .build();
    }
}