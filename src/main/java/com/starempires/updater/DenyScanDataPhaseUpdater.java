package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.RadialCoordinate;
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

    private void denySectorData(final Order order, final List<Empire> recipients, final Coordinate coordinate, final int radius) {
        final Empire empire = order.getEmpire();
        final RadialCoordinate radialCoordinate = new RadialCoordinate(coordinate, radius);
        recipients.forEach(recipient -> {
            empire.removeCoordinateScanAccess(recipient, radialCoordinate);
            addNews(order, "You have denied empire " + recipient + " access to "
                    + plural(RadialCoordinate.getSurroundingCoordinates(radialCoordinate).size(), "sector") + " of scan data");
        });
    }

    private void denyShipData(final Order order, final List<Empire> recipients, final List<Ship> ships) {
        final Empire empire = order.getEmpire();
        recipients.forEach(recipient -> {
            empire.removeShipScanAccess(recipient, ships);
            addNews(order, "You have denied empire " + recipient
                    + " access to scan data from " + plural(ships.size(), "ship"));
        });
    }

    private void denyShipClassData(final Order order, final List<Empire> recipients, final List<String> shipClassNames) {
        final Empire empire = order.getEmpire();
        final List<ShipClass> shipClasses = turnData.getShipClasses(empire, shipClassNames);
        recipients.forEach(recipient -> {
            empire.removeShipClassScanAccess(recipient, shipClasses);
            addNews(order, "You have denied empire " + recipient + " access to scan data from "
                            + plural(shipClasses.size(), "ship class", Constants.SUFFIX_ES));
        });
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

            if (order.getCoordinate() != null) {
                denySectorData(order, recipients, order.getCoordinate(), order.getRadius());
            }
            else if (order.getMapObject() != null) {
                denySectorData(order, recipients, order.getMapObject().getCoordinate(), order.getRadius());
            }
            else if (!CollectionUtils.isEmpty(order.getShips())) {
                denyShipData(order, recipients, order.getShips());
            }
            else if (order.isAllSectors()) {
                denyAllData(order, recipients);
            }
        });
    }
}