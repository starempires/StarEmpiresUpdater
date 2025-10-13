package com.starempires.updater;

import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProduceResourceUnitsPhaseUpdaterTest extends BaseTest {

    private ProduceResourceUnitsPhaseUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new ProduceResourceUnitsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        updater.update();
        assertEquals(24, world.getStockpile());
    }

    @Test
    void updateMultiplier() {
        world.setProductionMultiplier(2);
        updater.update();
        assertEquals(36, world.getStockpile());
        assertEquals(1.0, world.getProductionMultiplier());
    }
}