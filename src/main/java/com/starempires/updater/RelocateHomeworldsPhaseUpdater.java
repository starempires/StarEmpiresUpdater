package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.SitRep;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RelocateHomeworldsPhaseUpdater extends PhaseUpdater {

    static final class HomeworldComparator implements Comparator<World> {

        private static final Random RANDOM = ThreadLocalRandom.current();
        private final TurnData turnData;
        private final Empire empire;

        public HomeworldComparator(final Empire empire, TurnData turnData) {
            this.empire = empire;
            this.turnData = turnData;
        }

        @Override
        public int compare(final World world1, final World world2) {
            SitRep sitRep1 = turnData.getSitRep(empire, world1);
            SitRep sitRep2 = turnData.getSitRep(empire, world2);
            int rv = Double.compare(sitRep2.getDefensiveRatio(), sitRep1.getDefensiveRatio());
            if (rv == 0) {
                rv = sitRep2.getFriendlyDp() - sitRep1.getFriendlyDp();
                if (rv == 0) {
                    rv = world2.getProduction() - world1.getProduction();
                    if (rv == 0) {
                        World homeworld = turnData.getHomeworld(empire);
                        rv = homeworld.distanceTo(world2) - homeworld.distanceTo(world1);
                        if (rv == 0) {
                            rv = RANDOM.nextInt() - RANDOM.nextInt();
                        }
                    }
                }
            }
            return rv;
        }
    }

    public RelocateHomeworldsPhaseUpdater(final TurnData turnData) {
        super(Phase.RELOCATE_HOMEWORLDS, turnData);
    }

    private void relocateHomeworld(final Empire empire) {
        final List<World> ownedWorlds = Lists.newArrayList(turnData.getOwnedWorlds(empire));
        if (ownedWorlds.isEmpty()) {
            turnData.setHomeworld(empire, null);
            addNews(empire, "You have no homeworld");
        }
        else {
            ownedWorlds.sort(new HomeworldComparator(empire, turnData));
            final World homeworld = ownedWorlds.get(0);
            turnData.setHomeworld(empire, homeworld);
            homeworld.setProductionMultiplier(2.0); //
            final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(homeworld);
            newsEmpires.remove(empire);
            addNews(empire, "You have relocated your homeworld to " + homeworld);
            addNews(newsEmpires, "World %s is now the %s homeworld".formatted(homeworld, empire.getName()));
        }
    }

    @Override
    public void update() {
        final Set<Empire> empires = turnData.getActiveEmpires();
        empires.forEach(empire -> {
            final World world = turnData.getHomeworld(empire);
            if (world == null || !world.isOwnedBy(empire)) {
                relocateHomeworld(empire);
            }
        });
    }
}