package com.starempires.aws.lambda;

public record UpdateTurnHandlerInput(String sessionName, int turnNumber, boolean processAdminOnly) {

    public UpdateTurnHandlerInput(String sessionName, int turnNumber) {
        this(sessionName, turnNumber, false);
    }
}