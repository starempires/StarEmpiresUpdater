package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class AddPortalOrder extends Order {

    // order: ADDPORTAL coordinate portal-name
    final static private String REGEX = COORDINATE_CAPTURE_REGEX + SPACE_REGEX + ID_CAPTURE_REGEX
            + OPTIONAL_COLLAPSED_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude
    private Coordinate coordinate;
    @JsonInclude
    private String name;
    @JsonInclude
    private boolean collapsed;

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
            order.name = matcher.group(ID_GROUP);
            final String coordText = matcher.group(COORDINATE_GROUP);
            order.collapsed = StringUtils.isNotBlank(matcher.group(COLLAPSED_GROUP));
            order.coordinate = Coordinate.parse(coordText);
            order.setReady(true);
            turnData.addPortal(Portal.builder()
                    .name(order.name)
                    .coordinate(order.coordinate)
                    .collapsed(order.collapsed)
                    .build());
        } else {
            order.addError("Invalid ADDPORTAL order: " + parameters);
        }

        return order;
    }

    public static AddPortalOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddPortalOrder.builder();
        Order.parseReady(node, turnData, OrderType.ADDPORTAL, builder);
        return builder
                .coordinate(getCoordinateFromJsonNode(node.get("coordinate")))
                .name(getString(node, "name"))
                .collapsed(getBoolean(node, "collapsed"))
                .build();
    }
}