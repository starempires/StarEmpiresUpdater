package com.starempires.orders;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class RepairOrder extends ShipBasedOrder {
    // REPAIR ship {dp|ALL} world1 [world2…]

    private static final String SHIP_GROUP = "ship";
    private static final String DP_GROUP = "dp";
    private static final String WORLDS_GROUP = "worlds";
    private static final String SHIP_REGEX = "(?<" + SHIP_GROUP + ">\\w+)";
    private static final String DP_REGEX = "(?<" + DP_GROUP + ">(ALL|\\d+))";
    private static final String WORLDS_REGEX = "(?<" + WORLDS_GROUP + ">(\\w+)(?:\\w+\\w+)*)";
    private static final String REGEX = SHIP_REGEX + "\\s+" + DP_REGEX + "\\s+" + WORLDS_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private Ship ship;
    private int dpToRepair;
    private List<World> worlds = Lists.newArrayList();

    public static RepairOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final RepairOrder order = RepairOrder.builder().orderType(OrderType.REPAIR).empire(empire).parameters(parameters).build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String shipName = matcher.group(SHIP_GROUP);
            final String dpText = matcher.group(DP_GROUP);
            final String worldNames = matcher.group(WORLDS_GROUP);
            final Ship ship = empire.getShip(shipName);
            if (ship == null) {
                order.addError("Unknown ship : " + shipName);
                order.setReady(false);
                return order;
            }
            if (!ship.isAlive()) {
                order.addError("Ship %s is destroyed".formatted(shipName));
                order.setReady(false);
                return order;
            }
            if (!ship.isRepairable()) {
                order.addWarning(ship, "No repairs currently needed");
            }
            if (ship.isLoaded() && ship.getCarrier().isOrbital()) {
                order.addWarning(ship, "Orbital carrier %s will fully repair all cargo for free".formatted(ship.getShipClass()));
            }
            order.ship = ship;
            final int dpPerRU = ship.isOrbital() ? Constants.DEFAULT_ORBITAL_REPAIR_DP_PER_RU : Constants.DEFAULT_REPAIR_DP_PER_RU;

            int dpToRepair;
            if (StringUtils.equalsIgnoreCase(dpText, "ALL")) {
                dpToRepair = ship.getMaxRepairAmount();
            } else {
                dpToRepair = Integer.parseInt(dpText);
                if (dpToRepair < 1) {
                    order.addError("Invalid repair amount: " + dpToRepair);
                    order.setReady(false);
                } else if (dpToRepair > ship.getMaxRepairAmount()) {
                    order.addWarning(ship, "Only %d DP currently needed".formatted(ship.getMaxRepairAmount()));
                    dpToRepair = ship.getMaxRepairAmount();
                }
            }
            order.dpToRepair = dpToRepair;

            for (String worldName : worldNames.split(" ")) {
                final World world = turnData.getWorld(worldName);
                if (world == null || !empire.isKnownWorld(world)) {
                    order.addError("Unknown world: " + worldName);
                } else if (!world.isOwnedBy(empire)) {
                    order.addWarning(world, "You don't own world " + world);
                } else if (world.isInterdicted()) {
                    order.addWarning(world, "Currently interdicted; no repairs possible");
                } else if (world.isBlockaded() && !ship.isSameSector(world)) {
                    order.addWarning(world, "Currently blockaded; no offworld repairs possible.");
                } else if (world.getStockpile() < 1) {
                    order.addError("No RUs stockpiled at world " + worldName);
                } else {
                    final int dpToPay = Math.min(dpToRepair / dpPerRU, world.getStockpile());
                    if (dpToPay > 0) {
                        world.adjustStockpile(-dpToPay);
                        dpToRepair -= dpToPay;
                        order.addResult("World %s repairing %d DP on ship %s (%d fee, %d RU remaining)".formatted(world, dpToPay * dpPerRU, ship, dpToPay, world.getStockpile()));
                        order.worlds.add(world);
                    } else {
                        order.addWarning(world, "No further repairs needed");
                        order.worlds.add(world);
                    }
                }
            }

            if (order.worlds.isEmpty()) {
                order.addError("No valid repair worlds found");
                order.setReady(false);
            }
        } else {
            order.addError("Invalid repair order: " + parameters);
            order.setReady(false);
        }
        return order;
    }
}