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
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class RemoveShipOrder extends Order {
    // order: REMOVESHIP owner ship-name

    final static private String REGEX = OWNER_CAPTURE_REGEX + SPACE_REGEX + OBJECT_LIST_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Empire owner;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private List<Ship> ships;

    public static RemoveShipOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final RemoveShipOrder order = RemoveShipOrder.builder()
                .empire(empire)
                .orderType(OrderType.REMOVESHIP)
                .parameters(parameters)
                .gmOnly(OrderType.REMOVESHIP.isGmOnly())
                .ships(Lists.newArrayList())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String ownerName = matcher.group(OWNER_GROUP);

            final Empire owner = turnData.getEmpire(ownerName);
            if (owner == null) {
                order.addError("Unknown owner: " + ownerName);
                return order;
            }
            order.owner = owner;

            final String[] shipNames = matcher.group(OBJECT_LIST_GROUP).split(SPACE_REGEX);
            for (String shipName : shipNames) {
                final Ship ship = owner.getShip(shipName);
                if (ship == null) {
                    order.addError("Unknown ship: " + shipName);
                    return order;
                }
                order.ships.add(ship);
            }
            if (order.ships.isEmpty()) {
                order.addError("No valid ships specified");
                return order;
            }
            order.setReady(true);
        } else {
            order.addError("Invalid REMOVESHIP order: " + parameters);
        }

        return order;
    }

    public static RemoveShipOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = RemoveShipOrder.builder();
        Order.parseReady(node, turnData, OrderType.REMOVESHIP, builder);
        final Empire owner = getTurnDataItemFromJsonNode(node.get("owner"), turnData::getEmpire);
        return builder
                .owner(owner)
                .ships(getTurnDataListFromJsonNode(node.get("ships"), owner::getShip))
                .build();
    }
}