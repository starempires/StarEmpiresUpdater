package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
@NoArgsConstructor
public class BuildOrder extends WorldBasedOrder {

    //order: BUILD world-name {number|max} ship-class-name [name* | name1 name2 …]

    final static private String REGEX = ID_CAPTURE_REGEX + SPACE_REGEX + AMOUNT_CAPTURE_REGEX + SPACE_REGEX +
            SHIP_CLASS_CAPTURE_REGEX + SPACE_REGEX + SHIP_NAMES_CAPTURE_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude
    private String shipClassName;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean buildMax;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int count;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String basename;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> names;

    public static BuildOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters(parameters)
                .names(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String worldName = matcher.group(ID_GROUP);
            final String numberText = matcher.group(AMOUNT_GROUP);
            final String designName = matcher.group(SHIP_CLASS_GROUP);
            final String nameText = matcher.group(SHIP_NAMES_GROUP);

            boolean buildMax = false;
            int count = 0;
            if (numberText.equalsIgnoreCase(MAX_TOKEN)) {
                buildMax = true;
            }
            else {
                count = Integer.parseInt(numberText);
                if (count < 1) {
                    order.addError("Invalid build count: " + count);
                    return order;
                }
            }

            final World world = turnData.getWorld(worldName);
            if (!empire.isKnownWorld(world)) {
                order.addError("Unknown world: " + worldName);
                return order;
            }
            if (!world.isOwnedBy(empire)) {
                order.addWarning(world, "You do not currently own world " + world);
            }
            if (world.isInterdicted()) {
                order.addWarning(world, "Currently interdicted");
            }
            String basename = null;
            final List<String> names = Lists.newArrayList();
            final ShipClass shipClass = turnData.getShipClass(designName);
            if (!empire.isKnownShipClass(shipClass)) {
                order.addResult("Warning: Unknown ship class " + designName);
            }
            else {
                if (!shipClass.isBuildable()) {
                    order.addError("Cannot build additional %s class ships".formatted(shipClass));
                    return order;
                }
                final int cost = shipClass.getCost();
                int stockpile = world.getStockpile();
                if (buildMax) {
                    count = (int) (stockpile / cost);
                    if (count < 1) {
                        order.addError(world, "Insufficient stockpile (%d RU) to build any ships of class %s (cost %d RU)".formatted(stockpile, shipClass, cost));
                        return order;
                    }
                } else {
                    if (cost * count > stockpile) {
                        order.addError(world, "Insufficient stockpile (%d RU) to build %d ships of class %s (cost %d RU)".formatted(world.getStockpile(), count, shipClass, cost));
                        return order;
                    }
                }

                int nextBasenameNumber = 0;
                if (nameText != null) {
                    if (nameText.endsWith("*")) {
                        basename = nameText.substring(0, nameText.length() - 1);
                        nextBasenameNumber = empire.getLargestBasenameNumber(basename);
                    }
                    else {
                        names.addAll(List.of(nameText.split(SPACE_REGEX)));
                    }
                }
                String nextName;
                for (int i = 0; i < count; ++i) {
                    if (basename != null) {
                        nextName = basename + (nextBasenameNumber + i + 1);
                    }
                    else if (i < names.size()) {
                        nextName = names.get(i);
                    }
                    else {
                        nextName = null;
                    }
                    world.adjustStockpile(-cost);
                    empire.buildShip(shipClass, world, nextName, turnData.getTurnNumber());
                    order.addResult("Pending build for %s ship %s (cost %d RU, %d RU remaining)".formatted(shipClass, nextName, cost, world.getStockpile()));
                }
            }
            order.world = world;
            order.shipClassName = designName;
            order.buildMax = buildMax;
            order.count = count;
            order.basename = basename;
            order.names = names;
            order.setReady(true);
        }
        else {
            order.addError("Invalid BUILD order: " + parameters);
        }
        return order;
    }

    public static BuildOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = BuildOrder.builder();
        WorldBasedOrder.parseReady(node, turnData, OrderType.BUILD, builder);
        return builder
                .shipClassName(getString(node, "shipClassName"))
                .buildMax(getBoolean(node, "buildMax"))
                .count(getInt(node, "count"))
                .basename(getString(node, "basename"))
                .names(getStringList(node, "names"))
                .build();
    }
}