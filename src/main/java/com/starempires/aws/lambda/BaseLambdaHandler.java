package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Log4j2
public abstract class BaseLambdaHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    protected static final String SESSIONS_LOCATION = "starempires-sessions";
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    String getBody(Map<String, Object> event) {
        log.info("Received input: {}", event);
        // Extract and parse JSON body
        final String body = (String) event.get("body");
        if (StringUtils.isBlank(body)) {
            throw new IllegalArgumentException("No request body found");
        }
        return body;
    }

    // Helper method to create a normal API Gateway response
    Map<String, Object> createResponse(final int statusCode, final String message, final String data) {
        final String jsonSafeData = data
                .replace("\\", "\\\\")  // Escape backslashes
                .replace("\"", "\\\"")  // Escape quotes
                .replace("\r", "\\r")   // Escape carriage return
                .replace("\n", "\\n");
        return Map.of(
                "statusCode", statusCode,
                "headers", Map.of(
                       "Content-Type", "application/json",
                       "Access-Control-Allow-Origin", "*", // Allow all origins
                       "Access-Control-Allow-Methods", "OPTIONS,POST,GET",
                       "Access-Control-Allow-Headers", "Content-Type,Authorization"
                   ),
                "body", "{\"message\": \"" + message + "\", " +
                        "\"data\": \"" + jsonSafeData + "\"}"
        );
    }

    Map<String, Object> createJsonResponse(final int statusCode, final String message, final String data) {
        return Map.of(
                "statusCode", statusCode,
                "headers", Map.of(
                        "Content-Type", "application/json",
                        "Access-Control-Allow-Origin", "*", // Allow all origins
                        "Access-Control-Allow-Methods", "OPTIONS,POST,GET",
                        "Access-Control-Allow-Headers", "Content-Type,Authorization"
                ),
                "body", "{\"message\": \"" + message + "\", \"data\":" + data + "}"
        );
    }

    // Helper method to return JSON as a downloadable file
    Map<String, Object> createFileResponse(final int statusCode, final String message, final String data) {
        return Map.of(
                "statusCode", statusCode,
                "headers", Map.of(
                        "Content-Type", "application/json",
                        "Content-Disposition", "attachment; filename=\"response.json\"",
                        "Access-Control-Allow-Origin", "*", // Allow all origins
                        "Access-Control-Allow-Methods", "OPTIONS,POST,GET",
                        "Access-Control-Allow-Headers", "Content-Type,Authorization"
                ),
                "body", "{\"message\": \"" + message + "\", \"data\": \"" + data + "\"}"
        );
    }
}