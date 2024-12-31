package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.ShipCondition;

public class ApplyCombatDamagePhaseUpdater extends ApplyDamagePhaseUpdater {

    public ApplyCombatDamagePhaseUpdater(final TurnData turnData) {
        super(ShipCondition.DESTROYED_IN_COMBAT, Phase.APPLY_COMBAT_DAMAGE, turnData);
    }
}