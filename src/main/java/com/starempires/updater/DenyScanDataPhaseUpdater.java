package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.MappableObject;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.orders.DenyOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class DenyScanDataPhaseUpdater extends PhaseUpdater {

    public DenyScanDataPhaseUpdater(final TurnData turnData) {
        super(Phase.DENY_SCAN_ACCESS, turnData);
    }

    private void denyCoordinateData(final Order order, final List<Empire> recipients, final List<Coordinate> coordinates) {
        if (CollectionUtils.isNotEmpty(coordinates)) {
            final Empire empire = order.getEmpire();
            recipients.forEach(recipient -> {
                empire.removeCoordinateScanAccess(recipient, coordinates);
                addNews(order, "You have denied empire " + recipient + " access to "
                        + plural(coordinates.size(), "sector") + " of scan data");
            });
        }
    }

    private void denyMapObjectData(final Order order, final List<Empire> recipients, final List<MappableObject> mapObjects) {
        if (CollectionUtils.isNotEmpty(mapObjects)) {
            final Empire empire = order.getEmpire();
            recipients.forEach(recipient -> {
                empire.removeObjectScanAccess(recipient, mapObjects);
                addNews(order, "You have denied empire " + recipient + " access to "
                        + plural(mapObjects.size(), "location") + " of scan data");
            });
        }
    }

    private void denyShipData(final Order order, final List<Empire> recipients, final List<Ship> ships) {
        if (CollectionUtils.isNotEmpty(ships)) {
            final Empire empire = order.getEmpire();
            recipients.forEach(recipient -> {
                empire.removeShipScanAccess(recipient, ships);
                addNews(order, "You have denied empire " + recipient
                        + " access to scan data from " + plural(ships.size(), "ship"));
            });
        }
    }

    private void denyShipClassData(final Order order, final List<Empire> recipients, final List<ShipClass> shipClasses) {
        if (CollectionUtils.isNotEmpty(shipClasses)) {
            final Empire empire = order.getEmpire();
            recipients.forEach(recipient -> {
                empire.removeShipClassScanAccess(recipient, shipClasses);
                addNews(order, "You have denied empire " + recipient + " access to scan data from "
                        + plural(shipClasses.size(), "ship class", Constants.SUFFIX_ES));
            });
        }
    }

    private void denyAllData(final Order order, final List<Empire> recipients) {
        final Empire empire = order.getEmpire();
        recipients.forEach(recipient -> {
            empire.removeEmpireScanAccess(recipient);
            addNews(order, "You have denied empire " + recipient + " access to all scan data");
        });
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.DENY);
        orders.forEach(o -> {
            final DenyOrder order = (DenyOrder)o;
            final List<Empire> recipients = order.getRecipients();
            if (order.isAllSectors()) {
                denyAllData(order, recipients);
            }
            else {
                denyCoordinateData(order, recipients, order.getCoordinates());
                denyMapObjectData(order, recipients, order.getMapObjects());
                denyShipData(order, recipients, order.getShips());
            }
        });
    }
}