package com.starempires;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.starempires.constants.Constants;
import com.starempires.objects.Coordinate;
import com.starempires.objects.DeviceType;
import com.starempires.objects.Empire;
import com.starempires.objects.EmpireType;
import com.starempires.objects.HullParameters;
import com.starempires.objects.HullType;
import com.starempires.objects.MappableObject;
import com.starempires.orders.Order;
import com.starempires.orders.OrderType;
import com.starempires.objects.Portal;
import com.starempires.objects.Ship;
import com.starempires.objects.ShipClass;
import com.starempires.objects.ShipCondition;
import com.starempires.objects.SitRep;
import com.starempires.objects.Storm;
import com.starempires.objects.World;
import com.starempires.phases.Phase;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
@JsonPropertyOrder({"session", "turnNumber", "radius",
        "empires", "ships", "worlds", "portals", "storms", "hullParameters", "shipClasses"})
public class TurnData {

    @Getter
    private final String session;
    /**
     * turn number this data is for
     */
    @Getter
    @Setter
    private int turnNumber;
    @Getter
    private final int radius;
    /**
     * map of empire names to Empires
     */
    @JsonIgnore
    private final Map<String, Empire> empireNames = Maps.newHashMap();
    /**
     * map of world names to Worlds
     */
    @JsonIgnore
    private final Map<String, World> worldNames = Maps.newHashMap();
    /**
     * map of Coordinates to Worlds
     */
    @JsonIgnore
    private final Map<Coordinate, World> worldCoordinates = Maps.newHashMap();
    /**
     * map of portal names to Portals
     */
    @JsonIgnore
    private final Map<String, Portal> portalNames = Maps.newHashMap();
    /**
     * map of Coordinates to Sets of Portals
     */
    @JsonIgnore
    private final Map<Coordinate, Portal> portalCoordinates = Maps.newHashMap();
    /**
     * map of storm names to Storms
     */
    @JsonIgnore
    private final Map<String, Storm> stormNames = Maps.newHashMap();
    /**
     * map of Coordinates to Sets of Storms
     */
    @JsonIgnore
    @Getter
    private final Multimap<Coordinate, Storm> stormCoordinates = HashMultimap.create();
    /**
     * map of Coordinate to set of Ships
     */
    @JsonIgnore
    private final Multimap<Coordinate, Ship> deployedDevices = HashMultimap.create();
    /**
     * map of OrderTypes to list of Orders
     */
    @JsonIgnore
    private final ListMultimap<OrderType, Order> orders = ArrayListMultimap.create();
    /**
     * news for this turn
     */
    @JsonIgnore
    private final TurnNews news = new TurnNews();
    /**
     * map of parameter keys to values
     */
    @JsonIgnore
    private final Map<String, String> parameters = Maps.newHashMap();
    /**
     * map of ship class ids to ShipClasses
     */
    @JsonIgnore
    private final Map<HullType, HullParameters> hullParameters = Maps.newHashMap();
    /**
     * map of ship class names to ShipClasses
     */
    @JsonIgnore
    private final Map<String, ShipClass> shipClassNames = Maps.newHashMap();
    /**
     * set of destroyed ships that are potentially salvageable
     */
    @JsonIgnore
    @Getter
    private final Set<Ship> possibleSalvages = Sets.newHashSet();
    /**
     * map of Empires to home Worlds
     */
    @JsonIgnore
    private final Map<Empire, World> homeworlds = Maps.newHashMap();

    @Builder
    private TurnData(final String session, final int turnNumber, final int radius) {
        this.session = session;
        this.turnNumber = turnNumber;
        this.radius = radius;
    }

    public void addPortal(final @NonNull Portal portal) {
        portalNames.put(portal.getName(), portal);
        portalCoordinates.put(portal.getCoordinate(), portal);
    }

    public void addPortals(final Collection<Portal> portals) {
        portals.forEach(this::addPortal);
    }

    public void addWorld(final @NonNull World world) {
        worldNames.put(world.getName(), world);
        worldCoordinates.put(world.getCoordinate(), world);
    }

    public void addWorlds(final Collection<World> worlds) {
        worlds.forEach(this::addWorld);
    }

    public void addStorm(final @NonNull Storm storm) {
        stormNames.put(storm.getName(), storm);
        stormCoordinates.put(storm.getCoordinate(), storm);
    }

    public void addStorms(final Collection<Storm> storms) {
        storms.forEach(this::addStorm);
    }

    public void addEmpires(final @NonNull Collection<Empire> empires) {
        empires.forEach(e -> empireNames.put(e.getName(), e));
    }

    public void addOrder(@NonNull Order order) {
        orders.get(order.getOrderType()).add(order);
    }

    public Storm getStorm(final String name) {
        return stormNames.get(name);
    }

    public Collection<Storm> getStorms(final Coordinate coordinate) {
        return stormCoordinates.get(coordinate);
    }

