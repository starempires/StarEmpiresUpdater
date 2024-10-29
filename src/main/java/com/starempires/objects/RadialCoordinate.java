package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@Getter
public class RadialCoordinate extends Coordinate {

    private final int radius;

    @JsonCreator
    public RadialCoordinate(@JsonProperty("oblique") final int oblique,
                            @JsonProperty("y") final int y,
                            @JsonProperty("radius") final int radius) {
        super(oblique, y);
        this.radius = radius;
    }

    public static Set<Coordinate> getSurroundingCoordinates(final RadialCoordinate coordinate) {
        return getSurroundingCoordinates(coordinate, coordinate.getRadius());
    }

    /**
     * Parse text of the form "(oblique,y,radius)" into a Coordinate.
     * 
     * @param text
     *            The text to parse
     * @return The parsed Coordinate or null if blank input or unable to parse
     */
    public static RadialCoordinate parseRadial(final String text) {
        RadialCoordinate rv = null;
        if (StringUtils.isNotBlank(text)) {
            final String values = text.replace("(", "").replace(")", "").replace(" ", "");
            final String[] tokens = values.split(",");
            if (tokens.length == 3) {
                final int oblique = Integer.parseInt(tokens[0]);
                final int y = Integer.parseInt(tokens[1]);
                final int radius = Integer.parseInt(tokens[2]);
                rv = new RadialCoordinate(oblique, y, radius);
            }
        }
        return rv;
    }
}