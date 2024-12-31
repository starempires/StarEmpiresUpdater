package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.Ship;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifyShipsPhaseUpdater extends TransponderChangesPhaseUpdater {

    // identify all to empire1 [empire2 ...]
    // identify ship1 [ship2 ...] to empire1 [empire2 ...]
    // identify @ship-class1 [@ship-class2 ...] to empire1 [empire2 ...]
    private static final String SHIPS_GROUP = "ships";
    private static final String EMPIRES_GROUP = "empires";
    private static final String IDENTIFY_REGEX = "^identify\\s(?<" + SHIPS_GROUP + ">[@\\w]+(?:\\s+[@\\w]+)*)\\s+to\\s+(?<" + EMPIRES_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)\\s*$";
    private static final Pattern IDENTIFY_PATTERN = Pattern.compile(IDENTIFY_REGEX, Pattern.CASE_INSENSITIVE);

    public IdentifyShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.IDENTIFY_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.IDENTIFY);
        orders.forEach(order -> {
            final Matcher matcher = IDENTIFY_PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final Empire empire = order.getEmpire();
                final List<String> shipHandles = Arrays.asList(matcher.group(SHIPS_GROUP).split(" "));
                final List<String> empireNames = Arrays.asList(matcher.group(EMPIRES_GROUP).split(" "));
                final List<Empire> transponderEmpires = getTransponderEmpires(order, empireNames);
                final List<Ship> ships = getTransponderShips(order, shipHandles);
                transponderEmpires.forEach(transponderEmpire -> {
                    ships.forEach(ship -> ship.addTransponder(transponderEmpire));
                    addNewsResult(order, "You have identified " + plural(ships.size(), "ship") + " from empire " + transponderEmpire);
                });
            };
        });
    }
}