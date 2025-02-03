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

@Getter
@SuperBuilder
public class MoveOrder extends ShipBasedOrder {

    final static protected String DESTINATION_COORDINATE_GROUP = "destcoordinate";
    final static protected String DESTINATION_LOCATION_GROUP = "destlocation";
    final static protected String DESTINATION_COORDINATE_CAPTURE_REGEX = "(?<" + DESTINATION_COORDINATE_GROUP + ">" + COORDINATE_REGEX + ")";
    final static protected String DESTINATION_LOCATION_CAPTURE_REGEX = "(?<" + DESTINATION_LOCATION_GROUP + ">" + ID_REGEX + ")";

    final static private String REGEX = SHIP_GROUP_CAPTURE_REGEX + SPACE_REGEX + "to" + SPACE_REGEX + "(?:" + DESTINATION_COORDINATE_CAPTURE_REGEX + "|" + DESTINATION_LOCATION_CAPTURE_REGEX + ")";
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private Coordinate destination; // in galactic
    private String destinationText;

    public static MoveOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final MoveOrder order = MoveOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVE)
                .parameters(parameters)
                .ships(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final List<Ship> movers = getLocationShips(empire, matcher, order);
            if (!movers.isEmpty()) {
                order.ships.addAll(movers);
                final Coordinate startCoordinate = order.ships.stream().findAny().map(Ship::getCoordinate).orElse(null);

                final String coordText = matcher.group(DESTINATION_COORDINATE_GROUP);
                final String locationText = matcher.group(DESTINATION_LOCATION_GROUP);
                if (coordText != null) {
                    order.destinationText = coordText;
                    order.destination = empire.toGalactic(Coordinate.parse(coordText));
                } else if (locationText != null) {
                    order.destinationText = locationText;
                    order.destination = getCoordinateFromMapObject(empire, locationText);
                    if (order.destination == null) {
                        order.addError("Unknown destination: " + locationText);
                        order.setReady(false);
                        return order;
                    }
                } else {
                    order.addError("No valid destination found: " + parameters);
                    order.setReady(false);
                    return order;
                }

                final boolean sameSector = order.ships.stream().allMatch(attacker -> attacker.getCoordinate().equals(startCoordinate));
                if (!sameSector) {
                    order.addError("Movers not all in same sector");
                    order.ships.clear();
                    order.setReady(false);
                    return order;
                }

                for (final Ship ship : movers) {
                    if (ship.isLoaded()) {
                        order.addError(ship, "Loaded ships cannot move");
                    } else if (!ship.isAlive()) {
                        order.addError(ship, "Ship has been destroyed");
                    } else if (ship.getAvailableEngines() < 1) {
                        order.addError(ship, "No operational engines");
                    } else if (ship.getGunsOrderedToFire() > 0) {
                        order.addError(ship, "Ships ordered to fire cannot move");
                    } else {
                        int distance = ship.distanceTo(order.destination);
                        if (distance > ship.getAvailableEngines()) {
                            order.addError(ship, "Insufficient operational engines (%d) to reach destination %s (distance %d)".formatted(ship.getAvailableEngines(), order.destinationText, distance));
                        } else {
                            order.addOKResult(ship);
                            ship.setCoordinate(order.destination);
                        }
                    }
                }
            }

            if (order.ships.isEmpty()) {
                order.addError("No valid movers");
                order.setReady(false);
            }
        }
        else {
            order.addError("Invalid move order: " + parameters);
            order.setReady(false);
        }

        return order;
    }

    public static MoveOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = MoveOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.MOVE, builder);
        return builder
                .destination(getCoordinateFromJsonNode(node.get("destination")))
                .destinationText(getString(node, "destinationText"))
                .build();
    }
}