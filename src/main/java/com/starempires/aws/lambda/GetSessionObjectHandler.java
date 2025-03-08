package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.Validate;

import java.util.Map;

@Log4j2
public class GetSessionObjectHandler extends BaseLambdaHandler {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            final String body = getBody(event);

            // Parse JSON input into a Map
            GetSessionObjectHandlerInput input = OBJECT_MAPPER.readValue(body, GetSessionObjectHandlerInput.class);

            log.info("input: " + input);
            final String sessionName = input.sessionName();
            final String empireName = input.empireName();
            final int turnNumber = input.turnNumber();
            final SessionObject sessionObject = input.sessionObject();

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.notEmpty(empireName, "Missing empireName");
            Validate.notNull(sessionObject, "Missing sessionObject");

            final StarEmpiresDAO dao = new S3StarEmpiresDAO(SESSIONS_LOCATION, null);
            Map<String, Object> response;
            switch (sessionObject) {
                case NEWS -> {
                    final String news = dao.loadNews(sessionName, empireName, turnNumber);
                    final String message = "News for empire %s, session %s, turn %d"
                            .formatted(empireName, sessionName, turnNumber);
                    final String info = message + "\n" + String.join("\n", news) + "\n";
                    response = createResponse(200, info);
                }
                case ORDERS -> {
                    final String orders = dao.loadOrderResults(sessionName, empireName, turnNumber);
                    final String message = "Orders for empire %s, session %s, turn %d"
                            .formatted(empireName, sessionName, turnNumber);
                    final String info = message + "\n" + String.join("\n", orders) + "\n";
                    response = createResponse(200, info);
                }
                case SNAPSHOT -> {
                    final String snapshot = dao.loadSnapshot(sessionName, empireName, turnNumber);
                    response = createFileResponse(200, snapshot);
                }
                default -> throw new IllegalArgumentException("Unknown sessionObject: " + sessionObject);
            }

            return response;
        } catch (Exception e) {
            return createResponse(500, "Error processing request: " + e.getMessage());
        }
    }
}