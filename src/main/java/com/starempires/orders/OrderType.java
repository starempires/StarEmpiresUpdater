package com.starempires.orders;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderType {
    ADDCONNECTION(AddConnectionOrder.class, true),
    ADDKNOWN(AddKnownOrder.class, true),
    ADDPORTAL(AddPortalOrder.class, true),
    ADDSTORM(AddStormOrder.class, true),
    ADDSHIP(AddShipOrder.class, true),
    ADDSHIPCLASS(UnknownOrder.class, true),
    ADDWORLD(AddWorldOrder.class, true),
    AUTHORIZE(AuthorizeOrder.class, false),
    BUILD(BuildOrder.class, false),
    CONCEAL(UnknownOrder.class, false),
    DENY(DenyOrder.class, false),
    DEPLOY(DeployOrder.class, false),
    DESIGN(DesignOrder.class, false),
    DESTRUCT(DestructOrder.class, false),
    FIRE(FireOrder.class, false),
    GIVE(GiveOrder.class, false),
    IDENTIFY(UnknownOrder.class, false),
    LOAD(LoadOrder.class, false),
    MODIFYSHIP(ModifyShipOrder.class, true),
    MODIFYSTORM(ModifyStormOrder.class, true),
    MODIFYWORLD(ModifyWorldOrder.class, true),
    MOVE(MoveOrder.class, false),
    POOL(PoolOrder.class, false),
    RELOCATEOBJECT(RelocateObjectOrder.class, true),
    RELOCATESHIP(RelocateShipOrder.class, true),
    REMOVECONNECTION(RemoveConnectionOrder.class, true),
    REMOVEKNOWN(RemoveKnownOrder.class, true),
    REMOVEOBJECT(RemoveObjectOrder.class, true),
    REMOVESHIP(RemoveShipOrder.class, true),
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