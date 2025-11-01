package com.starempires.aws.lambda;

public record AddEmpireHandlerInput(String sessionName, String empireName, String abbreviation, String homeworld, String starbase) {
}