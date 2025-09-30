package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.TurnData;
import com.starempires.creator.SessionCreator;
import com.starempires.generator.SnapshotGenerator;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.Validate;

import java.util.List;
import java.util.Map;

@Log4j2
public class CreateSessionHandler extends BaseLambdaHandler {

    private static void ensureSessionFolderExists(final String sessionName) {
        try (S3Client s3 = S3Client.create()) {
            final PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(SESSIONS_LOCATION)
                    .key(sessionName + "/")
                    .build();
            s3.putObject(req, RequestBody.empty());
        }
    }

    private static void persistEmpireData(final String sessionName, final List<String> empireData) {
        final String key = sessionName + "/" + sessionName + ".empire-data.txt";
        final String body = String.join("\n", empireData);
        try (S3Client s3 = S3Client.create()) {
            final PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(SESSIONS_LOCATION)
                    .key(key)
                    .contentType("text/plain; charset=utf-8")
                    .build();
            s3.putObject(req, RequestBody.fromString(body));
        }
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            final String body = getBody(event);

            // Parse JSON input into a Map
            final CreateSessionHandlerInput input = OBJECT_MAPPER.readValue(body, CreateSessionHandlerInput.class);

            log.info("input: {}", input);
            final String sessionName = input.sessionName();
            final List<String> empireData = input.empireData();

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.notEmpty(empireData, "Missing empireData");

            ensureSessionFolderExists(sessionName);
            persistEmpireData(sessionName, empireData);

            final SessionCreator creator = new SessionCreator(sessionName, SESSIONS_LOCATION, GAME_DATA_LOCATION, CONFIG_FILE);
            final TurnData turnData = creator.createSession();
            final String message = "Created session %s".formatted(sessionName);
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