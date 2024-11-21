package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.UnloadOrder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * parameters:
 * UNLOAD ship1 ship2 ...
 */
public class UnloadShipPhaseUpdater extends PhaseUpdater {

    public UnloadShipPhaseUpdater(final TurnData turnData) {
        super(Phase.UNLOAD_SHIPS, turnData);
    }

    private void createUnloadOrders(final Empire empire, final List<String> shipHandles) {
        final List<Ship> ships = empire.getShips(shipHandles);
        ships.forEach(ship -> {
            if (ship.isLoaded()) {
                final String parameters = empire.getName() + ":" + ship.getHandle();
                final Order order = UnloadOrder.builder()
                        .empire(empire)
                        .orderType(OrderType.UNLOAD)
                        .parameters(parameters)
                        .synthetic(true)
                        .build();
                turnData.addOrder(order);
            }
        });
    }

    private void createDeployAutoUnloadOrders() {
        List<Order> orders = turnData.getOrders(OrderType.DEPLOY);
        orders.forEach(order -> {
            final List<String> deviceHandles = Arrays.asList(order.getParameters().split(" "));
            createUnloadOrders(order.getEmpire(), deviceHandles);
        });
    }

    @Override
    public void update() {
        createDeployAutoUnloadOrders();
        final List<Order> orders = turnData.getOrders(OrderType.UNLOAD);
        orders.forEach(o -> {
            final UnloadOrder order = (UnloadOrder) o;
            for (Ship ship : order.getShips()) {
                final Ship carrier = ship.getCarrier();
                if (carrier == null) {
                    addNewsResult(order, "Ship " + ship + " is not loaded");
                } else {
                    carrier.unloadCargo(ship);
                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                    addNewsResult(order, newsEmpires, "Ship " + ship + " unloaded from carrier " + carrier);
                }
            }
        });
    }
}