package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.SitRep;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.starempires.objects.IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR;

public class SalvageDesignsPhaseUpdater extends PhaseUpdater {

    public static final float SALVAGE_THRESHOLD = 3.f;

    public SalvageDesignsPhaseUpdater(final TurnData turnData) {
        super(Phase.SALVAGE_DESIGNS, turnData);
    }

    @Override
    public void update() {
        final List<Ship> possibleSalvages = Lists.newArrayList(turnData.getPossibleSalvages());
        possibleSalvages.sort(IDENTIFIABLE_NAME_COMPARATOR);
        possibleSalvages.stream().filter(Ship::isSalvageable).forEach(ship -> {
            final Collection<Empire> empires = turnData.getEmpiresPresent(ship);
            final List<SitRep> sitReps = empires.stream()
                    .map(empire -> turnData.getSitRep(empire, ship.getCoordinate()))
                    .toList();
            SitRep winner = sitReps.stream()
                    .collect(Collectors.groupingBy(SitRep::getFriendlyGuns))
                    .entrySet().stream()
                    .max(Comparator.comparingInt(Map.Entry::getKey))
                    .filter(entry -> entry.getValue().size() == 1)
                    .map(entry -> entry.getValue().get(0))
                    .orElse(null);
            if (winner != null) {
                final int salvageDp = sitReps.stream()
                        .filter(sitRep -> sitRep.getEmpire().equals(ship.getOwner()))
                        .mapToInt(SitRep::getFriendlyDp)
                        .max()
                        .orElse(0);
                if (winner.getFriendlyGuns() > SALVAGE_THRESHOLD * salvageDp) {
                    final Empire salvagingEmpire = winner.getEmpire();
                    final ShipClass shipClass = ship.getShipClass();
                    if (!salvagingEmpire.isKnownShipClass(shipClass)) {
                        salvagingEmpire.addKnownShipClass(shipClass);
                        addNews(salvagingEmpire, "You salvaged the design for the %s ship class %s from the debris of ship %s"
                                .formatted(ship.getOwner(), ship.getShipClass(), ship));
                        final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(ship);
                        newsEmpires.remove(salvagingEmpire);
                        addNews(newsEmpires, "%s salvaged the design for the %s ship class %s from the debris of ship %s"
                                .formatted(salvagingEmpire, ship.getOwner(), ship.getShipClass(), ship));
                    }
                }
            }
        });
    }
}