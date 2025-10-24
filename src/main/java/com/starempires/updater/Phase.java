package com.starempires.updater;

public enum Phase {

    // Astronomics
    FLUCTUATE_STORMS,
    REMOVE_MAP_OBJECTS,
    RELOCATE_OBJECTS,
    RELOCATE_SHIPS,
    ADD_PORTALS,
    ADD_SHIPS,
    ADD_STORMS,
    ADD_WORLDS,
    MODIFY_MAP_OBJECTS,
    REMOVE_KNOWN_ITEMS,
    ADD_KNOWN_ITEMS,
    DRIFT_MAP_OBJECTS,
    STABILIZE_PORTALS,
    COLLAPSE_PORTALS,
    // Logistics
    UNLOAD_SHIPS,
    DEPLOY_DEVICES,
    LOAD_SHIPS,
    // Combat
    SELF_DESTRUCT_SHIPS,
    FIRE_GUNS,
    APPLY_COMBAT_DAMAGE,
    REMOVE_DESTROYED_SHIPS_I,
    DETERMINE_OWNERSHIP_I,
    // Movement
    TRANSMIT_PORTAL_NAV_DATA,
    MOVE_SHIPS,
    TRAVERSE_PORTALS,
    ACQUIRE_NAV_DATA,
    WEATHER_STORMS,
    APPLY_STORM_DAMAGE,
    REMOVE_DESTROYED_SHIPS_II,
    DETERMINE_OWNERSHIP_II,
    RELOCATE_HOMEWORLDS,
    ESTABLISH_PROHIBITIONS,
    // Research
    SALVAGE_DESIGNS,
    CREATE_DESIGNS,
    GIVE_DESIGNS,
    // Maintenance
    BUILD_SHIPS,
    AUTO_REPAIR_SHIPS,
    REPAIR_SHIPS,
    TOGGLE_TRANSPONDER_MODES,
    CONCEAL_SHIPS,
    IDENTIFY_SHIPS,
    // Income
    PRODUCE_RESOURCE_UNITS,
    POOL_RESOURCE_UNITS,
    TRANSFER_RESOURCE_UNITS,
    // Scanning
    DENY_SCAN_ACCESS,
    AUTHORIZE_SCAN_ACCESS,
    COLLECT_SCAN_DATA,
    SHARE_SCAN_DATA,
    RECORD_NEW_MAP_OBJECTS;

    @Override
    public String toString() {
        String s = name().toLowerCase();
        s = s.replaceAll("_", " ");
        s = s.replaceAll("ii$", "II");
        s = s.replaceAll("i$", "I");
        return s;
    }

}