package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.World;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildShipsPhaseUpdater extends PhaseUpdater {

    // build [count] ship-class world [name]
    final static private String COUNT_GROUP = "count";
    final static private String SHIPCLASS_GROUP = "shipclass";
    final static private String WORLD_GROUP = "world";
    final static private String NAME_GROUP = "name";
    final static private String BUILD_REGEX = "^build\\s+(?<" + COUNT_GROUP + ">\\d+)?\\s*(?<" + SHIPCLASS_GROUP + ">\\w+)\\s+(?<" + WORLD_GROUP + ">\\w+)(?:\\s+(?<" + NAME_GROUP + ">\\w+))?$";

    final static private Pattern BUILD_PATTERN = Pattern.compile(BUILD_REGEX, Pattern.CASE_INSENSITIVE);

    public BuildShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.BUILD_SHIPS, turnData);
    }

    private void buildShips(final Order order, final ShipClass shipClass, final World world, final String shipName, int count) {
        final Empire empire = order.getEmpire();
        final Collection<Ship> ships = empire.getShips();
        final long existingNameCount = ships.stream().filter(ship -> ship.getName().startsWith(shipName)).count();
        final int cost = shipClass.getCost();
        for (int i = 1; i <= count; ++i) {
            final int stockpile = world.getStockpile();
            if (cost <= stockpile) {
                int remaining = world.adjustStockpile(-cost);
                final Ship ship = empire.buildShip(shipClass, world, shipName, turnData.getTurnNumber());
                addNewsResult(order, "You built " + shipClass + " ship " + ship + " at world "
                        + world + " (cost " + cost + "; " + remaining + " RU remaining)");
            } else {
                addNewsResult(order, "Insufficient stockpile (" + stockpile + ") on world "
                        + world + " to build ship class " + shipClass + " (cost " + cost + ")");
                break;
            }
        }
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.BUILD);
        orders.forEach(order -> {
            final Matcher matcher = BUILD_PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final Empire empire = order.getEmpire();
                final String countText = matcher.group(COUNT_GROUP);
                final String shipClassName = matcher.group(SHIPCLASS_GROUP);
                final String worldName = matcher.group(WORLD_GROUP);
                final String shipName = ObjectUtils.firstNonNull(matcher.group(NAME_GROUP), shipClassName);
                final int count = Integer.parseInt(ObjectUtils.firstNonNull(countText, "1"));

                final ShipClass shipClass = turnData.getShipClass(shipClassName);
                if (shipClass == null || !empire.isKnownShipClass(shipClass)) {
                    addNewsResult(order, "You have no design information for ship class " + shipClassName);
                } else {
                    final World world = turnData.getWorld(worldName);
                    if (world == null) {
                        addNewsResult(order, "Unknown world " + worldName);
                    } else if (!world.isOwnedBy(empire)) {
                        addNewsResult(order, "You do not own world " + worldName);
                    } else if (world.isInterdicted()) {
                        addNewsResult(order, "World " + world + " is interdicted; no builds possible.");
                    } else {
                        buildShips(order, shipClass, world, shipName, count);
                    }
                }
            }
        });
    }
}