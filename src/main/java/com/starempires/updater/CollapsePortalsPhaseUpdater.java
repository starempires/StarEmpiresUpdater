package com.starempires.updater;

import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CollapsePortalsPhaseUpdater extends PhaseUpdater {

    public CollapsePortalsPhaseUpdater(final TurnData turnData) {
        super(Phase.COLLAPSE_PORTALS, turnData);
    }

    @Override
    public void update() {
        final TurnData turnData = getTurnData();
        final Set<Portal> portals = turnData
                .getDeployedDevices().values().stream()
                .filter(Ship::isPortalHammer)
                .map(Ship::getCoordinate)
                .map(turnData::getPortals)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        portals.forEach(portal -> {
            final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(portal);
            portal.setCollapsed(true);
            newsEmpires.forEach(newsEmpire -> {
                addNews(newsEmpire, "Portal %s has collapsed".formatted(portal));
            });
        });
    }
}