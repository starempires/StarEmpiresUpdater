package com.starempires.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class OrderParserHandler implements RequestHandler<OrderParserHandlerInput, String> {
    @Override
    public String handleRequest(OrderParserHandlerInput input, Context context) {
        context.getLogger().log("Received input: " + input);
        return "Processed orders for empire %s, session %s, turn %d"
                .formatted(input.empireName(), input.sessionName(), input.turnNumber());
    }
}