package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.Ship;

import java.util.List;

public class IdentifyShipsPhaseUpdater extends TransponderChangesPhaseUpdater {

    public IdentifyShipsPhaseUpdater(final TurnData turnData) {
        super(Phase.IDENTIFY_SHIPS, turnData);
    }

    @Override
    public void update() {

        final List<Order> orders = turnData.getOrders(OrderType.IDENTIFY);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();

            final int index = order.indexOfIgnoreCase(Constants.TOKEN_TO);
            final List<String> shipHandles = order.getParameterSubList(0, index);
            final List<String> empireNames = order.getParameterSubList(index + 1);

            final List<Empire> transponderEmpires = getTransponderEmpires(order, empireNames);
            final List<Ship> ships = getTransponderShips(order, shipHandles);
            transponderEmpires.forEach(transponderEmpire -> {
                ships.forEach(ship -> {
                    ship.addTransponder(empire);
                });
                addNewsResult(order, empire,
                        "You have identified " + plural(ships.size(), "ship") + " to empire " + transponderEmpires);
            });
        });
    }
}