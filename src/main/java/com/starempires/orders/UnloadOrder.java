package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class UnloadOrder extends ShipBasedOrder {

    public static UnloadOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final UnloadOrder order = UnloadOrder.builder().orderType(OrderType.UNLOAD).empire(empire).parameters(parameters).build();
        final List<Ship> cargo = getShipsFromNames(empire, parameters, order);
        for (final Ship ship : cargo) {
            if (!ship.isLoaded()) {
                order.addError(ship, "Ship is not loaded");
            }
            else {
                order.ships.add(ship);
                ship.unloadFromCarrier();
                order.addOKResult(ship);
            }
        }
        order.setReady(!order.ships.isEmpty());
        return order;
    }

    public static UnloadOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = UnloadOrder.builder();
        ShipBasedOrder.parseReady(node, turnData, OrderType.UNLOAD, builder);
        return builder.build();
    }
}