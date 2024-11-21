package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderType {
    ADDKNOWN(UnknownOrder.class),
    AUTHORIZE(UnknownOrder.class),
    BUILD(UnknownOrder.class),
    CONCEAL(UnknownOrder.class),
    DENY(UnknownOrder.class),
    DEPLOY(DeployOrder.class),
    DESIGN(UnknownOrder.class),
    DESTRUCT(DestructOrder.class),
    FIRE(FireOrder.class),
    GIVE(UnknownOrder.class),
    IDENTIFY(UnknownOrder.class),
    LOAD(LoadOrder.class),
    MAPADD(UnknownOrder.class),
    MAPMODIFY(UnknownOrder.class),
    MAPMOVE(UnknownOrder.class),
    MAPREMOVE(UnknownOrder.class),
    MOVE(UnknownOrder.class),
    POOL(UnknownOrder.class),
    REMOVEKNOWN(UnknownOrder.class),
    REPAIR(UnknownOrder.class),
    TOGGLE(UnknownOrder.class),
    TRANSFER(UnknownOrder.class),
    TRANSMIT(TransmitOrder.class),
    TRAVERSE(UnknownOrder.class),
    UNKNOWN(UnknownOrder.class),
    UNLOAD(UnloadOrder.class);

    private final Class<? extends Order> orderClass;

    private OrderType(Class<? extends Order> orderClass) {
        this.orderClass = orderClass;
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}