package com.starempires.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.starempires.objects.Coordinate;
import com.starempires.objects.ShipClass;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Getter
@JsonInclude(Include.NON_EMPTY)
@SuperBuilder
@JsonPropertyOrder(alphabetic = true)
public class EmpireSnapshot extends IdentifiableObjectSnapshot {

    @JsonProperty("session")
    final private String session;
    @JsonProperty("abbreviation")
    final private String abbreviation;
    @JsonProperty("radius")
    final private int radius;
    @JsonProperty("rows")
    final private int rows;
    @JsonProperty("columns")
    final private int columns;
    @JsonInclude(Include.ALWAYS)
    @JsonProperty("turnNumber")
    final private int turnNumber;
    @JsonProperty("sectors")
    @JsonSerialize(using = SetToMapSerializer.class)
    final private Set<SectorSnapshot> sectors = Sets.newHashSet();
    @JsonProperty("shipClasses")
    final private Set<ShipClass> knownShipClasses = Sets.newHashSet();
    @JsonProperty("empires")
    final private Set<String> knownEmpires = Sets.newHashSet();
    @JsonProperty("ownedShips")
    final private Set<ShipSnapshot> ownedShips = Sets.newHashSet();
    @JsonProperty("ownedWorlds")
    final private Set<WorldSnapshot> ownedWorlds = Sets.newHashSet();
    @JsonProperty("colors")
    final private Map<String, String> colors = Maps.newHashMap();
    @JsonProperty("connections")
    @JsonSerialize(using = Coordinate.CoordinateMultimapSerializer.class)
    final private Multimap<Coordinate, Coordinate> connections = HashMultimap.create();
    final long timestamp;

    public void addConnections(final Multimap<Coordinate, Coordinate> conns) {
        connections.putAll(conns);
    }

    public void addSector(final SectorSnapshot sector) {
        sectors.add(sector);
    }

    public void addKnownShipClasses(final Collection<ShipClass> shipClasses) {
        knownShipClasses.addAll(shipClasses);
    }

    public void addKnownEmpires(final Collection<String> knownEmpireNames) {
        knownEmpires.addAll(knownEmpireNames);
    }

    public void addColors(final Map<String, String> map) {
        colors.putAll(map);
    }
}