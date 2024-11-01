package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;

import java.util.Collection;

public class StabilizePortalsPhaseUpdater extends PhaseUpdater {

    public StabilizePortalsPhaseUpdater(final TurnData turnData) {
        super(Phase.STABILIZE_PORTALS, turnData);
    }

    @Override
    public void update() {
        final Collection<Portal> portals = turnData.getAllPortals();
        portals.forEach(portal -> {
            if (portal.isCollapsed()) {
                portal.setCollapsed(false);
                final Collection<Empire> empires = turnData.getEmpiresPresent(portal);
                addNews(empires, "Portal " + portal + " has stabilized");
            }
        });
    }
}