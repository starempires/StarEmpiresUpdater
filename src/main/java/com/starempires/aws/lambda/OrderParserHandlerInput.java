package com.starempires.aws.lambda;

public record OrderParserHandlerInput(String sessionName, int turnNumber, String empireName, String ordersText) {}