    public World getWorld(final String name) {
        return worldNames.get(name);
    }

    public World getWorld(final Coordinate coordinate) {
        return worldCoordinates.get(coordinate);
    }

    public Portal getPortal(final String name) {
        return portalNames.get(name);
    }

    public Portal getPortal(final Coordinate coordinate) {
        return portalCoordinates.get(coordinate);
    }

    public Empire getEmpire(final String name) {
        return empireNames.get(name);
    }

    public void addNews(final Phase phase, final String text) {
        addNews(phase, empireNames.values(), text);
    }

    public void addNews(final Phase phase, final Collection<Empire> empires, final String text) {
        news.addNews(phase, empires, text);
    }

    public void addNews(final Phase phase, final Empire empire, final String text) {
        news.addNews(phase, empire, text);
    }

    public void addNewsHeader(final Phase phase) {
        addNews(phase, "");
        addNews(phase, Constants.DASHES);
        addNews(phase, "Phase " + phase);
        addNews(phase, Constants.DASHES);
    }

    public void addNewsFooter(final Phase phase) {
        addNews(phase, "");
        addNews(phase, Constants.DASHES);
    }

    public List<Order> getOrders(final OrderType orderType) {
        return ObjectUtils.firstNonNull(orders.get(orderType), Collections.emptyList());
    }

    public Collection<Ship> getLiveShips(final MappableObject object) {
        return getLiveShips(object.getCoordinate());
    }

    public Collection<Ship> getLiveShips(final Coordinate coordinate) {
        return getActiveEmpires().stream()
                .flatMap(empire -> empire.getLiveShips(coordinate).stream())
                .collect(Collectors.toSet());
    }

    public Ship getShip(final String empireHandle) {
        Ship ship = null;
        final String[] tokens = empireHandle.split(":");
        if (tokens.length == 2) {
            final String empireName = tokens[0].trim();
            final String handle = tokens[1].trim();
            final Empire empire = getEmpire(empireName);
            if (empire != null) {
                ship = empire.getShip(handle);
            }
        }
        return ship;
    }

    public Set<Empire> getEmpiresPresent(final MappableObject object) {
        return getEmpiresPresent(object.getCoordinate());
    }

    public Set<Empire> getEmpiresPresent(final Coordinate coordinate) {
        return getActiveEmpires().stream()
                .filter(empire -> !empire.getShips(coordinate).isEmpty())
                .collect(Collectors.toSet());
    }

    public int getIntParameter(final String parameter, final int defaultValue) {
        return Integer.parseInt(parameters.getOrDefault(parameter, Integer.toString(defaultValue)));
    }

    public double getDoubleParameter(final String parameter, final double defaultValue) {
        return Double.parseDouble(parameters.getOrDefault(parameter, Double.toString(defaultValue)));
    }

    public void removeDestroyedShips(final Collection<Ship> ships) {
        ships.forEach(ship -> {
            ship.getOwner().removeShip(ship);
            possibleSalvages.add(ship);
        });
    }

    public void addShipClass(final ShipClass shipClass) {
        shipClassNames.put(shipClass.getName(), shipClass);
    }

    public void addShipClasses(final Collection<ShipClass> classes) {
        classes.forEach(this::addShipClass);
    }

    public ShipClass getShipClass(final String name) {
        return shipClassNames.get(name.toLowerCase());
    }

    public void addHullParameters(final Collection<HullParameters> parameters) {
        parameters.forEach(p -> hullParameters.put(p.getHullType(), p));
    }

    public HullParameters getHullParameters(final HullType hullType) {
        return hullParameters.get(hullType);
    }

    public void unload(Ship ship) {
        Ship carrier = ship.getCarrier();
        if (carrier != null) {
            carrier.unloadCargo(ship);
        }
        ship.unloadFromCarrier();
    }

    public void load(Ship cargo, Ship carrier) {
        cargo.loadOntoCarrier(carrier);
        carrier.loadCargo(cargo);
    }

    public void deploy(final Ship device) {
        device.deploy();
        deployedDevices.put(device.getCoordinate(), device);
        device.destroy(ShipCondition.DESTROYED_DEVICE_DEPLOYMENT);
    }

    public void addPortalConnection(final String fromPortalName, final String toPortalName) {
        final Portal fromPortal = getPortal(fromPortalName);
        final Portal toPortal = getPortal(toPortalName);
        fromPortal.addConnection(toPortal);
    }

    public void setHomeworld(final Empire empire, final World world) {
        final World existingHomeworld = homeworlds.get(empire);
        if (existingHomeworld != null) {
            existingHomeworld.setHomeworld(false);
        }
        if (world == null) {
            homeworlds.remove(empire);
        } else {
            homeworlds.put(empire, world);
            world.setHomeworld(true);
        }
    }

