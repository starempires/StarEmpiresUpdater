package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class PoolOrder extends WorldBasedOrder {

    // order: POOL world [EXCEPT world1 world2….]
    private static final String REGEX = ID_CAPTURE_REGEX + OBJECT_LIST_EXCEPT_CAPTURE_REGEX;
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final List<World> exceptedWorlds;

    public static PoolOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final PoolOrder order = PoolOrder.builder()
                .empire(empire)
                .orderType(OrderType.POOL)
                .parameters(parameters)
                .exceptedWorlds(Lists.newArrayList())
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String worldName = matcher.group(ID_GROUP);
            final World world = turnData.getWorld(worldName);
            if (!empire.isKnownWorld(world)) {
                order.addError("Unknown world: " + worldName);
                return order;
            }
            order.world = world;

            final String exceptText = matcher.group(OBJECT_LIST_GROUP);
            if (exceptText != null) {
                for (String worldNameToExcept : exceptText.split(" ")) {
                    final World worldToExcept = turnData.getWorld(worldNameToExcept);
                    if (!empire.isKnownWorld(worldToExcept)) {
                        order.addError("Unknown world: " + worldNameToExcept);
                    } else {
                        if (!worldToExcept.isOwnedBy(empire)) {
                            order.addWarning(world, "You do not currently own world: " + world);
                        }
                        order.exceptedWorlds.add(worldToExcept);
                        order.addOKResult(worldToExcept);
                    }
                }
            }
            order.setReady(true);
        }
        else {
            order.addError("Invalid POOL order: " + parameters);
        }
        return order;
    }

    public static PoolOrder parseReady(final JsonNode node, final TurnData turnData) {
        final var builder = PoolOrder.builder();
        WorldBasedOrder.parseReady(node, turnData, OrderType.POOL, builder);
        final String name = getString(node, "carrier");
        return builder
                .exceptedWorlds(getTurnDataListFromJsonNode(node, turnData::getWorld))
                .build();
    }
}