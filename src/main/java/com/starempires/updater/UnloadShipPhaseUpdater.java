package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.DeployOrder;
import com.starempires.orders.FireOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.UnloadOrder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UnloadShipPhaseUpdater extends PhaseUpdater {

    public UnloadShipPhaseUpdater(final TurnData turnData) {
        super(Phase.UNLOAD_SHIPS, turnData);
    }

    private void createUnloadOrders(final Empire empire, final List<Ship> ships) {
            final String parameters = ships.stream()
                .map(Ship::getName)
                .collect(Collectors.joining(" "));
            final Order order = UnloadOrder.builder()
                    .empire(empire)
                    .orderType(OrderType.UNLOAD)
                    .parameters(parameters)
                    .synthetic(true)
                    .ships(ships)
                    .build();
            turnData.addOrder(order);
    }

    private void createAutoUnloadOrders() {
        final List<Order> deployOrders = turnData.getOrders(OrderType.DEPLOY);
        deployOrders.forEach(o -> {
            final DeployOrder order = (DeployOrder) o;
            // gather loaded devices that have been ordered to deploy
            final List<Ship> ships = order.getShips().stream()
                    .filter(Ship::isLoaded).toList();
            createUnloadOrders(order.getEmpire(), ships);
        });
        final List<Order> fireOrders = turnData.getOrders(OrderType.FIRE);
        fireOrders.forEach(o -> {
            final FireOrder order = (FireOrder) o;
            // gather loaded missiles that have been ordered to fire
            final List<Ship> ships = order.getShips().stream()
                    .filter(Ship::isLoaded)
                    .filter(Ship::isMissile)
                    .toList();
            createUnloadOrders(order.getEmpire(), ships);
        });
    }

    @Override
    public void update() {
        createAutoUnloadOrders();
        final List<Order> orders = turnData.getOrders(OrderType.UNLOAD);
        orders.forEach(o -> {
            final UnloadOrder order = (UnloadOrder) o;
            addOrderText(order);
            for (Ship ship : order.getShips()) {
                final Ship carrier = ship.getCarrier();
                if (carrier == null) {
                    addNews(order, "Ship " + ship + " is not loaded");
                } else {
                    turnData.unload(ship);
                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                    addNews(newsEmpires, "Ship " + ship + " unloaded from carrier " + carrier);
                }
            }
        });
    }
}