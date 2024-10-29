package com.starempires.objects;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderType {
    ADDKNOWN,
    AUTHORIZE,
    BREAK,
    BUILD,
    CONCEAL,
    DECLARE,
    DENY,
    DEPLOY,
    DESIGN,
    DESTRUCT,
    FIRE,
    GIVE,
    IDENTIFY,
    LOAD,
    MAPADD,
    MAPMODIFY,
    MAPMOVE,
    MAPREMOVE,
    MOVE,
    POOL,
    REMOVEKNOWN,
    REPAIR,
    TOGGLE,
    TRANSFER,
    TRANSMIT,
    TRAVERSE,
    UNLOAD;

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
