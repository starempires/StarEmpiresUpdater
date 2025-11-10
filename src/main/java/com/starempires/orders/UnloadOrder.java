package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@SuperBuilder
public class UnloadOrder extends ShipBasedOrder {

    // order: UNLOAD ship1 [ship2 ... ]
    private static final String REGEX = OBJECT_LIST_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    public static UnloadOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final UnloadOrder order = UnloadOrder
                .builder()
                .orderType(OrderType.UNLOAD)
                .empire(empire)
                .parameters(parameters)
                .ships(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String unloadNamesText = matcher.group(OBJECT_LIST_GROUP);
            final List<Ship> cargo = getShipsFromNames(empire, unloadNamesText, order);
            for (final Ship ship : cargo) {
                if (!ship.isLoaded()) {
                    order.addError(ship, "Ship is not loaded");
                } else {
                    order.ships.add(ship);
                    turnData.unload(ship);
                    order.addOKResult(ship);
                }
            }
            order.setReady(!order.ships.isEmpty());
        }
        else {
            order.addError("Invalid UNLOAD order: " + parameters);
        }
        return order;
    }

    public static UnloadOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = UnloadOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.UNLOAD, builder);
        return builder.build();
    }
}