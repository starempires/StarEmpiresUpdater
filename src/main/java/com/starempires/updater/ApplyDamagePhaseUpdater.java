package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;

import java.util.List;

public abstract class ApplyDamagePhaseUpdater extends PhaseUpdater {

    private final ShipCondition destroyedCondition;

    protected ApplyDamagePhaseUpdater(final ShipCondition destroyedCondition, final Phase phase, final TurnData turnData) {
        super(phase, turnData);
        this.destroyedCondition = destroyedCondition;
    }

    @Override
    public void update() {
        final List<Ship> damagedShips = turnData.shipsDamagedThisTurn();
        damagedShips.forEach(ship -> {
            ship.applyDamageAccrued(ShipCondition.DESTROYED_IN_COMBAT);
            if (!ship.isOneShot()) {
                final double opRating = Math.round(ship.getOperationRating() * 1000f) / 10f;
                addNews(ship.getOwner(), String.format("Ship %s now at %f%% OR ", ship, opRating));
                ship.applyDamageAccrued(destroyedCondition);
            }
        });
    }

}