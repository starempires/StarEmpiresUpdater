package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TraversePortalsPhaseUpdater extends PhaseUpdater {

    private static final String SHIPS_GROUP = "ships";
    private static final String ENTRANCE_GROUP = "entrance";
    private static final String EXIT_GROUP = "exit";
    private static final String TRAVERSE_REGEX = "^TRAVERSE\\s+(?<" + SHIPS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)\\s+THROUGH\\s+(?<" + ENTRANCE_GROUP + ">[\\w]+)(?:\\s+(?<" + EXIT_GROUP + ">[\\w-]+))?$";
    private static final Pattern TRAVERSE_PATTERN = Pattern.compile(TRAVERSE_REGEX, Pattern.CASE_INSENSITIVE);

    public TraversePortalsPhaseUpdater(final TurnData turnData) {
        super(Phase.TRAVERSE_PORTALS, turnData);
    }

    private void traversePortal(final Order order, final Portal entrance, final Portal exit, final List<Ship> ships) {
        final Empire empire = order.getEmpire();
        final Collection<Empire> entranceEmpires = turnData.getEmpiresPresent(entrance);
        entranceEmpires.remove(empire);

        final Collection<Empire> exitEmpires = turnData.getEmpiresPresent(exit);
        exitEmpires.remove(empire);

        ships.forEach(ship -> {
            empire.traverseShip(ship, exit.getCoordinate());
            addNewsResult(order, empire,
                    "Ship " + ship + " traversed wormnet from " + entrance + " to " + exit);
            addNews(entranceEmpires, "Ship " + ship + " entered portal " + entrance);
            addNews(exitEmpires, "Ship " + ship + " exited portal " + exit);
        });

        empire.addPortalTraversed(entrance);
        empire.addPortalTraversed(exit);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.TRAVERSE);
        orders.forEach(order -> {
            final Matcher traverseMatcher = TRAVERSE_PATTERN.matcher(order.getParametersAsString());
            if (traverseMatcher.matches()) {
                final Empire empire = order.getEmpire();
                final List<String> shipNames = Arrays.asList(traverseMatcher.group(SHIPS_GROUP).split(" "));
                final List<Ship> traversers = Lists.newArrayList();
                for (final String shipName: shipNames) {
                    final Ship ship = empire.getShip(shipName);
                    if (ship == null) {
                        addNewsResult(order, "Omitting unknown ship " + shipName);
                        shipNames.remove(shipName);
                    }
                    else if (!ship.isAlive()) {
                        addNewsResult(order, "Omitting destroyed ship " + ship);
                        shipNames.remove(shipName);
                    }
                    else if (ship.isLoaded()) {
                        order.addResult("Omitting loaded ship %s".formatted(ship));
                    }
                    else if (ship.getGunsActuallyFired() > 0) {
                        order.addResult("Omitting attacking ship %s".formatted(ship));
                    }

                    traversers.add(ship);
                }
                if (traversers.isEmpty()) {
                    addNewsResult(order, "No valid movers found");
                    return;
                }

                final String entranceName = traverseMatcher.group(ENTRANCE_GROUP);
                final String exitName = traverseMatcher.group(EXIT_GROUP);

                final Portal entrance = turnData.getPortal(entranceName);
                if (entrance == null) {
                    addNewsResult(order, "No entrance portal " + entranceName + " found");
                    return;
                }
                if (entrance.isCollapsed()) {
                    addNewsResult(order, "Entrance portal " + entranceName + " is collapsed; no entry possible");
                    return;
                }

                Portal exit = null;
                if (exitName != null ) {
                    exit = turnData.getPortal(exitName);
                    if (exit == null) {
                        addNewsResult(order, "No exit portal " + exitName + " found");
                        return;
                    }
                    if (exit.isCollapsed()) {
                        addNewsResult(order, "Exit portal " + exit + " is collapsed; no wormnet traversal possible");
                        return;
                    }
                    if (!entrance.isConnectedTo(exit)) {
                        addNewsResult(order, empire, "Disjoint wormnet portals " + entrance + " and " + exit);
                    }
                }
                else {
                    exit = entrance.selectRandomConnection();
                }
                traversePortal(order, entrance, exit, traversers);
            }
        });
    }
}