package com.starempires.orders;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class UnknownOrder extends Order {

    public static UnknownOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final UnknownOrder order = UnknownOrder.builder()
                .empire(empire)
                .orderType(OrderType.UNKNOWN)
                .parameters(parameters)
                .synthetic(true)
                .ready(false)
                .build();
        order.addResult("Unknown command: " + parameters);
        return order;
    }
}