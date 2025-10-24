package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class RelocateShipOrder extends ShipBasedOrder {

    // RELOCATESHIP empire ship1 [ship2 ... ] to coordinate

    final static private String REGEX = OWNER_CAPTURE_REGEX + SPACE_REGEX + SHIP_LIST_CAPTURE_REGEX + SPACE_REGEX + TO_TOKEN + SPACE_REGEX + COORDINATE_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private Coordinate coordinate;
    private Empire owner;

    public static RelocateShipOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final RelocateShipOrder order = RelocateShipOrder.builder()
                .empire(empire)
                .orderType(OrderType.RELOCATESHIP)
                .parameters(parameters)
                .ships(Lists.newArrayList())
                .gmOnly(true)
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String ownerText = matcher.group(OWNER_GROUP);
            final Empire owner = turnData.getEmpire(ownerText);
            final String coordText = matcher.group(COORDINATE_GROUP);
            order.coordinate = Coordinate.parse(coordText);

            if (owner == null) {
                order.addError("Unknown owning empire: " + ownerText);
                return order;
            }
            order.owner = owner;
            final List<Ship> movers = getShipsFromNames(owner, matcher.group(SHIP_LIST_GROUP), order);

            if (movers.isEmpty()) {
                order.addError("No valid ships to relocate");
            }
            else {
                order.ships.addAll(movers);
                order.setReady(true);
            }
        } else {
            order.addError("Invalid RELOCATESHIP order: " + parameters);
        }

        return order;
    }

    public static RelocateShipOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = RelocateShipOrder.builder();
        Order.parseReady(node, turnData, OrderType.RELOCATESHIP, builder);
        final Empire empire = turnData.getEmpire(node.get("empire").asText());
        return builder
                .ships(getTurnDataListFromJsonNode(node.get("ships"), empire::getShip))
                .coordinate(getCoordinateFromJsonNode(node.get("coordinate")))
                .gmOnly(true)
                .build();
    }
}