package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Storm;
import com.starempires.orders.AddStormOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;
import java.util.Set;

public class AddStormsPhaseUpdater extends PhaseUpdater {
    public AddStormsPhaseUpdater(TurnData turnData) {
        super(Phase.ADD_STORMS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.ADDSTORM);
        orders.forEach(o -> {
            final AddStormOrder order = (AddStormOrder) o;
            final Storm storm = Storm.builder()
                    .coordinate(order.getCoordinate())
                    .name(order.getName())
                    .intensity(order.getIntensity())
                    .build();
            turnData.addStorm(storm);
            final Set<Empire> empires = turnData.getEmpiresPresent(storm);
            empires.add(order.getEmpire());
            addNews(empires, "Storm %s has been added to sector %s".formatted(storm, storm.getCoordinate()));
        });
    }
}