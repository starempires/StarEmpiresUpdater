package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class RemoveKnownOrder extends KnownOrder {
    // order: REMOVENOWN object-type object1 [object2 ...] TO empire1 [empire2...]
    public static RemoveKnownOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        return KnownOrder.parse(
                turnData,
                empire,
                parameters,
                OrderType.REMOVEKNOWN,
                RemoveKnownOrder.builder()
        );
    }

    public static RemoveKnownOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = RemoveKnownOrder.builder();
        KnownOrder.parseReady(node, turnData, OrderType.REMOVEKNOWN, builder);
        return builder.build();
    }
}