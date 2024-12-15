package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class WorldBasedOrder extends Order {

    final static protected String WORLD_GROUP = "world";
    final static protected String WORLD_REGEX = "(?<" + WORLD_GROUP + ">@\\w+)";

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    protected World world;
}