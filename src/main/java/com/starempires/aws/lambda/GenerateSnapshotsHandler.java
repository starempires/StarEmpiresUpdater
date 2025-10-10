package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.TurnData;
import com.starempires.generator.SnapshotGenerator;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.Validate;

import java.util.Map;

@Log4j2
public class GenerateSnapshotsHandler extends BaseLambdaHandler {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            final String body = getBody(event);

            // Parse JSON input into a Map
            final GenerateSnapshotsHandlerInput input = OBJECT_MAPPER.readValue(body, GenerateSnapshotsHandlerInput.class);

            log.info("input: {}", input);
            final String sessionName = input.sessionName();
            final int turnNumber = input.turnNumber();

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.isNotNegative(turnNumber, "Turn number cannot be negative");

            final SnapshotGenerator generator = new SnapshotGenerator(SESSIONS_LOCATION, sessionName, turnNumber);
            final TurnData turnData = generator.loadTurnData();
            generator.generateSnapshots(turnData);
            final String message = "Generated snapshots for session %s, turn %s".formatted(sessionName, turnNumber);
            log.info(message);
            return createResponse(200, message, "OK");
        } catch (Exception e) {
            return createResponse(500, "Error processing request: " + e.getMessage(), "");
        }
    }
}