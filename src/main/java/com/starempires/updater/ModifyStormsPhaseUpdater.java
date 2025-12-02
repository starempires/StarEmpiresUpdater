package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Storm;
import com.starempires.orders.ModifyStormOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;
import java.util.Set;

public class ModifyStormsPhaseUpdater extends PhaseUpdater {

    public ModifyStormsPhaseUpdater(final TurnData turnData) {
        super(Phase.MODIFY_STORMS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.MODIFYSTORM);
        orders.forEach(o -> {
            final ModifyStormOrder order = (ModifyStormOrder) o;
            final Storm storm = order.getStorm();
            storm.setIntensity(order.getIntensity());
            final Set<Empire> empires = turnData.getEmpiresPresent(storm);
            empires.add(order.getEmpire());
            addNews(empires, "Storm %s now has intensity %d".formatted(storm, order.getIntensity()));
        });
    }
}