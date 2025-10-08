package com.starempires.objects;

import lombok.Getter;

@Getter
public class SitRep {

    private final Empire empire;
    private final Coordinate coordinate;
    private int friendlyGuns;
    private int friendlyDp;
    private int enemyGuns;
    private int enemyDp;

    public SitRep(final Empire empire, final Coordinate coordinate) {
        this.empire = empire;
        this.coordinate = coordinate;
    }

    public void add(final Ship ship) {
        if (!ship.isConqueringShip()) {
            return;
        }

        if (ship.getOwner().equals(empire)) {
            friendlyGuns += ship.getAvailableGuns();
            friendlyDp += ship.getDpRemaining();
        }
        else {
            enemyGuns += ship.getAvailableGuns();
            enemyDp += ship.getDpRemaining();
        }
    }

    public double getDefensiveRatio() {
        return getFriendlyDp() / (double) getEnemyGuns();
    }

    public boolean isEnemyToFriendlyRatioExceeded(final double threshold) {
        return enemyGuns > threshold * friendlyDp;
    }
}