package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.LoadOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.Collection;
import java.util.List;

/**
 * parameters:
 * LOAD ship1 [ship2 ….] ONTO carrier
 */
public class LoadShipPhaseUpdater extends PhaseUpdater {

    public LoadShipPhaseUpdater(final TurnData turnData) {
        super(Phase.LOAD_SHIPS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.LOAD);
        orders.forEach(o -> {
            final LoadOrder order = (LoadOrder) o;
            // load parameters are list of cargo handles to be loaded followed by carrier name last
            final Ship carrier = order.getCarrier();
            for (final Ship cargo : order.getShips()) {
                if (!cargo.isSameSector(carrier)) {
                    addNewsResult(order, "Carrier %s and cargo %s are not in the same sector".formatted(carrier, cargo));
                } else if (!carrier.canLoadCargo(cargo)) {
                    addNewsResult(order, "Carrier %s has insufficient free racks (%d) to load ship %s (tonnage %d)".formatted(carrier, carrier.getEmptyRacks(), cargo, cargo.getTonnage()));
                } else {
                    turnData.load(cargo, carrier);
                    final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(carrier);
                    addNewsResult(order, newsEmpires, "Ship " + cargo + " loaded onto carrier " + carrier);
                }
            }
        });
    }
}