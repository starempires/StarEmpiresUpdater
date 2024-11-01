package com.starempires.phases;

import com.starempires.TurnData;

public class DissipateStormsPhaseUpdater extends DissipateObjectPhaseUpdater {

    public DissipateStormsPhaseUpdater(final TurnData turnData) {
        super(Phase.DISSIPATE_STORMS, turnData);
    }

    @Override
    public void update() {
        removeStorms(false);
    }
}