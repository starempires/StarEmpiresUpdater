package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class World extends OwnableObject {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private final int production;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int stockpile;
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = Prohibition.ProhibitionNoneFilter.class)
    private Prohibition prohibition = Prohibition.NONE;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean homeworld;
    @JsonInclude
    private double productionMultiplier = 1.0;

    @Builder
    private World(final String name, final Coordinate coordinate, final Empire owner,
                  final int production, final int stockpile, final Prohibition prohibition, final boolean homeworld) {
        super(name, coordinate, owner);
        this.production = production;
        this.stockpile = stockpile;
        this.prohibition = prohibition;
        this.homeworld = homeworld;
    }

    @JsonCreator
    private World(@JsonProperty("name") final String name,
                  @JsonProperty("oblique") final int oblique,
                  @JsonProperty("y") final int y,
                  @JsonProperty("production") final int production,
                  @JsonProperty("stockpile") final int stockpile,
                  @JsonProperty("prohibition") final Prohibition prohibition,
                  @JsonProperty("productionMultiplier") final double productionMultiplier,
                  @JsonProperty("homeworld") final boolean homeworld) {
        this(name, new Coordinate(oblique, y), null, production, stockpile, prohibition, homeworld);
        this.productionMultiplier = productionMultiplier;
    }

    @JsonIgnore
    public boolean isBlockaded() {
        return prohibition == Prohibition.BLOCKADED || isInterdicted();
    }

    @JsonIgnore
    public boolean isInterdicted() {
        return prohibition == Prohibition.INTERDICTED;
    }

    public int adjustStockpile(final int amount) {
        return stockpile += amount;
    }

    @JsonIgnore
    public boolean isOwned() {
        return owner != null;
    }
}