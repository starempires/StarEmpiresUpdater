package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Ship;

import java.util.List;

public class ApplyStormDamagePhaseUpdater extends PhaseUpdater {

    public ApplyStormDamagePhaseUpdater(final TurnData turnData) {
        super(Phase.APPLY_STORM_DAMAGE, turnData);
    }

    @Override
    public void update() {
        final List<Ship> damagedShips = turnData.shipsStormDamagedThisTurn();
        damagedShips.forEach(ship -> {
            ship.applyStormDamageAccrued();
            if (!ship.isOneShot()) {
                final double opRating = Math.round(ship.getOperationRating() * 1000f) / 10f;
                addNews(ship.getOwner(), String.format("Ship %s now at %.1f%% OR ", ship, opRating));
            }
        });
    }
}