package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.starempires.objects.Empire;
import com.starempires.objects.Prohibition;
import com.starempires.objects.World;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@JsonInclude(Include.NON_DEFAULT)
@SuperBuilder
@Getter
@JsonPropertyOrder(alphabetic = true)
public class WorldSnapshot extends OwnableObjectSnapshot {

    private final int production;
    private final int stockpile;
    private final Prohibition prohibition;
    private final boolean homeworld;

    public static WorldSnapshot fromWorld(final World world, final Empire empire) {
        if (world == null) {
            return null;
        }
        if (empire.isGM()) {
            return forGM(world);
        }
        int stockpile = 0;
        Prohibition prohibition = null;
        boolean homeworld = false;
        String owner = null;
        int production = 0;

        switch (empire.getScanStatus(world)) {
            case VISIBLE:
                stockpile = world.getStockpile();
                if (world.getProhibition() != Prohibition.NONE) {
                    prohibition = world.getProhibition();
                }
                // intentional fall-through
            case SCANNED:
                homeworld = world.isHomeworld();
                // intentional fall-through
            case STALE:
                if (world.isOwned()) {
                    owner = world.getOwner().getName(); // most recent known
                }
                production = world.getProduction();
                // intentional fall-through
            default: // unknown
        }
        return WorldSnapshot.builder()
                .name(world.getName())
                .owner(owner)
                .production(production)
                .stockpile(stockpile)
                .prohibition(prohibition)
                .homeworld(homeworld)
                .build();
    }

    public static WorldSnapshot forGM(final World world) {
        if (world == null) {
            return null;
        }
        final int stockpile = world.getStockpile();
        final Prohibition prohibition = world.getProhibition() != Prohibition.NONE ? world.getProhibition() : null;
        final boolean homeworld = world.isHomeworld();
        final String owner = world.isOwned() ? world.getOwner().getName() : null;
        final int production = world.getProduction();
        return WorldSnapshot.builder()
                .name(world.getName())
                .owner(owner)
                .production(production)
                .stockpile(stockpile)
                .prohibition(prohibition)
                .homeworld(homeworld)
                .build();
    }
}