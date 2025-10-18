package com.starempires.aws.lambda;

import java.util.List;
import java.util.Map;

public record CreateSessionHandlerInput(String sessionName, List<String> empireData, Map<String, String> overrideProps) {}