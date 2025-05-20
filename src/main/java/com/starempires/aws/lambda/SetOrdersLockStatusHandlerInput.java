package com.starempires.aws.lambda;

public record SetOrdersLockStatusHandlerInput(String sessionName, int turnNumber, String empireName, boolean locked) {}