package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

@Getter
@EqualsAndHashCode(callSuper = true)
public class RadialCoordinate extends Coordinate {

    private final int radius;

    @JsonCreator
    public RadialCoordinate(@JsonProperty("oblique") final int oblique,
                            @JsonProperty("y") final int y,
                            @JsonProperty("radius") final int radius) {
        super(oblique, y);
        this.radius = radius;
    }

    public RadialCoordinate(final Coordinate coordinate, final int radius) {
        this(coordinate.getOblique(), coordinate.getY(), radius);
    }

    public static Set<Coordinate> getSurroundingCoordinates(final RadialCoordinate coordinate) {
        return getSurroundingCoordinates(coordinate, coordinate.getRadius());
    }
}