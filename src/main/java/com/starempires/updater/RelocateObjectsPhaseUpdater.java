package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.MappableObject;
import com.starempires.orders.RelocateObjectOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public class RelocateObjectsPhaseUpdater extends PhaseUpdater {

    public RelocateObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.RELOCATE_OBJECTS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.RELOCATEOBJECT);
        orders.forEach(o -> {
            final RelocateObjectOrder order = (RelocateObjectOrder) o;
            final Coordinate coordinate = order.getCoordinate();
            final MappableObject target = ObjectUtils.firstNonNull(order.getWorld(), order.getPortal(), order.getStorm());
            target.setCoordinate(coordinate);
            final String kind = target.getClass().getSimpleName();
            addNews(order, kind + " " + target + " has been moved to sector " + coordinate);
        });
    }
}