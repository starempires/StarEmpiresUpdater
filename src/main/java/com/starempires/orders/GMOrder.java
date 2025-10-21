package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class GMOrder extends Order {

    protected static void parseReady(final JsonNode node, final TurnData turnData, final OrderType orderType, final ShipBasedOrder.ShipBasedOrderBuilder<?, ?> builder) {
        Order.parseReady(node, turnData, orderType, builder);
        builder.gmOnly(true);
    }
}