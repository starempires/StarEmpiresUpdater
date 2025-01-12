package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
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
public class LoadOrder extends ShipBasedOrder {

    private static final String CARGO_GROUP = "cargo";
    private static final String CARRIER_GROUP = "carrier";
    private static final String REGEX = "(?<" + CARGO_GROUP + ">\\w+(?:\\s+\\w+)*)\\s+onto\\s+(?<" + CARRIER_GROUP + ">(\\w+))\\s*$";
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private final Ship carrier;

    public static LoadOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final LoadOrder order = LoadOrder.builder()
                .empire(empire)
                .orderType(OrderType.LOAD)
                .parameters(parameters)
                .ships(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String carrierName = matcher.group(CARRIER_GROUP);
            final Ship carrier = empire.getShip(carrierName);
            if (carrier == null) {
                order.addError("You do not own carrier " + carrierName);
                order.setReady(false);
            } else {
                final List<Ship> cargo = getShipsFromNames(empire, matcher.group(CARGO_GROUP), order);
                for (final Ship ship : cargo) {
                    if (ship.isLoaded()) {
                        order.addError(ship, "Already loaded onto carrier %s".formatted(carrier));
                    } else if (!ship.isSameSector(carrier)) {
                        order.addError(ship, "Not in same sector as carrier %s".formatted(carrier));
                    } else if (!carrier.canLoadCargo(ship)) {
                        order.addError(ship, "Carrier %s has insufficient free racks (%d) to load ship (tonnage %d)".formatted(carrier, carrier.getEmptyRacks(), ship.getTonnage()));
                    } else {
                        order.ships.add(ship);
                        ship.loadOntoCarrier(carrier);
                        order.addOKResult(ship);
                    }
                }
                order.setReady(!order.ships.isEmpty());
            }
        } else {
            order.addError("Invalid load order: " + parameters);
            order.setReady(false);
        }
        return order;
    }

    public static LoadOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = LoadOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.LOAD, builder);
        final String name = getString(node, "carrier");
        return builder
                .carrier(name == null ? null : turnData.getShip(name))
                .build();
    }
}