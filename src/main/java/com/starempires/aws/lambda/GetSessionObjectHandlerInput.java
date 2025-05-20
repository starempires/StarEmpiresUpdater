package com.starempires.aws.lambda;

enum SessionObject {
    NEWS,
    ORDERS,
    ORDERS_STATUS,
    ORDERS_LOCK_STATUS,
    SNAPSHOT
}

public record GetSessionObjectHandlerInput(String sessionName, int turnNumber, String empireName, SessionObject sessionObject) {}