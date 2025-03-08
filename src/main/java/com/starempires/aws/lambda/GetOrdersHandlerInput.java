package com.starempires.aws.lambda;

public record GetOrdersHandlerInput(String sessionName, int turnNumber, String empireName) {}