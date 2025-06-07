package com.starempires.objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class HullParameters {

    public static final float MISSILE_TONNAGE_COST = 5.0f;

    private final HullType hullType;
    private final double arMultiplier;
    private final int baseGuns;
    private final int baseDp;
    private final int baseEngines;
    private final int baseScan;
    private final int baseRacks;
    private final int baseTonnage;
    private final int baseCost;
    private final int maxGuns;
    private final int maxDp;
    private final int maxEngines;
    private final int maxScan;
    private final int maxRacks;
    private final int costGuns;
    private final int costDp;
    private final int costEngines;
    private final int costScan;
    private final int costRacks;
    private final int tonnageGuns;
    private final int tonnageDp;
    private final int tonnageEngines;
    private final int tonnageScan;
    private final int tonnageRacks;

    private int exp(final int value, final double divisor) {
        return (int) Math.ceil(Math.exp(value / divisor));
    }

    public int getCost(final int guns, final int tonnage) {
        return (int) Math.round(Math.max(1, Math.exp(guns / (MISSILE_TONNAGE_COST * tonnage))));
    }

    public int getCost(final int guns, final int dp, final int engines, final int scan, final int racks) {
        return exp(guns, costGuns) +
               exp(dp, costDp) +
               exp(engines, costEngines) +
               exp(scan, costScan) +
               exp(racks, costRacks);
    }

    public int getTonnage(final int guns, final int dp, final int engines, final int scan, final int racks) {
        return exp(guns, tonnageGuns) +
                exp(dp, tonnageDp) +
                exp(engines, tonnageEngines) +
                exp(scan, tonnageScan) +
                exp(racks, tonnageRacks);
    }

    @Override
    public String toString() {
        return Objects.toString(hullType);
    }
}