package com.starempires.phases;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Order;
import com.starempires.objects.OrderType;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PoolResourceUnitsPhaseUpdater extends PhaseUpdater {

    public PoolResourceUnitsPhaseUpdater(final TurnData turnData) {
        super(Phase.POOL_RESOURCE_UNITS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.POOL);
        orders.forEach(order -> {
            final Empire empire = order.getEmpire();
            final String poolWorldName = order.getStringParameter(0);
            final World poolWorld = turnData.getWorld(poolWorldName);
            if (poolWorld == null || !empire.isKnownWorld(poolWorld)) {
                addNewsResult(order, empire, "Unknown world " + poolWorldName);
            }
            else if (!poolWorld.getOwner().equals(empire)) {
                addNewsResult(order, empire, "You do not own world " + poolWorld);
            }
            else {
                final List<String> exceptWorldNames = Lists.newArrayList(poolWorldName);
                if (order.getParameters().size() > 1) {
                    exceptWorldNames.addAll(order.getParameterSubList(1));
                }

                final AtomicInteger total = new AtomicInteger();
                final Collection<World> ownedWorlds = turnData.getOwnedWorlds(empire);
                final List<World> worlds = Lists.newArrayList(ownedWorlds);
                Collections.sort(worlds);
                worlds.forEach(world -> {
                    final int stockpile = world.getStockpile();
                    if (stockpile > 0) {
                        if (!exceptWorldNames.contains(world.getName())) {
                            if (!world.isBlockaded()) {
                                addNewsResult(order, empire, "Pooled " + stockpile + " RU from world " + world);
                                total.addAndGet(stockpile);
                                world.setStockpile(0);
                            }
                            else {
                                addNewsResult(order, empire, "World " + world + " is blockaded");
                            }
                        }
                    }
                });
                addNewsResult(order, empire, "Pooled " + total + " RU to world " + poolWorld);
                final int existingStockpile = poolWorld.getStockpile();
                poolWorld.setStockpile(existingStockpile + total.get());
            }
        });
    }
}