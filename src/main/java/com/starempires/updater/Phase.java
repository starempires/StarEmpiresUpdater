package com.starempires.updater;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum Phase {

    // Administration
    REMOVE_CONNECTIONS(Stage.ADMINISTRATION),
    REMOVE_OBJECTS(Stage.ADMINISTRATION),
    REMOVE_SHIPS(Stage.ADMINISTRATION),
    RELOCATE_OBJECTS(Stage.ADMINISTRATION),
    RELOCATE_SHIPS(Stage.ADMINISTRATION),
    ADD_CONNECTIONS(Stage.ADMINISTRATION),
    ADD_PORTALS(Stage.ADMINISTRATION),
    ADD_SHIPS(Stage.ADMINISTRATION),
    ADD_STORMS(Stage.ADMINISTRATION),
    ADD_WORLDS(Stage.ADMINISTRATION),
    MODIFY_SHIPS(Stage.ADMINISTRATION),
    MODIFY_STORMS(Stage.ADMINISTRATION),
    MODIFY_WORLDS(Stage.ADMINISTRATION),
    REMOVE_KNOWN_ITEMS(Stage.ADMINISTRATION),
    ADD_KNOWN_ITEMS(Stage.ADMINISTRATION),
    // Logistics
    UNLOAD_SHIPS(Stage.LOGISTICS),
    DEPLOY_DEVICES(Stage.LOGISTICS),
    APPLY_DEPLOYMENT_DAMAGE(Stage.LOGISTICS),
    LOAD_SHIPS(Stage.LOGISTICS),
    // Astronomics
    STABILIZE_PORTALS(Stage.ASTRONOMICS),
    COLLAPSE_PORTALS(Stage.ASTRONOMICS),
    FLUCTUATE_STORMS(Stage.ASTRONOMICS),
    DRIFT_MAP_OBJECTS(Stage.ASTRONOMICS),
    // Combat
    SELF_DESTRUCT_SHIPS(Stage.COMBAT),
    FIRE_GUNS(Stage.COMBAT),
    APPLY_COMBAT_DAMAGE(Stage.COMBAT),
    REMOVE_DESTROYED_SHIPS_I(Stage.COMBAT),
    DETERMINE_OWNERSHIP_I(Stage.COMBAT),
    // Movement
    TRANSMIT_PORTAL_NAV_DATA(Stage.MOVEMENT),
    MOVE_SHIPS(Stage.MOVEMENT),
    TRAVERSE_PORTALS(Stage.MOVEMENT),
    ACQUIRE_NAV_DATA(Stage.MOVEMENT),
    WEATHER_STORMS(Stage.MOVEMENT),
    APPLY_STORM_DAMAGE(Stage.MOVEMENT),
    REMOVE_DESTROYED_SHIPS_II(Stage.MOVEMENT),
    DETERMINE_OWNERSHIP_II(Stage.MOVEMENT),
    ESTABLISH_HOMEWORLDS(Stage.MOVEMENT),
    ESTABLISH_PROHIBITIONS(Stage.MOVEMENT),
    // Research
    SALVAGE_DESIGNS(Stage.RESEARCH),
    CREATE_DESIGNS(Stage.RESEARCH),
    GIVE_DESIGNS(Stage.RESEARCH),
    // Maintenance
    BUILD_SHIPS(Stage.MAINTENANCE),
    AUTO_REPAIR_SHIPS(Stage.MAINTENANCE),
    REPAIR_SHIPS(Stage.MAINTENANCE),
    TOGGLE_TRANSPONDER_MODES(Stage.MAINTENANCE),
    CONCEAL_SHIPS(Stage.MAINTENANCE),
    IDENTIFY_SHIPS(Stage.MAINTENANCE),
    // Income
    PRODUCE_RESOURCE_UNITS(Stage.INCOME),
    POOL_RESOURCE_UNITS(Stage.INCOME),
    TRANSFER_RESOURCE_UNITS(Stage.INCOME),
    // Scanning
    DENY_SCAN_ACCESS(Stage.SCANNING),
    AUTHORIZE_SCAN_ACCESS(Stage.SCANNING),
    COLLECT_SCAN_DATA(Stage.SCANNING),
    SHARE_SCAN_DATA(Stage.SCANNING),
    RECORD_NEW_MAP_OBJECTS(Stage.SCANNING);

    private final Stage stage;

    Phase(final Stage stage) {
        this.stage = stage;
    }

    /**
     * Returns all phases that belong to the specified stage.
     *
     * @param stage The stage to filter by
     * @return List of phases in the specified stage, in declaration order
     */
    public static List<Phase> getPhasesByStage(final Stage stage) {
        return Arrays.stream(Phase.values())
                .filter(phase -> phase.stage == stage)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String s = name().toLowerCase();
        s = s.replaceAll("_", " ");
        s = s.replaceAll("ii$", "II");
        s = s.replaceAll("i$", "I");
        return s;
    }
}