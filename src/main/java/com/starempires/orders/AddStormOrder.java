package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class AddStormOrder extends Order {

    // ADDSTORM coord name rating
    final static protected String RATING_GROUP = "rating";
    final static protected String RATING_CAPTURE_REGEX = "(?<" + RATING_GROUP + ">" + INT_REGEX + ")";
    final static private String REGEX = COORDINATE_CAPTURE_REGEX + SPACE_REGEX + ID_CAPTURE_REGEX + SPACE_REGEX + RATING_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude
    private Coordinate coordinate;
    @JsonInclude
    private String name;
    @JsonInclude
    private int rating;

    public static AddStormOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final AddStormOrder order = AddStormOrder.builder()
                .empire(empire)
                .orderType(OrderType.ADDSTORM)
                .parameters(parameters)
                .gmOnly(OrderType.ADDSTORM.isGmOnly())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            order.name = matcher.group(ID_GROUP);
            final String coordText = matcher.group(COORDINATE_GROUP);
            order.rating = Integer.parseInt(matcher.group(RATING_GROUP));
            order.coordinate = Coordinate.parse(coordText);
            order.setReady(true);
        } else {
            order.addError("Invalid ADDSTORM order: " + parameters);
        }

        return order;
    }

    public static AddStormOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddStormOrder.builder();
        Order.parseReady(node, turnData, OrderType.ADDSTORM, builder);
        return builder
                .coordinate(getCoordinateFromJsonNode(node.get("coordinate")))
                .name(getString(node, "name"))
                .rating(getInt(node, "rating"))
                .gmOnly(true)
                .build();
    }
}