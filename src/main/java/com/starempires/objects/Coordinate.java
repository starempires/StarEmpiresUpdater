package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a map coordinate with (oblique, y) values.
 * 
 * @author john
 *
 */
@Log4j2
@AllArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode
public class Coordinate implements Comparable<Coordinate> {

    public static final CoordinateComparator COORDINATE_COMPARATOR = new CoordinateComparator();

    public static class CoordinateComparator implements Comparator<Coordinate> {
        @Override
        public int compare(@NonNull final Coordinate c1, @NonNull final Coordinate c2) {
            return ObjectUtils.compare(c1, c2);
        }
    }

    public static class CoordinateMultimapSerializer extends JsonSerializer<Multimap<Coordinate, ? extends Coordinate>> {
        @Override
        public void serialize(Multimap<Coordinate, ? extends Coordinate> objects, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
            final Map<String, List<String>> map = objects.asMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().getName(),
                            entry -> entry.getValue().stream().map(Coordinate::getName).toList()
                    ));
            gen.writeObject(map);
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Multimap<Coordinate, ? extends Coordinate> value) {
            return (value == null || value.isEmpty());
        }
    }

    static class EmpireCoordinateMultimapSerializer extends JsonSerializer<Multimap<Empire, ? extends Coordinate>> {
        @Override
        public void serialize(Multimap<Empire, ? extends Coordinate> objects, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
            final Map<Empire, List<? extends Coordinate>> map = objects.asMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> List.copyOf(entry.getValue()) // Convert Collection to List
                    ));
            gen.writeObject(map);
        }

        @Override
        public boolean isEmpty(SerializerProvider provider, Multimap<Empire, ? extends Coordinate> value) {
            return (value == null || value.isEmpty());
        }
    }

    static class DeferredEmpireCoordinateMultimapDeserializer extends JsonDeserializer<Multimap<Empire, ? extends Coordinate>> {
        @Override
        public Multimap<Empire, ? extends Coordinate> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException, IOException {
            return HashMultimap.create();
        }
    }

    /** oblique (northwest-southeast) value for this coordinate */
    @JsonInclude(Include.ALWAYS)
    private int oblique;
    /** y (north-south) value for this coordinate */
    @JsonInclude(Include.ALWAYS)
    private int y;

    public Coordinate(final Coordinate coordinate) {
        this(coordinate.getOblique(), coordinate.getY());
    }

    @Override
    public int compareTo(final @NonNull Coordinate coordinate) {
        int rv = oblique - coordinate.oblique;
        if (rv == 0) {
            rv = y - coordinate.y;
        }
        return rv;
    }

    @Override
    public String toString() {
        return "(" + oblique + "," + y + ")";
    }

    @JsonIgnore
    public String getName() {
        return (oblique + "_" + y).replace("-", "n");
    }

    public static @NonNull Coordinate delta(final @NonNull Coordinate source, final @NonNull Coordinate destination) {
        final int deltaOblique = destination.getOblique() - source.getOblique();
        final int deltaY = destination.getY() - source.getY();
        return new Coordinate(deltaOblique, deltaY);
    }

    public static int distance(final Coordinate source, final Coordinate destination) {
        final int distance;
        final Coordinate delta = delta(source, destination);
        final int deltaOblique = delta.getOblique();
        final int deltaY = delta.getY();
        if (Math.signum(deltaOblique) == Math.signum(deltaY)) {
            distance = Math.max(Math.abs(deltaOblique), Math.abs(deltaY));
        }
        else {
            distance = Math.abs(deltaOblique) + Math.abs(deltaY);
        }
        return distance;
    }

    public int distanceTo(Coordinate destination) {
        return distance(this, destination);
    }

    public static List<Coordinate> parse(List<String> texts) {
        final List<Coordinate> coordinates;
        if (CollectionUtils.isEmpty(texts)) {
            coordinates = Collections.emptyList();
        }
        else {
            coordinates = texts.stream().map(Coordinate::parse).collect(Collectors.toList());
        }
        return coordinates;
    }

    /**
     * Parse text of the form "(oblique,y)" into a Coordinate.
     * 
     * @param text
     *            The text to parse
     * @return The parsed Coordinate or null if blank input or unable to parse
     */
    public static Coordinate parse(final String text) {
        Coordinate rv = null;
        if (StringUtils.isNotBlank(text)) {
            final String values = text.replace("(", "").replace(")", "").replace(" ", "");
            final String[] tokens = values.split(",");
            if (tokens.length == 2) {
                final int oblique = Integer.parseInt(tokens[0]);
                final int y = Integer.parseInt(tokens[1]);
                rv = new Coordinate(oblique, y);
            }
        }
        return rv;
    }

    public static @NonNull Set<Coordinate> getSurroundingCoordinatesWithoutOrigin(final @NonNull MappableObject object, final int radius) {
        return getSurroundingCoordinatesWithoutOrigin(object.getCoordinate(), radius);
    }

    public static @NonNull Set<Coordinate> getSurroundingCoordinatesWithoutOrigin(final @NonNull Coordinate coordinate, final int radius) {
        final Set<Coordinate> coordinates = getSurroundingCoordinates(coordinate, radius);
        coordinates.remove(coordinate);
        return coordinates;
    }

    public static @NonNull Set<Coordinate> getSurroundingCoordinates(final @NonNull MappableObject object, final int radius) {
        return getSurroundingCoordinates(object.getCoordinate(), radius);
    }

    /**
     * Return Set of Coordinates within given radius, inclusive.
     *
     * @param coordinate
     *            The Coordinate at the center of the Set
     * @param radius
     *            The radius out from the center to gather Coordinates
     * @return The Set of Coordinates within the given radius of the center
     */
    public static @NonNull Set<Coordinate> getSurroundingCoordinates(final Coordinate coordinate, final int radius) {
        final Set<Coordinate> coordinates = Sets.newHashSet();
        for (int y = radius; y >= 0; y--) {
            for (int oblique = y - radius; oblique <= radius; oblique++) {
                final Coordinate c = new Coordinate(oblique + coordinate.getOblique(), y + coordinate.getY());
                coordinates.add(c);
            }
        }

        for (int y = -1; y >= -radius; y--) {
            for (int oblique = -radius; oblique <= radius + y; oblique++) {
                final Coordinate c = new Coordinate(oblique + coordinate.getOblique(), y + coordinate.getY());
                coordinates.add(c);
            }
        }
        return coordinates;
    }

    /**
     * Return List of Coordinates in the hollow "ring" out from this Coordinate at the
     * given distance. The number of Coordinates in the ring will always be the distance * 6.
     *
     * @param distance
     *            The distance out from a center (0,0) Coordinate
     * @return The List of Coordinates in the given ring
     */
    public static List<Coordinate> getSurroundingRing(final int distance) {
        final int capacity = distance * 6;
        final List<Coordinate> rv;
        if (distance > 0) {
            final Coordinate[] coordinates = new Coordinate[capacity + 1];
            for (int i = 0; i <= distance; i++) {
                coordinates[i] = new Coordinate(i, distance);
                coordinates[distance + i] = new Coordinate(distance, distance - i);
                coordinates[2 * distance + i] = new Coordinate(distance - i, -i);
                coordinates[3 * distance + i] = new Coordinate(-i, -distance);
                coordinates[4 * distance + i] = new Coordinate(-distance, -distance + i);
                coordinates[5 * distance + i] = new Coordinate(-distance + i, i);
            }
            rv = Lists.newArrayList(coordinates);
            rv.remove(rv.size() - 1); // last element is the same as first element
        }
        else {
            rv = Collections.emptyList();
        }
        return rv;
    }

    /**
     * Translate this Coordinate by the given Coordinate.
     *
     * @param coordinate
     *            The Coordinate to translate by
     * @return This Coordinate
     */
    public Coordinate translate(final @NonNull Coordinate coordinate) {
        oblique += coordinate.getOblique();
        y += coordinate.getY();
        return this;
    }

    /**
     * Rotate this Coordinate by the given HexDirection
     *
     * @param rotation
     *            The rotation to apply
     * @return This Coordinate rotated
     */
    public Coordinate rotate(final @NonNull HexDirection rotation) {
        final int rotatedOblique;
        final int rotatedY = switch (rotation) {
            case DIRECTION_0 -> {
                rotatedOblique = oblique;
                yield y;
            }
            case DIRECTION_60 -> {
                rotatedOblique = y;
                yield y - oblique;
            }
            case DIRECTION_120 -> {
                rotatedOblique = y - oblique;
                yield -oblique;
            }
            case DIRECTION_180 -> {
                rotatedOblique = -oblique;
                yield -y;
            }
            case DIRECTION_240 -> {
                rotatedOblique = -y;
                yield oblique - y;
            }
            case DIRECTION_300 -> {
                rotatedOblique = oblique - y;
                yield oblique;
            }
        };

        oblique = rotatedOblique;
        y = rotatedY;
        return this;
    }

    /**
     * Mirror this Coordinate across the y axis.
     *
     * @return This Coordinate mirrored
     */
    public Coordinate horizontalMirror() {
        y = oblique - y;
        return this;
    }

    /**
     * Mirror this Coordinate across the oblique axis.
     *
     * @return This Coordinate mirrored
     */
    public Coordinate verticalMirror() {
        final int mirrorOblique = -oblique;
        final int mirrorY = y - oblique;
        oblique = mirrorOblique;
        y = mirrorY;
        return this;
    }

    /**
     * Negate this Coordinate across the oblique and y axes
     *
     * @return This Coordinate negated
     */
    public static @NonNull Coordinate negate(Coordinate coordinate) {
        final Coordinate negative = new Coordinate(coordinate);
        negative.oblique = -negative.oblique;
        negative.y = -negative.y;
        return negative;
    }

    public boolean isBeyondMinDistanceToObjects(final @NotNull Collection<? extends MappableObject> objects, final int minDistance) {
        return objects.stream().map(MappableObject::getCoordinate).allMatch(c -> this.distanceTo(c) >= minDistance);
    }
}