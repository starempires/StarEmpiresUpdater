package com.starempires.updater;

import com.starempires.objects.World;
import com.starempires.util.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProduceResourceUnitsPhaseUpdaterTest extends BaseTest {

    private ProduceResourceUnitsPhaseUpdater updater;
    private World world;

    @BeforeEach
    void setUp() {
        world = createWorld("world", ZERO_COORDINATE, 10);
        world.setOwner(empire1);
        updater = new ProduceResourceUnitsPhaseUpdater(turnData);
    }

    @Test
    void update() {
        updater.update();
        assertEquals(10, world.getStockpile());
    }

    @Test
    void updateMultiplier() {
        world.setProductionMultiplier(2);
        updater.update();
        assertEquals(20, world.getStockpile());
        assertEquals(1.0, world.getProductionMultiplier());
    }
}