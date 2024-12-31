package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Storm;

import java.util.Collection;

public abstract class DissipateObjectPhaseUpdater extends PhaseUpdater {

    public DissipateObjectPhaseUpdater(final Phase phase, final TurnData turnData) {
        super(phase, turnData);
    }

    protected void removeStorms(final boolean nebulaeOnly) {
        final Collection<Storm> storms = turnData.getAllStorms();
        storms.forEach(storm -> {
            // TODO
        });
    }

}