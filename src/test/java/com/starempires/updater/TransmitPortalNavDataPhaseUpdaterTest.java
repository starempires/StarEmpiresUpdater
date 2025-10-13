package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.objects.Empire;
import com.starempires.orders.OrderType;
import com.starempires.orders.TransmitOrder;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransmitPortalNavDataPhaseUpdaterTest extends BaseTest {

    private TransmitPortalNavDataPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new TransmitPortalNavDataPhaseUpdater(turnData);
    }

    @Test
    void update() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        empire1.addKnownPortal(portal);
        empire1.addNavData(portal);
        empire1.addKnownEmpire(empire2);
        final TransmitOrder order = TransmitOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSMIT)
                .parameters("portal to empire2")
                .portals(Lists.newArrayList(portal))
                .recipients(Lists.newArrayList(empire2))
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertTrue(empire2.getKnownPortals().contains(portal));
        assertTrue(empire2.hasNavData(portal));
    }

    @Test
    void updateNoNavData() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        empire1.addKnownEmpire(empire2);
        final TransmitOrder order = TransmitOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSMIT)
                .parameters("portal to empire2")
                .portals(Lists.newArrayList(portal))
                .recipients(Lists.newArrayList(empire2))
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertFalse(empire2.getKnownPortals().contains(portal));
        assertFalse(empire2.hasNavData(portal));
    }

    @Test
    void updateUnknownRecipient() {
        final Empire empire1 = createEmpire("empire1");
        final Empire empire2 = createEmpire("empire2");
        empire1.addKnownPortal(portal);
        empire1.addNavData(portal);
        final TransmitOrder order = TransmitOrder.builder()
                .empire(empire1)
                .orderType(OrderType.TRANSMIT)
                .parameters("portal to empire2")
                .portals(Lists.newArrayList(portal))
                .recipients(Lists.newArrayList(empire2))
                .build();
        turnData.addOrders(Lists.newArrayList(order));
        updater.update();
        assertFalse(empire2.getKnownPortals().contains(portal));
        assertFalse(empire2.hasNavData(portal));
    }
}