package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.DeployOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.Collection;
import java.util.List;

public class DeployDevicesPhaseUpdater extends PhaseUpdater {

    public DeployDevicesPhaseUpdater(final TurnData turnData) {
        super(Phase.DEPLOY_DEVICES, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DEPLOY);
        orders.forEach(o -> {
            final DeployOrder order = (DeployOrder) o;
            addOrderText(order);
            for (final Ship device : order.getShips()) {
                final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(device);
                turnData.deploy(device);
                newsEmpires.forEach(newsEmpire -> {
                    addNews(newsEmpire, "%s deployed %s %s in sector %s".formatted(device.getOwner(), device.getDeviceType(), device, newsEmpire.toLocal(device.getCoordinate())));
                });
            }
        });
    }
}