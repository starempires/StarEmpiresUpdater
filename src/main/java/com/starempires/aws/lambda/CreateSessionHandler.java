package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.Validate;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Log4j2
public class CreateSessionHandler extends BaseLambdaHandler {

    private static void ensureSessionFolderExists(final String sessionName) {
        try (S3Client s3 = S3Client.create()) {
            final PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(SESSIONS_LOCATION)
                    .key(sessionName + "/")
                    .build();
            s3.putObject(req, RequestBody.empty());
            log.info("Created session folder s3://{}:{}", SESSIONS_LOCATION, sessionName);
        }
    }

    private static void persistSessionConfigData(final String sessionName, final Map<String, String> overrideProps) {
        try (S3Client s3 = S3Client.create()) {
            // 1️⃣ Read the default config file from game data
            log.info("Reading base config from s3://{}/{}", GAME_DATA_LOCATION, CONFIG_FILE);
            final GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(GAME_DATA_LOCATION)
                    .key(CONFIG_FILE)
                    .build();
            final String baseConfig = s3.getObjectAsBytes(getReq).asUtf8String();

            // Load defaults into Properties
            final Properties properties = new Properties();
            try (var reader = new StringReader(baseConfig)) {
                properties.load(reader);
            }

            // 2️⃣ Merge in overrideProps
            overrideProps.forEach(properties::setProperty);

            // 3️⃣ Write merged config to the session folder
            final String mergedBody = properties.stringPropertyNames().stream()
                    .sorted() // sort keys alphabetically
                    .map(k -> k + "=" + properties.getProperty(k))
                    .collect(Collectors.joining("\n"));

            final String sessionKey = sessionName + "/" + sessionName + "." + CONFIG_FILE;
            final PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(SESSIONS_LOCATION)
                    .key(sessionKey)
                    .contentType("text/plain; charset=utf-8")
                    .build();

            s3.putObject(putReq, RequestBody.fromString(mergedBody));

            log.info("Wrote merged session config to s3://{}/{}", SESSIONS_LOCATION, sessionKey);
        } catch (Exception e) {
            log.error("Error persisting session config for {}: {}", sessionName, e.getMessage(), e);
            throw new RuntimeException("Failed to persist session config", e);
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
            final Map<String, String> overrideProps = ObjectUtils.firstNonNull(input.overrideProps(), Map.of());

            Validate.notEmpty(sessionName, "Missing sessionName");

            ensureSessionFolderExists(sessionName);
            persistSessionConfigData(sessionName, overrideProps);

            final String message = "Created session %s".formatted(sessionName);
            log.info(message);
            return createResponse(200, message, "OK");
        } catch (Exception e) {
            return createResponse(500, "Error processing request: " + e.getMessage(), "");
        }
    }
}