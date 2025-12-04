package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.updater.TurnUpdater;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.Validate;

import java.util.Map;

@Log4j2
public class UpdateTurnHandler extends BaseLambdaHandler {
    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            final String body = getBody(event);

            // Parse JSON input into a Map
            final UpdateTurnHandlerInput input = OBJECT_MAPPER.readValue(body, UpdateTurnHandlerInput.class);

            log.info("input: {}", input);
            final String sessionName = input.sessionName();
            final int turnNumber = input.turnNumber();
            final boolean processAdminOnly = input.processAdminOnly();

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.isNotNegative(turnNumber, "Turn number cannot be negative");

            final TurnUpdater updater = new TurnUpdater(SESSIONS_LOCATION, sessionName, turnNumber);
            final String message;
            if (processAdminOnly) {
                updater.processAdminOnly();
                message = "Processed administration phases for session %s".formatted(sessionName);
            }
            else {
                updater.updateTurn();
                message = "Updated turn %s for session %s".formatted(turnNumber, sessionName);
            }
            log.info(message);
            return createResponse(200, message, "OK");
        } catch (Exception e) {
            return createResponse(500, "Error processing request: " + e.getMessage(), "");
        }
    }
}