package com.starempires.updater;

import com.starempires.objects.Empire;
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
        empire.addKnownShipClass(newClass);
        final Empire recipient = createEmpire("recipient");
        final GiveOrder order = GiveOrder.builder()
                .empire(empire)
                .orderType(OrderType.GIVE)
                .parameters("NewClass to recipient")
                .shipClasses(List.of(newClass))
                .recipients(List.of(recipient))
                .build();
        turnData.addOrder(order);
        updater.update();
        assertTrue(recipient.isKnownShipClass(newClass));
    }
}