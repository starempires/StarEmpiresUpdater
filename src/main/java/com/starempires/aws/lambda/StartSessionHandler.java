package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.TurnData;
import com.starempires.creator.SessionCreator;
import com.starempires.generator.SnapshotGenerator;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.Validate;

import java.util.Map;

@Log4j2
public class StartSessionHandler extends BaseLambdaHandler {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            final String body = getBody(event);

            // Parse JSON input into a Map
            final StartSessionHandlerInput input = OBJECT_MAPPER.readValue(body, StartSessionHandlerInput.class);

            log.info("input: {}", input);
            final String sessionName = input.sessionName();

            Validate.notEmpty(sessionName, "Missing sessionName");

            final SessionCreator creator = new SessionCreator(sessionName, SESSIONS_LOCATION, GAME_DATA_LOCATION);
            final TurnData turnData = creator.createSession();
            final String message = "Initialized session %s".formatted(sessionName);
            log.info(message);
            final SnapshotGenerator generator = new SnapshotGenerator(SESSIONS_LOCATION, sessionName, 0);
            generator.generateSnapshots(turnData);
            log.info("Generated snapshots for session {}, turn 0", sessionName);
            return createResponse(200, message, "OK");
        } catch (Exception e) {
            return createResponse(500, "Error processing request: " + e.getMessage(), "");
        }
    }
}