package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderType {
    ADDCONNECTION(AddConnectionOrder.class, true),
    ADDKNOWN(UnknownOrder.class, true),
    ADDPORTAL(AddPortalOrder.class, true),
    ADDSTORM(AddStormOrder.class, true),
    ADDSHIP(AddShipOrder.class, true),
    ADDSHIPCLASS(UnknownOrder.class, true),
    ADDWORLD(AddWorldOrder.class, true),
    AUTHORIZE(AuthorizeOrder.class, false),
    BUILD(BuildOrder.class, false),
    CONCEAL(Order.class, false),
    DENY(DenyOrder.class, false),
    DEPLOY(DeployOrder.class, false),
    DESIGN(DesignOrder.class, false),
    DESTRUCT(DestructOrder.class, false),
    FIRE(FireOrder.class, false),
    GIVE(GiveOrder.class, false),
    IDENTIFY(UnknownOrder.class, false),
    LOAD(LoadOrder.class, false),
    MAPMODIFY(UnknownOrder.class, true),
    MAPREMOVE(UnknownOrder.class, true),
    MOVE(MoveOrder.class, false),
    POOL(PoolOrder.class, false),
    RELOCATEOBJECT(RelocateObjectOrder.class, true),
    RELOCATESHIP(RelocateShipOrder.class, true),
    REMOVEKNOWN(UnknownOrder.class, true),
    REPAIR(RepairOrder.class, false),
    TOGGLE(ToggleOrder.class, false),
    TRANSFER(TransferOrder.class, false),
    TRANSMIT(TransmitOrder.class, false),
    TRAVERSE(TraverseOrder.class, false),
    UNKNOWN(UnknownOrder.class, false),
    UNLOAD(UnloadOrder.class, false);

    private final Class<? extends Order> orderClass;
    private final boolean gmOnly;

    OrderType(Class<? extends Order> orderClass, final boolean gmOnly) {
        this.orderClass = orderClass;
        this.gmOnly = gmOnly;
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}