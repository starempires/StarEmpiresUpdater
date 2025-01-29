package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderType {
    ADDKNOWN(UnknownOrder.class),
    AUTHORIZE(UnknownOrder.class),
    BUILD(BuildOrder.class),
    CONCEAL(Order.class),
    DENY(UnknownOrder.class),
    DEPLOY(DeployOrder.class),
    DESIGN(DesignOrder.class),
    DESTRUCT(DestructOrder.class),
    FIRE(FireOrder.class),
    GIVE(GiveOrder.class),
    IDENTIFY(UnknownOrder.class),
    LOAD(LoadOrder.class),
    MAPADD(UnknownOrder.class),
    MAPMODIFY(UnknownOrder.class),
    MAPMOVE(UnknownOrder.class),
    MAPREMOVE(UnknownOrder.class),
    MOVE(MoveOrder.class),
    POOL(PoolOrder.class),
    REMOVEKNOWN(UnknownOrder.class),
    REPAIR(RepairOrder.class),
    TOGGLE(ToggleOrder.class),
    TRANSFER(TransferOrder.class),
    TRANSMIT(TransmitOrder.class),
    TRAVERSE(TraverseOrder.class),
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