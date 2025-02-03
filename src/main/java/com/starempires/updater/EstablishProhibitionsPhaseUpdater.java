package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.OwnableObject;
import com.starempires.objects.Prohibition;
import com.starempires.objects.Ship;
import com.starempires.objects.SitRep;
import com.starempires.objects.World;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class EstablishProhibitionsPhaseUpdater extends PhaseUpdater {

    public static final float BLOCKADE_THRESHOLD = 2.0f;
    public static final float INTERDICTION_THRESHOLD = 3.0f;

    public EstablishProhibitionsPhaseUpdater(TurnData turnData) {
        super(Phase.ESTABLISH_BLOCKADES, turnData);
    }

    @Override
    public void update() {
        final List<World> worlds = Lists.newArrayList(turnData.getAllWorlds());
        worlds.sort(OwnableObject.OWNER_COMPARATOR);

        worlds.forEach(world -> {
            world.setProhibition(Prohibition.NONE); // clear existing prohibitions
            if (world.isOwned()) {
                final Set<Ship> starbases = turnData.getStarbases(world);
                if (CollectionUtils.isEmpty(starbases)) {
                    final SitRep sitRep = turnData.getSitRep(world.getOwner(), world);
                    final Collection<Empire> empires = turnData.getEmpiresPresent(world);
                    if (!world.isHomeworld() && sitRep.isEnemyToFriendlyRatioExceeded(INTERDICTION_THRESHOLD)) {
                        world.setProhibition(Prohibition.INTERDICTED);
                        addNews(empires, "World " + world + " is now interdicted.");
                    }
                    else if (sitRep.isEnemyToFriendlyRatioExceeded(BLOCKADE_THRESHOLD)) {
                        world.setProhibition(Prohibition.BLOCKADED);
                        addNews(empires, "World " + world + " is now blockaded.");
                    }
                } // else starbases prevent prohibitions
            }
        });
    }
}