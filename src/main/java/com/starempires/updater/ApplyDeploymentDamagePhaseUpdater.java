package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Ship;

import java.util.List;

public class ApplyDeploymentDamagePhaseUpdater extends ApplyDamagePhaseUpdater {

    public ApplyDeploymentDamagePhaseUpdater(final TurnData turnData) {
        super(Phase.APPLY_DEPLOYMENT_DAMAGE, turnData);
    }

    @Override
    public void update() {
        final List<Ship> damagedShips = turnData.shipsDeployedThisTurn();
        update(damagedShips, Ship::applyDeploymentDamage, Ship::inflictDeploymentDamage);
    }
}