package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.orders.OrderParser;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.Validate;

import java.util.List;
import java.util.Map;

@Log4j2
public class OrderParserHandler extends BaseLambdaHandler {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            final String body = getBody(event);

            // Convert JSON body into OrderParserHandlerInput record
            OrderParserHandlerInput input = OBJECT_MAPPER.readValue(body, OrderParserHandlerInput.class);

            final String sessionName = input.sessionName();
            final String empireName = input.empireName();
            final int turnNumber = input.turnNumber();
            final String ordersText = input.ordersText();
            log.info("sessionName: {}",  sessionName);
            log.info("empireName: {}", empireName);
            log.info("turnNumber: {}", turnNumber);

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.notEmpty(empireName, "Missing empireName");

            final List<String> orders = List.of(ordersText.split("\n"));

            final OrderParser parser = new OrderParser(SESSIONS_LOCATION, sessionName, empireName, turnNumber);
            final List<String> results = parser.processOrders(orders);
            final String message = "Processed %d orders for empire %s, session %s, turn %d"
                    .formatted(results.size(), empireName, sessionName, turnNumber);
            log.info(message);
            final String response = message + "\n" + String.join("\n", results) + "\n";
            return createResponse(200, response);
        } catch (Exception ex) {
            log.error("Update failed", ex);
            return createResponse(500, "Error parsing request: " + ex.getMessage());
        }
    }
}