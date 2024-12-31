package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class BuildOrder extends WorldBasedOrder {

    //BUILD world {number|REMAINING} design [name* | name1 name2 …]
    final static private String WORLD_GROUP = "world";
    final static private String NUMBER_GROUP = "number";
    final static private String DESIGN_GROUP = "design";
    final static private String NAMES_GROUP = "names";
    final static private String NUMBER_REGEX = "(?<" + NUMBER_GROUP + ">\\d+|max)";
    final static private String DESIGN_REGEX = "(?<" + DESIGN_GROUP + ">\\w+)";
    final static private String NAMES_REGEX = "(?<" + NAMES_GROUP + ">(?:\\w+\\s+(?:\\w+)*)|\\w+\\*)?";
    final static private String REGEX = WORLD_REGEX + "\\s+" + NUMBER_REGEX + "\\s+" + DESIGN_REGEX + "\\s+" + NAMES_REGEX;
    final static private Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private ShipClass shipClass;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean buildMax;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int count;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String basename;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<String> names = Lists.newArrayList();

    public static BuildOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final BuildOrder order = BuildOrder.builder()
                .empire(empire)
                .orderType(OrderType.BUILD)
                .parameters(parameters)
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String worldName = matcher.group(WORLD_GROUP);
            final String numberText = matcher.group(NUMBER_GROUP);
            final String designName = matcher.group(DESIGN_GROUP);
            final String nameText = matcher.group(NAMES_GROUP);

            boolean buildMax = false;
            int count = 0;
            if (numberText.equalsIgnoreCase("max")) {
                buildMax = true;
            }
            else {
                count = Integer.parseInt(numberText);
                if (count < 1) {
                    order.addError("Invalid build count: " + count);
                    order.setReady(false);
                    return order;
                }
            }

            final World world = turnData.getWorld(worldName);
            if (world == null || !empire.isKnownWorld(world)) {
                order.addError("Unknown world: " + worldName);
                order.setReady(false);
                return order;
            }
            if (world.isInterdicted()) {
                order.addWarning(world, "Currently interdicted");
            }
            String basename = null;
            final List<String> names = Lists.newArrayList();
            final ShipClass shipClass = turnData.getShipClass(designName);
            if (shipClass == null || !empire.isKnownShipClass(shipClass)) {
                order.addWarning(shipClass, "Unknown ship class");
            }
            else {
                if (shipClass.isStarbase()) {
                    order.addError("Cannot build additional starbases");
                    order.setReady(false);
                    return order;
                }
                final int cost = shipClass.getCost();
                int stockpile = world.getStockpile();
                if (buildMax) {
                    count = (int) (stockpile / cost);
                    if (count < 1) {
                        order.addError(world, "Insufficient stockpile (%d) to build any ships of class %s".formatted(stockpile, shipClass));
                        order.setReady(false);
                        return order;
                    }
                } else {
                    if (cost * count > stockpile) {
                        order.addError(world, "Insufficient stockpile (%d) to build %d ships of class %s".formatted(world.getStockpile(), count, shipClass));
                        order.setReady(false);
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
                        names.addAll(List.of(nameText.split("\\s+")));
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
                    order.addResult("Pending build for %s ship %s (%d RU remaining)".formatted(shipClass, nextName, world.getStockpile()));
                }
            }
            order.world = world;
            order.shipClass = shipClass;
            order.buildMax = buildMax;
            order.count = count;
            order.basename = basename;
            order.names.addAll(names);
        }
        else {
            order.addError("Invalid build order: " + parameters);
            order.setReady(false);
        }
        return order;
    }
}