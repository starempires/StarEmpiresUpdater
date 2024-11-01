package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;

import java.util.List;

public class ConcealShipsPhaseUpdater extends TransponderChangesPhaseUpdater {

    public ConcealShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.CONCEAL_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.CONCEAL);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();

            final int index = order.indexOfIgnoreCase(Constants.TOKEN_FROM);
            final List<String> shipHandles = order.getParameterSubList(0, index);
            final List<String> empireNames = order.getParameterSubList(index + 1);

            final List<Empire> transponderEmpires = getTransponderEmpires(order, empireNames);
            final List<Ship> ships = getTransponderShips(order, shipHandles);
            transponderEmpires.forEach(transponderEmpire -> {
                ships.forEach(ship -> {
                    ship.removeTransponder(empire);
                });
                addNewsResult(order, empire,
                        "You have concealed " + plural(ships.size(), "ship") + " from empire " + transponderEmpire);
            });
        });
    }
}