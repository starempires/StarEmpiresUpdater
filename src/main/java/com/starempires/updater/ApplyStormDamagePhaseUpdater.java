package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Ship;

import java.util.List;

public class ApplyStormDamagePhaseUpdater extends ApplyDamagePhaseUpdater {

    public ApplyStormDamagePhaseUpdater(final TurnData turnData) {
        super(Phase.APPLY_STORM_DAMAGE, turnData);
    }

    @Override
    public void update() {
        final List<Ship> damagedShips = turnData.shipsStormDamagedThisTurn();
        update(damagedShips, Ship::applyStormDamageAccrued, Ship::inflictStormDamage);
    }
}