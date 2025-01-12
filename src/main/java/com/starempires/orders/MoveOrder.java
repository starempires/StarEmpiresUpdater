package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
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

    final static private String ABSOLUTE_MOVE_GROUP = "to";
    final static private String MOVE_REGEX = "(?<" + ABSOLUTE_MOVE_GROUP + ">\\s+to\\s+)?" + COORDINATE_REGEX + "|" + LOCATION_REGEX;
    final static private Pattern MOVE_PATTERN = Pattern.compile(MOVE_REGEX, Pattern.CASE_INSENSITIVE);

    private Coordinate destination;
    private String destinationText;

    public static MoveOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final MoveOrder order = MoveOrder.builder()
                .empire(empire)
                .orderType(OrderType.MOVE)
                .parameters(parameters)
                .build();
        final Matcher matcher = MOVE_PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final boolean relativeMove = matcher.group(ABSOLUTE_MOVE_GROUP) == null;

            final List<Ship> movers = getLocationShips(empire, parameters, order);
            final Coordinate startCoordinate = order.ships.stream().findAny().map(Ship::getCoordinate).orElse(null);

            final String coordText = matcher.group(COORDINATE_GROUP);
            final String locationText = matcher.group(LOCATION_GROUP);
            Coordinate destination = null;
            if (coordText != null) {
                order.destinationText = coordText;
                if (relativeMove) {
                   destination = startCoordinate.translateToNew(Coordinate.parse(coordText));
                }
                else {
                    destination = Coordinate.parse(coordText);
                }
            } else if (locationText != null) {
                order.destinationText = locationText;
                if (relativeMove) {
                    order.addError("Location not supported for relative movement");
                    order.setReady(false);
                    return order;
                }
                destination = getCoordinateFromMapObject(empire, locationText);
                if (destination == null) {
                    order.addError("Unknown destination: " + locationText);
                    order.setReady(false);
                    return order;
                }
            }
            else {
                order.addError("Invalid move order: " + parameters);
                order.setReady(false);
                return order;
            }

            final boolean sameSector = order.ships.stream().allMatch(attacker -> attacker.getCoordinate() == startCoordinate);
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
                    int distance = ship.distanceTo(destination);
                    if (distance > ship.getAvailableEngines()) {
                        order.addError(ship, "Insufficient operational engines (%d) to reach destination %s (distance %d)".formatted(ship.getAvailableEngines(), order.destinationText, distance));
                    }
                    else {
                        order.ships.add(ship);
                        order.addOKResult(ship);
                        ship.setCoordinate(destination);
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
        final String name = getString(node, "carrier");
        return builder
                .destination(getCoordinateFromJsonNode(node))
                .destinationText(getString(node, "destinationText"))
                .build();
    }
}