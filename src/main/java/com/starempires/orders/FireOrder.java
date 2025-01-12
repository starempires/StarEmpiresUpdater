package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.Ship;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class FireOrder extends ShipBasedOrder {

    final static private String TARGETS_GROUP = "targets";
    final static private String TARGET_ORDER_GROUP = "targetorder";
    final static private String ATTACKERS_GROUP = "attackers";

    // parameters are FIRE [asc|desc] (oblique,y) AT empire1 [empire2 ...]
    // parameters are FIRE [asc|desc] @location AT empire1 [empire2 ...]
    // parameters are FIRE [asc|desc] ship1 [ship2 ...] AT empire1 [empire2 ...]
    final static private String FIRE_REGEX = "(?:(?<" + TARGET_ORDER_GROUP + ">asc|desc)\\s+)?(?<" + ATTACKERS_GROUP + ">\\.+)\\s+at\\s+(?<" + TARGETS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)$";
    final static private Pattern FIRE_PATTERN = Pattern.compile(FIRE_REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final List<Empire> targets;
    private Coordinate coordinate;
    private boolean ascending;

    public static FireOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final FireOrder order = FireOrder.builder()
                .empire(empire)
                .orderType(OrderType.FIRE)
                .parameters(parameters)
                .targets(Lists.newArrayList())
                .build();
        final Matcher matcher = FIRE_PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String targetOrderText = matcher.group(TARGET_ORDER_GROUP);
            if (targetOrderText != null) {
                order.ascending = targetOrderText.equalsIgnoreCase("asc");
            }

            final String attackerText = matcher.group(ATTACKERS_GROUP);
            final List<Ship> attackers = getLocationShips(empire, attackerText, order);
            for (final Ship ship : attackers) {
                if (ship.getAvailableGuns() < 1) {
                    order.addError(ship, "Ship has no operational guns");
                } else if (ship.isMissile()) {
                    if (!ship.isLoaded() && !ship.wasJustUnloaded()) {
                        order.addError(ship, "Missile is not loaded");
                    } else {
                        if (ship.isLoaded()) {
                            order.addWarning(ship, "Missile will be unloaded");
                        }
                        order.ships.add(ship);
                        order.addOKResult(ship);
                    }
                } else if (ship.isLoaded()) {
                    order.addWarning(ship, "Loaded ships cannot fire");
                } else {
                    order.ships.add(ship);
                    order.addOKResult(ship);
                }
            }

            order.coordinate = order.ships.stream().findAny().map(Ship::getCoordinate).orElse(null);
            final boolean sameSector = order.ships.stream().allMatch(attacker -> attacker.getCoordinate() == order.coordinate);
            if (!sameSector) {
                order.addError("Attackers not all in same sector");
                order.ships.clear();
            }

            if (order.ships.isEmpty()) {
                order.addError("No valid attackers");
                order.setReady(false);
                return order;
            }

            final String[] targetsText = matcher.group(TARGETS_GROUP).split(" ");
            for (final String targetEmpire : targetsText) {
                final Empire target = turnData.getEmpire(targetEmpire);
                if (target == null) {
                    order.addError(targetEmpire, "Unknown target");
                }
                else {
                    if (target.getShips(order.coordinate).isEmpty()) {
                        order.addError("Empire %s has no ships attackers' sector %s".formatted(target, order.coordinate));
                    }
                    else {
                        order.targets.add(target);
                        order.addOKResult(target);
                    }
                }
            }

            if (order.targets.isEmpty()) {
                order.addError("No valid targets");
                order.setReady(false);
                return order;
            }
        } else {
            order.addError("Invalid fire order: " + parameters);
            order.setReady(false);
        }

        order.setReady(!order.ships.isEmpty());
        return order;
    }

    public static FireOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = FireOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.FIRE, builder);
        return builder
             .targets(getTurnDataListFromJsonNode(node, turnData::getEmpire))
             .coordinate(getCoordinateFromJsonNode(node.get("coordinate")))
             .ascending(getBoolean(node, "ascending"))
             .build();
    }
}