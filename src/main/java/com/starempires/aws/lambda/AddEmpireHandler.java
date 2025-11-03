package com.starempires.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.starempires.objects.EmpireType;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.Validate;

import java.util.Map;

@Log4j2
public class AddEmpireHandler extends BaseLambdaHandler {

    private static final String EMPIRE_DATA_FILENAME = "empire-data.txt";

    private static void persistEmpireData(final AddEmpireHandlerInput input) {
        final String sessionName = input.sessionName();
        final String empireName = input.empireName();
        final String abbreviation = input.abbreviation();
        final String homeworld = input.homeworld();
        final String starbase = input.starbase();
        final String empireType = input.empireType();
        final String key = sessionName + "/" + sessionName + "." + EMPIRE_DATA_FILENAME;

        String existing = "";
        try (S3Client s3 = S3Client.create()) {
            try {
                final GetObjectRequest getReq = GetObjectRequest.builder()
                        .bucket(SESSIONS_LOCATION)
                        .key(key)
                        .build();
                existing = s3.getObjectAsBytes(getReq).asUtf8String();
            } catch (Exception e) {
                log.info("No existing empire data found at s3://{}/{} (creating new file)", SESSIONS_LOCATION, key);
            }

            final String newLine = String.join(",", empireName, abbreviation, empireType, homeworld, starbase);
            final String updatedBody = existing.isEmpty() ? newLine : existing.stripTrailing() + "\n" + newLine;

            final PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(SESSIONS_LOCATION)
                    .key(key)
                    .contentType("text/plain; charset=utf-8")
                    .build();
            s3.putObject(putReq, RequestBody.fromString(updatedBody));

            log.info("Appended empire {} to s3://{}:{}", empireName, SESSIONS_LOCATION, key);
        }
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            final String body = getBody(event);

            // Parse JSON input into a Map
            final AddEmpireHandlerInput input = OBJECT_MAPPER.readValue(body, AddEmpireHandlerInput.class);

            log.info("input: {}", input);
            final String sessionName = input.sessionName();
            final String empireName = input.empireName();
            final String abbreviation = input.abbreviation();
            final String homeworld = input.homeworld();
            final String starbase = input.starbase();
            final String empireType = input.empireType();

            Validate.notEmpty(sessionName, "Missing sessionName");
            Validate.notEmpty(empireName, "Missing empireName");
            Validate.notEmpty(abbreviation, "Missing abbreviation");
            Validate.notEmpty(homeworld, "Missing homeworld");
            Validate.notEmpty(starbase, "Missing starbase");
            Validate.notNull(EnumUtils.getEnumIgnoreCase(EmpireType.class, input.empireType()), "Invalid empire type: " + empireType);

            persistEmpireData(input);

            final String message = "Added empire %s to session %s".formatted(empireName, sessionName);
            log.info(message);
            return createResponse(200, message, "OK");
        } catch (Exception e) {
            return createResponse(500, "Error processing request: " + e.getMessage(), "");
        }
    }
}