package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.IdentifiableObject;
import com.starempires.orders.AddKnownOrder;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;

import java.util.List;
import java.util.function.Predicate;

public class AddKnownItemsPhaseUpdater extends PhaseUpdater {

    public AddKnownItemsPhaseUpdater(final TurnData turnData) {
        super(Phase.ADD_KNOWN_ITEMS, turnData);
    }

    private <T extends IdentifiableObject> void addKnowledge(
            final Order order,
            final Empire empire,
            final Predicate<T> action,
            final String type,
            final T object) {
        if (action.test(object)) {
            final String message = "Added %s knowledge of %s %s".formatted(empire, type, object);
            addNews(empire, message);
            addNews(order.getEmpire(), message);
        }
    }

    @Override
    public void update() {
        final List<Order> orders = turnData.getOrders(OrderType.ADDKNOWN);
        orders.forEach(o -> {
            final AddKnownOrder order = (AddKnownOrder) o;
            final List<Empire> recipients = order.getRecipients();
            recipients.forEach(recipient -> {
                order.getWorlds().forEach(world ->
                        addKnowledge(order, recipient, recipient::addKnownWorld, "world", world));
                order.getPortals().forEach(portal ->
                        addKnowledge(order, recipient, recipient::addKnownPortal, "portal", portal));
                order.getStorms().forEach(storm ->
                        addKnowledge(order, recipient, recipient::addKnownStorm, "storm", storm));
                order.getShipClasses().forEach(shipClass ->
                        addKnowledge(order, recipient, recipient::addKnownShipClass, "ship class", shipClass));
                order.getContacts().forEach(contact ->
                        addKnowledge(order, recipient, recipient::addKnownEmpire, "contact", contact));
            });
        });
    }
}