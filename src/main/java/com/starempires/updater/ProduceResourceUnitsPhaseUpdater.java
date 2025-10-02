package com.starempires.updater;

import com.google.common.collect.Lists;
import com.starempires.TurnData;
import com.starempires.objects.Empire;
import com.starempires.objects.World;

import java.util.Collection;
import java.util.List;

import static com.starempires.objects.IdentifiableObject.IDENTIFIABLE_NAME_COMPARATOR;

public class ProduceResourceUnitsPhaseUpdater extends PhaseUpdater {

    public ProduceResourceUnitsPhaseUpdater(final TurnData turnData) {
        super(Phase.PRODUCE_RESOURCE_UNITS, turnData);
    }

    @Override
    public void update() {
        final List<World> worlds = Lists.newArrayList(turnData.getAllWorlds());
        worlds.sort(IDENTIFIABLE_NAME_COMPARATOR);
        worlds.stream().filter(World::isOwned).forEach(world -> {
            final Collection<Empire> newsEmpires = turnData.getEmpiresPresent(world);
            final double multiplier = world.getProductionMultiplier();
            final int production = world.getProduction();
            final int amount = (int) Math.ceil(production * multiplier);
            world.adjustStockpile(Math.max(amount, 0));
            world.setProductionMultiplier(1.0); // reset any production modifiers after production

            String text = "World " + world + " produced " + amount + " RU";
            if (multiplier != 1.0) {
                text += " (" + production + " x " + multiplier + ")";
            }
            text += " (stockpile now " + world.getStockpile() + " RU)";
            addNews(newsEmpires, text);
        });
    }
}