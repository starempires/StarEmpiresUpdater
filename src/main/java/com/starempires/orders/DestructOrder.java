package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
public class DestructOrder extends ShipBasedOrder {

    // order: DESTRUCT ship1 [ship2 ...]
    private static final String REGEX = LOCATION_OR_SHIP_LIST_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    public static DestructOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final DestructOrder order = DestructOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESTRUCT)
                .parameters(parameters)
                .ships(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final List<Ship> destructors = getLocationShips(empire, matcher, order);
            for (final Ship ship : destructors) {
                if (ship.isStarbase()) {
                    order.addError(ship, "Starbase cannot be self-destructed");
                } else if (ship.isLoaded()) {
                    order.addError(ship, "Ship is loaded onto carrier %s and cannot be self-destructed.".formatted(ship.getCarrier()));
                } else {
                    if (ship.hasLoadedCargo()) {
                        order.addWarning(ship, "Self-destructing carrier will self-destruct %d loaded cargo ships.".formatted(ship.getCargo().size()));
                    }
                    else {
                        order.addOKResult(ship);
                    }
                    order.ships.add(ship);
                }
            }

            order.setReady(!order.ships.isEmpty());
        } else {
            order.addError("Invalid DESTRUCT order: " + parameters);
        }
        return order;
    }

    public static DestructOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = DestructOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.DESTRUCT, builder);
        return builder.build();
    }
}