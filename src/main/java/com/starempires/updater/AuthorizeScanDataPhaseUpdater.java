package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.Empire;
import com.starempires.objects.RadialCoordinate;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.orders.AuthorizeOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class AuthorizeScanDataPhaseUpdater extends PhaseUpdater {

    public AuthorizeScanDataPhaseUpdater(final TurnData turnData) {
        super(Phase.AUTHORIZE_SCAN_ACCESS, turnData);
    }

    private void authorizeSectorData(final Order order, final List<Empire> recipients, final Coordinate coordinate, final int radius) {
        final Empire empire = order.getEmpire();
        final RadialCoordinate radialCoordinate = new RadialCoordinate(coordinate, radius);
        recipients.forEach(recipient -> {
            empire.addCoordinateScanAccess(recipient, radialCoordinate);
            addNews(order, "You have authorized empire " + recipient + " access to "
                    + plural(RadialCoordinate.getSurroundingCoordinates(radialCoordinate).size(), "sector") + " of scan data");
        });
    }

    private void authorizeShipData(final Order order, final List<Empire> recipients, final List<Ship> ships) {
        final Empire empire = order.getEmpire();
        final List<Ship> validShips = Lists.newArrayList();
        for (Ship ship: ships) {
            if (!ship.isAlive()) {
                addNews(empire, "Ship %s is destroyed".formatted(ship));
            }
            else {
                validShips.add(ship);
            }
        }

        recipients.forEach(recipient -> {
            empire.addShipScanAccess(recipient, validShips);
            addNews(order, "You have authorized empire " + recipient
                    + " access to scan data from " + plural(validShips.size(), "ship"));
        });
    }

    private void authorizeShipClassData(final Order order, final List<Empire> recipients, final List<String> shipClassNames) {
        final Empire empire = order.getEmpire();
        final List<ShipClass> shipClasses = turnData.getShipClasses(empire, shipClassNames);

        recipients.forEach(recipient -> {
            empire.addShipClassScanAccess(recipient, shipClasses);
            addNews(order,
                    "You have authorized empire " + recipient + " access to scan data from "
                            + plural(shipClasses.size(), "ship class", Constants.SUFFIX_ES));
        });
    }

    private void authorizeAllData(final Order order, final List<Empire> recipients) {
        final Empire empire = order.getEmpire();
        recipients.forEach(recipient -> {
            empire.addEmpireScanAccess(recipient);
            addNews(order, "You have authorized empire " + recipient + " access to all scan data");
        });
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.AUTHORIZE);
        orders.forEach(o -> {
            final AuthorizeOrder order = (AuthorizeOrder)o;
            final List<Empire> recipients = order.getRecipients();
            if (order.getCoordinate() != null) {
                authorizeSectorData(order, recipients, order.getCoordinate(), order.getRadius());
            }
            else if (order.getMapObject() != null) {
                authorizeSectorData(order, recipients, order.getMapObject().getCoordinate(), order.getRadius());
            }
            else if (!CollectionUtils.isEmpty(order.getShips())) {
                authorizeShipData(order, recipients, order.getShips());
            }
            else if (order.isAllSectors()) {
                authorizeAllData(order, recipients);
            }
        });
    }
}