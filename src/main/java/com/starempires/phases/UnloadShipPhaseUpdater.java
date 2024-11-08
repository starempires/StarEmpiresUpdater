package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parameters:
 *  UNLOAD ship1 ship2 ...
 */
public class UnloadShipPhaseUpdater extends PhaseUpdater {

    final private String SHIPS_GROUP = "ships";
    final private String PARAMETERS_REGEX = "^unload\\s*(?<" + SHIPS_GROUP + ">[\\w]+(?:\\s+[\\w]+)*)$";
    final private Pattern PATTERN = Pattern.compile(PARAMETERS_REGEX, Pattern.CASE_INSENSITIVE);

    public UnloadShipPhaseUpdater(final TurnData turnData) {
        super(Phase.UNLOAD_SHIPS, turnData);
    }

    private void createUnloadOrders(final Empire empire, final List<String> shipHandles) {
        final List<Ship> ships = empire.getShips(shipHandles);
        ships.forEach(ship -> {
            if (ship.isLoaded()) {
                final String parameters = empire.getName() + ":" + ship.getHandle();
                final Order order = Order.builder()
                        .empire(empire)
                        .orderType(OrderType.UNLOAD)
                        .parameters(Lists.newArrayList(parameters))
                        .synthetic(true)
                        .build();
                turnData.addOrder(order);
            }
        });
    }

    private void createDeployAutoUnloadOrders() {
        List<Order> orders = turnData.getOrders(OrderType.DEPLOY);
        orders.forEach(order -> {
            final List<String> deviceHandles = order.getParameters();
            createUnloadOrders(order.getEmpire(), deviceHandles);
        });
    }

    @Override
    public void update() {
        createDeployAutoUnloadOrders();
        final List<Order> orders = turnData.getOrders(OrderType.UNLOAD);
        orders.forEach(order -> {
            final Matcher matcher = PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final List<String> shipNames = Lists.newArrayList(matcher.group(SHIPS_GROUP).split(" "));
                // parameters are a list of empire:ship handles to unload
                for (String shipName : shipNames) {
                    final Ship ship = turnData.getShip(shipName);
                    final Ship carrier = ship.getCarrier();
                    if (carrier == null) {
                        addNewsResult(order, "Ship " + ship + " is not loaded");
                    }
                    else {
                        carrier.unloadCargo(ship);
                        final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                        addNewsResult(order, newsEmpires, "Ship " + ship + " unloaded from carrier " + carrier);
                    }
                }
            }
        });
    }
}