    public World getHomeworld(final Empire empire) {
        return homeworlds.get(empire);
    }

    public final Set<World> getOwnedWorlds(final Empire empire) {
        return worldNames.values().stream()
                .filter(world -> world.getOwner() != null)
                .filter(world -> world.getOwner().equals(empire))
                .collect(Collectors.toSet());
    }

    public SitRep getSitRep(final Empire empire, final MappableObject object) {
        return getSitRep(empire, object.getCoordinate());
    }

    public SitRep getSitRep(final Empire empire, final Coordinate coordinate) {
        final SitRep sitrep = new SitRep(empire, coordinate);
        final Collection<Ship> ships = getLiveShips(coordinate);
        ships.forEach(sitrep::add);
        return sitrep;
    }

    @JsonIgnore
    public Set<Empire> getActiveEmpires() {
        return empireNames.values().stream()
                .filter(empire -> empire.getEmpireType() == EmpireType.ACTIVE).collect(Collectors.toSet());
    }

    public boolean isInNebula(final Coordinate coordinate) {
        return stormCoordinates.containsKey(coordinate);
    }

    public boolean isInNebula(final MappableObject object) {
        return isInNebula(object.getCoordinate());
    }

    public Set<Ship> getStarbases(final MappableObject object) {
        return getStarbases(object.getCoordinate());
    }

    public Set<Ship> getStarbases(final Coordinate coordinate) {
        return getActiveEmpires().stream().map(empire -> empire.getStarbase(coordinate))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void removeWorld(final World world) {
        worldNames.remove(world.getName());
        worldCoordinates.remove(world.getCoordinate());
        empireNames.values().forEach(empire -> empire.removeKnownWorld(world));
    }

    public void removePortal(Portal portal) {
        portalNames.remove(portal.getName());
        portalCoordinates.remove(portal.getCoordinate());
        portalNames.values().forEach(p -> p.removeConnection(portal));
        empireNames.values().forEach(empire -> empire.removeKnownPortal(portal));
    }

    public void removeStorm(Storm storm) {
        stormNames.remove(storm.getName());
        stormCoordinates.removeAll(storm.getCoordinate());
    }

    @JsonIgnore
    public Multimap<Portal, Portal> getAllConnections() {
        final Multimap<Portal, Portal> connections = HashMultimap.create();
        for (Portal portal : portalNames.values()) {
            connections.putAll(portal, portal.getConnections());
        }
        return connections;
    }

    public Collection<Ship> getDeployedDevices(final MappableObject object, final DeviceType type) {
        return getDeployedDevices(object.getCoordinate(), type);
    }

    public Set<Ship> getDeployedDevices(final DeviceType type) {
        return deployedDevices.values().stream()
                .filter(device -> device.getDeviceType().equals(type))
                .collect(Collectors.toSet());
    }

    public Collection<Ship> getDeployedDevices(final Coordinate coordinate, final DeviceType type) {
        final Collection<Ship> coordinateDevices = deployedDevices.get(coordinate);
        return coordinateDevices.stream()
                .filter(device -> device.getDeviceType() == type)
                .collect(Collectors.toSet());
    }

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    public List<ShipClass> getShipClasses(final Empire empire, final Collection<String> shipClassNames) {
        return shipClassNames.stream().map(this::getShipClass)
                .filter(Objects::nonNull).filter(empire::isKnownShipClass)
                .collect(Collectors.toList());
    }

    public int fireMissile(Ship missile, Ship target) {
        final int missileGuns = missile.getAvailableGuns();
        target.inflictCombatDamage(missileGuns);
        missile.fireGuns(missileGuns);
        missile.destroy(ShipCondition.DESTROYED_IN_COMBAT);
        return missileGuns;
    }

    public List<Ship> shipsDamagedThisTurn() {
        return getAllShips().stream().filter(Ship::hasReceivedDamage).collect(Collectors.toList());
    }

    @JsonProperty("empires")
    public Collection<Empire> getAllEmpires() {
        return empireNames.values();
    }

    @JsonProperty("ships")
    public Collection<Ship> getAllShips() {
        return getActiveEmpires().stream()
                .flatMap(empire -> empire.getShips().stream())
                .collect(Collectors.toSet());
    }

    @JsonProperty("worlds")
    public Collection<World> getAllWorlds() {
        return worldNames.values();
    }

    @JsonProperty("portals")
    public Collection<Portal> getAllPortals() {
        return portalNames.values();
    }

    @JsonProperty("storms")
    public Collection<Storm> getAllStorms() {
        return stormNames.values();
    }

    @JsonProperty("hullParameters")
    public Collection<HullParameters> getAllHullParameters() {
        return hullParameters.values();
    }

    @JsonProperty("shipClasses")
    public Collection<ShipClass> getAllShipClasses() {
        return shipClassNames.values();
    }
}