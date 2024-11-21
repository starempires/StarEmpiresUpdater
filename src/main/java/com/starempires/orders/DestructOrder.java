package com.starempires.orders;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
public class DestructOrder extends ShipBasedOrder {

    public static DestructOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final DestructOrder order = DestructOrder.builder()
                .empire(empire)
                .orderType(OrderType.DESTRUCT)
                .parameters(parameters)
                .build();
        final List<Ship> destructors = getLocationShips(empire, parameters, order);
        for (final Ship ship : destructors) {
            if (ship.isStarbase()) {
                order.addError(ship, "Starbase cannot be self-destructed");
            }
            else {
                if (ship.isLoaded()) {
                    order.addError(ship, "Ship is loaded onto carrier %s and cannot be self-destructed.".formatted(ship.getCarrier()));
                }
                else if (ship.hasLoadedCargo()) {
                    order.addWarning(ship, "Self-destructing carrier will self-destruct %d loaded cargo ships.".formatted(ship.getCargo().size()));
                    order.ships.addAll(ship.getCargoGroup());
                }
                else {
                    order.ships.add(ship);
                    order.addOKResult(ship);
                }
            }
        }

        order.setReady(!order.ships.isEmpty());
        return order;
    }
}