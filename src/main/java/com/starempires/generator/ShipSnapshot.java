package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.starempires.objects.Coordinate;
import com.starempires.objects.DeviceType;
import com.starempires.objects.Empire;
import com.starempires.objects.HullType;
import com.starempires.objects.ScanStatus;
import com.starempires.objects.Ship;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_DEFAULT)
@SuperBuilder
@Getter
@JsonPropertyOrder(alphabetic = true)
public class ShipSnapshot extends OwnableObjectSnapshot {

    private final String serialNumber;
    private final List<String> conditions;
    private final Set<String> cargo;
    private final String shipClass;
    private final double opRating;
    private final int dpRemaining;
    private final int emptyRacks;
    private final String carrier;
    // ship class info
    private final HullType hull;
    private final DeviceType device;
    private final boolean starbase;
    private final int dp;
    private final int guns;
    private final int engines;
    private final int scan;
    private final int racks;
    private final int tonnage;
    private final int cost;
    private final int ar;
    private final int opGuns;
    private final int opEngines;
    private final int opScan;

    public static ShipSnapshot fromShip(final Ship ship, final Empire empire) {
        ShipSnapshot snapshot = null;
        final Coordinate coordinate = ship.getCoordinate();
        if (ship.isOwnedBy(empire) || empire.isGM()) {
            snapshot = ShipSnapshot.builder()
                    .coordinate(empire.toLocal(coordinate))
                    .serialNumber(ship.getSerialNumber())
                    .name(ship.getName())
                    .owner(ship.getOwner().getName())
                    .shipClass(ship.getShipClass().getName())
                    .conditions(ship.getAbbreviatedConditions())
                    .opRating(ship.getOperationRating())
                    .dpRemaining(ship.getDpRemaining())
                    .emptyRacks(ship.getEmptyRacks())
                    .cargo(ship.getCargo().stream().map(Ship::getHandle).collect(Collectors.toSet()))
                    .carrier(ship.getCarrier() == null ? null : ship.getCarrier().getHandle())
                    .opGuns(ship.getAvailableGuns())
                    .opEngines(ship.getAvailableEngines())
                    .opScan(ship.getAvailableScan())
                    .hull(ship.getShipClass().getHullType())
                    .device(ship.getShipClass().getDeviceType())
                    .starbase(ship.getShipClass().isStarbase())
                    .dp(ship.getShipClass().getDp())
                    .guns(ship.getShipClass().getGuns())
                    .engines(ship.getShipClass().getEngines())
                    .scan(ship.getShipClass().getScan())
                    .racks(ship.getShipClass().getRacks())
                    .tonnage(ship.getShipClass().getTonnage())
                    .cost(ship.getShipClass().getCost())
                    .ar(ship.getShipClass().getAr())
                    .build();
        }
        else if (empire.getScanStatus(coordinate) == ScanStatus.VISIBLE) {
            final ShipSnapshotBuilder<? extends ShipSnapshot, ?> builder = ShipSnapshot.builder()
                    .coordinate(empire.toLocal(coordinate))
                    .serialNumber(ship.getSerialNumber())
                    .name(ship.getName())
                    .owner(ship.getOwner().getName())
                    .shipClass(ship.getShipClass().getName())
                    .hull(ship.getShipClass().getHullType())
                    .conditions(ship.getAbbreviatedConditions())
                    .tonnage(ship.getShipClass().getTonnage())
                    .starbase(ship.getShipClass().isStarbase());

            if (empire.isKnownShipClass(ship.getShipClass())) {
                builder.dpRemaining(ship.getDpRemaining())
                       .opRating(ship.getOperationRating())
                       .opGuns(ship.getAvailableGuns())
                       .opEngines(ship.getAvailableEngines())
                       .opScan(ship.getAvailableScan())
                       .dp(ship.getShipClass().getDp())
                       .guns(ship.getShipClass().getGuns())
                       .engines(ship.getShipClass().getEngines())
                       .scan(ship.getShipClass().getScan())
                       .racks(ship.getShipClass().getRacks())
                       .cost(ship.getShipClass().getCost())
                       .ar(ship.getShipClass().getAr());
            }
            snapshot = builder.build();
        }
        else if (empire.getScanStatus(coordinate) == ScanStatus.SCANNED) {
            if (ship.isTransponderSet(empire)) {
                snapshot = ShipSnapshot.builder()
                        .coordinate(empire.toLocal(coordinate))
                        .serialNumber(ship.getSerialNumber())
                        .name(ship.getName())
                        .owner(ship.getOwner().getName())
                        .shipClass(ship.getShipClass().getName())
                        .tonnage(ship.getShipClass().getTonnage())
                        .build();
            }
        }
        return snapshot;
    }
}