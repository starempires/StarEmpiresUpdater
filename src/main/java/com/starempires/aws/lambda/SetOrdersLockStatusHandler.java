package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.dao.S3StarEmpiresDAO;
import com.starempires.dao.StarEmpiresDAO;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.Validate;

import java.util.Map;

@Log4j2
public class SetOrdersLockStatusHandler extends BaseLambdaHandler {

    private static final StarEmpiresDAO DAO = new S3StarEmpiresDAO(SESSIONS_LOCATION, null);

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            final String body = getBody(event);

            // Convert JSON body into OrderParserHandlerInput record
            SetOrdersLockStatusHandlerInput input = OBJECT_MAPPER.readValue(body, SetOrdersLockStatusHandlerInput.class);

            final String sessionName = input.sessionName();
            final String empireName = input.empireName();
            final int turnNumber = input.turnNumber();
            final boolean locked = input.locked();
            log.info("sessionName: {}",  sessionName);
            log.info("empireName: {}", empireName);
            log.info("turnNumber: {}", turnNumber);
            log.info("locked: {}", locked);

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.notEmpty(empireName, "Missing empireName");

            final String message;
            if (locked) {
                DAO.lockOrders(sessionName, empireName, turnNumber);
                message = "Locked orders for empire %s, session %s, turn %d".formatted(empireName, sessionName, turnNumber);
            }
            else {
                DAO.unlockOrders(sessionName, empireName, turnNumber);
                message = "Unlocked orders for empire %s, session %s, turn %d".formatted(empireName, sessionName, turnNumber);
            }
            log.info(message);
            return createResponse(200, message, "OK");
        } catch (Exception ex) {
            log.error("Update failed", ex);
            return createResponse(500, "Error parsing request: " + ex.getMessage(), "");
        }
    }
}