package com.starempires.aws.lambda;

public record UpdateTurnHandlerInput(String sessionName, int turnNumber) {}