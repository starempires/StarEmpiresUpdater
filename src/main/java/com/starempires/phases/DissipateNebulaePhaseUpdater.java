package com.starempires.phases;

import com.starempires.TurnData;

public class DissipateNebulaePhaseUpdater extends DissipateObjectPhaseUpdater {

    public DissipateNebulaePhaseUpdater(final TurnData turnData) {
        super(Phase.DISSIPATE_NEBULAE, turnData);
    }

    @Override
    public void update() {
        removeStorms(true);
    }
}