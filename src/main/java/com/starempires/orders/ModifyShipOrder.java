package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.Ship;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class ModifyShipOrder extends Order {
    // order: MODIFYSHIP owner ship dp
    final static private String REGEX = OWNER_CAPTURE_REGEX + SPACE_REGEX + SHIP_CAPTURE_REGEX + SPACE_REGEX + DP_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Empire owner;
    @JsonInclude
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Ship ship;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private int dp;

    public static ModifyShipOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final ModifyShipOrder order = ModifyShipOrder.builder()
                .empire(empire)
                .orderType(OrderType.MODIFYSHIP)
                .parameters(parameters)
                .gmOnly(OrderType.MODIFYSHIP.isGmOnly())
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
            final String shipName = matcher.group(SHIP_GROUP);
            final Ship ship = owner.getShip(shipName);
            if (ship == null) {
                order.addError("Unknown ship: " + shipName);
                return order;
            }
            order.owner = owner;
            order.ship = ship;
            order.dp = Integer.parseInt(matcher.group(DP_GROUP));
            order.setReady(true);
        } else {
            order.addError("Invalid MODIFYSHIP order: " + parameters);
        }

        return order;
    }

    public static ModifyShipOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = ModifyShipOrder.builder();
        Order.parseReady(node, turnData, OrderType.MODIFYSHIP, builder);
        final Empire owner = getTurnDataItemFromJsonNode(node.get("owner"), turnData::getEmpire);
        return builder
                .owner(owner)
                .ship(getTurnDataItemFromJsonNode(node.get("ship"), owner::getShip))
                .dp(getInt(node, "dp"))
                .build();
    }
}