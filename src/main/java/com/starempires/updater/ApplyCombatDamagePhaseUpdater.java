package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Ship;

import java.util.List;

public class ApplyCombatDamagePhaseUpdater extends ApplyDamagePhaseUpdater {

    public ApplyCombatDamagePhaseUpdater(final TurnData turnData) {
        super(Phase.APPLY_COMBAT_DAMAGE, turnData);
    }

    @Override
    public void update() {
        final List<Ship> damagedShips = turnData.shipsCombatDamagedThisTurn();
        update(damagedShips, Ship::applyCombatDamageAccrued);
    }
}