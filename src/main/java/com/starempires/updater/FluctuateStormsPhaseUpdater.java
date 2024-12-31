package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Storm;

import java.util.Collection;

public class FluctuateStormsPhaseUpdater extends PhaseUpdater {

    public FluctuateStormsPhaseUpdater(TurnData turnData) {
        super(Phase.FLUCTUATE_STORMS, turnData);
    }

    @Override
    public void update() {
        final Collection<Storm> storms = turnData.getAllStorms();
        storms.forEach(storm -> {
            final int fluctuation = storm.getFluctuation(turnData.getTurnNumber());
            if (fluctuation > 0) {
                final int rating = storm.getRating() + fluctuation;
                storm.setRating(rating);
                final Collection<Empire> empires = turnData.getEmpiresPresent(storm);
                addNews(empires, "Rating for storm %s fluctuates %d to %d".formatted(storm, fluctuation, rating));
            }
        });
    }
}