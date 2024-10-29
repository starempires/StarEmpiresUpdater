package com.starempires.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.annotation.JsonValue;

@RequiredArgsConstructor
@Getter
public enum ShipCondition {

    AUTO_REPAIRED("A"),
    BUILT("B"),
    COLLECTED_SCAN_DATA(null),
    CONCEALED(null),
    DAMAGED_BY_STORM("S"),
    DEPLOYED("P"),
    DESTROYED_BY_STORM("X"),
    DESTROYED_DEVICE_DEPLOYMENT("X"),
    DESTROYED_IN_COMBAT("X"),
    FIRED_GUNS("F"),
    HIT_IN_COMBAT("H"),
    IDENTIFIED(null),
    LOADED_CARGO("l"),
    LOADED_ONTO_CARRIER("L"),
    MOVED("M"),
    REPAIRED("R"),
    SELF_DESTRUCTED("D"),
    TOGGLED_TRANSPONDER(null),
    TRAVERSED_WORMNET("W"),
    UNLOADED_CARGO("u"),
    UNLOADED_FROM_CARRIER("U");

    private final String abbreviation;

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase().replace('_', ' ');
    }
}