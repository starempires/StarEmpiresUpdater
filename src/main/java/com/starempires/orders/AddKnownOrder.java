package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class AddKnownOrder extends KnownOrder {
    // order: ADDKNOWN object-type object1 [object2 ...] TO empire1 [empire2...]

    public static AddKnownOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        return KnownOrder.parse(
                turnData,
                empire,
                parameters,
                OrderType.ADDKNOWN,
                AddKnownOrder.builder()
        );
    }

    public static AddKnownOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = AddKnownOrder.builder();
        KnownOrder.parseReady(node, turnData, OrderType.ADDKNOWN, builder);
        return builder.build();
    }
}