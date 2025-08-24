package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
public abstract class EmpireBasedOrder extends Order {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    final List<Empire> recipients;

    protected static void parseReady(final JsonNode node, final TurnData turnData, final OrderType orderType, final EmpireBasedOrder.EmpireBasedOrderBuilder<?, ?> builder) {
        Order.parseReady(node,  turnData, orderType, builder);
        builder.recipients(getTurnDataListFromJsonNode(node.get("recipients"), turnData::getEmpire));
    }
}