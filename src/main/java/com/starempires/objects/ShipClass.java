package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonInclude(Include.NON_DEFAULT)
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(force = true)
@SuperBuilder
@AllArgsConstructor
public class ShipClass extends IdentifiableObject {

    /** hull type of this ship class */
    @JsonProperty
    private final HullType hullType;
    /** device type of this shipclass (if it's a device) or null of none */
    @JsonProperty
    private final DeviceType deviceType;

    private final int guns;
    private final int dp;
    private final int engines;
    private final int scan;
    private final int racks;
    private final int tonnage;
    private final int cost;
    private final int ar;

    @JsonIgnore
    public boolean isDevice() {
        return hullType == HullType.DEVICE;
    }

    @JsonIgnore
    public boolean isMissile() {
        return hullType == HullType.MISSILE;
    }

    @JsonIgnore
    public boolean isOrbital() {
        return hullType == HullType.ORBITAL;
    }

    @JsonIgnore
    public boolean isWing() {
        return hullType == HullType.WING;
    }

    @JsonIgnore
    public boolean isCapitalShip() {
        return hullType == HullType.CAPITAL_SHIP;
    }

    @JsonIgnore
    public boolean isGunship() {
        return hullType == HullType.GUNSHIP;
    }

    @JsonIgnore
    public boolean isPortalHammer() {
        return isDeviceOfType(DeviceType.PORTAL_HAMMER);
    }

    @JsonIgnore
    public boolean isIonShield() {
        return isDeviceOfType(DeviceType.ION_SHIELD);
    }

    @JsonIgnore
    public boolean isIonGenerator() {
        return isDeviceOfType(DeviceType.ION_GENERATOR);
    }

    @JsonIgnore
    public boolean isPollutionBomb() {
        return isDeviceOfType(DeviceType.POLLUTION_BOMB);
    }

    @JsonIgnore
    public boolean isStarbase() {
        return "starbase".equalsIgnoreCase(name);
    }

    public boolean isDeviceOfType(final DeviceType type) {
        return deviceType == type;
    }
}