package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipCondition;

import java.util.List;

public class ApplyCombatDamagePhaseUpdater extends PhaseUpdater {

    public ApplyCombatDamagePhaseUpdater(final TurnData turnData) {
        super(Phase.APPLY_COMBAT_DAMAGE, turnData);
    }

    @Override
    public void update() {
        final List<Ship> damagedShips = turnData.shipsCombatDamagedThisTurn();
        damagedShips.forEach(ship -> {
            ship.applyCombatDamageAccrued(ShipCondition.DESTROYED_IN_COMBAT);
            if (!ship.isOneShot()) {
                final double opRating = Math.round(ship.getOperationRating() * 1000f) / 10f;
                addNews(ship.getOwner(), String.format("Ship %s now at %.1f%% OR ", ship, opRating));
            }
        });
    }
}