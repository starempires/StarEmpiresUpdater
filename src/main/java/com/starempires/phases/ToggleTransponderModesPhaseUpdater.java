package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.Ship;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToggleTransponderModesPhaseUpdater extends PhaseUpdater {

    private static final String ALL_SHIPS_TOKEN = "all";

    // toggle {public|private} {all| ship1 [ship2 ... ] }
    private static final String MODE_GROUP = "mode";
    private static final String SHIP_GROUP = "ship";
    private static final String TOGGLE_TRANSPONDER_REGEX = "^toggle\\s+(?<" + MODE_GROUP + ">public|private)\\s+(?<" + SHIP_GROUP + ">\\w+)$";
    private static final Pattern TOGGLE_PATTERN = Pattern.compile(TOGGLE_TRANSPONDER_REGEX, Pattern.CASE_INSENSITIVE);

    public ToggleTransponderModesPhaseUpdater(final TurnData turnData) {
        super(Phase.TOGGLE_TRANSPONDER_MODES, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TOGGLE);
        orders.forEach(order -> {
            final Matcher matcher = TOGGLE_PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final String mode = matcher.group(MODE_GROUP);
                final List<String> shipHandles = Arrays.asList(matcher.group(SHIP_GROUP).split(" "));
                final Empire empire = order.getEmpire();
                final List<Ship> ships = Lists.newArrayList();
                if (shipHandles.contains(ALL_SHIPS_TOKEN)) {
                    ships.addAll(empire.getShips().stream().filter(Ship::isAlive).toList());
                } else {
                    for (final String shipHandle : shipHandles) {
                        final Ship ship = empire.getShip(shipHandle);
                        if (ship == null) {
                            addNewsResult(order, "You do not own ship " + shipHandle);
                        } else {
                            ships.add(ship);
                        }
                    }
                }
                final boolean isPublic = mode.equalsIgnoreCase(Constants.TOKEN_PUBLIC);
                ships.forEach(ship -> ship.toggleTransponder(isPublic));
                addNewsResult(order,
                        mode.toLowerCase() + " transponder mode on " + plural(ships.size(), "ship"));
            }
        });
    }
}