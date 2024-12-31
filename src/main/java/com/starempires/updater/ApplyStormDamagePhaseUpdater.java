package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.ShipCondition;

public class ApplyStormDamagePhaseUpdater extends ApplyDamagePhaseUpdater {

    public ApplyStormDamagePhaseUpdater(final TurnData turnData) {
        super(ShipCondition.DESTROYED_BY_STORM, Phase.APPLY_COMBAT_DAMAGE, turnData);
    }
}