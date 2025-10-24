package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Storm;
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

    private Storm storm;

    public static AddStormOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final AddStormOrder order = AddStormOrder.builder()
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
            final String stormName = matcher.group(ID_GROUP);
            final String coordText = matcher.group(COORDINATE_GROUP);
            final int rating = Integer.parseInt(matcher.group(RATING_GROUP));

            final Coordinate coordinate = Coordinate.parse(coordText);

            order.storm = Storm.builder()
                    .name(stormName)
                    .rating(rating)
                    .coordinate(coordinate)
                    .build();
            order.setReady(true);
        } else {
            order.addError("Invalid ADDPORTAL order: " + parameters);
        }

        return order;
    }

    public static AddStormOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddStormOrder.builder();
        Order.parseReady(node, turnData, OrderType.ADDSTORM, builder);
        return builder
                .storm(getTurnDataItemFromJsonNode(node.get("storm"), turnData::getStorm))
                .gmOnly(true)
                .build();
    }
}