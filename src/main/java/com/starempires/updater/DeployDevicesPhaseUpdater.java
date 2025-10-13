package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.orders.DeployOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * parameters:
 * DEPLOY device1 device2 ...
 */
public class DeployDevicesPhaseUpdater extends PhaseUpdater {

    public DeployDevicesPhaseUpdater(final TurnData turnData) {
        super(Phase.DEPLOY_DEVICES, turnData);
    }

    private void deployDevice(final Order order, final Ship device) {
        final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(device);
        final Set<Ship> starbases = turnData.getStarbases(device);
        if (CollectionUtils.isEmpty(starbases)) {
            turnData.deploy(device);
            newsEmpires.forEach(newsEmpire -> {
                addNews(newsEmpire, "%s deployed device %s in sector %s".formatted(device.getOwner(), device, newsEmpire.toLocal(device.getCoordinate())));
            });
        } else {
            newsEmpires.forEach(newsEmpire -> {
                addNews(newsEmpire,
                        "Starbase %s prevented activation of %s device %s deployed in sector %s".formatted(starbases.stream().findFirst().orElseThrow(), device.getOwner(), device, newsEmpire.toLocal(device.getCoordinate())));
            });
        }
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DEPLOY);
        orders.forEach(o -> {
            final DeployOrder order = (DeployOrder) o;
            addOrderText(order);
            for (final Ship ship : order.getShips()) {
                deployDevice(order, ship);
            }
        });
    }
}