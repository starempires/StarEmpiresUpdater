package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class TraverseOrder extends ShipBasedOrder {

    // TRAVERSE {ships|location|coord} portal {entry} [{exit}]
    final static private String ENTRY_GROUP = "entry";
    final static private String EXIT_GROUP = "exit";
    final static private String ENTRY_CAPTURE_REGEX = "(?<" + ENTRY_GROUP + ">" + ID_REGEX + ")";
    final static private String EXIT_CAPTURE_REGEX = "(?:" + SPACE_REGEX + "(?<" + EXIT_GROUP + ">" + ID_REGEX + "))?";
    final static private String REGEX = SHIP_GROUP_CAPTURE_REGEX + SPACE_REGEX + "portal" + SPACE_REGEX + ENTRY_CAPTURE_REGEX + EXIT_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Portal entry;
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Portal exit;

    public static TraverseOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final TraverseOrder order = TraverseOrder.builder()
                .empire(empire)
                .orderType(OrderType.TRAVERSE)
                .parameters(parameters)
                .ships(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final List<Ship> movers = getLocationShips(empire, matcher, order);
            final Coordinate shipCoordinate = movers.stream().findAny().map(Ship::getCoordinate).orElse(null);
            final boolean sameSector = movers.stream().allMatch(attacker -> attacker.getCoordinate() == shipCoordinate);
            if (!sameSector) {
                order.addError("Movers not all in same sector");
                order.ships.clear();
                order.setReady(false);
                return order;
            }

            final List<Ship> validMovers = Lists.newArrayList();
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
                    validMovers.add(ship);
                }
            }

            if (validMovers.isEmpty()) {
                order.addError("No valid movers");
                order.setReady(false);
                return order;
            }
            order.ships.addAll(validMovers);

            final String entryText = matcher.group(ENTRY_GROUP);
            final String exitText = matcher.group(EXIT_GROUP);
            final Portal entry = turnData.getPortal(entryText);
            if (!empire.isKnownPortal(entry)) {
                order.addError("Unknown entry portal: " + entryText);
                order.setReady(false);
                return order;
            }
            if (!entry.getCoordinate().equals(shipCoordinate)) {
                order.addError("Ships not in same sector as Entry portal " + entry);
                order.setReady(false);
                return order;
            }
            if (entry.isCollapsed()) {
                order.addWarning(entry, "Entrance portal " + entry + " currently collapsed");
            }
            order.entry = entry;

            if (exitText != null) {
                final Portal exit = turnData.getPortal(exitText);
                if (exit == null) {
                    order.addError("Unknown exit portal: " + exitText);
                    order.setReady(false);
                    return order;
                }
                if (empire.hasNavData(exit)) {
                    if (!entry.isConnectedTo(exit)) {
                        order.addError("Entry portal %s and exit portal %s are not connected".formatted(entry, exit));
                        return order;
                    }
                    order.exit = exit;
                }
                else {
                    order.addWarning(exit, "No nav data for exit portal");
                }
            }

            validMovers.forEach(order::addOKResult);
        }
        else {
            order.addError("Invalid TRAVERSE order: " + parameters);
            order.setReady(false);
        }
        return order;
    }

    public static TraverseOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = TraverseOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.TRAVERSE, builder);
        return builder
                .entry(getTurnDataItemFromJsonNode(node.get("entry"), turnData::getPortal))
                .exit(getTurnDataItemFromJsonNode(node.get("exit"), turnData::getPortal))
                .build();
    }
}