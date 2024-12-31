package com.starempires.orders;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@SuperBuilder
public class PoolOrder extends WorldBasedOrder {

    // POOL world [EXCEPT world1 world2….]
    private static final String EXCEPT_GROUP = "worlds";
    private static final String EXCEPT_REGEX = "(?<" + EXCEPT_GROUP + ">(\\w+)(?:\\w+\\w+)*)";
    private static final String REGEX = WORLD_REGEX + "( EXCEPT " + EXCEPT_REGEX + ")*";
    private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    private final List<World> exceptedWorlds = Lists.newArrayList();

    public static PoolOrder parse(final TurnData turnData, final Empire empire, final String parameters) {
        final PoolOrder order = PoolOrder.builder()
                .empire(empire)
                .orderType(OrderType.POOL)
                .parameters(parameters)
                .build();
        final Matcher matcher = PATTERN.matcher(parameters);
        if (matcher.matches()) {
            final String worldName = matcher.group(WORLD_GROUP);
            final World world = turnData.getWorld(worldName);
            if (world == null || !empire.isKnownWorld(world)) {
                order.addError("Unknown world: " + worldName);
                order.setReady(false);
                return order;
            }
            order.addOKResult(world);
            order.world = world;

            final String exceptText = matcher.group(EXCEPT_GROUP);
            if (exceptText != null) {
                for (String worldNameToExcept : exceptText.split(" ")) {
                    final World worldToExcept = turnData.getWorld(worldNameToExcept);
                    if (worldToExcept == null || !empire.isKnownWorld(worldToExcept)) {
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
        }
        return order;
    }
}