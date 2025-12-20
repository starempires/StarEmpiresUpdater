package com.starempires.updater;

import com.starempires.objects.Ship;
import com.starempires.orders.ModifyShipOrder;
import com.starempires.orders.OrderType;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModifyShipsPhaseUpdaterTest extends BaseTest {

    private ModifyShipsPhaseUpdater updater;

    @BeforeEach
    public void setUp() {
        updater = new ModifyShipsPhaseUpdater(turnData);
    }

    @Test
    public void update() {
        final Ship frigate = createShip(frigateClass, ZERO_COORDINATE, "frigate", empire1);
        final ModifyShipOrder order = ModifyShipOrder.builder()
                .orderType(OrderType.MODIFYSHIP)
                .gmOnly(true)
                .ship(frigate)
                .dp(2)
                .publicMode(true)
                .owner(empire1)
                .empire(gm)
                .build();
        turnData.addOrder(order);
        updater.update();
        assertEquals(2, frigate.getDpRemaining());
        assertTrue(frigate.isPublicTransponder());

    }
}