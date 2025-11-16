package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.starempires.constants.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Empire extends IdentifiableObject {
    /**
     * two-letter abbreviation of this empire
     */
    private final String abbreviation;
    /**
     * type for this empire
     */
    private final EmpireType empireType;
    /**
     * frame of reference for this empire
     */
    private final FrameOfReference frameOfReference;
    /**
     * ids of empires known to this one
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<Empire> knownEmpires = Sets.newHashSet();
    /**
     * ids of ship classes known to this one
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<ShipClass> knownShipClasses = Sets.newHashSet();
    /**
     * map of worlds currently known by this empire to the corresponding last turn scanned
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<World> knownWorlds = Sets.newHashSet();
    /**
     * map of portals currently known by this empire to the corresponding last turn scanned
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<Portal> knownPortals = Sets.newHashSet();
    /**
     * map of storms currently known by this empire to the corresponding last turn scanned
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<Storm> knownStorms = Sets.newHashSet();
    /**
     * portals which this empire traversed this turn
     */
    @JsonIgnore
    private final Set<Portal> portalsTraversed = Sets.newHashSet();
    /**
     * portals for which this empire currently has nav data
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<Portal> portalNavData = Sets.newHashSet();
    /**
     * collected and shared scan data for this empire
     */
    private final ScanData scanData = new ScanData();
    /**
     * map of foreign empires to Sets of Coordinates for which scan access has been authorized
     */
    @JsonSerialize(using = Coordinate.EmpireCoordinateMultimapSerializer.class)
    @JsonDeserialize(using = Coordinate.DeferredEmpireCoordinateMultimapDeserializer.class)
    private final Multimap<Empire, Coordinate> shareCoordinates = HashMultimap.create();

    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectMultimapSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectMultimapDeserializer.class)
    private final Multimap<Empire, MappableObject> shareObjects = HashMultimap.create();
    /**
     * map of empires to sets of Ships for which scan access has been authorized
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectMultimapSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectMultimapDeserializer.class)
    private final Multimap<Empire, Ship> shareShips = HashMultimap.create();
    /**
     * set of empires which are authorized to receive all scan data
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<Empire> shareEmpires = Sets.newHashSet();
    /**
     * map of empires to Sets of ShipClasses for which scan access has been * authorized
     */
    @JsonSerialize(using = IdentifiableObject.IdentifiableObjectMultimapSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectMultimapDeserializer.class)
    private final Multimap<Empire, ShipClass> shareShipClasses = HashMultimap.create();
    @JsonIgnore
    private final Fleet fleet = new Fleet();
    private final Map<String, String> mapColors = Maps.newHashMap();

    @Builder
    @JsonCreator
    private Empire(
            @JsonProperty("name") final String name,
            @JsonProperty("abbreviation") final String abbreviation,
            @JsonProperty("frameOfReference") final FrameOfReference frameOfReference,
            @JsonProperty("empireType") final EmpireType empireType,
            @JsonProperty("scanData") final ScanData scanData) {
        super(name);
        this.abbreviation = abbreviation;
        this.frameOfReference = frameOfReference;
        this.empireType = empireType;
        if (scanData != null) {
            this.scanData.putAll(scanData);
        }
    }

    public boolean owns(final @NonNull OwnableObject object) {
        return object.owner == this;
    }

    public void addKnownEmpire(final Empire empire) {
        knownEmpires.add(empire);
    }

    public void addKnownEmpires(final Set<Empire> empires) {
        empires.forEach(this::addKnownEmpire);
    }

    public void addKnownWorld(final World world) {
        knownWorlds.add(world);
    }

    public void addKnownPortal(final Portal portal) {
        knownPortals.add(portal);
    }

    public void addKnownPortal(final Portal portal, final boolean hasNavData) {
        knownPortals.add(portal);
        if (hasNavData) {
            portalNavData.add(portal);
        } else {
            portalNavData.remove(portal);
        }
    }

    public void addKnownStorm(final Storm storm) {
        knownStorms.add(storm);
    }

    public void addKnownShipClass(final ShipClass shipClass) {
        knownShipClasses.add(shipClass);
    }

    public boolean hasNavData(final Portal portal) {
        return portalNavData.contains(portal);
    }

    public void addNavData(final Portal portal) {
        portalNavData.add(portal);
    }

    public void removeNavData(final Portal portal) {
        portalNavData.remove(portal);
    }

    public boolean isKnownEmpire(final Empire empire) {
        return empire == this || knownEmpires.contains(empire);
    }

    public boolean isKnownShipClass(final ShipClass shipClass) {
        return knownShipClasses.contains(shipClass);
    }

    public boolean isKnownPortal(final Portal portal) {
        return knownPortals.contains(portal);
    }

    public boolean isKnownWorld(final World world) {
        return knownWorlds.contains(world);
    }

    public boolean isKnownStorm(final Storm storm) {
        return knownStorms.contains(storm);
    }

    public void setAllScanStatus(final ScanStatus status) {
        scanData.setAllScan(status);
    }

    public void mergeScanStatus(final @NonNull MappableObject object, final ScanStatus status, final int turnNumber) {
        mergeScanStatus(object.getCoordinate(), status, turnNumber);
    }

    public void mergeObjectScanStatuses(final @NonNull Collection<? extends MappableObject> objects, final ScanStatus status, final int turnNumber) {
        mergeScanStatus(objects.stream().map(MappableObject::getCoordinate).toList(), status, turnNumber);
    }

    public void mergeScanStatus(final Coordinate coordinate, final ScanStatus status, final int turnNumber) {
        scanData.mergeScanStatus(coordinate, status, turnNumber);
    }

    public void mergeScanStatus(final Collection<Coordinate> coordinates, final ScanStatus status, final int turnNumber) {
        scanData.mergeScanStatus(coordinates, status, turnNumber);
    }

    public void mergeScanStatusAndShare(final ScanData newScan) {
        scanData.mergeScanStatusAndShare(this, newScan);
    }

    public ScanStatus getScanStatus(final Coordinate coordinate) {
        return scanData.getScanStatus(coordinate);
    }

    public ScanStatus getScanStatus(final @NonNull MappableObject object) {
        return getScanStatus(object.getCoordinate());
    }

    public void addCoordinateScanAccess(final Empire empire, final List<Coordinate> coordinates) {
        shareCoordinates.putAll(empire, coordinates);
    }

    public void removeCoordinateScanAccess(final Empire empire, final List<Coordinate> coordinates) {
        shareCoordinates.get(empire).removeAll(coordinates);
    }

    public void addObjectScanAccess(final Empire empire, final List<MappableObject> objects) {
        shareObjects.putAll(empire, objects);
    }

    public void removeObjectScanAccess(final Empire empire, final List<MappableObject> objects) {
        shareObjects.get(empire).removeAll(objects);
    }

    public void addEmpireScanAccess(final Empire empire) {
        removeEmpireScanAccess(empire); // remove all existing forms of sharing for this empire
        shareEmpires.add(empire);
    }

    public void removeEmpireScanAccess(final Empire empire) {
        // removing "all data" access also removes other forms of sharing
        shareEmpires.remove(empire);
        shareCoordinates.removeAll(empire);
        shareObjects.removeAll(empire);
        shareShips.removeAll(empire);
        shareShipClasses.removeAll(empire);
    }

    public void addShipScanAccess(final Empire empire, final Collection<Ship> ships) {
        shareShips.putAll(empire, ships);
    }

    public void removeShipScanAccess(final Empire empire, final Collection<Ship> ships) {
        shareShips.get(empire).removeAll(ships);
    }

    public void addShipClassScanAccess(final Empire empire, final Collection<ShipClass> shipClasses) {
        shareShipClasses.putAll(empire, shipClasses);
    }

    public void removeShipClassScanAccess(final Empire empire, final Collection<ShipClass> shipClasses) {
        shareShipClasses.get(empire).removeAll(shipClasses);
    }

    /**
     * Get all scan coordinates for this empire
     *
     * @return Collection of coordinates scanned by this empire
     */
    @JsonIgnore
    public Collection<Coordinate> getScanCoordinates() {
        return scanData.getCoordinates();
    }

    public void addScan(final Coordinate coordinate, final ScanStatus status) {
        scanData.setScanStatus(coordinate, status);
    }

    public void addScanHistory(final Coordinate coordinate, final int lastTurnScanned) {
        scanData.setLastTurnScanned(coordinate, lastTurnScanned);
    }

    public void removeKnownWorld(final World world) {
        knownWorlds.remove(world);
    }

    public void removeKnownPortal(final Portal portal) {
        knownPortals.remove(portal);
    }

    public void removeKnownEmpire(final Empire empire) {
        knownEmpires.remove(empire);
    }

    public void removeKnownShipClass(final ShipClass shipClass) {
        knownShipClasses.remove(shipClass);
    }

    public Coordinate toLocal(final Coordinate coordinate) {
        return frameOfReference.toLocal(coordinate);
    }

    public Coordinate toGalactic(final Coordinate coordinate) {
        return frameOfReference.toGalactic(coordinate);
    }

    public int getLastTurnScanned(final Coordinate coordinate) {
        return scanData.getLastTurnScanned(coordinate);
    }

    public int computeMaxScanExtent() {
        final Coordinate localOriginInGalactic = this.toGalactic(new Coordinate(0, 0));
        final Collection<Coordinate> coordinates = scanData.getCoordinates();
        return coordinates.stream().map(localOriginInGalactic::distanceTo).max(Integer::compare).orElse(0);
        // TODO add broadcast sectors?
    }

    public void addSharedScan(final Coordinate coordinate, final Empire empire, final ScanStatus status) {
        scanData.addShare(coordinate, status, empire);
    }

    public void addPortalTraversed(final Portal portal) {
        portalsTraversed.add(portal);
    }

    public void addShip(final Ship ship) {
        fleet.addShip(ship);
    }

    public Ship getShip(final String handle) {
        return fleet.getShipByHandle(handle);
    }

    public List<Ship> getShips(final @NonNull Collection<String> handles) {
        return handles.stream().map(this::getShip).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Collection<Ship> getShips(final ShipClass shipClass) {
        return fleet.getShipsByClass(shipClass);
    }

    public Collection<Ship> getShips(final Coordinate coordinate) {
        return fleet.getShipsByCoordinate(coordinate);
    }

    public Set<Ship> getLiveShips(final Coordinate coordinate) {
        return fleet.getShipsByCoordinate(coordinate).stream()
                .filter(Ship::isAlive)
                .collect(Collectors.toSet());
    }

    public Set<Ship> getDeadShips(final Coordinate coordinate) {
        return fleet.getShipsByCoordinate(coordinate).stream()
                .filter(s -> !s.isAlive())
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<Ship> getLiveShips() {
        return getShips().stream()
                .filter(Ship::isAlive)
                .collect(Collectors.toSet());
    }

    public Ship getStarbase(final Coordinate coordinate) {
        return getShips(coordinate).stream()
                .filter(Ship::isStarbase)
                .findFirst()
                .orElse(null);
    }

    public void moveShip(final @NonNull Ship ship, final Coordinate coordinate) {
        fleet.moveShips(ship.getCargoGroup(), coordinate);
    }

    public void traverseShip(final @NonNull Ship ship, final Coordinate coordinate) {
        fleet.traverseShips(ship.getCargoGroup(), coordinate);
    }

    public void removeShip(final Ship ship) {
        fleet.removeShip(ship);
    }

    @JsonIgnore
    public Collection<Ship> getShips() {
        return fleet.getAllShips();
    }

    @JsonIgnore
    public String getNewSerialNumber() {
        boolean done = false;
        String serialNumber = null;
        while (!done) {
            final int value = ThreadLocalRandom.current().nextInt(1, (int) Math.pow(16, Constants.SERIAL_NUMBER_HEX_DIGITS));
            final String hex = String.format(Constants.FORMAT_SERIAL_NUMBER, value);
            serialNumber = abbreviation + hex;
            if (!fleet.serialNumberExists(serialNumber)) {
                done = true;
            }
        }
        return serialNumber;
    }

    public Ship buildShip(final @NonNull ShipClass shipClass, final @NonNull MappableObject object, final String shipName,
                          final int turnNumber) {
        return buildShip(shipClass, object.getCoordinate(), shipName, turnNumber);
    }

    public Ship buildShip(final @NonNull ShipClass shipClass, final @NonNull Coordinate coordinate, final String name,
                          final int turnNumber) {
        final Ship ship = Ship.builder()
                .coordinate(coordinate)
                .dpRemaining(shipClass.getDp())
                .name(name)
                .owner(this)
                .serialNumber(getNewSerialNumber())
                .shipClass(shipClass)
                .turnBuilt(turnNumber)
                .build();
        addShip(ship);
        ship.addCondition(ShipCondition.BUILT);
        return ship;
    }

    public void setMapColor(final String object, final String color) {
        mapColors.put(object, color);
    }

    @JsonIgnore
    public World getHomeworld() {
        return knownWorlds.stream().filter(World::isHomeworld).findFirst().orElse(null);
    }

    @JsonIgnore
    public boolean isGM() {
        return empireType == EmpireType.GM;
    }

    @JsonIgnore
    public boolean isObserver() {
        return empireType == EmpireType.OBSERVER;
    }

    @JsonIgnore
    public boolean isActive() {
        return empireType == EmpireType.ACTIVE;
    }

    @JsonIgnore
    public int getLargestBasenameNumber(final String basename) {
        return fleet.getLargestBasenameNumber(basename);
    }

    public void clearShipConditions() {
        fleet.clearShipConditions();
    }

}