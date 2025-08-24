package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

/**
 * A nameable entity with a coordinate and a drift policy
 * 
 * @author john
 *
 */
@Getter
@NoArgsConstructor(force = true)
public abstract class MappableObject extends IdentifiableObject {

    @JsonIgnore
    @Setter
    protected Coordinate coordinate;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected final List<HexDirection> drift = Lists.newArrayList();

    protected MappableObject(final String name, final Coordinate coordinate) {
        super(name);
        this.coordinate = coordinate;
    }

    public int getOblique() {
        return coordinate.getOblique();
    }

    public int getY() {
        return coordinate.getY();
    }

    public int distanceTo(final Coordinate destination) {
        return coordinate.distanceTo(destination);
    }

    public int distanceTo(final @NonNull MappableObject object) {
        return distanceTo(object.coordinate);
    }

    public boolean isSameSector(final @NonNull MappableObject object) {
        return coordinate.equals(object.coordinate);
    }

    public void addDrift(final int step, final HexDirection direction) {
        drift.add(direction);
    }
}