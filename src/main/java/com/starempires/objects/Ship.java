package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
public class Ship extends OwnableObject {

    /** turn this ship was built */
    private final int turnBuilt;
    /** serial number of this ship */
    private final String serialNumber;
    /** set of conditions that apply to this ship */
    @JsonIgnore
    private final Set<ShipCondition> conditions = Sets.newHashSet();
    /** dp remaining for this ship */
    private int dpRemaining;
    /** carrier ship */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = IdentifiableObject.DeferredIdentifiableObjectDeserializer.class)
    private Ship carrier;
    /** cargo ships loaded onto this one */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<Ship> cargo = Sets.newHashSet();
    /** damage accrued thus turn by this ship */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int damageAccrued;
    /** the ShipClass of this ship */
    @JsonSerialize(using = IdentifiableObjectSerializer.class)
    @JsonDeserialize(using = DeferredIdentifiableObjectDeserializer.class)
    private ShipClass shipClass;
    /** empires to whom this ship has its transponder set */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonSerialize(using = IdentifiableObjectCollectionSerializer.class)
    @JsonDeserialize(using = DeferredIdentifiableObjectCollectionDeserializer.class)
    private final Set<Empire> transponders = Sets.newHashSet();
    /** is this ship transponder public? */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean publicTransponder;
    /** guns ordered to fire */
    @JsonIgnore
    private int gunsOrderedToFire;
    /** guns fired this turn by this ship */
    @JsonIgnore
    private int gunsActuallyFired;

    @Builder
    private Ship(final String name, final Coordinate coordinate, final Empire owner,
                 final int turnBuilt, final String serialNumber, final int dpRemaining, final Ship carrier,
                 final ShipClass shipClass, final boolean publicTransponder) {
        super(name, coordinate, owner);
        this.turnBuilt = turnBuilt;
        this.serialNumber = serialNumber;
        this.dpRemaining = dpRemaining;
        this.carrier = carrier;
        this.shipClass = shipClass;
        this.publicTransponder = publicTransponder;
        if (this.carrier != null) {
            this.carrier.addCargo(this);
        }
    }

    @JsonCreator
    private Ship(@JsonProperty("name") final String name,
                  @JsonProperty("oblique") final int oblique,
                  @JsonProperty("y") final int y,
                  @JsonProperty("turnBuilt") final int turnBuilt,
                  @JsonProperty("serialNumber") final String serialNumber,
                  @JsonProperty("dpRemaining") final int dpRemaining,
                 @JsonProperty("publicTransponder") final boolean publicTransponder) {
        this (name, new Coordinate(oblique, y), null, turnBuilt, serialNumber, dpRemaining, null, null, publicTransponder);
    }

    public void setCarrier(final Ship carrier) {
        this.carrier = carrier;
        if (carrier != null) {
            carrier.addCargo(this);
        }
    }

    /**
     * @return This ship plus all its cargo
     */
    @JsonIgnore
    public Set<Ship> getCargoGroup() {
        final Set<Ship> cargoGroup = Sets.newHashSet(cargo);
        cargoGroup.add(this);
        return cargoGroup;
    }

    public void addCondition(final ShipCondition condition) {
        conditions.add(condition);
    }

    public boolean hasCondition(final ShipCondition condition) {
        return conditions.contains(condition);
    }

    public void unloadCargo(final Ship ship) {
        if (cargo.remove(ship)) {
            addCondition(ShipCondition.UNLOADED_CARGO);
        }
    }

    public void addCargo(final Ship ship) {
        cargo.add(ship);
    }

    public void loadCargo(final Ship ship) {
        addCargo(ship);
        addCondition(ShipCondition.LOADED_CARGO);
    }

    @JsonIgnore
    public boolean isLoaded() {
        return carrier != null;
    }

    public boolean hasLoadedCargo() {
        return !cargo.isEmpty();
    }

    public boolean wasJustUnloaded() {
        return conditions.contains(ShipCondition.UNLOADED_FROM_CARRIER);
    }

    public void unloadFromCarrier() {
        if (isLoaded()) {
            carrier = null;
            addCondition(ShipCondition.UNLOADED_FROM_CARRIER);
        }
    }

    @JsonIgnore
    public String getHandle() {
        return ObjectUtils.firstNonNull(name, serialNumber);
    }

    public void loadOntoCarrier(final Ship carrier) {
        this.carrier = carrier;
        addCondition(ShipCondition.LOADED_ONTO_CARRIER);
    }

    public void deploy() {
        if (isDevice()) {
            addCondition(ShipCondition.DEPLOYED);
        }
    }

    private void inflictDamage(final int damage) {
        damageAccrued += damage;
    }

    public void inflictCombatDamage(final int damage) {
        inflictDamage(damage);
        addCondition(ShipCondition.HIT_IN_COMBAT);
    }

    public void inflictStormDamage(final int damage) {
        inflictDamage(damage);
        addCondition(ShipCondition.DAMAGED_BY_STORM);
    }

    public void applyDamageAccrued(final ShipCondition destroyedCondition) {
        dpRemaining -= damageAccrued;
        if (dpRemaining <= 0) {
            destroy(destroyedCondition);
            dpRemaining = 0;
        }
    }

    public void destroy(final ShipCondition destroyedCondition) {
        dpRemaining = 0;
        addCondition(destroyedCondition);
    }

    public void destruct() {
        destroy(ShipCondition.SELF_DESTRUCTED);
    }

    @JsonIgnore
    public int getUnfiredGuns() {
        return getAvailableGuns() - gunsActuallyFired;
    }

    @JsonIgnore
    public double getOperationRating() {
        return Math.sqrt((double) dpRemaining / (double) getDp());
    }

    @JsonIgnore
    public int getAvailableGuns() {
        return (int) Math.round(getGuns() * getOperationRating());
    }

    @JsonIgnore
    public int getAvailableEngines() {
        return (int) Math.round(getEngines() * getOperationRating());
    }

    @JsonIgnore
    public int getAvailableScan() {
        return (int) Math.round(getScan() * getOperationRating());
    }

    public void fireGuns(final int guns) {
        if (guns > 0) {
            gunsActuallyFired = Math.min(getGuns(), gunsActuallyFired + guns);
            addCondition(ShipCondition.FIRED_GUNS);
        }
    }

    @JsonIgnore
    public boolean isAlive() {
        return dpRemaining > 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHandle());
    }

    @Override
    public boolean equals(final Object obj) {
        boolean rv = false;
        if (obj instanceof Ship ship) {
            if (StringUtils.equals(getHandle(), ship.getHandle())) {
                rv = true;
            }
        }
        return rv;
    }

    @Override
    public String toString() {
        return getHandle();
    }

    @JsonIgnore
    public int getGuns() {
        return shipClass.getGuns();
    }

    @JsonIgnore
    public int getDp() {
        return shipClass.getDp();
    }

    @JsonIgnore
    public int getEngines() {
        return shipClass.getEngines();
    }

    @JsonIgnore
    public int getScan() {
        return shipClass.getScan();
    }

    @JsonIgnore
    public int getRacks() {
        return shipClass.getRacks();
    }

    @JsonIgnore
    public int getTonnage() {
        return shipClass.getTonnage();
    }

    @JsonIgnore
    public int getCost() {
        return shipClass.getCost();
    }

    @JsonIgnore
    public int getAr() {
        return shipClass.getAr();
    }

    @JsonIgnore
    public DeviceType getDeviceType() {
        return shipClass.getDeviceType();
    }

    @JsonIgnore
    public boolean isDevice() {
        return shipClass.isDevice();
    }

    @JsonIgnore
    public boolean isMissile() {
        return shipClass.isMissile();
    }

    @JsonIgnore
    public boolean isOrbital() {
        return shipClass.isOrbital();
    }

    @JsonIgnore
    public boolean isConqueringShip() {
        return getAvailableGuns() > 0;
    }

    @JsonIgnore
    public boolean isWing() {
        return shipClass.isWing();
    }

    @JsonIgnore
    public int getMaxRepairAmount() {
        return getDp() - dpRemaining;
    }

    public void repair(final int amount) {
        dpRemaining = Math.min(getDp(), dpRemaining + amount);
    }

    @JsonIgnore
    public int getAutoRepair() {
        int ar = 0;
        if (isAlive()) {
            ar = Math.min(getAr(), getMaxRepairAmount());
            repair(ar);
        }
        return ar;
    }

    public void moveTo(final Coordinate destination) {
        coordinate = destination;
        addCondition(ShipCondition.MOVED);
    }

    public void traverseTo(final Coordinate destination) {
        moveTo(destination);
        addCondition(ShipCondition.TRAVERSED_WORMNET);
    }

    @JsonIgnore
    public boolean isSalvageable() {
        return !isAlive() && ! conditions.contains(ShipCondition.SELF_DESTRUCTED)
                && (! conditions.contains(ShipCondition.FIRED_GUNS) || !isMissile());
    }

    @JsonIgnore
    public boolean isStarbase() {
        return shipClass.isStarbase();
    }

    @JsonIgnore
    public boolean isPortalHammer() {
        return shipClass.isPortalHammer();
    }

    public boolean hasReceivedDamage() {
        return damageAccrued > 0;
    }

    public boolean hasAccruedDamageExceededRemainingDp() {
        return damageAccrued > dpRemaining;
    }

    @JsonIgnore
    public boolean isRepairable() {
        return getMaxRepairAmount() > 0;
    }

    @JsonIgnore
    public boolean isOneShot() {
        return isDevice() || isMissile();
    }

    public void addTransponder(final Empire empire) {
        transponders.add(empire);
    }

    public void removeTransponder(final Empire empire) {
        transponders.remove(empire);
    }

    public void removeAllTransponders() {
        transponders.clear();
    }

    public void addTransponders(final Collection<Empire> empires) {
        transponders.addAll(empires);
    }

    public boolean isTransponderSet(final Empire empire) {
        boolean rv = false;
        if (isPublicTransponder()) {
            rv = true;
        }
        else {
            rv = transponders.contains(empire);
        }
        return rv;
    }

    public boolean isDeviceOfType(final DeviceType type) {
        return shipClass.isDeviceOfType(type);
    }

    @JsonIgnore
    public int getEmptyRacks() {
        return getRacks() - cargo.stream().mapToInt(Ship::getTonnage).sum();
    }

    public boolean canLoadCargo(final Ship ship) {
        return getEmptyRacks() >= ship.getTonnage();
    }

    public void toggleTransponder(final boolean state) {
        if (publicTransponder != state) {
            publicTransponder = state;
            addCondition(ShipCondition.TOGGLED_TRANSPONDER);
        }
    }

    public void orderGunsToFire(final int gunsToFire) {
        this.gunsOrderedToFire = Math.max(gunsToFire, getUnfiredGuns());
    }

    public boolean hasUnfiredGuns() {
        return getUnfiredGuns() > 0;
    }

    public boolean isVisibleToEmpire(final Empire empire) {
        return isOwnedBy(empire) ||
                empire.getScanStatus(this) == ScanStatus.VISIBLE ||
                empire.getScanStatus(this) == ScanStatus.SCANNED && isTransponderSet(empire);
    }
}