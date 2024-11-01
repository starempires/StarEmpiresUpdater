package com.starempires.phases;

import com.starempires.TurnData;

public class DetermineOwnershipIPhaseUpdater extends DetermineOwnershipPhaseUpdater {

    public DetermineOwnershipIPhaseUpdater(final TurnData turnData) {
        super(Phase.DETERMINE_OWNERSHIP_I, turnData);
    }
}