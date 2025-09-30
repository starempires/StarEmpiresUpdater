package com.starempires.aws.lambda;

import java.util.List;

public record CreateSessionHandlerInput(String sessionName, List<String> empireData) {}