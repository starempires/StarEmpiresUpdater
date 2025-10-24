package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class AddWorldOrder extends WorldBasedOrder {

    // ADDWORLD coord name production stockpile owner
    final static protected String PRODUCTION_GROUP = "production";
    final static protected String PRODUCTION_CAPTURE_REGEX = "(?<" + PRODUCTION_GROUP + ">" + INT_REGEX + ")";
    final static protected String STOCKPILE_GROUP = "stockpile";
    final static protected String STOCKPILE_CAPTURE_REGEX = "(?<" + STOCKPILE_GROUP + ">" + INT_REGEX + ")";

    final static private String REGEX = COORDINATE_CAPTURE_REGEX + SPACE_REGEX + ID_CAPTURE_REGEX + SPACE_REGEX + PRODUCTION_CAPTURE_REGEX + SPACE_REGEX + STOCKPILE_CAPTURE_REGEX +
                                        OPTIONAL_OWNER_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    public static AddWorldOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final AddWorldOrder order = AddWorldOrder.builder()
                .empire(empire)
                .orderType(OrderType.ADDWORLD)
                .parameters(parameters)
                .gmOnly(OrderType.ADDWORLD.isGmOnly())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String worldName = matcher.group(ID_GROUP);
            final String coordText = matcher.group(COORDINATE_GROUP);
            final int production = Integer.parseInt(matcher.group(PRODUCTION_GROUP));
            final int stockpile = Integer.parseInt(matcher.group(STOCKPILE_GROUP));
            final String ownerName = matcher.group(OWNER_GROUP);

            final Coordinate coordinate = Coordinate.parse(coordText);
            Empire owner = null;
            if (ownerName != null) {
                owner = turnData.getEmpire(ownerName);
                if (owner == null) {
                    order.addError("Unknown owner: " + ownerName);
                    return order;
                }
            }

            order.world = World.builder()
                    .name(worldName)
                    .coordinate(coordinate)
                    .production(production)
                    .stockpile(stockpile)
                    .owner(owner)
                    .build();
            order.setReady(true);
        } else {
            order.addError("Invalid ADDWORLD order: " + parameters);
        }

        return order;
    }

    public static AddWorldOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddWorldOrder.builder();
        Order.parseReady(node, turnData, OrderType.ADDWORLD, builder);
        return builder
                .world(getTurnDataItemFromJsonNode(node.get("world"), turnData::getWorld))
                .gmOnly(true)
                .build();
    }
}