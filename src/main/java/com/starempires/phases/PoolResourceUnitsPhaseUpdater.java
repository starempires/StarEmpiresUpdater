package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.World;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.starempires.objects.IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR;

public class PoolResourceUnitsPhaseUpdater extends PhaseUpdater {

    // pool to-world [except world1 [world2 ...]]
    private static final String WORLD_GROUP = "world";
    private static final String EXCEPT_GROUP = "except";
    private static final String POOL_REGEX = "^pool\\s(?<" + WORLD_GROUP + ">[\\w]+)(?:\\s+(?<" + EXCEPT_GROUP + ">(?:\\w+\\s*)+))?\\s*$";

    private static final Pattern POOL_PATTERN = Pattern.compile(POOL_REGEX, Pattern.CASE_INSENSITIVE);

    public PoolResourceUnitsPhaseUpdater(final TurnData turnData) {
        super(Phase.POOL_RESOURCE_UNITS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.POOL);
        orders.forEach(order -> {
            final Matcher matcher = POOL_PATTERN.matcher(order.getParametersAsString());
            if (matcher.matches()) {
                final Empire empire = order.getEmpire();
                final String poolWorldName = matcher.group(WORLD_GROUP);
                final World poolWorld = turnData.getWorld(poolWorldName);
                if (poolWorld == null || !empire.isKnownWorld(poolWorld)) {
                    addNewsResult(order, empire, "Unknown world " + poolWorldName);
                } else if (!poolWorld.isOwnedBy(empire)) {
                    addNewsResult(order, empire, "You do not own world " + poolWorld);
                } else {
                    final String exceptText = matcher.group(EXCEPT_GROUP);
                    final List<World> exceptWorlds = Lists.newArrayList();
                    if (exceptText != null) {
                        final List<String> exceptWorldNames = Lists.newArrayList(exceptText.split(" "));
                        exceptWorldNames.forEach(exceptWorldName -> {
                            final World exceptWorld = turnData.getWorld(exceptWorldName);
                            if (exceptWorld == null || !empire.isKnownWorld(exceptWorld)) {
                                addNewsResult(order, empire, "Unknown world " + exceptWorldName);
                            } else if (!exceptWorld.isOwnedBy(empire)) {
                                addNewsResult(order, empire, "You do not own world " + exceptWorld);
                            } else {
                                exceptWorlds.add(exceptWorld);
                            }
                        });
                    }

                    final AtomicInteger total = new AtomicInteger();
                    final Set<World> ownedWorlds = turnData.getOwnedWorlds(empire);
                    ownedWorlds.removeAll(exceptWorlds);
                    final List<World> worlds = Lists.newArrayList(ownedWorlds);
                    worlds.sort(IDENTIFIABLE_NAME_COMPARATOR);
                    worlds.forEach(world -> {
                        final int stockpile = world.getStockpile();
                        if (stockpile > 0) {
                            if (world.isBlockaded()) {
                                addNewsResult(order, empire, "World " + world + " is blockaded");
                            } else {
                                addNewsResult(order, empire, "Pooled " + stockpile + " RU from world " + world);
                                total.addAndGet(stockpile);
                                world.setStockpile(0);
                            }
                        }
                    });
                    final int existingStockpile = poolWorld.getStockpile();
                    poolWorld.setStockpile(existingStockpile + total.get());
                    addNewsResult(order, "Pooled " + total + " RU to world " + poolWorld);
                }
            }
        });
    }
}