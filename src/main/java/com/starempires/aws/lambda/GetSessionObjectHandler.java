package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.Validate;

import java.util.Map;

@Log4j2
public class GetSessionObjectHandler extends BaseLambdaHandler {

    private static final StarEmpiresDAO DAO = new S3StarEmpiresDAO(SESSIONS_LOCATION, null);

    @Override
    public Map<String, Object> handleRequest(final Map<String, Object> event, final Context context) {
        try {
            final String body = getBody(event);

            // Parse JSON input into a Map
            GetSessionObjectHandlerInput input = OBJECT_MAPPER.readValue(body, GetSessionObjectHandlerInput.class);

            log.info("input: {}", input);
            final String sessionName = input.sessionName();
            final String empireName = input.empireName();
            final int turnNumber = input.turnNumber();
            final SessionObject sessionObject = input.sessionObject();

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.notEmpty(empireName, "Missing empireName");
            Validate.notNull(sessionObject, "Missing sessionObject");

            log.info("Fetching turn {} {} for empire {}, session {}", turnNumber, sessionObject, empireName, sessionName);
            final String data;
            boolean isJson = false;
            switch (sessionObject) {
                case NEWS -> {
                    data = DAO.loadNews(sessionName, empireName, turnNumber);
                }
                case ORDERS -> {
                    data = DAO.loadOrderResults(sessionName, empireName, turnNumber);
                }
                case ORDERS_STATUS -> {
                    final StarEmpiresDAO.OrderStatus status = DAO.getOrderStatus(sessionName, empireName, turnNumber);
                    data = status.toString();
                }
                case SNAPSHOT -> {
                    data = DAO.loadSnapshot(sessionName, empireName, turnNumber);
                    isJson = true;
                }
                default -> throw new IllegalArgumentException("Unknown sessionObject: " + sessionObject);
            }
            final String message;
            final Map<String, Object> response;
            if (data == null) {
                message = "No turn %d %s found for empire %s, session %s"
                        .formatted(turnNumber, sessionObject.toString().toLowerCase(), empireName, sessionName);
                response = createResponse(404, message, "");
            }
            else {
                message = "Turn %d %s for empire %s, session %s"
                        .formatted(turnNumber, sessionObject.toString().toLowerCase(), empireName, sessionName);
                if (isJson) {
                    response = createJsonResponse(200, message, data);
                }
                else {
                    response = createResponse(200, message, data);
                }
            }
            log.debug("Response {}", response);

            return response;
        } catch (Exception e) {
            return createResponse(500, "Error processing request: " + e.getMessage(), "");
        }
    }
}