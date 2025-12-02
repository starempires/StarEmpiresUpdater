package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class AddConnectionOrder extends ConnectionOrder {
    // order: ADDCONNECTION entry-portal exit-portal

    public static AddConnectionOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        return ConnectionOrder.parse(
                turnData,
                empire,
                parameters,
                OrderType.ADDCONNECTION,
                AddConnectionOrder.builder()
        );
    }

    public static AddConnectionOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddConnectionOrder.builder();
        ConnectionOrder.parseReady(node, turnData, OrderType.ADDCONNECTION, builder);
        return builder.build();
    }
}