package com.starempires.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AuthorizeOrder extends ShareScanOrder {

    // order: AUTHORIZE (coordinate|@location) radius TO empire1 [empire2 ...]
    // order: AUTHORIZE ship1 [ship2 ...] TO empire1 [empire2 ...]
    // order: AUTHORIZE ALL TO empire1 [empire2 ...]

    public static AuthorizeOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final AuthorizeOrder order = AuthorizeOrder.builder()
                .empire(empire)
                .orderType(OrderType.AUTHORIZE)
                .parameters(parameters)
                .recipients(Lists.newArrayList())
                .ships(Lists.newArrayList())
                .build();
        return parse(turnData, empire, parameters, order);
    }
    
    public static AuthorizeOrder parseReady(final JsonNode node, final TurnData turnData) {
        return ShareScanOrder.parseReadyShareScan(node, turnData, OrderType.AUTHORIZE, AuthorizeOrder.builder());
    }
}