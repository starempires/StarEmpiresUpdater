package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class DenyOrder extends ShareScanOrder {

    // order: DENY coordinate1 [coordinate2 ...] TO empire1 [empire2 ...]
    // order: DENY @location1 [@location2 ...] TO empire1 [empire2 ...]
    // order: DENY ship1 [ship2 ...] TO empire1 [empire2 ...]
    // order: DENY ALL TO empire1 [empire2 ...]

    public static DenyOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final DenyOrder order = DenyOrder.builder()
                .empire(empire)
                .orderType(OrderType.DENY)
                .parameters(parameters)
                .recipients(Lists.newArrayList())
                .mapObjects(Lists.newArrayList())
                .coordinates(Lists.newArrayList())
                .ships(Lists.newArrayList())
                .build();
        return parse(turnData, empire, parameters, order);
    }
    
    public static DenyOrder parseReady(final JsonNode node, final TurnData turnData) {
        return ShareScanOrder.parseReadyShareScan(node, turnData, OrderType.DENY, DenyOrder.builder());
    }
}