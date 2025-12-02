package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.MappableObject;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.RemoveKnownOrder;
import com.starempires.orders.RemoveObjectOrder;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class RemoveObjectsPhaseUpdater extends PhaseUpdater {

    public RemoveObjectsPhaseUpdater(final TurnData turnData) {
        super(Phase.REMOVE_OBJECTS, turnData);
    }

    private <T extends MappableObject> void removeObjects(
        final RemoveObjectOrder order,
        final Consumer<T> action,
        final String type,
        final List<T> objects
    ) {
        objects.forEach(object -> {
            final Collection<Empire> empires = turnData.getEmpiresPresent(object);
            empires.add(order.getEmpire());
            action.accept(object);
            addNews(empires, "%s %s has been removed".formatted(type, object));
        });
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.REMOVEOBJECT);
        orders.forEach(o -> {
            final RemoveObjectOrder order = (RemoveObjectOrder) o;
            removeObjects(order, turnData::removeWorld, "world", order.getWorlds());
            removeObjects(order, turnData::removePortal, "portal", order.getPortals());
            removeObjects(order, turnData::removeStorm, "storm", order.getStorms());
            final RemoveKnownOrder removeKnownOrder = RemoveKnownOrder.builder()
                    .orderType(OrderType.REMOVEKNOWN)
                    .empire(order.getEmpire())
                    .worlds(order.getWorlds())
                    .portals(order.getPortals())
                    .storms(order.getStorms())
                    .recipients(turnData.getActiveEmpires().stream().toList())
                    .synthetic(true)
                    .gmOnly(true)
                    .build();
            turnData.addOrder(removeKnownOrder);
        });
    }
}