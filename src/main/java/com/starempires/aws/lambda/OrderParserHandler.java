package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.starempires.orders.OrderParser;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.utils.Validate;

@Log4j2
public class OrderParserHandler implements RequestHandler<OrderParserHandlerInput, String> {

    @Override
    public String handleRequest(OrderParserHandlerInput input, Context context) {
        try {
            context.getLogger().log("Received input: " + input);
            final String sessionsLocation = input.sessionsLocation();
            final String sessionName = input.sessionName();
            final String empireName = input.empireName();
            final int turnNumber = input.turnNumber();
            context.getLogger().log("sessionsLocation: " + sessionsLocation);
            context.getLogger().log("sessionName: " + sessionName);
            context.getLogger().log("empireName: " + empireName);
            context.getLogger().log("turnNumber: " + turnNumber);

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.notEmpty(empireName, "Missing empireName");

            final OrderParser parser = new OrderParser(sessionsLocation, sessionName, empireName, turnNumber);
            parser.processOrders();
            return "Processed orders for empire %s, session %s, turn %d"
                    .formatted(empireName, sessionName, turnNumber);
        } catch (Exception exception) {
            log.error("Update failed", exception);
            return "Error: Update failed - " + exception.getMessage();
        }
    }
}