package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.Ship;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConcealShipsPhaseUpdater extends TransponderChangesPhaseUpdater {

    // conceal all from empire1 [empire2 ...]
    // conceal ship1 [ship2 ...] from empire1 [empire2 ...]
    // conceal @ship-class1 [@ship-class2 ...] from empire1 [empire2 ...]
    private static final String SHIPS_GROUP = "ships";
    private static final String EMPIRES_GROUP = "empires";
    private static final String CONCEAL_REGEX = "^conceal\\s(?<" + SHIPS_GROUP + ">[@\\w]+(?:\\s+[@\\w]+)*)\\s+from\\s+(?<" + EMPIRES_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)\\s*$";
    private static final Pattern CONCEAL_PATTERN = Pattern.compile(CONCEAL_REGEX, Pattern.CASE_INSENSITIVE);

    public ConcealShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.CONCEAL_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.CONCEAL);
        orders.forEach(order -> {
            final Matcher matcher = CONCEAL_PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final Empire empire = order.getEmpire();
                final List<String> shipHandles = Arrays.asList(matcher.group(SHIPS_GROUP).split(" "));
                final List<String> empireNames = Arrays.asList(matcher.group(EMPIRES_GROUP).split(" "));
                final List<Empire> transponderEmpires = getTransponderEmpires(order, empireNames);
                final List<Ship> ships = getTransponderShips(order, shipHandles);
                transponderEmpires.forEach(transponderEmpire -> {
                    ships.forEach(ship -> ship.removeTransponder(transponderEmpire));
                    addNewsResult(order, "You have concealed " + plural(ships.size(), "ship") + " from empire " + transponderEmpire);
                });
            };
        });
    }
}