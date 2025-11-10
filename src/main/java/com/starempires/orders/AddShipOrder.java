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
import com.starempires.objects.ShipClass;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuperBuilder
@Getter
public class AddShipOrder extends Order {

    // order: ADDSHIP coordinate owner number design name*

    final static private String REGEX = COORDINATE_CAPTURE_REGEX + SPACE_REGEX + OWNER_CAPTURE_REGEX + SPACE_REGEX +
            COUNT_CAPTURE_REGEX + SPACE_REGEX + SHIP_CLASS_CAPTURE_REGEX + SPACE_REGEX + SHIP_NAMES_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude
    private Coordinate coordinate;
    @JsonInclude
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private ShipClass shipClass;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int count;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String basename;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> names;
    @JsonInclude
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Empire owner;

    public static AddShipOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final AddShipOrder order = AddShipOrder.builder()
                .empire(empire)
                .orderType(OrderType.ADDSHIP)
                .parameters(parameters)
                .gmOnly(OrderType.ADDSHIP.isGmOnly())
                .names(Lists.newArrayList())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String coordText = matcher.group(COORDINATE_GROUP);
            final String nameText = matcher.group(SHIP_NAMES_GROUP);
            final int count = Integer.parseInt(matcher.group(COUNT_GROUP));
            final String ownerName = matcher.group(OWNER_GROUP);
            final String shipClassName = matcher.group(SHIP_CLASS_GROUP);

            order.coordinate = Coordinate.parse(coordText);
            order.count = count;
            final Empire owner = turnData.getEmpire(ownerName);
            if (owner == null) {
                order.addError("Unknown owner: " + ownerName);
                return order;
            }
            order.owner = owner;

            final ShipClass shipClass = turnData.getShipClass(shipClassName);
            if (shipClass == null) {
                order.addError("Unknown ship class: " + shipClassName);
                return order;
            }
            order.shipClass = shipClass;

            if (nameText.endsWith("*")) {
                order.basename = nameText.substring(0, nameText.length() - 1);
            }
            else {
               List<String> names = List.of(nameText.split(SPACE_REGEX));
               if (names.size() != count) {
                   order.addError("Number of names %d does not match count %d".formatted(names.size(), count));
                   return order;
               }
               order.names.addAll(names);
            }
            order.setReady(true);
        } else {
            order.addError("Invalid ADDSHIP order: " + parameters);
        }

        return order;
    }

    public static AddShipOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddShipOrder.builder();
        Order.parseReady(node, turnData, OrderType.ADDSHIP, builder);
        final Empire owner = getTurnDataItemFromJsonNode(node.get("owner"), turnData::getEmpire);
        return builder
                .coordinate(getCoordinateFromJsonNode(node.get("coordinate")))
                .shipClass(getTurnDataItemFromJsonNode(node.get("shipClass"), turnData::getShipClass))
                .count(getInt(node, "count"))
                .basename(getString(node, "basename"))
                .names(getStringList(node, "names"))
                .owner(getTurnDataItemFromJsonNode(node.get("owner"), turnData::getEmpire))
                .gmOnly(true)
                .build();
    }
}