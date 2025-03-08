package com.starempires.aws.lambda;

enum SessionObject {
    NEWS,
    ORDERS,
    SNAPSHOT
}

public record GetSessionObjectHandlerInput(String sessionName, int turnNumber, String empireName, SessionObject sessionObject) {}