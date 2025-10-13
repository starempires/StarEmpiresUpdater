package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.World;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.orders.PoolOrder;

import java.util.List;
import java.util.Optional;
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
            addOrderText(order);
            final Empire empire = order.getEmpire();
            final World poolWorld = order.getWorld();
            if (!poolWorld.isOwnedBy(empire)) {
                addNewsResult(order, "You do not own world " + poolWorld);
            } else {
                final List<World> exceptWorlds = Optional.ofNullable(order.getExceptedWorlds()).orElse(List.of());
                exceptWorlds.forEach(world -> {
                    if (!world.isOwnedBy(empire)) {
                        addNewsResult(order, "You do not own world " + world);
                        order.getExceptedWorlds().remove(world);
                    }
                });

                final AtomicInteger total = new AtomicInteger();
                final List<World> ownedWorlds = Lists.newArrayList(turnData.getOwnedWorlds(empire));
                ownedWorlds.removeAll(exceptWorlds);
                ownedWorlds.remove(poolWorld);
                ownedWorlds.sort(IDENTIFIABLE_NAME_COMPARATOR);
                ownedWorlds.forEach(world -> {
                    final int stockpile = world.getStockpile();
                    if (stockpile > 0) {
                        if (world.isBlockaded()) {
                            addNewsResult(order, empire, "Cannot pool RU from blockaded world %s".formatted(world));
                        } else {
                            addNewsResult(order, empire, "Pooled %d RU from %s to %s".formatted(stockpile, world, poolWorld));
                            total.addAndGet(stockpile);
                            world.setStockpile(0);
                        }
                    }
                });
                poolWorld.adjustStockpile(total.get());
                addNewsResult(order, "Pooled %d RU from %s to %s (stockpile now %d)".formatted(total.get(),
                        plural(ownedWorlds.size(), "other world"), poolWorld, poolWorld.getStockpile()));
            }
        });
    }
}