package com.starempires.aws.lambda;

public record GenerateSnapshotsHandlerInput(String sessionName, int turnNumber) {}