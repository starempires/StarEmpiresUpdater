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

    // order: LOAD ship1 [ship2 ...] ONTO carrier

    private static final String REGEX = OBJECT_LIST_CAPTURE_REGEX + SPACE_REGEX + ONTO_TOKEN + SPACE_REGEX + SHIP_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Ship carrier;

    public static LoadOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final LoadOrder order = LoadOrder.builder()
                .empire(empire)
                .orderType(OrderType.LOAD)
                .parameters(parameters)
                .ships(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String carrierName = matcher.group(SHIP_GROUP);
            final Ship carrier = empire.getShip(carrierName);
            if (carrier == null) {
                order.addError("You do not own carrier " + carrierName);
            } else {
                final List<Ship> cargo = getShipsFromNames(empire, matcher.group(OBJECT_LIST_GROUP), order);
                for (final Ship ship : cargo) {
                    if (ship.equals(carrier)) {
                        order.addError(ship, "Cannot load ship onto itself");
                    } else if (ship.isLoaded()) {
                        order.addError(ship, "Already loaded onto carrier %s".formatted(carrier));
                    } else if (!ship.isSameSector(carrier)) {
                        order.addError(ship, "Not in same sector as carrier %s".formatted(carrier));
                    } else if (!carrier.canLoadCargo(ship)) {
                        order.addError(ship, "Carrier %s has insufficient free racks (%d) to load ship (tonnage %d)".formatted(carrier, carrier.getEmptyRacks(), ship.getTonnage()));
                    } else {
                        order.ships.add(ship);
                        turnData.load(ship, carrier);
                        order.addResult("%s: OK (%d empty racks remaining)".formatted(ship, carrier.getEmptyRacks()));
                        order.carrier = carrier;
                    }
                }
                order.setReady(!order.ships.isEmpty());
            }
        } else {
            order.addError("Invalid LOAD order: " + parameters);
        }
        return order;
    }

    public static LoadOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = LoadOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.LOAD, builder);
        final String carrierName = getString(node, "carrier");
        final String empireName = getString(node, "empire");
        final Empire empire = turnData.getEmpire(empireName);
        return builder
                .carrier(carrierName == null ? null : empire.getShip(carrierName))
                .build();
    }
}