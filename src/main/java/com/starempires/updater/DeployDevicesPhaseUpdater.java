package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import com.starempires.orders.DeployOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.Collection;
import java.util.List;

/**
 * parameters:
 * DEPLOY device1 device2 ...
 */
public class DeployDevicesPhaseUpdater extends PhaseUpdater {

    public DeployDevicesPhaseUpdater(final TurnData turnData) {
        super(Phase.DEPLOY_DEVICES, turnData);
    }

    private void deployDevice(final Ship device) {
        final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(device);
        turnData.deploy(device);
        newsEmpires.forEach(newsEmpire -> {
            addNews(newsEmpire, "%s deployed %s %s in sector %s".formatted(device.getOwner(), device.getDeviceType(), device, newsEmpire.toLocal(device.getCoordinate())));
        });
        if (device.isPortalHammer()) {
            final Collection<Portal> portals = turnData.getPortals(device.getCoordinate());
            portals.forEach(portal -> {
                portal.setCollapsed(true);
                newsEmpires.forEach(newsEmpire -> {
                    addNews(newsEmpire, "Portal %s has collapsed".formatted(portal));
                });
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
                deployDevice(ship);
            }
        });
    }
}