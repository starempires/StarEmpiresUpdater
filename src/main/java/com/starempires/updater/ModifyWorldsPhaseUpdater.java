package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.World;
import com.starempires.orders.ModifyWorldOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ModifyWorldsPhaseUpdater extends PhaseUpdater {

    public ModifyWorldsPhaseUpdater(final TurnData turnData) {
        super(Phase.MODIFY_WORLDS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.MODIFYWORLD);
        orders.forEach(o -> {
            final ModifyWorldOrder order = (ModifyWorldOrder) o;
            final World world = order.getWorld();
            final int production = order.getProduction();
            final int stockpile = order.getStockpile();
            final Empire owner = order.getOwner();
            final Set<Empire> empires = turnData.getEmpiresPresent(world);
            if (production != world.getProduction()) {
                world.setProduction(production);
                addNews(order, "World %s now has production %d".formatted(world, production));
            }
            if (stockpile != world.getStockpile()) {
                world.setStockpile(stockpile);
                addNews(order, "World %s now has stockpile %d".formatted(world, stockpile));
            }
            if (!Objects.equals(owner, world.getOwner())) {
                world.setOwner(owner);
                if (owner == null) {
                    addNews(empires, "World %s now unowned".formatted(world));
                }
                else {
                    empires.add(owner);
                    addNews(empires, "World %s now owned by %s".formatted(world, owner));
                }
            }
        });
    }
}