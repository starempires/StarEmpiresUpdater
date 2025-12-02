package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class RemoveConnectionOrder extends ConnectionOrder {
    // order: REMOVECONNECTION entry-portal exit-portal
    public static RemoveConnectionOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        return ConnectionOrder.parse(
                turnData,
                empire,
                parameters,
                OrderType.REMOVECONNECTION,
                RemoveConnectionOrder.builder()
        );
    }

    public static RemoveConnectionOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = RemoveConnectionOrder.builder();
        ConnectionOrder.parseReady(node, turnData, OrderType.REMOVECONNECTION, builder);
        return builder.build();
    }
}