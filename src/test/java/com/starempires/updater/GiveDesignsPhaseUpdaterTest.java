package com.starempires.updater;

import com.starempires.objects.ShipClass;
import com.starempires.orders.GiveOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GiveDesignsPhaseUpdaterTest extends BaseTest {

    private GiveDesignsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new GiveDesignsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final ShipClass newClass = ShipClass.builder()
                .name("NewClass")
                .build();
        turnData.addShipClass(newClass);
        empire1.addKnownShipClass(newClass);
        final GiveOrder order = GiveOrder.builder()
                .empire(empire1)
                .orderType(OrderType.GIVE)
                .parameters("NewClass to recipient")
                .shipClasses(List.of(newClass))
                .recipients(List.of(empire2))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(empire2.isKnownShipClass(newClass));
    }
}