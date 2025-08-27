package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;

import java.util.Collection;
import java.util.Set;

public class AcquireNavDataPhaseUpdater extends PhaseUpdater {

    public AcquireNavDataPhaseUpdater(final TurnData turnData) {
        super(Phase.ACQUIRE_NAV_DATA, turnData);
    }

    @Override
    public void update() {
        final Collection<Empire> empires = turnData.getAllEmpires();
        empires.forEach(empire -> {
            final Set<Portal> portalsTraversed = empire.getPortalsTraversed();
            portalsTraversed.forEach(portal -> {
                if (!empire.hasNavData(portal)) {
                    empire.addNavData(portal);
                    addNews(empire, "You have acquired navigation data for portal " + portal);
                }
            });
        });
    }
}