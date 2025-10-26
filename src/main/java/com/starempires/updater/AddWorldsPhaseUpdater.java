package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.World;
import com.starempires.orders.AddWorldOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;

public class AddWorldsPhaseUpdater extends PhaseUpdater {
    public AddWorldsPhaseUpdater(TurnData turnData) {
        super(Phase.ADD_WORLDS, turnData);
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.ADDWORLD);
        orders.forEach(o -> {
            final AddWorldOrder order = (AddWorldOrder) o;
            final World world = World.builder()
                    .coordinate(order.getCoordinate())
                    .name(order.getName())
                    .production(order.getProduction())
                    .stockpile(order.getStockpile())
                    .owner(order.getOwner())
                    .build();
            turnData.addWorld(world);
            String message = "Added world %s (production %d, stockpile %d) in sector %s".formatted(world, world.getProduction(), world.getStockpile(), world.getCoordinate());
            if (world.isOwned()) {
                world.getOwner().addKnownWorld(world);
                message += " (owner %s)".formatted(world.getOwner());
            }
            addNews(order, message);
        });
    }
}