package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.TurnData;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public abstract class WorldBasedOrder extends Order {

    protected final static String WORLD_GROUP = "world";
    protected final static String WORLD_CAPTURE_REGEX = "(?<" + WORLD_GROUP + ">" + ID_REGEX + ")";

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    protected World world;

    protected static void parseReady(final JsonNode node, final TurnData turnData, final OrderType orderType, final WorldBasedOrder.WorldBasedOrderBuilder<?, ?> builder) {
        Order.parseReady(node, turnData, orderType, builder);
        builder.world(getTurnDataItemFromJsonNode(node.get("world"), turnData::getWorld));
    }
}