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

    public int getCost(final int guns, final int tonnage) {
        return (int)Math.round(Math.max(1, Math.exp(guns / (MISSILE_TONNAGE_COST * tonnage))));
    }

   private int computeComponent(final int delta, final int denominator) {
        if (delta == 0 || denominator == 0) {
            return 0;
        }

        // Compute exponential impact
        final double impact = Math.ceil(Math.exp(Math.abs(delta) / (double)denominator));

        // Positive delta increases cost, negative delta decreases cost
        return Math.toIntExact(delta > 0 ? Math.round(impact) : -Math.round(impact));
    }

    public int getCost(final int guns, final int dp, final int engines, final int scan, final int racks) {
        final int addGuns = guns - baseGuns;
        final int addDp = dp - baseDp;
        final int addEngines = engines - baseEngines;
        final int addScan = scan - baseScan;
        final int addRacks = racks - baseRacks;
        final int additionalCost =
                computeComponent(addGuns, costGuns) +
                computeComponent(addDp, costDp) +
                computeComponent(addEngines, costEngines) +
                computeComponent(addScan, costScan) +
                computeComponent(addRacks, costRacks);

      return Math.max(1, baseCost + additionalCost);
    }

    public int getTonnage(final int guns, final int dp, final int engines, final int scan, final int racks) {
        final int addGuns = guns - baseGuns;
        final int addDp = dp - baseDp;
        final int addEngines = engines - baseEngines;
        final int addScan = scan - baseScan;
        final int addRacks = racks - baseRacks;
        final int additionalTonnage =
                computeComponent(addGuns, tonnageGuns) +
                computeComponent(addDp, tonnageDp) +
                computeComponent(addEngines, tonnageEngines) +
                computeComponent(addScan, tonnageScan) +
                computeComponent(addRacks, tonnageRacks);
        return Math.max(1, baseTonnage + additionalTonnage);
    }

    @Override
    public String toString() {
        return Objects.toString(hullType);
    }
}