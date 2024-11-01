package com.starempires.phases;

import com.starempires.TurnData;
import com.starempires.constants.Constants;
import com.starempires.objects.Empire;
import com.starempires.objects.Prohibition;
import com.starempires.objects.Ship;
import com.starempires.objects.SitRep;
import com.starempires.objects.World;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Set;

public class EstablishProhibitionsPhaseUpdater extends PhaseUpdater {

    public EstablishProhibitionsPhaseUpdater(TurnData turnData) {
        super(Phase.ESTABLISH_BLOCKADES, turnData);
    }

    @Override
    public void update() {
        final Collection<World> worlds = turnData.getAllWorlds();
        final double blockadeThreashold = turnData.getDoubleParameter(Constants.PARAMETER_BLOCKADE_THRESHOLD,
                Constants.DEFAULT_BLOCKADE_THRESHOLD);
        final double interdictionThreshold = turnData.getDoubleParameter(Constants.PARAMETER_INTERDICTION_THRESHOLD,
                Constants.DEFAULT_INTERDICTION_THRESHOLD);

        worlds.forEach(world -> {
            if (world.isOwned()) {
                final Set<Ship> starbases = turnData.getStarbases(world);
                if (CollectionUtils.isEmpty(starbases)) {
                    final SitRep sitRep = turnData.getSitRep(world.getOwner(), world);
                    final Collection<Empire> empires = turnData.getEmpiresPresent(world);
                    if (!world.isHomeworld() && sitRep.isEnemyToFriendlyRatioExceeded(interdictionThreshold)) {
                        world.setProhibition(Prohibition.INTERDICTED);
                        addNews(empires, "World " + world + " is now interdicted.");
                    }
                    else if (sitRep.isEnemyToFriendlyRatioExceeded(blockadeThreashold)) {
                        world.setProhibition(Prohibition.BLOCKADED);
                        addNews(empires, "World " + world + " is now blockaded.");
                    }
                } // else starbases prevent prohibitions
            }
        });
    }
}