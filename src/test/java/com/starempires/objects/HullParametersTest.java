package com.starempires.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class HullParametersTest {

    /**
     * Build a simple HullParameters with configurable base/cost denominators.
     * All other fields are set to neutral defaults so only the specific component under test contributes.
     */
    private HullParameters params(
            int baseCost,
            int baseTonnage,
            int baseGuns,
            int baseDp,
            int baseEngines,
            int baseScan,
            int baseRacks,
            int costGunsDen,
            int costDpDen,
            int costEnginesDen,
            int costScanDen,
            int costRacksDen,
            int tonnageGunsDen,
            int tonnageDpDen,
            int tonnageEnginesDen,
            int tonnageScanDen,
            int tonnageRacksDen
    ) {
        return HullParameters.builder()
                .hullType(null)
                .baseGuns(baseGuns)
                .baseDp(baseDp)
                .baseEngines(baseEngines)
                .baseScan(baseScan)
                .baseRacks(baseRacks)
                .baseTonnage(baseTonnage)
                .baseCost(baseCost)
                .maxGuns(999)
                .maxDp(999)
                .maxEngines(999)
                .maxScan(999)
                .maxRacks(999)
                .costGuns(costGunsDen)
                .costDp(costDpDen)
                .costEngines(costEnginesDen)
                .costScan(costScanDen)
                .costRacks(costRacksDen)
                .tonnageGuns(tonnageGunsDen)
                .tonnageDp(tonnageDpDen)
                .tonnageEngines(tonnageEnginesDen)
                .tonnageScan(tonnageScanDen)
                .tonnageRacks(tonnageRacksDen)
                .build();
    }

    // -----------------------------
    // getCost(int guns, int tonnage)
    // -----------------------------

    @Test
    void missileCost_minimumIsOne() {
        HullParameters hp = params(10, 20, 0, 0, 0, 0, 0,
                0,0,0,0,0, 0,0,0,0,0);
        // guns=0 => exp(0) = 1, max(1, 1) = 1, round(1) = 1
        assertEquals(1, hp.getCost(0, 5));
    }

    @Test
    void missileCost_roundingBehavior() {
        HullParameters hp = params(10, 20, 0, 0, 0, 0, 0,
                0,0,0,0,0, 0,0,0,0,0);
        // exp(0.2) ≈ 1.221..., round => 1
        assertEquals(1, hp.getCost(5, 5));
        // exp(1.0) ≈ 2.718..., round => 3
        assertEquals(3, hp.getCost(25, 5));
    }

    // ---------------------------------------------
    // getCost(int guns, int dp, int engines, int scan, int racks)
    // ---------------------------------------------

    @Test
    void multiCost_noChange_returnsBase() {
        HullParameters hp = params(10, 20, 2, 3, 4, 5, 6,
                1,1,1,1,1, 1,1,1,1,1);
        assertEquals(10, hp.getCost(2, 3, 4, 5, 6));
    }

    @Test
    void multiCost_increaseGuns_addsCeilExpImpact() {
        // Only guns should contribute (denominators for others are large but deltas=0 anyway)
        HullParameters hp = params(10, 20, 2, 3, 4, 5, 6,
                1, 1000, 1000, 1000, 1000,
                1, 1000, 1000, 1000, 1000);
        // delta(guns)=+1; denom=1 => ceil(exp(1))=3; round(3)=3
        assertEquals(13, hp.getCost(3, 3, 4, 5, 6));
    }

    @Test
    void multiCost_decreaseGuns_subtractsCeilExpImpact() {
        HullParameters hp = params(10, 20, 2, 3, 4, 5, 6,
                1, 1000, 1000, 1000, 1000,
                1, 1000, 1000, 1000, 1000);
        // delta(guns) = -1 => subtract 3
        assertEquals(7, hp.getCost(1, 3, 4, 5, 6));
    }

    @Test
    void multiCost_minBound_applies() {
        HullParameters hp = params(2, 20, 2, 3, 4, 5, 6,
                1, 1000, 1000, 1000, 1000,
                1, 1000, 1000, 1000, 1000);
        // baseCost=2; delta(guns)=-1 => 2 - 3 = -1, but Math.max(1, ...) => 1
        assertEquals(1, hp.getCost(1, 3, 4, 5, 6));
    }

    // -----------------------------
    // getTonnage(...)
    // -----------------------------

    @Test
    void tonnage_noChange_returnsBase() {
        HullParameters hp = params(10, 20, 2, 3, 4, 5, 6,
                1,1,1,1,1, 1,1,1,1,1);
        assertEquals(20, hp.getTonnage(2, 3, 4, 5, 6));
    }

    @Test
    void tonnage_increaseGuns_addsCeilExpImpact() {
        HullParameters hp = params(10, 20, 2, 3, 4, 5, 6,
                1000, 1000, 1000, 1000, 1000,
                1, 1000, 1000, 1000, 1000);
        // delta(guns)=+1, denom=1 => +3
        assertEquals(23, hp.getTonnage(3, 3, 4, 5, 6));
    }

    @Test
    void tonnage_decreaseGuns_subtractsCeilExpImpact_butMin1() {
        HullParameters hp = params(10, 2, 2, 3, 4, 5, 6,
                1000, 1000, 1000, 1000, 1000,
                1, 1000, 1000, 1000, 1000);
        // baseTonnage=2; delta(guns)=-1 => 2 - 3 = -1; Math.max(1, ...) => 1
        assertEquals(1, hp.getTonnage(1, 3, 4, 5, 6));
    }
}