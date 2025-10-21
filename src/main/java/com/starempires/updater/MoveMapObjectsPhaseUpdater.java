package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Coordinate;
import com.starempires.objects.MappableObject;
import com.starempires.orders.MoveMapObjectOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public class MoveMapObjectsPhaseUpdater extends PhaseUpdater {

    public MoveMapObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.MOVE_MAP_OBJECTS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.MOVEMAPOBJECT);
        orders.forEach(o -> {
            final MoveMapObjectOrder order = (MoveMapObjectOrder) o;
            final Coordinate coordinate = order.getCoordinate();
            final MappableObject target = ObjectUtils.firstNonNull(order.getWorld(), order.getPortal(), order.getStorm());
            target.setCoordinate(coordinate);
            final String kind = target.getClass().getSimpleName();
            addNews(order, kind + " " + target + " has been moved to sector " + coordinate);
        });
    }
}