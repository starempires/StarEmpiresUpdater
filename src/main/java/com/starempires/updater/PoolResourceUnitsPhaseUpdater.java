package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.World;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.PoolOrder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.starempires.objects.IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR;

public class PoolResourceUnitsPhaseUpdater extends PhaseUpdater {

    public PoolResourceUnitsPhaseUpdater(final TurnData turnData) {
        super(Phase.POOL_RESOURCE_UNITS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.POOL);
        orders.forEach(o -> {
            final PoolOrder order = (PoolOrder) o;
            final Empire empire = order.getEmpire();
            final World poolWorld = order.getWorld();
            if (!poolWorld.isOwnedBy(empire)) {
                addNewsResult(order, "You do not own world " + poolWorld);
            } else {
                final List<World> exceptWorlds = order.getExceptedWorlds();
                exceptWorlds.forEach(exceptWorld -> {
                    if (!exceptWorld.isOwnedBy(empire)) {
                        addNewsResult(order, empire, "You do not own world " + exceptWorld);
                        exceptWorlds.remove(exceptWorld);
                    }

                    final AtomicInteger total = new AtomicInteger();
                    final List<World> ownedWorlds = Lists.newArrayList(turnData.getOwnedWorlds(empire));
                    ownedWorlds.removeAll(exceptWorlds);
                    ownedWorlds.remove(poolWorld);
                    ownedWorlds.sort(IDENTIFIABLE_NAME_COMPARATOR);
                    ownedWorlds.forEach(world -> {
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
                    poolWorld.adjustStockpile(total.get());
                    addNewsResult(order, "Pooled %d RU from %d %s to %s (stockpile now %d)".formatted(total.get(), ownedWorlds.size(), plural(ownedWorlds.size(), "world"), poolWorld, poolWorld.getStockpile()));
                });
            }
        });
    }
}