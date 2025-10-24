package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@SuperBuilder
@Getter
public class AddShipOrder extends ShipBasedOrder {

    // ADDSHIP coordinate owner number design name*
    final static private String NUMBER_GROUP = "number";
    final static private String NUMBER_CAPTURE_REGEX = "(?<" + NUMBER_GROUP + ">" + INT_REGEX + ")";

    final static private String REGEX = COORDINATE_CAPTURE_REGEX + SPACE_REGEX + OWNER_CAPTURE_REGEX + SPACE_REGEX + NUMBER_CAPTURE_REGEX + SPACE_REGEX + SHIP_CLASS_CAPTURE_REGEX + SPACE_REGEX + NAMES_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    public static AddShipOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final AddShipOrder order = AddShipOrder.builder()
                .empire(empire)
                .orderType(OrderType.ADDSHIP)
                .parameters(parameters)
                .gmOnly(OrderType.ADDSHIP.isGmOnly())
                .ships(Lists.newArrayList())
                .build();
        if (!empire.isGM()) {
            order.addError("Command available only to GM");
            return order;
        }
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String coordText = matcher.group(COORDINATE_GROUP);
            final String nameText = matcher.group(NAMES_GROUP);
            final int number = Integer.parseInt(matcher.group(NUMBER_GROUP));
            final String ownerName = matcher.group(OWNER_GROUP);
            final String shipClassName = matcher.group(SHIP_CLASS_GROUP);

            Coordinate coordinate = Coordinate.parse(coordText);
            final Empire owner = turnData.getEmpire(ownerName);
            if (owner == null) {
                order.addError("Unknown owner: " + ownerName);
                return order;
            }

            final ShipClass shipClass = turnData.getShipClass(shipClassName);
            if (shipClass == null) {
                order.addError("Unknown ship class: " + shipClassName);
                return order;
            }

            List<String> names = Lists.newArrayList();
            if (nameText.endsWith("*")) {
                String basename = nameText.substring(0, nameText.length() - 1);
                int nextBasenameNumber = empire.getLargestBasenameNumber(basename);
                IntStream.range(0, number).forEach(i -> names.add(basename + (nextBasenameNumber + i + 1)));
            }
            else {
               names.addAll(List.of(nameText.split(SPACE_REGEX)));
               if (names.size() != number) {
                   order.addError("Number of names does not match number of ships");
                   return order;
               }
            }

            for (int i = 0; i < number; i++) {
                final Ship ship = Ship.builder()
                            .coordinate(coordinate)
                            .dpRemaining(shipClass.getDp())
                            .name(names.get(i))
                            .owner(owner)
                            .serialNumber(empire.getNewSerialNumber())
                            .shipClass(shipClass)
                            .turnBuilt(turnData.getTurnNumber())
                            .build();
                 order.ships.add(ship);
            }
            order.setReady(true);
        } else {
            order.addError("Invalid ADDSHIP order: " + parameters);
        }

        return order;
    }

    public static AddShipOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddShipOrder.builder();
        Order.parseReady(node, turnData, OrderType.ADDWORLD, builder);
        final Empire owner = getTurnDataItemFromJsonNode(node.get("owner"), turnData::getEmpire);
        return builder
                .ships(getTurnDataListFromJsonNode(node.get("ships"), owner::getShip))
                .gmOnly(true)
                .build();
    }
}