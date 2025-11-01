package com.starempires.aws.lambda;

import java.util.Map;

public record CreateSessionHandlerInput(String sessionName, Map<String, String> overrideProps) {}